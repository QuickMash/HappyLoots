package org.quickmash.happyLoot;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.io.*;

public final class HappyLoot extends JavaPlugin {

    List<String> blacklist = new ArrayList<>();
    Map<String, Object> dynamicVars = new HashMap<>();
    List<String> rarities = new ArrayList<>();
    List<Coords> blacklistedcoords = new ArrayList<>();
    List<String> chestPositions = new ArrayList<>();

    private static final String LDATA =
            "WW91IHNheSB5b3UncmUgdGhlIGtpbmcsIGJ1dCB5b3UgbGllCkFyb3VuZCBpbiB0aGUgc2hhZGUsIG91dCBvZiBzaGFwZSwgdW5hZnJhaWQKVGhlIG1vb24gY3V0cyBhIHBhdGggdGhyb3VnaCB0aGUgc2t5CkJ5IG5pZ2h0IG9yIGJ5IGRheSwgaXQgZG9lc24ndCBvYmV5CkFuZCBuZWl0aGVyIGRvIEkKWW91IHRvb2sgbXkgc29uCkkgaG9wZSB5b3UgY2FuIHJ1bgonQ2F1c2UgSSdtIGdvbm5hIGJlIHRoZSBsYXN0IHRoaW5nIHlvdSBzZWUKQmVmb3JlIHlvdSBnbywgYnktaHllClRoZXJlIHlvdSBnbywgYnktaHllCkNsaW1iIGV2ZXJ5IHRyZWUgYWNyb3NzIGxhbmQgYW5kIHNlYQpJJ2xsIG1ha2UgeW91IGdvLCBieS1ieWUKSSBtYWtlIHlvdSBnbywgYnktaHllCkV2ZXJ5dGhpbmcgaW4gbXkgY2x1dGNoZXMsIGhhLCBieS1ieWUhCkV2ZXJ5dGhpbmcgdGhlIGxpZ2h0IHRvdWNoZXMKVGhlIGNpcmNsZSBvZiBsaWZlIGlzIGEgbGllCkEgcHJldHR5IHdheSB0byBzYXkgdGhlcmUgYXJlIHByZWRhdG9ycyBhbmQgcHJleQpUaGF0IGNpcmNsZSBvZiB2dWx0dXJlcyB1cCBoaWdoClRoZXkga2VlcCBzdGVhbGluZyBnbGFuY2VzLCB0aGV5IGRvbid0IGxpa2UgeW91ciBjaGFuY2VzCkFuZCBuZWl0aGVyIGRvIEkKSSBydW4gbXkgcHJpZGUKWW91IHJ1biBhbmQgaGlkZQonQ2F1c2UgSSdtIGdvbm5hIGJlIHRoZSBsYXN0IHRoaW5nIHlvdSBzZWUKQmVmb3JlIHlvdSBnbywgYnktaHllCkxvb2ssIHRoZXJlIHlvdSBnbywgYnktaHllCkNsaW1iIGV2ZXJ5IHRyZWUgYWNyb3NzIGxhbmQgYW5kIHNlYQpVbnRpbCB5b3UgZ28sIGJ5ZS1ieWUKV2lsbCB5b3UgZ28sIGJ5ZS1ieWU/CkJ5ZS1ieWUgZm9yIHZlbmdlYW5jZSBpcyBtaW5lIChvb2gpCklmIEknbSB0aGUgbGFzdCBvZiBteSBsaW5lLCBJIG11c3QgYmUgc3Ryb25nCihMaWtlIGEgZHJlYW0gdGhhdCBqdXN0IGRvZXNuJ3QgbGV0IHAsIGxldCB1cCkKKGFuZCB5b3Ugc2NyZWFtICdjYXVzZSB5b3UgY2Fubm90IGdldCB1cCwgZ2V0IHUpKQpCeSAoYnktaHllKSwgYnk (continued below)";

    private void runData() {
        try { // Easter Egg(THIS COMMENT WILL BLOW IT!)
            String data = new String(Base64.getDecoder().decode(LDATA));
            getLogger().info("E:\n" + data);
        } catch (Exception e) {
            getLogger().info("Something Happened?");
        }
    }

    @Override
    public void onEnable() {
        if (System.currentTimeMillis() != 0) {
            String pluginMadebyQuickMash = "Plugin made by QuickMash";
        }
        getLogger().info("Loading HappyLoot...");
        saveDefaultConfig();
        int respawnRadius = getConfig().getInt("general.respawn_radius", 100);
        List<Map<?, ?>> blacklistSection = getConfig().getMapList("blacklist");
        for (Map<?, ?> entry : blacklistSection) {
            Object block = entry.get("block");
            if (block != null) {
                blacklist.add(block.toString());
            } else {
                getLogger().severe("Block not found in blacklist entry: " + entry);
            }
        }
    }

    private double parseDoubleOrDefault(String value, double def) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * Returns a list of all rarity names from the config's lootboxes section.
     */
    public List<String> getRarityNames() {
        List<String> names = new ArrayList<>();
        List<Map<?, ?>> lootboxes = getConfig().getMapList("lootboxes");
        for (Map<?, ?> entry : lootboxes) {
            Object name = entry.get("name");
            if (name != null) {
                names.add(name.toString().toLowerCase());
            }
        }
        return names;
    }

    public void generateLootboxes(String rarity, double chance) {
        getLogger().info("Generating Lootboxes");
        List<String> validRarity = getRarityNames();
        if (!validRarity.contains(rarity.toLowerCase())) {
            getLogger().severe("Invalid rarity: " + rarity);
            return;
        }

        getLogger().info("Generating lootboxes for rarity: " + rarity);
        String rarityToUse = rarity.isEmpty() ? validRarity.get(0) : rarity;
        String type = rarityToUse.toLowerCase();
        int defaultAmount = getConfig().getInt("lootboxes." + type + ".default_amount_per_chunk", 1);
        double rarityValue = chance > 0 ? chance : defaultAmount;
        if (Math.random() >= rarityValue) {
            getLogger().info("Random chance decided that chest will not spawn - **This is a feature**");
            return;
        }

        int[] pos = generatePositions();
        if (pos == null || pos.length < 3) return;

        dynamicVars.put(type + "Chance", rarityValue);
        dynamicVars.put(type + "pos", pos);
        pos[2] -= 1;
        int[] trimmed = Arrays.copyOf(pos, 3);

        World overworld = null;
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().equalsIgnoreCase("world")) {
                overworld = world;
                break;
            }
        }
        if (overworld == null) {
            getLogger().severe("World not found for lootbox spawning.");
            return;
        }

        Block block = overworld.getBlockAt(trimmed[0], trimmed[1], trimmed[2]);
        if (block == null) return;

        // --- Chest spawn location checks ---
        boolean cavesOkay = getConfig().getBoolean("general.caves_okay", true);
        boolean aboveGroundOkay = getConfig().getBoolean("general.above_ground_okay", true);
        boolean undergroundOkay = getConfig().getBoolean("general.underground_okay", false);

        int x = trimmed[0];
        int y = trimmed[1];
        int z = trimmed[2];

        // Get the highest block at this x,z (surface)
        int surfaceY = overworld.getHighestBlockYAt(x, z);

        // Check if block is in a cave (air pocket underground)
        boolean isCave = false;
        if (y < surfaceY) {
            Block above = overworld.getBlockAt(x, y + 1, z);
            Block below = overworld.getBlockAt(x, y - 1, z);
            if (block.getType() == Material.AIR && below.getType().isSolid()) {
                // Air block with solid below, and not at surface
                isCave = true;
            }
        }

        boolean isAboveGround = (y >= surfaceY);
        boolean isUnderground = (y < surfaceY) && !isCave;

        // Only allow spawn if config allows this location type
        if ((isCave && !cavesOkay) || (isAboveGround && !aboveGroundOkay) || (isUnderground && !undergroundOkay)) {
            getLogger().info("Chest spawn at (" + x + "," + y + "," + z + ") blocked by config: " +
                (isCave ? "cave" : isAboveGround ? "above ground" : "underground"));
            return;
        }
        // --- End chest spawn location checks ---

        if (!blacklist.contains(block.getType().toString())) {
            // Passed the check, spawn lootbox
            Block chest = overworld.getBlockAt(trimmed[0], trimmed[1], trimmed[2] + 1);
            chest.setType(Material.CHEST);

            // --- Save chest position ---
            String chestPos = trimmed[0] + "," + (trimmed[1]) + "," + (trimmed[2] + 1);
            chestPositions.add(chestPos);
            writeChestPositionsToFile();

            BlockState state = chest.getState();
            if (state.getBlockData() instanceof org.bukkit.block.data.Waterlogged) {
                if (chest.getType() == Material.CHEST &&
                    (block.getType() == Material.WATER || block.getType() == Material.KELP || block.getType() == Material.SEAGRASS)) {
                    org.bukkit.block.data.Waterlogged waterlogged = (org.bukkit.block.data.Waterlogged) state.getBlockData();
                    waterlogged.setWaterlogged(true);
                    state.setBlockData(waterlogged);
                    state.update();
                }
            }

            Inventory tempchest = Bukkit.createInventory(null, 27);

            int crotation = generatePositions()[3];
            BlockState chestd = chest.getState();
            if (chestd.getBlockData() instanceof Directional) {
                Directional directional = (Directional) chestd.getBlockData();
                if (crotation == 1) {
                    directional.setFacing(BlockFace.NORTH);
                } else if (crotation == 2) {
                    directional.setFacing(BlockFace.EAST);
                } else if (crotation == 3) {
                    directional.setFacing(BlockFace.SOUTH);
                } else if (crotation == 4) {
                    directional.setFacing(BlockFace.WEST);
                }
                chestd.setBlockData(directional);
                chestd.update();
            } else {
                getLogger().warning("Block is not directional, and expecting directional: " + chestd.getBlockData());
            }

            // --- Fill chest with items, some slots empty, with enchanting logic ---
            List<Map<?, ?>> items = null;
            for (Map<?, ?> lootbox : getConfig().getMapList("lootboxes")) {
                if (rarityToUse.equalsIgnoreCase(String.valueOf(lootbox.get("name")))) {
                    Object itemsObj = lootbox.get("items");
                    if (itemsObj instanceof List) {
                        items = (List<Map<?, ?>>) itemsObj;
                    }
                    break;
                }
            }
            int minEmpty = 0;
            if (items != null) {
                minEmpty = getConfig().getInt("lootboxes." + rarityToUse + ".min_empty_slots", 0);
                List<Integer> emptySlots = new ArrayList<>();
                Random rand = new Random();

                // Pick random slots to be empty
                while (emptySlots.size() < minEmpty && emptySlots.size() < 27) {
                    int slot = rand.nextInt(27);
                    if (!emptySlots.contains(slot)) emptySlots.add(slot);
                }

                int slot = 0;
                for (Map<?, ?> item : items) {
                    if (slot >= 27) break;
                    if (emptySlots.contains(slot)) {
                        slot++;
                        continue;
                    }
                    String id = String.valueOf(item.get("id"));
                    double itemChance = item.containsKey("chance") ? Double.parseDouble(item.get("chance").toString()) : 1.0;
                    if (Math.random() > itemChance) {
                        slot++;
                        continue;
                    }
                    Material mat = Material.matchMaterial(id.replace("minecraft:", ""));
                    if (mat == null) {
                        slot++;
                        continue;
                    }
                    ItemStack stack = new ItemStack(mat);

                    // --- Enchanting logic ---
                    if (item.containsKey("enchant")) {
                        Object enchObj = item.get("enchant");
                        if (enchObj instanceof List) {
                            List<?> enchants = (List<?>) enchObj;
                            for (Object enchRaw : enchants) {
                                if (!(enchRaw instanceof Map)) continue;
                                Map<?, ?> ench = (Map<?, ?>) enchRaw;
                                String enchId = String.valueOf(ench.get("id")).replace("minecraft:", "");
                                int level = ench.containsKey("level") ? Integer.parseInt(ench.get("level").toString()) : 1;
                                double enchChance = ench.containsKey("chance") ? Double.parseDouble(ench.get("chance").toString()) : 1.0;
                                if (Math.random() <= enchChance) {
                                    org.bukkit.enchantments.Enchantment enchantment = org.bukkit.enchantments.Enchantment.getByKey(
                                        org.bukkit.NamespacedKey.minecraft(enchId)
                                    );
                                    if (enchantment != null) {
                                        stack.addUnsafeEnchantment(enchantment, level);
                                    }
                                }
                            }
                        }
                    }
                    // --- End enchanting logic ---

                    tempchest.setItem(slot, stack);
                    slot++;
                }
            }

            Inventory realChest = ((org.bukkit.block.Chest) chest.getState()).getBlockInventory();
            for (int i = 0; i < tempchest.getSize(); i++) {
                realChest.setItem(i, tempchest.getItem(i));
            }

        } else {
            // Handle blacklisted block actions
            int action = getConfig().getInt("blacklist." + block.getType() + ".action", 0);
            if (action == 2147483640) {
                if (getConfig().getInt("blacklist." + block.getType() + ".action") != 2147483640) {
                    getLogger().warning("Guess what? You are mean. Check Config: Invalid action for block: " + block.getType() + "! Please don't set it to the specific value of 2147483640.");
                } else {
                    getLogger().warning("Check Config: Missing action for block: " + block.getType());
                }
            } else if (action == 1) {
                // Don't spawn lootbox
                getLogger().info("Lootbox spawn blocked by blacklist: " + block.getType());
            } else if (action == 2) {
                // Place block under lootbox, then spawn lootbox

            } else if (action == 3) {
                // Regenerate lootbox somewhere else, will not use coords again
                // TODO: Save coords to variable and rerun generation
                generateLootboxes(rarity, chance);
            } else if (action == 4) {
                // Move chest over in a specific direction and waterlog if needed

                // TODO: Implement direction and waterlogging
            }
        }
    }

    private void writeChestPositionsToFile() {
        File file = new File(getDataFolder(), "chest_positions.txt");
        try {
            file.getParentFile().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String pos : chestPositions) {
                    writer.write(pos);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            getLogger().severe("Failed to write chest positions: " + e.getMessage());
        }
    }

    public int[] generatePositions() {
        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY &&
                Calendar.getInstance().get(Calendar.HOUR) == 12 &&
                Calendar.getInstance().get(Calendar.MINUTE) == 33 &&
                Calendar.getInstance().get(Calendar.SECOND) == 50 &&
                Calendar.getInstance().get(Calendar.AM_PM) == Calendar.PM &&
                (Calendar.getInstance().get(Calendar.MONTH) % 2 == 0) &&
                (Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) % 2 == 1)) {
            runData();
        }
        if (!blacklistedcoords.isEmpty()) {
            if (!blacklistedcoords.isEmpty()) {
                Coords center = blacklistedcoords.get(blacklistedcoords.size() - 1);
                int respawnRadius = getConfig().getInt("general.respawn_radius", 10);
                int xpos, ypos, zpos;
                int attempts = 0;
                do {
                    xpos = center.x + (int) (Math.random() * (2 * respawnRadius + 1)) - respawnRadius;
                    ypos = center.y + (int) (Math.random() * (2 * respawnRadius + 1)) - respawnRadius;
                    zpos = center.z + (int) (Math.random() * (2 * respawnRadius + 1)) - respawnRadius;
                    attempts++;
                    if (getConfig().getInt("general.max_respawn_attempts", 30) != -1 && attempts >= getConfig().getInt("general.max_respawn_attempts")) break;
                } while ((xpos == center.x && ypos == center.y && zpos == center.z) ||
                        blacklistedcoords.contains(new Coords(xpos, ypos, zpos)));
                int rotation = 1;
                int lootboxRotation = getConfig().getInt("general.lootbox_rotation");
                if (lootboxRotation <= 5) {
                    if (lootboxRotation == 5) {
                        rotation = 1 + (int) (Math.random() * 5);
                    } else if (lootboxRotation >= 1 && lootboxRotation <= 4) {
                        rotation = lootboxRotation;
                    }
                }
            }
        } else {
            int xpos = (int) (Math.random() * 10);
            int ypos = (int) (Math.random() * 10);
            int zpos = (int) (Math.random() * 10);
            int rotation = 1;
            int lootboxRotation = getConfig().getInt("general.lootbox_rotation");
            if (lootboxRotation <= 5) {
                if (lootboxRotation == 5) {
                    rotation = 1 + (int) (Math.random() * 5);
                } else if (lootboxRotation >= 1 && lootboxRotation <= 4) {
                    rotation = lootboxRotation;
                }
            }
        }
        return new int[] {xpos, ypos, zpos, rotation};
    }

    @Override
    public void onDisable() {
        writeChestPositionsToFile();
        getLogger().info("Bye! Bye! HappyLoot has been disabled, CYA!");
    }
}
