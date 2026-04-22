package project.kompass.betterLadders;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class LadderListener implements Listener {

    // 1. FASTER CLIMBING
    @EventHandler
    public void onLadderClimb(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Block block = player.getLocation().getBlock();

        if (block.getType() == Material.LADDER) {
            Vector velocity = player.getVelocity();

            // If player is looking up and moving, give them a boost
            if (player.getLocation().getPitch() < -15 && velocity.getY() > 0) {
                player.setVelocity(velocity.add(new Vector(0, 0.15, 0)));
            }
            // If player is looking down, speed up the descent
            else if (player.getLocation().getPitch() > 15 && player.isSneaking()) {
                player.setVelocity(velocity.add(new Vector(0, -0.2, 0)));
            }
        }
    }

    // 2. TELEPORT TO TOP/BOTTOM
    @EventHandler
    public void onLadderInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.LADDER) {
            Player player = event.getPlayer();

            // Only trigger if they are holding nothing (to allow block placement)
            if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                Block start = event.getClickedBlock();
                boolean goUp = player.getLocation().getPitch() < 0;

                Block target = findLadderEnd(start, goUp);

                // Teleport to the center of the target ladder block
                player.teleport(target.getLocation().add(0.5, 0.1, 0.5).setDirection(player.getLocation().getDirection()));
                event.setCancelled(true);
            }
        }
    }

    private Block findLadderEnd(Block start, boolean up) {
        BlockFace face = up ? BlockFace.UP : BlockFace.DOWN;
        Block current = start;
        while (current.getRelative(face).getType() == Material.LADDER) {
            current = current.getRelative(face);
        }
        return current;
    }

    //3. CHAIN PLACING LADDERS
    @EventHandler
    public void onLadderChainPlace(PlayerInteractEvent event) {
        // 1. Check if the player is sneaking and right-clicking a block
        if (!event.getPlayer().isSneaking() || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();
        ItemStack itemInHand = event.getItem();

        // 2. Ensure they are clicking a ladder AND holding a ladder
        if (clickedBlock != null && clickedBlock.getType() == Material.LADDER &&
                itemInHand != null && itemInHand.getType() == Material.LADDER) {

            // 3. Find the bottom-most ladder in this specific column
            Block bottomLadder = clickedBlock;
            while (bottomLadder.getRelative(BlockFace.DOWN).getType() == Material.LADDER) {
                bottomLadder = bottomLadder.getRelative(BlockFace.DOWN);
            }

            Block targetBlock = bottomLadder.getRelative(BlockFace.DOWN);

            // 4. Check if the space below is empty (Air or replaceable like grass)
            if (targetBlock.getType().isAir() || targetBlock.isReplaceable()) {

                // Place the ladder
                targetBlock.setType(Material.LADDER, false);

                // 5. Copy the 'Facing' direction from the clicked ladder
                // This ensures the floating ladder faces the same way as the one it's hanging from
                targetBlock.setBlockData(clickedBlock.getBlockData());

                // 6. Visuals and Survival mechanics
                player.swingMainHand();
                targetBlock.getWorld().playSound(targetBlock.getLocation(),
                        org.bukkit.Sound.BLOCK_LADDER_PLACE, 1.0f, 1.0f);

                if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                }

                // Cancel the event so they don't accidentally place a ladder on the face they clicked
                event.setCancelled(true);
            }
        }
    }
}