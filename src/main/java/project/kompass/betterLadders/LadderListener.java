package project.kompass.betterLadders;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class LadderListener implements Listener {

    // 1. TELEPORT TO TOP/BOTTOM
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

    // 2. CHAIN PLACING LADDERS
    @EventHandler
    public void onLadderChainPlace(PlayerInteractEvent event) {
        if (!event.getPlayer().isSneaking() || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();
        ItemStack itemInHand = event.getItem();

        if (clickedBlock != null && clickedBlock.getType() == Material.LADDER &&
                itemInHand != null && itemInHand.getType() == Material.LADDER) {

            Block bottomLadder = clickedBlock;
            while (bottomLadder.getRelative(BlockFace.DOWN).getType() == Material.LADDER) {
                bottomLadder = bottomLadder.getRelative(BlockFace.DOWN);
            }

            Block targetBlock = bottomLadder.getRelative(BlockFace.DOWN);

            if (targetBlock.getType().isAir() || targetBlock.isReplaceable()) {
                targetBlock.setType(Material.LADDER, false);
                targetBlock.setBlockData(clickedBlock.getBlockData());

                player.swingMainHand();
                targetBlock.getWorld().playSound(targetBlock.getLocation(),
                        org.bukkit.Sound.BLOCK_LADDER_PLACE, 1.0f, 1.0f);

                if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                }
                event.setCancelled(true);
            }
        }
    }

    // 3. PREVENT FLOATING LADDERS FROM BREAKING
    @EventHandler
    public void onLadderPhysics(BlockPhysicsEvent event) {
        if (event.getBlock().getType() == Material.LADDER) {
            event.setCancelled(true);
        }
    }
}