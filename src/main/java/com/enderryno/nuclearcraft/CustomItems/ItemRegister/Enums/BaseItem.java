package com.enderryno.nuclearcraft.CustomItems.ItemRegister.Enums;

import com.enderryno.nuclearcraft.CustomItems.ItemInterfaces.GenericItem;


public enum BaseItem {

    GasMask(1, "Gas Mask", null),
    GasMaskFilter(2, "Gas mask filter", null);

    private final int id;
    private final String displayName;
    private final Class<GenericItem> customItemClass;

    BaseItem(int id, String displayName, Class<GenericItem> customItemClass){
        this.id = id;
        this.displayName = displayName;
        this.customItemClass = customItemClass;
    }




    public String getDisplayName() {
        return displayName;
    }

    public int getId() {
        return id;
    }

    public Class<?> getCustomItemClass() {
        return customItemClass;
    }
}
