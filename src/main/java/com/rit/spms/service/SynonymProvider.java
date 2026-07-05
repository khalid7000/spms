package com.rit.spms.service;

import com.rit.spms.domain.enums.SwotQuadrant;

import java.util.List;

/** Live synonym suggestions while a user types a SWOT word. See {@link WordNetSynonymProvider}. */
public interface SynonymProvider {
    List<String> suggestSynonyms(SwotQuadrant quadrant, String partialWord);

    /** Used by {@link SwotWordClusterer} to group a quadrant's submitted words into synonym families for voting. */
    boolean areSynonyms(String wordA, String wordB);
}
