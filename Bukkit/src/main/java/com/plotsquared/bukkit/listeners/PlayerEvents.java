package com.plotsquared.bukkit.listeners;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotHandler;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotInventory;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.RegExUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.bukkit.BukkitMain;
import com.plotsquared.bukkit.object.BukkitLazyBlock;
import com.plotsquared.bukkit.object.BukkitPlayer;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.listener.PlayerBlockEventType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Player Events involving plots
 *
 */
@SuppressWarnings({ "deprecation", "unchecked" })
public class PlayerEvents extends com.plotsquared.listener.PlotListener implements Listener {
    
    private boolean pistonBlocks = true;
    private float lastRadius;
    // To prevent recursion
    private boolean tmp_teleport = true;
    
    public static void sendBlockChange(final org.bukkit.Location bloc, final Material type, final byte data) {
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                final String world = bloc.getWorld().getName();
                final int x = bloc.getBlockX();
                final int z = bloc.getBlockZ();
                final int distance = Bukkit.getViewDistance() * 16;
                for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
                    PlotPlayer player = entry.getValue();
                    final Location loc = player.getLocation();
                    if (loc.getWorld().equals(world)) {
                        if (16 * Math.abs(loc.getX() - x) / 16 > distance || 16 * Math.abs(loc.getZ() - z) / 16 > distance) {
                            continue;
                        }
                        ((BukkitPlayer) player).player.sendBlockChange(bloc, type, data);
                    }
                }
            }
        }, 3);
    }
    
    @EventHandler
    public void onRedstoneEvent(final BlockRedstoneEvent event) {
        final Block block = event.getBlock();
        switch (block.getType()) {
            case REDSTONE_LAMP_OFF:
            case REDSTONE_WIRE:
            case REDSTONE_LAMP_ON:
            case PISTON_BASE:
            case PISTON_STICKY_BASE:
            case IRON_DOOR_BLOCK:
            case LEVER:
            case WOODEN_DOOR:
            case FENCE_GATE:
            case WOOD_BUTTON:
            case STONE_BUTTON:
            case IRON_PLATE:
            case WOOD_PLATE:
            case STONE_PLATE:
            case GOLD_PLATE:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
            case IRON_TRAPDOOR:
            case SPRUCE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case ACACIA_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case POWERED_RAIL:
                return;
            default:
                final Location loc = BukkitUtil.getLocation(block.getLocation());
                PlotArea area = loc.getPlotArea();
                if (area == null) {
                    return;
                }
                final Plot plot = area.getOwnedPlotAbs(loc);
                if (plot == null) {
                    return;
                }
                final Flag redstone = FlagManager.getPlotFlagRaw(plot, "redstone");
                if (redstone != null) {
                    if ((Boolean) redstone.getValue()) {
                        return;
                    } else {
                        event.setNewCurrent(0);
                        return;
                    }
                }
                if (Settings.REDSTONE_DISABLER) {
                    if (UUIDHandler.getPlayer(plot.owner) == null) {
                        boolean disable = true;
                        for (final UUID trusted : plot.getTrusted()) {
                            if (UUIDHandler.getPlayer(trusted) != null) {
                                disable = false;
                                break;
                            }
                        }
                        if (disable) {
                            event.setNewCurrent(0);
                            return;
                        }
                    }
                }
                if (Settings.REDSTONE_DISABLER_UNOCCUPIED) {
                    for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
                        if (plot.equals(entry.getValue().getCurrentPlot())) {
                            return;
                        }
                    }
                    event.setNewCurrent(0);
                }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPhysicsEvent(final BlockPhysicsEvent event) {
        switch (event.getChangedType()) {
            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON: {
                final Block block = event.getBlock();
                final Location loc = BukkitUtil.getLocation(block.getLocation());
                PlotArea area = loc.getPlotArea();
                if (area == null) {
                    return;
                }
                final Plot plot = area.getOwnedPlotAbs(loc);
                if (plot == null) {
                    return;
                }
                if (FlagManager.isPlotFlagFalse(plot, "redstone")) {
                    event.setCancelled(true);
                }
                return;
            }
            case DRAGON_EGG:
            case ANVIL:
            case SAND:
            case GRAVEL:
                final Block block = event.getBlock();
                final Location loc = BukkitUtil.getLocation(block.getLocation());
                PlotArea area = loc.getPlotArea();
                if (area == null) {
                    return;
                }
                final Plot plot = area.getOwnedPlotAbs(loc);
                if (plot != null && FlagManager.isPlotFlagTrue(plot, "disable-physics")) {
                    event.setCancelled(true);
                    return;
                }
                return;
            default:
                break;
        }
    }
    
    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent event) {
        final Projectile entity = event.getEntity();
        final Location loc = BukkitUtil.getLocation(entity);
        if (!PS.get().hasPlotArea(loc.getWorld())) {
            return;
        }
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlotAbs(loc);
        //
        final ProjectileSource shooter = entity.getShooter();
        if (shooter instanceof Player) {
            final PlotPlayer pp = BukkitUtil.getPlayer((Player) shooter);
            if (plot == null) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_PROJECTILE_UNOWNED)) {
                    entity.remove();
                }
                return;
            }
            if (plot.isAdded(pp.getUUID()) || Permissions.hasPermission(pp, C.PERMISSION_PROJECTILE_OTHER)) {
                return;
            }
            entity.remove();
        } else if (!(shooter instanceof Entity) && shooter != null) {
            if (plot == null) {
                entity.remove();
                return;
            }
            final Location sLoc = BukkitUtil.getLocation(((BlockProjectileSource) shooter).getBlock().getLocation());
            if (!area.contains(sLoc.getX(), sLoc.getZ())) {
                entity.remove();
                return;
            }
            final Plot sPlot = area.getOwnedPlotAbs(sLoc);
            if (sPlot == null || !PlotHandler.sameOwners(plot, sPlot)) {
                entity.remove();
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void PlayerCommand(final PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase().replaceAll("/", "").trim();
        if (msg.isEmpty()) {
            return;
        }
        final String[] split = msg.split(" ");
        final PluginCommand cmd = Bukkit.getServer().getPluginCommand(split[0]);
        if (cmd == null) {
            if (split[0].equals("plotme") || split[0].equals("ap")) {
                final Player player = event.getPlayer();
                if (Settings.USE_PLOTME_ALIAS) {
                    player.performCommand("plots " + StringMan.join(Arrays.copyOfRange(split, 1, split.length), " "));
                } else {
                    MainUtil.sendMessage(BukkitUtil.getPlayer(player), C.NOT_USING_PLOTME);
                }
                event.setCancelled(true);
                return;
            }
        }
        final Player player = event.getPlayer();
        final BukkitPlayer pp = (BukkitPlayer) BukkitUtil.getPlayer(player);
        Plot plot = pp.getCurrentPlot();
        if (plot == null) {
            return;
        }
        Flag flag = FlagManager.getPlotFlagRaw(plot, "blocked-cmds");
        if (flag == null || Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_BLOCKED_CMDS)) {
            return;
        }
        final List<String> v = (List<String>) flag.getValue();
        final String[] parts = msg.split(" ");
        String c = parts[0];
        if (parts[0].contains(":")) {
            c = parts[0].split(":")[1];
            msg = msg.replace(parts[0].split(":")[0] + ":", "");
        }
        final String l = c;
        final List<String> aliases = new ArrayList<>();
        for (final HelpTopic cmdLabel : Bukkit.getServer().getHelpMap().getHelpTopics()) {
            if (c.equals(cmdLabel.getName())) {
                break;
            }
            PluginCommand p;
            final String label = cmdLabel.getName().replaceFirst("/", "");
            if (aliases.contains(label)) {
                continue;
            }
            if ((p = Bukkit.getPluginCommand(label)) != null) {
                for (String a : p.getAliases()) {
                    if (aliases.contains(a)) {
                        continue;
                    }
                    aliases.add(a);
                    a = a.replaceFirst("/", "");
                    if (!a.equals(label) && a.equals(c)) {
                        c = label;
                        break;
                    }
                }
            }
        }
        if (!l.equals(c)) {
            msg = msg.replace(l, c);
        }
        for (final String s : v) {
            Pattern pattern;
            if (!RegExUtil.compiledPatterns.containsKey(s)) {
                RegExUtil.compiledPatterns.put(s, pattern = Pattern.compile(s));
            } else {
                pattern = RegExUtil.compiledPatterns.get(s);
            }
            if (pattern.matcher(msg).matches()) {
                MainUtil.sendMessage(pp, C.COMMAND_BLOCKED);
                String perm;
                if (plot.isAdded(pp.getUUID())) {
                    perm = "plots.admin.command.blocked-cmds.shared";
                } else {
                    perm = "plots.admin.command.blocked-cmds.other";
                }
                if (!Permissions.hasPermission(pp, perm)) {
                    event.setCancelled(true);
                }
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConnect(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        BukkitUtil.getPlayer(event.getPlayer()).unregister();
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        // Now
        String name = pp.getName();
        StringWrapper sw = new StringWrapper(name);
        final UUID uuid = pp.getUUID();
        UUIDHandler.add(sw, uuid);

        Location loc = pp.getLocation();
        PlotArea area = loc.getPlotArea();
        if (area != null) {
            Plot plot = area.getPlot(loc);
            if (plot != null) {
                plotEntry(pp, plot);
            }
        }
        // Delayed

        // Async
        TaskManager.runTaskLaterAsync(new Runnable() {
            @Override
            public void run() {
                if (!player.hasPlayedBefore() && player.isOnline()) {
                    player.saveData();
                }
                EventUtil.manager.doJoinTask(pp);
            }
        }, 20);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PlayerMove(final PlayerMoveEvent event) {
        final org.bukkit.Location from = event.getFrom();
        final org.bukkit.Location to = event.getTo();
        int x2;
        if (MathMan.roundInt(from.getX()) != (x2 = MathMan.roundInt(to.getX()))) {
            final Player player = event.getPlayer();
            final PlotPlayer pp = BukkitUtil.getPlayer(player);
            // Cancel teleport
            TaskManager.TELEPORT_QUEUE.remove(pp.getName());
            // Set last location
            Location loc = BukkitUtil.getLocation(to);
            pp.setMeta("location", loc);
            PlotArea area = loc.getPlotArea();
            if (area == null) {
                pp.deleteMeta("lastplot");
                return;
            }
            Plot now = area.getPlot(loc);
            final Plot lastPlot = pp.getMeta("lastplot");
            if (now == null) {
                if (lastPlot != null && !plotExit(pp, lastPlot)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
                    if (lastPlot.equals(BukkitUtil.getLocation(from).getPlot())) {
                        player.teleport(from);
                    } else {
                        player.teleport(player.getWorld().getSpawnLocation());
                    }
                    event.setCancelled(true);
                    return;
                }
            } else if (now.equals(lastPlot)) {
                return;
            } else if (!plotEntry(pp, now)) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                player.teleport(from);
                event.setCancelled(true);
                return;
            }
            final Integer border = area.getBorder();
            if (x2 > border) {
                to.setX(border - 4);
                player.teleport(event.getTo());
                MainUtil.sendMessage(pp, C.BORDER);
                return;
            } else if (x2 < -border) {
                to.setX(-border + 4);
                player.teleport(event.getTo());
                MainUtil.sendMessage(pp, C.BORDER);
                return;
            }
            return;
        }
        int z2;
        if (MathMan.roundInt(from.getZ()) != (z2 = MathMan.roundInt(to.getZ()))) {
            final Player player = event.getPlayer();
            final PlotPlayer pp = BukkitUtil.getPlayer(player);
            // Cancel teleport
            TaskManager.TELEPORT_QUEUE.remove(pp.getName());
            // Set last location
            Location loc = BukkitUtil.getLocation(to);
            pp.setMeta("location", loc);
            PlotArea area = loc.getPlotArea();
            if (area == null) {
                pp.deleteMeta("lastplot");
                return;
            }
            Plot now = area.getPlot(loc);
            final Plot lastPlot = pp.getMeta("lastplot");
            if (now == null) {
                if (lastPlot != null && !plotExit(pp, lastPlot)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
                    if (lastPlot.equals(BukkitUtil.getLocation(from).getPlot())) {
                        player.teleport(from);
                    } else {
                        player.teleport(player.getWorld().getSpawnLocation());
                    }
                    event.setCancelled(true);
                    return;
                }
            } else if (now.equals(lastPlot)) {
                return;
            } else if (!plotEntry(pp, now)) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                player.teleport(from);
                event.setCancelled(true);
                return;
            }
            final Integer border = area.getBorder();
            if (z2 > border) {
                to.setZ(border - 4);
                player.teleport(event.getTo());
                MainUtil.sendMessage(pp, C.BORDER);
            } else if (z2 < -border) {
                to.setZ(-border + 4);
                player.teleport(event.getTo());
                MainUtil.sendMessage(pp, C.BORDER);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final PlotPlayer plr = BukkitUtil.getPlayer(player);
        Location loc = plr.getLocation();
        PlotArea area = loc.getPlotArea();
        if (area == null || !area.PLOT_CHAT && !plr.getAttribute("chat")) {
            return;
        }
        final Plot plot = area.getPlotAbs(loc);
        if (plot == null) {
            return;
        }
        final String message = event.getMessage();
        String format = C.PLOT_CHAT_FORMAT.s();
        final String sender = event.getPlayer().getDisplayName();
        final PlotId id = plot.getId();
        final Set<Player> recipients = event.getRecipients();
        recipients.clear();
        for (final Player p : Bukkit.getOnlinePlayers()) {
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            if (pp.getAttribute("chatspy")) {
                String spy = event.getFormat();
                spy = String.format(spy, sender, message);
                pp.sendMessage(spy);
            } else if (plot.equals(pp.getCurrentPlot())) {
                recipients.add(p);
            }
        }
        format = format.replaceAll("%plot_id%", id.x + ";" + id.y).replaceAll("%sender%", "%s").replaceAll("%msg%", "%s");
        format = ChatColor.translateAlternateColorCodes('&', format);
        event.setFormat(format);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void BlockDestroy(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Location loc = BukkitUtil.getLocation(event.getBlock().getLocation());
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        final Plot plot = area.getPlotAbs(loc);
        if (plot != null) {
            if (event.getBlock().getY() == 0) {
                event.setCancelled(true);
                return;
            }
            final PlotPlayer pp = BukkitUtil.getPlayer(player);
            if (!plot.hasOwner()) {
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_UNOWNED);
                event.setCancelled(true);
                return;
            } else if (!plot.isAdded(pp.getUUID())) {
                final Flag destroy = FlagManager.getPlotFlagRaw(plot, "break");
                final Block block = event.getBlock();
                if (destroy != null && ((HashSet<PlotBlock>) destroy.getValue())
                        .contains(new PlotBlock((short) block.getTypeId(), block.getData()))) {
                    return;
                }
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_OTHER)) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_OTHER);
                event.setCancelled(true);
            } else if (Settings.DONE_RESTRICTS_BUILDING && plot.getFlags().containsKey("done")) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_ROAD)) {
            return;
        }
        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_ROAD);
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBigBoom(final EntityExplodeEvent event) {
        Location loc = BukkitUtil.getLocation(event.getLocation());
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            if (!PS.get().hasPlotArea(loc.getWorld())) {
                return;
            }
            final Iterator<Block> iter = event.blockList().iterator();
            while (iter.hasNext()) {
                iter.next();
                if (loc.getPlotArea() != null) {
                    iter.remove();
                }
            }
            return;
        }
        Plot plot = area.getOwnedPlot(loc);
        if (plot != null) {
            if (FlagManager.isPlotFlagTrue(plot, "explosion")) {
                List<MetadataValue> meta = event.getEntity().getMetadata("plot");
                Plot origin;
                if (meta.isEmpty()) {
                    origin = plot;
                } else {
                    origin = (Plot) meta.get(0).value();
                }
                if (lastRadius != 0) {
                    final List<Entity> nearby = event.getEntity().getNearbyEntities(lastRadius, lastRadius, lastRadius);
                    for (final Entity near : nearby) {
                        if (near instanceof TNTPrimed || near.getType() == EntityType.MINECART_TNT) {
                            if (!near.hasMetadata("plot")) {
                                near.setMetadata("plot", new FixedMetadataValue((Plugin) PS.get().IMP, plot));
                            }
                        }
                    }
                    lastRadius = 0;
                }
                final Iterator<Block> iter = event.blockList().iterator();
                while (iter.hasNext()) {
                    final Block b = iter.next();
                    loc = BukkitUtil.getLocation(b.getLocation());
                    if (!area.contains(loc.getX(), loc.getZ()) || !origin.equals(area.getOwnedPlot(loc))) {
                        iter.remove();
                    }
                }
                return;
            }
        }
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWorldChanged(final PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        final PlotPlayer pp = BukkitUtil.getPlayer(player);


        // Delete last location
        pp.deleteMeta("location");
        Plot plot = (Plot) pp.deleteMeta("lastplot");
        if (plot != null) {
            plotExit(pp, plot);
        }

        if (BukkitMain.worldEdit != null) {
            if (!Permissions.hasPermission(pp, C.PERMISSION_WORLDEDIT_BYPASS)) {
                if (pp.getAttribute("worldedit")) {
                    pp.removeAttribute("worldedit");
                }
            }
        }
        if (Settings.PERMISSION_CACHING) {
            pp.deleteMeta("perm");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPeskyMobsChangeTheWorldLikeWTFEvent(final EntityChangeBlockEvent event) {
        final String world = event.getBlock().getWorld().getName();
        if (!PS.get().hasPlotArea(world)) {
            return;
        }
        final Entity e = event.getEntity();
        if (!(e instanceof org.bukkit.entity.FallingBlock)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityBlockForm(final EntityBlockFormEvent e) {
        final String world = e.getBlock().getWorld().getName();
        if (!PS.get().hasPlotArea(world)) {
            return;
        }
        if (BukkitUtil.getLocation(e.getBlock().getLocation()).getPlotArea() != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBS(final BlockSpreadEvent e) {
        final Block b = e.getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (loc.isPlotRoad()) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBF(final BlockFormEvent e) {
        final Block b = e.getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (loc.isPlotRoad()) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBD(final BlockDamageEvent event) {
        final Player player = event.getPlayer();
        Location loc = BukkitUtil.getLocation(event.getBlock().getLocation());
        if (player == null) {
            if (loc.isPlotRoad()) {
                event.setCancelled(true);
                return;
            }
        }
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        final Plot plot = area.getPlotAbs(loc);
        if (plot != null) {
            if (loc.getY() == 0) {
                event.setCancelled(true);
                return;
            }
            if (!plot.hasOwner()) {
                final PlotPlayer pp = BukkitUtil.getPlayer(player);
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                    return;
                }
                event.setCancelled(true);
                return;
            }
            final PlotPlayer pp = BukkitUtil.getPlayer(player);
            if (!plot.isAdded(pp.getUUID())) {
                final Flag destroy = FlagManager.getPlotFlagRaw(plot, "break");
                final Block block = event.getBlock();
                if (destroy != null && ((HashSet<PlotBlock>) destroy.getValue())
                        .contains(new PlotBlock((short) block.getTypeId(), block.getData()))) {
                    return;
                }
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_OTHER)) {
                    return;
                }
                event.setCancelled(true);
                return;
            }
            return;
        }
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_ROAD)) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFade(final BlockFadeEvent e) {
        final Block b = e.getBlock();
        Location loc = BukkitUtil.getLocation(b.getLocation());
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        if (area.getOwnedPlot(loc) == null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChange(final BlockFromToEvent e) {
        Block from = e.getBlock();
        Block to = e.getToBlock();
        Location tloc = BukkitUtil.getLocation(to.getLocation());
        PlotArea area = tloc.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlot(tloc);
        Location floc = BukkitUtil.getLocation(from.getLocation());
        if (!area.contains(floc.getX(), floc.getZ()) || !Objects.equals(plot, area.getOwnedPlot(floc)) || (plot != null && FlagManager.isPlotFlagTrue(plot, "disable-physics"))) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGrow(final BlockGrowEvent e) {
        final Block b = e.getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (loc.isUnownedPlotArea()) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonExtend(final BlockPistonExtendEvent event) {
        final Block block = event.getBlock();
        final Location loc = BukkitUtil.getLocation(block.getLocation());
        final BlockFace face = event.getDirection();
        final Vector relative = new Vector(face.getModX(), face.getModY(), face.getModZ());
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            if (!PS.get().hasPlotArea(loc.getWorld())) {
                return;
            }
            for (final Block b : event.getBlocks()) {
                if (BukkitUtil.getLocation(b.getLocation().add(relative)).getPlotArea() != null) {
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        final Plot plot = area.getOwnedPlot(loc);
        if (plot == null) {
            event.setCancelled(true);
            return;
        }
        final List<Block> blocks = event.getBlocks();
        for (final Block b : blocks) {
            final Location bloc = BukkitUtil.getLocation(b.getLocation().add(relative));
            if (!area.contains(bloc.getX(), bloc.getZ())) {
                event.setCancelled(true);
                return;
            }
            if (!plot.equals(area.getOwnedPlot(bloc))) {
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonRetract(final BlockPistonRetractEvent event) {
        final Block block = event.getBlock();
        Location loc = BukkitUtil.getLocation(block.getLocation());
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            if (!PS.get().hasPlotArea(loc.getWorld())) {
                return;
            }
            if (pistonBlocks) {
                try {
                    for (final Block pulled : event.getBlocks()) {
                        loc = BukkitUtil.getLocation(pulled.getLocation());
                        if (loc.getPlotArea() != null) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                } catch (final Throwable e) {
                    pistonBlocks = false;
                }
            }
            if (!pistonBlocks && block.getType() != Material.PISTON_BASE) {
                final BlockFace dir = event.getDirection();
                loc = BukkitUtil.getLocation(block.getLocation().add(dir.getModX() * 2, dir.getModY() * 2, dir.getModZ() * 2));
                if (loc.getPlotArea() != null) {
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        Plot plot = area.getOwnedPlot(loc);
        if (pistonBlocks) {
            try {
                for (final Block pulled : event.getBlocks()) {
                    loc = BukkitUtil.getLocation(pulled.getLocation());
                    if (!area.contains(loc.getX(), loc.getZ())) {
                        event.setCancelled(true);
                        return;
                    }
                    Plot newPlot = area.getOwnedPlot(loc);
                    if (!Objects.equals(plot, newPlot)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            } catch (final Throwable e) {
                pistonBlocks = false;
            }
        }
        if (!pistonBlocks && block.getType() != Material.PISTON_BASE) {
            final BlockFace dir = event.getDirection();
            loc = BukkitUtil.getLocation(block.getLocation().add(dir.getModX() * 2, dir.getModY() * 2, dir.getModZ() * 2));
            if (!area.contains(loc)) {
                event.setCancelled(true);
                return;
            }
            Plot newPlot = area.getOwnedPlot(loc);
            if (!Objects.equals(plot, newPlot)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDispense(final BlockDispenseEvent e) {
        Material type = e.getItem().getType();
        if (type != Material.WATER_BUCKET && type != Material.LAVA_BUCKET) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(e.getVelocity().toLocation(e.getBlock().getWorld()));
        if (loc.isPlotRoad()) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStructureGrow(final StructureGrowEvent e) {
        if (!PS.get().hasPlotArea(e.getWorld().getName())) {
            return;
        }
        final List<BlockState> blocks = e.getBlocks();
        if (blocks.isEmpty()) {
            return;
        }
        Location loc = BukkitUtil.getLocation(blocks.get(0).getLocation());
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            for (int i = blocks.size() - 1; i >= 0; i--) {
                loc = BukkitUtil.getLocation(blocks.get(i).getLocation());
                if (loc.getPlotArea() != null) {
                    blocks.remove(i);
                }
            }
            return;
        } else {
            Plot origin = area.getOwnedPlot(loc);
            if (origin == null) {
                e.setCancelled(true);
                return;
            }
            for (int i = blocks.size() - 1; i >= 0; i--) {
                loc = BukkitUtil.getLocation(blocks.get(i).getLocation());
                if (!area.contains(loc.getX(), loc.getZ())) {
                    blocks.remove(i);
                    continue;
                }
                final Plot plot = area.getOwnedPlot(loc);
                if (!Objects.equals(plot, origin)) {
                    e.getBlocks().remove(i);
                }
            }
        }
        final Plot origin = area.getPlotAbs(loc);
        if (origin == null) {
            e.setCancelled(true);
            return;
        }
        for (int i = blocks.size() - 1; i >= 0; i--) {
            loc = BukkitUtil.getLocation(blocks.get(i).getLocation());
            final Plot plot = area.getOwnedPlot(loc);
            if (!Objects.equals(plot, origin)) {
                e.getBlocks().remove(i);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        PlotArea area = pp.getPlotAreaAbs();
        if (area == null) {
            return;
        }
        PlayerBlockEventType eventType = null;
        BukkitLazyBlock lb;
        Location loc;
        final Action action = event.getAction();
        switch (action) {
            case PHYSICAL: {
                eventType = PlayerBlockEventType.TRIGGER_PHYSICAL;
                Block block = event.getClickedBlock();
                lb = new BukkitLazyBlock(block);
                loc = BukkitUtil.getLocation(block.getLocation());
                break;
            }
            case RIGHT_CLICK_BLOCK: {
                Block block = event.getClickedBlock();
                loc = BukkitUtil.getLocation(block.getLocation());
                final Material blockType = block.getType();
                final int blockId = blockType.getId();
                switch (blockType) {
                    case ANVIL:
                    case ACACIA_DOOR:
                    case BIRCH_DOOR:
                    case DARK_OAK_DOOR:
                    case IRON_DOOR:
                    case JUNGLE_DOOR:
                    case SPRUCE_DOOR:
                    case TRAP_DOOR:
                    case IRON_TRAPDOOR:
                    case WOOD_DOOR:
                    case WOODEN_DOOR:
                    case TRAPPED_CHEST:
                    case ENDER_CHEST:
                    case CHEST:
                    case ACACIA_FENCE_GATE:
                    case BIRCH_FENCE_GATE:
                    case DARK_OAK_FENCE_GATE:
                    case FENCE_GATE:
                    case JUNGLE_FENCE_GATE:
                    case SPRUCE_FENCE_GATE:
                    case LEVER:
                    case DIODE:
                    case DIODE_BLOCK_OFF:
                    case DIODE_BLOCK_ON:
                    case COMMAND:
                    case REDSTONE_COMPARATOR:
                    case REDSTONE_COMPARATOR_OFF:
                    case REDSTONE_COMPARATOR_ON:
                    case REDSTONE_ORE:
                    case WOOD_BUTTON:
                    case STONE_BUTTON:
                    case BEACON:
                    case BED_BLOCK:
                    case SIGN:
                    case WALL_SIGN:
                    case SIGN_POST:
                    case ENCHANTMENT_TABLE:
                    case BREWING_STAND:
                    case STANDING_BANNER:
                    case BURNING_FURNACE:
                    case FURNACE:
                    case CAKE_BLOCK:
                    case DISPENSER:
                    case DROPPER:
                    case HOPPER:
                    case NOTE_BLOCK:
                    case JUKEBOX:
                    case WORKBENCH:
                        eventType = PlayerBlockEventType.INTERACT_BLOCK;
                        break;
                    case DRAGON_EGG:
                        eventType = PlayerBlockEventType.TELEPORT_OBJECT;
                        break;
                    default:
                        if (blockId > 197) {
                            eventType = PlayerBlockEventType.INTERACT_BLOCK;
                        }
                        break;
                }
                lb = new BukkitLazyBlock(blockId, block);
                final ItemStack hand = player.getItemInHand();
                if (eventType != null) {
                    break;
                }
                Material type = (hand == null) ? null : hand.getType();
                int id = (type == null) ? 0 : type.getId();
                if (id == 0) {
                    eventType = PlayerBlockEventType.INTERACT_BLOCK;
                    lb = new BukkitLazyBlock(0, block);
                    break;
                } else if (id < 198) {
                    loc = BukkitUtil.getLocation(block.getRelative(event.getBlockFace()).getLocation());
                    eventType = PlayerBlockEventType.PLACE_BLOCK;
                    lb = new BukkitLazyBlock(id, block);
                    break;
                }
                Material handType = hand.getType();
                lb = new BukkitLazyBlock(new PlotBlock((short) handType.getId(), (byte) 0));
                switch (handType) {
                    case MONSTER_EGG:
                    case MONSTER_EGGS:
                        eventType = PlayerBlockEventType.SPAWN_MOB;
                        break;

                    case ARMOR_STAND:
                        loc = BukkitUtil.getLocation(block.getRelative(event.getBlockFace()).getLocation());
                        eventType = PlayerBlockEventType.PLACE_MISC;
                        break;

                    case WRITTEN_BOOK:
                    case BOOK_AND_QUILL:
                    case BOOK:
                        eventType = PlayerBlockEventType.READ;
                        break;

                    case APPLE:
                    case BAKED_POTATO:
                    case MUSHROOM_SOUP:
                    case BREAD:
                    case CARROT:
                    case CARROT_ITEM:
                    case COOKIE:
                    case GRILLED_PORK:
                    case POISONOUS_POTATO:
                    case MUTTON:
                    case PORK:
                    case POTATO:
                    case POTATO_ITEM:
                    case POTION:
                    case PUMPKIN_PIE:
                    case RABBIT:
                    case RABBIT_FOOT:
                    case RABBIT_STEW:
                    case RAW_BEEF:
                    case RAW_FISH:
                    case RAW_CHICKEN:
                        eventType = PlayerBlockEventType.EAT;
                        break;
                    case MINECART:
                    case STORAGE_MINECART:
                    case POWERED_MINECART:
                    case HOPPER_MINECART:
                    case EXPLOSIVE_MINECART:
                    case COMMAND_MINECART:
                    case BOAT:
                        eventType = PlayerBlockEventType.PLACE_VEHICLE;
                        break;
                    case PAINTING:
                    case ITEM_FRAME:
                        loc = BukkitUtil.getLocation(block.getRelative(event.getBlockFace()).getLocation());
                        eventType = PlayerBlockEventType.PLACE_HANGING;
                        break;
                    default:
                        eventType = PlayerBlockEventType.INTERACT_BLOCK;
                        break;
                }
                break;
            }
            case LEFT_CLICK_BLOCK:
                Block block = event.getClickedBlock();
                loc = BukkitUtil.getLocation(block.getLocation());
                eventType = PlayerBlockEventType.BREAK_BLOCK;
                lb = new BukkitLazyBlock(block);
                break;
            default:
                return;
        }
        if (!EventUtil.manager.checkPlayerBlockEvent(pp, eventType, loc, lb, true)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void MobSpawn(final CreatureSpawnEvent event) {
        final Entity entity = event.getEntity();
        if (entity instanceof Player) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(entity.getLocation());
        final PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        final CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        switch (reason) {
            case SPAWNER_EGG:
            case DISPENSE_EGG:
                if (!area.SPAWN_EGGS) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case BREEDING:
                if (!area.SPAWN_BREEDING) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case CUSTOM:
                if (!area.SPAWN_CUSTOM && entity.getType().getTypeId() != 30) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case SPAWNER:
                if (!area.MOB_SPAWNER_SPAWNING) {
                    event.setCancelled(true);
                    return;
                }
                break;
        }
        final Plot plot = area.getOwnedPlotAbs(loc);
        if (plot == null) {
            if (!area.MOB_SPAWNING) {
                event.setCancelled(true);
            }
            return;
        }
        if (checkEntity(entity, plot)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityFall(final EntityChangeBlockEvent event) {
        if (event.getEntityType() != EntityType.FALLING_BLOCK) {
            return;
        }
        final Block block = event.getBlock();
        final World world = block.getWorld();
        final String worldname = world.getName();
        if (!PS.get().hasPlotArea(worldname)) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(block.getLocation());
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        final Plot plot = area.getOwnedPlotAbs(loc);
        if (plot == null) {
            event.setCancelled(true);
            return;
        }
        if (FlagManager.isPlotFlagTrue(plot, "disable-physics")) {
            event.setCancelled(true);
            return;
        }
        if (event.getTo().hasGravity()) {
            Entity entity = event.getEntity();
            List<MetadataValue> meta = entity.getMetadata("plot");
            if (meta.isEmpty()) {
                return;
            }
            Plot origin = (Plot) meta.get(0).value();
            if (origin != null && !origin.equals(plot)) {
                event.setCancelled(true);
                entity.remove();
            }
        }
        else if (event.getTo() == Material.AIR) {
            event.getEntity().setMetadata("plot", new FixedMetadataValue((Plugin) PS.get().IMP, plot));
        }
    }
    
    @EventHandler
    public void onPrime(final ExplosionPrimeEvent event) {
        lastRadius = event.getRadius() + 1;
    }

    public boolean checkEntity(Plot plot, String... flags) {
        int[] mobs = null;
        for (String flag : flags) {
            int i;
            switch (flag) {
                case "entity-cap":
                    i = 0;
                    break;
                case "mob-cap":
                    i = 3;
                    break;
                case "hostile-cap":
                    i = 2;
                    break;
                case "animal-cap":
                    i = 1;
                    break;
                case "vehicle-cap":
                    i = 4;
                    break;
                case "misc-cap":
                    i = 5;
                    break;
                default:
                    i = 0;
            }
            final Flag plotFlag = FlagManager.getPlotFlagRaw(plot, flag);
            if (plotFlag == null) {
                continue;
            }
            if (mobs == null) {
                mobs = plot.countEntities();
            }
            if (mobs[i] >= (Integer) plotFlag.getValue()) {
                return true;
            }
        }
        return false;
    }

    public boolean checkEntity(final Entity entity, final Plot plot) {
        if (plot == null || plot.owner == null || plot.getFlags().isEmpty() && plot.getArea().DEFAULT_FLAGS.isEmpty()) {
            return false;
        }
        switch (entity.getType()) {
            case PLAYER:
                return false;
            case SMALL_FIREBALL:
            case FIREBALL:
            case DROPPED_ITEM:
            case EGG:
            case THROWN_EXP_BOTTLE:
            case SPLASH_POTION:
            case SNOWBALL:
            case ENDER_PEARL:
            case ARROW:
            case TIPPED_ARROW:
            case SPECTRAL_ARROW:
            case SHULKER_BULLET:
            case DRAGON_FIREBALL:
                // projectile
            case PRIMED_TNT:
            case FALLING_BLOCK:
                // Block entities
            case ENDER_CRYSTAL:
            case COMPLEX_PART:
            case FISHING_HOOK:
            case ENDER_SIGNAL:
            case EXPERIENCE_ORB:
            case LEASH_HITCH:
            case FIREWORK:
            case WEATHER:
            case AREA_EFFECT_CLOUD:
            case LIGHTNING:
            case WITHER_SKULL:
            case UNKNOWN:
                // non moving / unremovable
                return checkEntity(plot, "entity-cap");
            case ITEM_FRAME:
            case PAINTING:
            case ARMOR_STAND:
                return checkEntity(plot, "entity-cap", "misc-cap");
            // misc
            case MINECART:
            case MINECART_CHEST:
            case MINECART_COMMAND:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
            case BOAT:
                return checkEntity(plot, "entity-cap", "vehicle-cap");
            case RABBIT:
            case SHEEP:
            case MUSHROOM_COW:
            case OCELOT:
            case PIG:
            case SQUID:
            case VILLAGER:
            case IRON_GOLEM:
            case WOLF:
            case CHICKEN:
            case COW:
            case SNOWMAN:
            case BAT:
                // animal
                return checkEntity(plot, "entity-cap", "mob-cap", "animal-cap");
            case BLAZE:
            case CAVE_SPIDER:
            case CREEPER:
            case ENDERMAN:
            case ENDERMITE:
            case ENDER_DRAGON:
            case GHAST:
            case GIANT:
            case GUARDIAN:
            case MAGMA_CUBE:
            case PIG_ZOMBIE:
            case SILVERFISH:
            case SKELETON:
            case SLIME:
            case SPIDER:
            case WITCH:
            case WITHER:
            case ZOMBIE:
            case SHULKER:
                // monster
                return checkEntity(plot, "entity-cap", "mob-cap", "hostile-cap");
            case HORSE:
                Horse horse = ((Horse) entity);
                if (horse.getVariant().equals(Horse.Variant.SKELETON_HORSE) && entity.getPassenger() instanceof Skeleton) {
                    return checkEntity(plot, "entity-cap", "mob-cap", "hostile-cap");
                }
                return checkEntity(plot, "entity-cap", "mob-cap", "animal-cap");
            default:
                if (entity instanceof LivingEntity) {
                    if (entity instanceof Animals) {
                        return checkEntity(plot, "entity-cap", "mob-cap", "animal-cap");
                    } else if (entity instanceof Monster) {
                        return checkEntity(plot, "entity-cap", "mob-cap", "hostile-cap");
                    } else {
                        return checkEntity(plot, "entity-cap", "mob-cap");
                    }
                } else if (entity instanceof Vehicle) {
                    return checkEntity(plot, "entity-cap", "vehicle-cap");
                } else if (entity instanceof Hanging) {
                    return checkEntity(plot, "entity-cap", "misc-cap");
                }
                return checkEntity(plot, "entity-cap");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(final BlockIgniteEvent e) {
        final Player player = e.getPlayer();
        final Block b = e.getBlock();
        final Location loc;
        if (b != null) {
            loc = BukkitUtil.getLocation(b.getLocation());
        } else {
            final Entity ent = e.getIgnitingEntity();
            if (ent != null) {
                loc = BukkitUtil.getLocation(ent);
            } else if (player != null) {
                loc = BukkitUtil.getLocation(player);
            } else {
                return;
            }
        }
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        if (e.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            e.setCancelled(true);
            return;
        }
        if (player == null) {
            e.setCancelled(true);
            return;
        }
        final Player p = e.getPlayer();
        final Plot plot = area.getOwnedPlotAbs(loc);
        if (plot == null) {
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD)) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_ROAD);
                e.setCancelled(true);
            }
        } else {
            if (!plot.hasOwner()) {
                final PlotPlayer pp = BukkitUtil.getPlayer(p);
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_UNOWNED);
                    e.setCancelled(true);
                }
            } else {
                final PlotPlayer pp = BukkitUtil.getPlayer(p);
                if (!plot.isAdded(pp.getUUID())) {
                    if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(final PlayerTeleportEvent event) {
        if (event.getTo() == null || event.getFrom() == null) {
            BukkitUtil.getPlayer(event.getPlayer()).deleteMeta("location");
            BukkitUtil.getPlayer(event.getPlayer()).deleteMeta("lastplot");
            return;
        }
        final org.bukkit.Location from = event.getFrom();
        final org.bukkit.Location to = event.getTo();
        int x2;
        if (MathMan.roundInt(from.getX()) != (x2 = MathMan.roundInt(to.getX()))) {
            final Player player = event.getPlayer();
            final PlotPlayer pp = BukkitUtil.getPlayer(player);
            Location loc = BukkitUtil.getLocation(to);
            pp.setMeta("location", loc);
            PlotArea area = loc.getPlotArea();
            if (area == null) {
                return;
            }
            Plot now = area.getPlotAbs(loc);
            final Plot lastPlot = pp.getMeta("lastplot");
            if (now == null) {
                if (lastPlot != null && !plotExit(pp, lastPlot) && tmp_teleport) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
                    if (lastPlot.equals(area.getPlot(BukkitUtil.getLocation(from)))) {
                        tmp_teleport = false;
                        player.teleport(from);
                        tmp_teleport = true;
                    } else {
                        Location spawn = BukkitUtil.getLocation(player.getWorld().getSpawnLocation());
                        if (spawn.getEuclideanDistanceSquared(pp.getLocation()) > 2) {
                            tmp_teleport = false;
                            player.teleport(player.getWorld().getSpawnLocation());
                            tmp_teleport = true;
                        }
                    }
                    event.setCancelled(true);
                    return;
                }
            } else if (lastPlot != null && now.equals(lastPlot)) {
                return;
            } else if (!plotEntry(pp, now) && tmp_teleport) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                if (!now.equals(area.getPlot(BukkitUtil.getLocation(from)))) {
                    tmp_teleport = false;
                    player.teleport(from);
                    tmp_teleport = true;
                } else {
                    Location spawn = BukkitUtil.getLocation(player.getWorld().getSpawnLocation());
                    if (spawn.getEuclideanDistanceSquared(pp.getLocation()) > 2) {
                        tmp_teleport = false;
                        player.teleport(player.getWorld().getSpawnLocation());
                        tmp_teleport = true;
                    }
                }
                event.setCancelled(true);
                return;
            }
            final Integer border = area.getBorder();
            if (tmp_teleport) {
                if (x2 > border) {
                    to.setX(border - 4);
                    tmp_teleport = false;
                    player.teleport(event.getTo());
                    tmp_teleport = true;
                    MainUtil.sendMessage(pp, C.BORDER);
                    return;
                } else if (x2 < -border) {
                    to.setX(-border + 4);
                    tmp_teleport = false;
                    player.teleport(event.getTo());
                    tmp_teleport = true;
                    MainUtil.sendMessage(pp, C.BORDER);
                    return;
                }
            }
            return;
        }
        int z2;
        if (MathMan.roundInt(from.getZ()) != (z2 = MathMan.roundInt(to.getZ()))) {
            final Player player = event.getPlayer();
            final PlotPlayer pp = BukkitUtil.getPlayer(player);
            // Set last location
            Location loc = BukkitUtil.getLocation(to);
            pp.setMeta("location", loc);
            final PlotArea area = loc.getPlotArea();
            if (area == null) {
                return;
            }
            Plot now = area.getPlotAbs(loc);
            final Plot lastPlot = pp.getMeta("lastplot");
            if (now == null) {
                if (lastPlot != null && !plotExit(pp, lastPlot) && tmp_teleport) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
                    if (lastPlot.equals(area.getPlot(BukkitUtil.getLocation(from)))) {
                        tmp_teleport = false;
                        player.teleport(from);
                        tmp_teleport = true;
                    } else {
                        Location spawn = BukkitUtil.getLocation(player.getWorld().getSpawnLocation());
                        if (spawn.getEuclideanDistanceSquared(pp.getLocation()) > 2) {
                            tmp_teleport = false;
                            player.teleport(player.getWorld().getSpawnLocation());
                            tmp_teleport = true;
                        }
                    }
                    event.setCancelled(true);
                    return;
                }
            } else if (lastPlot != null && now.equals(lastPlot)) {
                return;
            } else if (!plotEntry(pp, now) && tmp_teleport) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                if (!now.equals(area.getPlot(BukkitUtil.getLocation(from)))) {
                    tmp_teleport = false;
                    player.teleport(from);
                    tmp_teleport = true;
                } else {
                    Location spawn = BukkitUtil.getLocation(player.getWorld().getSpawnLocation());
                    if (spawn.getEuclideanDistanceSquared(pp.getLocation()) > 2) {
                        tmp_teleport = false;
                        player.teleport(player.getWorld().getSpawnLocation());
                        tmp_teleport = true;
                    }
                }
                event.setCancelled(true);
                return;
            }
            final Integer border = area.getBorder();
            if (tmp_teleport) {
                if (z2 > border) {
                    to.setZ(border - 4);
                    tmp_teleport = false;
                    player.teleport(event.getTo());
                    tmp_teleport = true;
                    MainUtil.sendMessage(pp, C.BORDER);
                } else if (z2 < -border) {
                    to.setZ(-border + 4);
                    tmp_teleport = false;
                    player.teleport(event.getTo());
                    tmp_teleport = true;
                    MainUtil.sendMessage(pp, C.BORDER);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(final PlayerBucketEmptyEvent e) {
        final BlockFace bf = e.getBlockFace();
        final Block b = e.getBlockClicked().getLocation().add(bf.getModX(), bf.getModY(), bf.getModZ()).getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        final PlotPlayer pp = BukkitUtil.getPlayer(e.getPlayer());
        final Plot plot = area.getPlotAbs(loc);
        if (plot == null) {
            if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD)) {
                return;
            }
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_ROAD);
            e.setCancelled(true);
        } else if (!plot.hasOwner()) {
            if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                return;
            }
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_UNOWNED);
            e.setCancelled(true);
        } else if (!plot.isAdded(pp.getUUID())) {
            final Flag use = FlagManager.getPlotFlagRaw(plot, C.FLAG_USE.s());
            if (use != null && ((HashSet<PlotBlock>) use.getValue()).contains(new PlotBlock((short) e.getBucket().getId(), (byte) 0))) {
                return;
            }
            if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                return;
            }
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(final InventoryClickEvent event) {
        final HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player)) {
            return;
        }
        final Player player = (Player) clicker;
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        final PlotInventory inv = pp.getMeta("inventory");
        if (inv != null && event.getRawSlot() == event.getSlot()) {
            if (!inv.onClick(event.getSlot())) {
                event.setCancelled(true);
                inv.close();
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(final InventoryCloseEvent event) {
        final HumanEntity closer = event.getPlayer();
        if (!(closer instanceof Player)) {
            return;
        }
        final Player player = (Player) closer;
        BukkitUtil.getPlayer(player).deleteMeta("inventory");
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(final PlayerQuitEvent event) {
        final PlotPlayer pp = BukkitUtil.getPlayer(event.getPlayer());
        pp.unregister();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketFill(final PlayerBucketFillEvent e) {
        final Block b = e.getBlockClicked();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        final Player p = e.getPlayer();
        final PlotPlayer pp = BukkitUtil.getPlayer(p);
        final Plot plot = area.getPlotAbs(loc);
        if (plot == null) {
            if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD)) {
                return;
            }
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_ROAD);
            e.setCancelled(true);
        } else {
            if (!plot.hasOwner()) {
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_UNOWNED);
                e.setCancelled(true);
            } else if (!plot.isAdded(pp.getUUID())) {
                final Flag use = FlagManager.getPlotFlagRaw(plot, C.FLAG_USE.s());
                final Block block = e.getBlockClicked();
                if (use != null && ((HashSet<PlotBlock>) use.getValue()).contains(new PlotBlock((short) block.getTypeId(), block.getData()))) {
                    return;
                }
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleCreate(final VehicleCreateEvent event) {
        final Vehicle entity = event.getVehicle();
        final Location loc = BukkitUtil.getLocation(entity);
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlotAbs(loc);
        if (plot == null) {
            entity.remove();
            return;
        }
        if (checkEntity(entity, plot)) {
            entity.remove();
            return;
        }
        if (Settings.KILL_ROAD_VEHICLES) {
            entity.setMetadata("plot", new FixedMetadataValue((Plugin) PS.get().IMP, plot));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingPlace(final HangingPlaceEvent e) {
        final Block b = e.getBlock().getRelative(e.getBlockFace());
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        final Player p = e.getPlayer();
        final PlotPlayer pp = BukkitUtil.getPlayer(p);
        final Plot plot = area.getPlotAbs(loc);
        if (plot == null) {
            if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD)) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_ROAD);
                e.setCancelled(true);
            }
        } else {
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_UNOWNED);
                    e.setCancelled(true);
                }
                return;
            } else if (!plot.isAdded(pp.getUUID())) {
                if (!FlagManager.isPlotFlagTrue(plot, C.FLAG_HANGING_PLACE.s())) {
                    if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                        e.setCancelled(true);
                    }
                    return;
                }
            }
            if (checkEntity(e.getEntity(), plot)) {
                e.setCancelled(true);
            }

        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreakByEntity(final HangingBreakByEntityEvent e) {
        final Entity r = e.getRemover();
        if (r instanceof Player) {
            final Player p = (Player) r;
            final Location l = BukkitUtil.getLocation(e.getEntity());
            PlotArea area = l.getPlotArea();
            if (area == null) {
                return;
            }
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            final Plot plot = area.getPlotAbs(l);
            if (plot == null) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_ROAD)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_ROAD);
                    e.setCancelled(true);
                }
            } else {
                if (!plot.hasOwner()) {
                    if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_UNOWNED);
                        e.setCancelled(true);
                    }
                } else if (!plot.isAdded(pp.getUUID())) {
                    if (FlagManager.isPlotFlagTrue(plot, C.FLAG_HANGING_BREAK.s())) {
                        return;
                    }
                    if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_OTHER)) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_OTHER);
                        e.setCancelled(true);
                    }
                }
            }
        } else if (r instanceof Projectile) {
            final Projectile p = (Projectile) r;
            if (p.getShooter() instanceof Player) {
                final Player shooter = (Player) p.getShooter();
                Location loc = BukkitUtil.getLocation(e.getEntity());
                PlotArea area = loc.getPlotArea();
                if (area == null) {
                    return;
                }
                final PlotPlayer player = BukkitUtil.getPlayer(shooter);
                final Plot plot = area.getPlotAbs(BukkitUtil.getLocation(e.getEntity()));
                if (plot != null) {
                    if (!plot.hasOwner()) {
                        if (!Permissions.hasPermission(player, C.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                            MainUtil.sendMessage(player, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_UNOWNED);
                            e.setCancelled(true);
                        }
                    } else if (!plot.isAdded(player.getUUID())) {
                        if (!FlagManager.isPlotFlagTrue(plot, C.FLAG_HANGING_BREAK.s())) {
                            if (!Permissions.hasPermission(player, C.PERMISSION_ADMIN_DESTROY_OTHER)) {
                                MainUtil.sendMessage(player, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_OTHER);
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        } else {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent e) {
        final Location l = BukkitUtil.getLocation(e.getRightClicked().getLocation());
        PlotArea area = l.getPlotArea();
        if (area == null) {
            return;
        }
        final Player p = e.getPlayer();
        final PlotPlayer pp = BukkitUtil.getPlayer(p);
        final Plot plot = area.getPlotAbs(l);
        if (plot == null) {
            if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD)) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_INTERACT_ROAD);
                e.setCancelled(true);
            }
        } else {
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_INTERACT_UNOWNED);
                    e.setCancelled(true);
                }
            } else if (!plot.isAdded(pp.getUUID())) {
                final Entity entity = e.getRightClicked();
                if (entity instanceof Monster && FlagManager.isPlotFlagTrue(plot, C.FLAG_HOSTILE_INTERACT.s())) {
                    return;
                }
                if (entity instanceof Animals && FlagManager.isPlotFlagTrue(plot, C.FLAG_ANIMAL_INTERACT.s())) {
                    return;
                }
                if (entity instanceof Tameable && ((Tameable) entity).isTamed() && FlagManager.isPlotFlagTrue(plot, C.FLAG_TAMED_INTERACT.s())) {
                    return;
                }
                if (entity instanceof Vehicle && FlagManager.isPlotFlagTrue(plot, C.FLAG_VEHICLE_USE.s())) {
                    return;
                }
                if (entity instanceof Player && FlagManager.isPlotFlagTrue(plot, C.FLAG_PLAYER_INTERACT.s())) {
                    return;
                }
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_INTERACT_OTHER);
                    e.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleDestroy(final VehicleDestroyEvent e) {
        final Location l = BukkitUtil.getLocation(e.getVehicle());
        PlotArea area = l.getPlotArea();
        if (area == null) {
            return;
        }
        final Entity d = e.getAttacker();
        if (d instanceof Player) {
            final Player p = (Player) d;
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            final Plot plot = area.getPlotAbs(l);
            if (plot == null) {
                if (!Permissions.hasPermission(pp, "plots.admin.vehicle.break.road")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.vehicle.break.road");
                    e.setCancelled(true);
                }
            } else {
                if (!plot.hasOwner()) {
                    if (!Permissions.hasPermission(pp, "plots.admin.vehicle.break.unowned")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.vehicle.break.unowned");
                        e.setCancelled(true);
                        return;
                    }
                    return;
                }
                if (!plot.isAdded(pp.getUUID())) {
                    if (FlagManager.isPlotFlagTrue(plot, "vehicle-break")) {
                        return;
                    }
                    if (!Permissions.hasPermission(pp, "plots.admin.vehicle.break.other")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.vehicle.break.other");
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(final PotionSplashEvent event) {
        final ThrownPotion damager = event.getPotion();
        final Location l = BukkitUtil.getLocation(damager);
        if (!PS.get().hasPlotArea(l.getWorld())) {
            return;
        }
        for (final LivingEntity victim : event.getAffectedEntities()) {
            if (!entityDamage(l, damager, victim)) {
                event.setIntensity(victim, 0);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(final EntityDamageByEntityEvent e) {
        final Entity damager = e.getDamager();
        final Location l = BukkitUtil.getLocation(damager);
        if (!PS.get().hasPlotArea(l.getWorld())) {
            return;
        }
        final Entity victim = e.getEntity();
        if (!entityDamage(l, damager, victim)) {
            e.setCancelled(true);
        }
    }
    
    public boolean entityDamage(final Location l, final Entity damager, final Entity victim) {
        final Location dloc = BukkitUtil.getLocation(damager);
        final Location vloc = BukkitUtil.getLocation(victim);
        PlotArea dArea = dloc.getPlotArea();
        PlotArea vArea = dArea != null && dArea.contains(vloc.getX(), vloc.getZ()) ? dArea : vloc.getPlotArea();
        if (dArea == null && vArea == null) {
            return true;
        }

        final Plot dplot = dArea != null ? dArea.getPlot(dloc) : null;
        final Plot vplot = vArea != null ? vArea.getPlot(vloc) : null;
        
        Plot plot;
        String stub;
        if (dplot == null && vplot == null) {
            if (dArea == null) {
                return true;
            }
            plot = null;
            stub = "road";
        } else {
            // Priorize plots for close to seamless pvp zones
            plot = vplot == null ? dplot : dplot == null || !(victim instanceof Player) ? vplot :
                    victim.getTicksLived() > damager.getTicksLived() ? dplot : vplot;
            stub = plot.hasOwner() ? "other" : "unowned";
        }
        
        Player player;
        if (damager instanceof Player) { // attacker is player
            player = (Player) damager;
        } else if (damager instanceof Projectile) {
            final Projectile projectile = (Projectile) damager;
            final ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player) { // shooter is player
                player = (Player) shooter;
            } else { // shooter is not player
                player = null;
            }
        } else { // Attacker is not player
            player = null;
        }
        if (player != null) {
            final PlotPlayer pp = BukkitUtil.getPlayer(player);
            if (victim instanceof Hanging) { // hanging
                if (plot != null && (FlagManager.isPlotFlagTrue(plot, "hanging-break") || plot.isAdded(pp.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.destroy." + stub)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.destroy." + stub);
                    return false;
                }
            } else if (victim.getEntityId() == 30) {
                if (plot != null && (FlagManager.isPlotFlagTrue(plot, "misc-break") || plot.isAdded(pp.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.destroy." + stub)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.destroy." + stub);
                    return false;
                }
            } else if (victim instanceof Monster || victim instanceof EnderDragon) { // victim is monster
                if (plot != null && (FlagManager.isPlotFlagTrue(plot, "hostile-attack") || FlagManager.isPlotFlagTrue(plot, "pve") || plot
                        .isAdded(pp.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.pve." + stub)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.pve." + stub);
                    return false;
                }
            } else if (victim instanceof Tameable) { // victim is tameable
                if (plot != null && (FlagManager.isPlotFlagTrue(plot, "tamed-attack") || FlagManager.isPlotFlagTrue(plot, "pve") || plot
                        .isAdded(pp.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.pve." + stub)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.pve." + stub);
                    return false;
                }
            } else if (victim instanceof Player) {
                if (plot != null) {
                    final Flag pvp = FlagManager.getPlotFlagRaw(plot, C.FLAG_PVP.s());
                    if (pvp == null) {
                        return true;
                    } else {
                        if ((Boolean) pvp.getValue()) {
                            return true;
                        } else if (!Permissions.hasPermission(pp, "plots.admin.pve." + stub)) {
                            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.pve." + stub);
                            return false;
                        }
                    }
                }
                if (!Permissions.hasPermission(pp, "plots.admin.pvp." + stub)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.pvp." + stub);
                    return false;
                }
            } else if (victim instanceof Creature) { // victim is animal
                if (plot != null && (FlagManager.isPlotFlagTrue(plot, "animal-attack") || FlagManager.isPlotFlagTrue(plot, "pve") || plot
                        .isAdded(pp.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.pve." + stub)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.pve." + stub);
                    return false;
                }
            } else if (victim instanceof Vehicle) { // Vehicles are managed in vehicle destroy event
                return true;
            } else { // victim is something else
                if (plot != null && (FlagManager.isPlotFlagTrue(plot, "pve") || plot.isAdded(pp.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.pve." + stub)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.pve." + stub);
                    return false;
                }
            }
            return true;
        }
        // player is null
        return !(damager instanceof Arrow && !(victim instanceof Creature));
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerEggThrow(final PlayerEggThrowEvent e) {
        final Location l = BukkitUtil.getLocation(e.getEgg().getLocation());
        PlotArea area = l.getPlotArea();
        if (area == null) {
            return;
        }
        final Player p = e.getPlayer();
        final PlotPlayer pp = BukkitUtil.getPlayer(p);
        final Plot plot = area.getPlot(l);
        if (plot == null) {
            if (!Permissions.hasPermission(pp, "plots.admin.projectile.road")) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.projectile.road");
                e.setHatching(false);
            }
        } else {
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, "plots.admin.projectile.unowned")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.projectile.unowned");
                    e.setHatching(false);
                }
            } else if (!plot.isAdded(pp.getUUID())) {
                if (!Permissions.hasPermission(pp, "plots.admin.projectile.other")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.projectile.other");
                    e.setHatching(false);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BlockCreate(final BlockPlaceEvent event) {
        final Location loc = BukkitUtil.getLocation(event.getBlock().getLocation());
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        final Player player = event.getPlayer();
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        final Plot plot = area.getPlotAbs(loc);
        if (plot != null) {
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_UNOWNED);
                    event.setCancelled(true);
                    return;
                }
            } else if (!plot.isAdded(pp.getUUID())) {
                final Flag place = FlagManager.getPlotFlagRaw(plot, C.FLAG_PLACE.s());
                final Block block = event.getBlock();
                if ((place == null || !((HashSet<PlotBlock>) place.getValue()).contains(new PlotBlock((short) block.getTypeId(), block.getData())))
                && !Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                    event.setCancelled(true);
                    return;
                }
            } else if (Settings.DONE_RESTRICTS_BUILDING && plot.getFlags().containsKey("done")) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                    event.setCancelled(true);
                    return;
                }
            }
            if (FlagManager.isPlotFlagTrue(plot, C.FLAG_DISABLE_PHYSICS.s())) {
                final Block block = event.getBlockPlaced();
                if (block.getType().hasGravity()) {
                    sendBlockChange(block.getLocation(), block.getType(), block.getData());
                }
            }
            if (loc.getY() > area.MAX_BUILD_HEIGHT && loc.getY() < area.MIN_BUILD_HEIGHT && !Permissions
                    .hasPermission(pp, C.PERMISSION_ADMIN_BUILD_HEIGHTLIMIT)) {
                event.setCancelled(true);
                MainUtil.sendMessage(pp, C.HEIGHT_LIMIT.s().replace("{limit}", "" + area.MAX_BUILD_HEIGHT));
            }
        } else if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_ROAD);
            event.setCancelled(true);
        }
    }
}
