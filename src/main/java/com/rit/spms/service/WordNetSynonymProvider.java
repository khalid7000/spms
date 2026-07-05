package com.rit.spms.service;

import com.rit.spms.domain.enums.SwotQuadrant;
import com.rit.spms.exception.AiUnavailableException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Free, fully offline synonym lookup via WordNet (extJWNL + the bundled WordNet 3.1 dataset —
 * no network call, no AI model, no API key). This is deliberately not behind the app.ai.provider
 * switch: synonym lookup is a deterministic dictionary lookup, not a generative task, so there's
 * nothing an LLM does better here.
 */
@Service
@Slf4j
public class WordNetSynonymProvider implements SynonymProvider {

    private static final int MAX_SYNONYMS = 10;
    private static final String RESOURCE_PROPERTIES = "extjwnl_resource_properties.xml";

    private Dictionary dictionary;

    @PostConstruct
    void init() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(RESOURCE_PROPERTIES)) {
            if (in == null) {
                log.warn("{} not found on classpath; synonym suggestions will be unavailable", RESOURCE_PROPERTIES);
                return;
            }
            dictionary = Dictionary.getInstance(in);
        } catch (JWNLException | java.io.IOException e) {
            log.warn("Could not initialize WordNet dictionary; synonym suggestions will be unavailable", e);
        }
    }

    @Override
    public List<String> suggestSynonyms(SwotQuadrant quadrant, String partialWord) {
        if (dictionary == null) {
            throw new AiUnavailableException("Synonym dictionary is not available");
        }
        String word = partialWord.trim().toLowerCase();
        if (word.isEmpty()) {
            return List.of();
        }

        Set<String> synonyms = new LinkedHashSet<>();
        try {
            for (POS pos : POS.getAllPOS()) {
                IndexWord indexWord = dictionary.getIndexWord(pos, word);
                if (indexWord == null) {
                    continue;
                }
                for (Synset synset : indexWord.getSenses()) {
                    for (Word w : synset.getWords()) {
                        String lemma = w.getLemma().replace('_', ' ');
                        if (!lemma.equalsIgnoreCase(word)) {
                            synonyms.add(lemma);
                        }
                    }
                }
            }
        } catch (JWNLException e) {
            throw new AiUnavailableException("Synonym lookup failed: " + e.getMessage(), e);
        }

        return synonyms.stream().limit(MAX_SYNONYMS).toList();
    }

    /**
     * True if wordB shows up as a synonym (same synset, any part of speech) of wordA.
     * Used to cluster a quadrant's submitted words into synonym families for voting —
     * see {@link SwotWordClusterer}. Deliberately does not consult MAX_SYNONYMS since
     * this checks membership across the *full* synset, not just the top suggestions.
     */
    @Override
    public boolean areSynonyms(String wordA, String wordB) {
        if (dictionary == null) {
            return false;
        }
        String a = wordA.trim().toLowerCase();
        String b = wordB.trim().toLowerCase();
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        if (a.equals(b)) {
            return true;
        }
        try {
            for (POS pos : POS.getAllPOS()) {
                IndexWord indexWord = dictionary.getIndexWord(pos, a);
                if (indexWord == null) {
                    continue;
                }
                for (Synset synset : indexWord.getSenses()) {
                    for (Word w : synset.getWords()) {
                        if (w.getLemma().replace('_', ' ').equalsIgnoreCase(b)) {
                            return true;
                        }
                    }
                }
            }
        } catch (JWNLException e) {
            log.warn("Synonym comparison failed for '{}' vs '{}'", wordA, wordB, e);
        }
        return false;
    }
}
