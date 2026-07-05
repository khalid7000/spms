package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** One votable ballot option — either an individual submitted word, or (synonymGroup=true) a
 *  synthetic "vote for the whole synonym family" option combining several WordNet-linked words. */
@Data
@Builder
public class SwotBallotCandidateResponse {
    /** The value to submit when voting for this option (see SwotWordClusterer.WordCluster.key). */
    private String word;
    /** Human-readable text to render — always populated, use this rather than `word` for display. */
    private String displayLabel;
    private int submitterCount;
    private boolean synonymGroup;
    /** Sibling words WordNet auto-detected as synonyms of this one (empty if it has none). */
    private List<String> relatedWords;
}
