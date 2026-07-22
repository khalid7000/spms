package com.rit.spms.service;

import com.rit.spms.domain.enums.VsmNodeType;

import java.util.List;

/** Shared prompt construction for {@link VsmDraftGenerator} implementations. */
final class VsmAiPrompts {

    private VsmAiPrompts() {
    }

    static String buildDraftPrompt(String processDescription, List<VsmNodeType> allowedNodeTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are helping a department/unit leader turn a plain-English description of a process ")
                .append("they run into a lean Value Stream Map (VSM) -- a diagram of the steps the work ")
                .append("actually flows through, with bottlenecks and waste called out, not a generic ")
                .append("flowchart or org chart.\n\n");
        sb.append("Process description:\n").append(processDescription).append("\n\n");
        sb.append("You may only use these node types, each identified by its exact code:\n");
        for (VsmNodeType type : allowedNodeTypes) {
            sb.append("- ").append(type.name()).append(": ").append(describeNodeType(type)).append("\n");
        }
        sb.append("\nBuild a sequence of PROCESS steps in the order work flows through them. Where the ")
                .append("description mentions a supplier, an external party, or the ultimate customer/beneficiary, ")
                .append("add a SUPPLIER_CUSTOMER node at that end of the flow. Where a step's throughput, wait ")
                .append("time, or fail/rework rate is mentioned or clearly implied, fill in that PROCESS node's ")
                .append("cycleTimeMinutes/completeAccuratePercent/failRatePercent fields -- leave them null if not ")
                .append("stated or implied, don't invent numbers. Mark any step that is clearly a bottleneck ")
                .append("(high fail rate, long delay, frequent rework) by adding a KAIZEN_BURST node connected to ")
                .append("it, rather than inventing an improvement plan yourself.\n\n");
        sb.append("Give every node a short, unique tempId string (e.g. \"n1\", \"n2\"). Every edge must reference ")
                .append("two of those tempIds via sourceTempId/targetTempId, and use edgeType MATERIAL_FLOW for ")
                .append("the normal forward flow of work, or INFORMATION_FLOW for a schedule, signal, or a ")
                .append("rework/failure loop pointing back to an earlier step. Produce only what the description ")
                .append("actually supports -- a shorter, accurate map beats a padded, speculative one.");
        return sb.toString();
    }

    /** Same content as above, plus an explicit JSON-shape instruction for models without native structured output. */
    static String buildDraftPromptWithJsonShape(String processDescription, List<VsmNodeType> allowedNodeTypes) {
        return buildDraftPrompt(processDescription, allowedNodeTypes)
                + "\n\nRespond with ONLY a single JSON object (no markdown, no code fences, no commentary) "
                + "matching exactly this shape:\n"
                + "{\"nodes\": [{\"tempId\": \"string\", \"nodeType\": \"string\", \"title\": \"string\", "
                + "\"description\": \"string or null\", \"cycleTimeMinutes\": number or null, "
                + "\"completeAccuratePercent\": number or null, \"failRatePercent\": number or null}], "
                + "\"edges\": [{\"sourceTempId\": \"string\", \"targetTempId\": \"string\", "
                + "\"edgeType\": \"MATERIAL_FLOW or INFORMATION_FLOW\", \"label\": \"string or null\"}]}";
    }

    private static String describeNodeType(VsmNodeType type) {
        return switch (type) {
            case PROCESS -> "a single step in the process";
            case DATA_BOX -> "a small block of measurements attached near a step (only use if the description gives standalone data not tied to one step)";
            case SUPPLIER_CUSTOMER -> "the external supplier feeding the process, or the customer/beneficiary receiving its output";
            case KAIZEN_BURST -> "a flagged bottleneck or improvement opportunity, connected to the step it concerns";
            case INVENTORY -> "a queue or backlog of work-in-progress sitting between steps";
            case SUPERMARKET -> "a managed buffer/pool of ready-to-use work items between steps";
            case PUSH_ARROW -> "work being pushed forward regardless of downstream readiness";
            case SHIPMENT -> "a batch handoff or delivery of finished output";
            case KANBAN_BATCH -> "a fixed-size batch of work pulled through the process";
        };
    }
}
