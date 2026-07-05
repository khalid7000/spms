package com.rit.spms.service;

import com.rit.spms.domain.SwotEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Groups a quadrant's distinct submitted SWOT words into synonym families (via
 * {@link SynonymProvider}/WordNet) so the ranked-vote ballot can offer a combined
 * "vote for the whole family" option alongside each individual word, with the
 * family's count summing its members'. Every word still gets its own individual
 * cluster too — grouping only ADDS the combined option, it never replaces the
 * individual choices.
 *
 * This is the single source of truth for "what are the votable candidates and
 * their keys/counts" for a quadrant: {@link SwotVotingService} calls it three
 * times — building the ballot, validating a submitted vote's word against the
 * valid set, and resolving display labels/tie-break counts at tally time — and
 * all three must agree on the same keys, which is why this logic lives in one
 * place rather than being reimplemented at each call site.
 */
@Service
@RequiredArgsConstructor
public class SwotWordClusterer {

    /** Prefixes a combined-family vote key so it can never collide with a real (always plain-lowercase) word. */
    private static final String GROUP_KEY_PREFIX = "grp:";
    private static final int MAX_KEY_LENGTH = 95; // stays under the normalized_word VARCHAR(100) column

    private final SynonymProvider synonymProvider;

    /**
     * One votable candidate: either a single submitted word, or (isGroup=true) a
     * synthetic option representing several words WordNet linked as synonyms.
     *
     * @param key              the value actually stored/compared as the vote's normalized word —
     *                         the plain normalized word for an individual candidate, or a synthetic
     *                         "grp:word1+word2" key for a combined family option
     * @param displayLabel     human-readable text for the UI (original casing; joined for a family)
     * @param relatedWords     for an individual word that WordNet linked to others, its sibling
     *                         display words (empty if it has none); for a family option, all of
     *                         its member display words
     * @param submitterCount   distinct-submitter count — summed across members for a family option
     * @param isGroup          true only for the synthetic combined-family option
     */
    public record WordCluster(
            String key,
            String displayLabel,
            List<String> relatedWords,
            int submitterCount,
            boolean isGroup) {
    }

    private record WordInfo(String normalizedWord, String displayWord, int submitterCount) {
    }

    /** Clusters all distinct words in {@code entries} (expected to already be filtered to one quadrant). */
    public List<WordCluster> cluster(List<SwotEntry> entries) {
        List<WordInfo> words = entries.stream()
                .collect(Collectors.groupingBy(SwotEntry::getNormalizedWord))
                .entrySet().stream()
                .map(e -> new WordInfo(
                        e.getKey(),
                        e.getValue().get(0).getWord(),
                        (int) e.getValue().stream().map(x -> x.getUser().getId()).distinct().count()))
                .toList();

        // Union-Find over the pairwise WordNet synonym relation — O(n^2) comparisons, fine for the
        // handful of distinct words a single quadrant realistically collects.
        Map<String, String> parent = new HashMap<>();
        for (WordInfo w : words) {
            parent.put(w.normalizedWord(), w.normalizedWord());
        }
        for (int i = 0; i < words.size(); i++) {
            for (int j = i + 1; j < words.size(); j++) {
                String wordA = words.get(i).normalizedWord();
                String wordB = words.get(j).normalizedWord();
                if (synonymProvider.areSynonyms(wordA, wordB)) {
                    union(parent, wordA, wordB);
                }
            }
        }

        Map<String, List<WordInfo>> groups = new LinkedHashMap<>();
        for (WordInfo w : words) {
            groups.computeIfAbsent(find(parent, w.normalizedWord()), k -> new ArrayList<>()).add(w);
        }

        List<WordCluster> clusters = new ArrayList<>();
        for (List<WordInfo> group : groups.values()) {
            List<String> allDisplayWordsInGroup = group.stream().map(WordInfo::displayWord).toList();

            // Every member is still its own individually-votable candidate...
            for (WordInfo w : group) {
                List<String> siblings = allDisplayWordsInGroup.stream()
                        .filter(d -> !d.equals(w.displayWord()))
                        .toList();
                clusters.add(new WordCluster(w.normalizedWord(), w.displayWord(), siblings, w.submitterCount(), false));
            }

            // ...plus, only when WordNet actually linked 2+ words, one additional combined option.
            if (group.size() > 1) {
                List<String> sortedNormalized = group.stream().map(WordInfo::normalizedWord).sorted().toList();
                String key = truncate(GROUP_KEY_PREFIX + String.join("+", sortedNormalized));
                String label = truncate(String.join(" + ", allDisplayWordsInGroup));
                int totalCount = group.stream().mapToInt(WordInfo::submitterCount).sum();
                clusters.add(new WordCluster(key, label, allDisplayWordsInGroup, totalCount, true));
            }
        }
        return clusters;
    }

    private String truncate(String s) {
        return s.length() <= MAX_KEY_LENGTH ? s : s.substring(0, MAX_KEY_LENGTH - 1) + "…";
    }

    private String find(Map<String, String> parent, String x) {
        while (!parent.get(x).equals(x)) {
            parent.put(x, parent.get(parent.get(x))); // path halving
            x = parent.get(x);
        }
        return x;
    }

    private void union(Map<String, String> parent, String a, String b) {
        String rootA = find(parent, a);
        String rootB = find(parent, b);
        if (!rootA.equals(rootB)) {
            parent.put(rootA, rootB);
        }
    }
}
