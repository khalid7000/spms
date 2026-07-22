package com.rit.spms.domain.enums;

/**
 * The lean-VSM symbol a {@link com.rit.spms.domain.VsmNode} renders as. A superset covering every
 * symbol this module anticipates ever needing; each value is tagged to exactly one {@link
 * VsmNotationPack} so an Admin can slice which ones actually appear on a given installation's canvas
 * palette (see {@link VsmNotationPack}). Only the GENERIC-pack values have a canvas renderer as of
 * Phase 1 -- the MANUFACTURING ones are reserved here so adding their renderers later is additive,
 * not a re-migration.
 */
public enum VsmNodeType {
    PROCESS(VsmNotationPack.GENERIC),
    DATA_BOX(VsmNotationPack.GENERIC),
    SUPPLIER_CUSTOMER(VsmNotationPack.GENERIC),
    KAIZEN_BURST(VsmNotationPack.GENERIC),
    INVENTORY(VsmNotationPack.MANUFACTURING),
    SUPERMARKET(VsmNotationPack.MANUFACTURING),
    PUSH_ARROW(VsmNotationPack.MANUFACTURING),
    SHIPMENT(VsmNotationPack.MANUFACTURING),
    KANBAN_BATCH(VsmNotationPack.MANUFACTURING);

    private final VsmNotationPack pack;

    VsmNodeType(VsmNotationPack pack) {
        this.pack = pack;
    }

    public VsmNotationPack getPack() {
        return pack;
    }
}
