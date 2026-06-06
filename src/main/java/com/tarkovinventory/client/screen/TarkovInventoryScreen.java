    private IItemHandler getRigItemHandler() {
        ItemStack rig = getEquippedRigItem();
        if (rig.isEmpty()) return null;
        // Use custom Tarkov rig inventory (independent of mod inventory)
        return BackpackCompat.getRigInventoryHandler(rig);
    }
