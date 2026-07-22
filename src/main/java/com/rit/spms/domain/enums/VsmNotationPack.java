package com.rit.spms.domain.enums;

/**
 * A bundle of {@link VsmNodeType} symbols an Admin can turn on/off for this installation (see the
 * {@code VSM_ENABLED_NOTATION_PACKS} row in {@code organization_setting}). Full lean-manufacturing
 * VSM notation (inventory, supermarkets, shipments...) is overkill for e.g. a government planning
 * office but exactly right for a manufacturing-adjacent department -- packs let an Admin slice which
 * symbol set their leaders see on the canvas palette, without every installation being stuck with
 * every symbol this module ever grows to support.
 */
public enum VsmNotationPack {
    /** Process box, data box, supplier/customer, kaizen burst -- always available, the minimal
     *  generic lean-VSM vocabulary every installation needs regardless of industry. */
    GENERIC,
    /** Inventory, supermarket, push-arrow, shipment, kanban-batch -- classic manufacturing symbols,
     *  off by default. Node types are reserved in {@link VsmNodeType} now but have no canvas
     *  renderer yet; building them out is a later phase, not a redesign. */
    MANUFACTURING
}
