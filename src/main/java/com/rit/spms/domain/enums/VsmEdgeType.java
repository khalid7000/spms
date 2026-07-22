package com.rit.spms.domain.enums;

/** Standard lean-VSM connector distinction: a solid material-flow arrow vs. a dashed
 *  information-flow arrow (e.g. a schedule or a rework/failure signal looping back upstream). */
public enum VsmEdgeType {
    MATERIAL_FLOW,
    INFORMATION_FLOW
}
