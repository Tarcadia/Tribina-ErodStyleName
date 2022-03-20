package net.tarcadia.tribina.erod.stylename.util.wrap;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import net.tarcadia.tribina.erod.stylename.StyleName;
import net.tarcadia.tribina.erod.stylename.util.type.Pair;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerPacketWrap {

    private static final Map<Integer, Player> eidPlayer = new HashMap<>();
    private static final Map<Integer, Player> eidVehiclePlayer = new HashMap<>();
    private static final Map<String, Integer> followerEID = new HashMap<>();
    private static final Map<String, UUID> followerUUID = new HashMap<>();

    private static final Set<Pair<Player, Player>> followerViewed = new HashSet<>();
    private static final Set<Pair<Player, Player>> playerInView = new HashSet<>();
    private static final Set<Player> playerCanView = new HashSet<>();

    private PlayerPacketWrap() {}

    public static int getFollowerEID(@NotNull Player player) {
        if (!followerEID.containsKey(player.getName()) || followerEID.get(player.getName()) == null) {
            var eid = new Random().nextInt(Integer.MAX_VALUE);
            followerEID.put(player.getName(), eid);
            return eid;
        } else {
            return followerEID.get(player.getName());
        }
    }

    @NotNull
    public static UUID getFollowerUUID(@NotNull Player player) {
        if (!followerUUID.containsKey(player.getName()) || followerUUID.get(player.getName()) == null) {
            var uuid = UUID.randomUUID();
            followerUUID.put(player.getName(), uuid);
            return uuid;
        } else {
            return followerUUID.get(player.getName());
        }
    }

    public static void setEIDPlayer(@NotNull Player player) {
        eidPlayer.put(player.getEntityId(), player);
    }

    public static void removeEIDPlayer(@NotNull Player player) {
        eidPlayer.remove(player.getEntityId());
    }

    public static void setEIDVehiclePlayer(@NotNull Vehicle vehicle, @NotNull Player player) {
        eidPlayer.put(vehicle.getEntityId(), player);
    }

    public static void removeEIDVehiclePlayer(@NotNull Vehicle vehicle, @NotNull Player player) {
        eidPlayer.remove(vehicle.getEntityId());
    }

    @Nullable
    public static Player getEIDPlayer(int eid) {
        Player player;
        if (eidVehiclePlayer.containsKey(eid) && (player = eidVehiclePlayer.get(eid)) != null) {
            return player;
        } else if (eidPlayer.containsKey(eid) && (player = eidPlayer.get(eid)) != null) {
            return player;
        } else {
            return null;
        }
    }

    public static void setPlayerInView(@NotNull Player viewer, @NotNull Player player) {
        var view = new Pair<>(viewer, player);
        playerInView.add(view);
        if (playerCanView.contains(viewer) && !followerViewed.contains(view)) showPlayerFollower(viewer, player);
    }

    public static void removePlayerInView(@NotNull Player viewer, @NotNull Player player) {
        var view = new Pair<>(viewer, player);
        playerInView.remove(view);
        if (followerViewed.contains(view)) hidePlayerFollower(viewer, player);
    }

    public static boolean getPlayerInView(@NotNull Player viewer, @NotNull Player player) {
        var view = new Pair<>(viewer, player);
        return playerInView.contains(view);
    }

    public static boolean getPlayerInView(@NotNull Pair<Player, Player> view) {
        return playerInView.contains(view);
    }

    public static void setPlayerCanView(@NotNull Player viewer) {
        playerCanView.add(viewer);
        for (var view : playerInView) if (view.x().equals(viewer)) {
            Player player;
            if (!followerViewed.contains(view) && (player = view.y()) != null) {
                showPlayerFollower(viewer, player);
            }
        }
    }

    public static void removePlayerCanView(@NotNull Player viewer) {
        playerCanView.remove(viewer);
        for (var view : playerInView) if (view.x().equals(viewer)) {
            Player player;
            if (followerViewed.contains(view) && (player = view.y()) != null) {
                hidePlayerFollower(viewer, player);
            }
        }
    }

    public static boolean getPlayerCanView(@NotNull Player viewer) {
        return playerCanView.contains(viewer);
    }

    public static void showPlayerFollower(@NotNull Player viewer, @NotNull Player player) {
        var pm = ProtocolLibrary.getProtocolManager();
        var view = new Pair<>(viewer, player);
        if (getPlayerInView(view) && getPlayerCanView(viewer) && !followerViewed.contains(view)) {
            try {
                var packetSpawn = wrapFollowerSpawn(player);
                var packetMeta = wrapFollowerMeta(player);
                pm.sendServerPacket(viewer, packetSpawn);
                pm.sendServerPacket(viewer, packetMeta);
                followerViewed.add(view);
            } catch (Exception e) {
                StyleName.logger.warning("Unable to show player " + player.getName() + "'s follower for " + viewer.getName() + ".");
            }
        }
    }

    public static void hidePlayerFollower(@NotNull Player viewer, @NotNull Player player) {
        var pm = ProtocolLibrary.getProtocolManager();
        var view = new Pair<>(viewer, player);
        if (getPlayerInView(view) && getPlayerCanView(viewer) && !followerViewed.contains(view)) {
            try {
                var packetDestroy = wrapFollowerDestroy(player);
                pm.sendServerPacket(viewer, packetDestroy);
                followerViewed.remove(view);
            } catch (Exception e) {
                StyleName.logger.warning("Unable to hide player " + player.getName() + "'s follower for " + viewer.getName() + ".");
            }
        }
    }

    public static void metaPlayerFollower(@NotNull Player viewer, @NotNull Player player) {
        var pm = ProtocolLibrary.getProtocolManager();
        var view = new Pair<>(viewer, player);
        if (followerViewed.contains(view)) {
            try {
                var packetMeta = wrapFollowerMeta(player);
                pm.sendServerPacket(viewer, packetMeta);
            } catch (Exception e) {
                StyleName.logger.warning("Unable to send player " + player.getName() + "'s follower's metadata for " + viewer.getName() + ".");
            }
        }
    }

    public static void movePlayerFollower(@NotNull Player viewer, @NotNull Player player) {
        var pm = ProtocolLibrary.getProtocolManager();
        var view = new Pair<>(viewer, player);
        if (followerViewed.contains(view)) {
            try {
                var packetMove = wrapFollowerMove(player);
                pm.sendServerPacket(viewer, packetMove);
            } catch (Exception e) {
                StyleName.logger.warning("Unable to send player " + player.getName() + "'s follower's move for " + viewer.getName() + ".");
            }
        }
    }

    public static void showOneFollower(@NotNull Player player) {
        var sn = StyleName.plugin;
        var pm = ProtocolLibrary.getProtocolManager();
        for (var p : player.getWorld().getPlayers()) if (p != player && !p.getGameMode().equals(GameMode.SPECTATOR)) {
            try {
                var packetSpawn = wrapFollowerSpawn(player);
                var packetMeta = wrapFollowerMeta(player);
                pm.sendServerPacket(p, packetSpawn);
                pm.sendServerPacket(p, packetMeta);
            } catch (Exception e) {
                StyleName.logger.warning("Unable to wrap the player follower spawn packet.");
            }
        }
    }

    public static void moveOneFollower(@NotNull Player player) {
        var sn = StyleName.plugin;
        var pm = ProtocolLibrary.getProtocolManager();
        for (var p : player.getWorld().getPlayers()) if (p != player && !p.getGameMode().equals(GameMode.SPECTATOR)) {
            try {
                var packetMove = wrapFollowerMove(player);
                var packetMeta = wrapFollowerMeta(player);
                pm.sendServerPacket(p, packetMove);
                pm.sendServerPacket(p, packetMeta);
            } catch (Exception e) {
                StyleName.logger.warning("Unable to wrap the player follower move and meta packet.");
            }
        }
    }

    public static void hideOneFollower(@NotNull Player player) {
        var sn = StyleName.plugin;
        var pm = ProtocolLibrary.getProtocolManager();
        for (var p : player.getWorld().getPlayers()) if (p != player) {
            try {
                var packetDestroy = wrapFollowerDestroy(player);
                pm.sendServerPacket(p, packetDestroy);
            } catch (Exception e) {
                StyleName.logger.warning("Unable to wrap the player follower destroy packet.");
            }
        }
    }

    public static void hideAllFollower(@NotNull Player player) {
        var sn = StyleName.plugin;
        var pm = ProtocolLibrary.getProtocolManager();
        for (var p : player.getWorld().getPlayers()) if (p != player) {
            try {
                var packetDestroy = wrapFollowerDestroy(p);
                pm.sendServerPacket(player, packetDestroy);
            } catch (Exception e) {
                StyleName.logger.warning("Unable to wrap the player follower destroy packet.");
            }
        }
    }

    public static void showAllFollower(@NotNull Player player) {
        var sn = StyleName.plugin;
        var pm = ProtocolLibrary.getProtocolManager();
        for (var p : player.getWorld().getPlayers()) if (p != player) {
            try {
                var packetSpawn = wrapFollowerSpawn(p);
                var packetMeta = wrapFollowerMeta(p);
                pm.sendServerPacket(player, packetSpawn);
                pm.sendServerPacket(player, packetMeta);
            } catch (Exception e) {
                StyleName.logger.warning("Unable to wrap the player follower spawn packet.");
            }
        }
    }

    @NotNull
    public static PacketContainer wrapFollowerSpawn(@NotNull Player player) {
        var sn = StyleName.plugin;
        var pm = ProtocolLibrary.getProtocolManager();
        var eid = getFollowerEID(player);
        var uuid = getFollowerUUID(player);
        var packetSpawn = pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        double offset = 2.1;
        if (!sn.getPlayerRawNameVisibility(player)) offset -= 0.3;
        if (player.isSneaking()) offset -= 0.4;
        else if (player.isGliding() || player.isSwimming()) offset -= 1.2;
        else if (player.isSleeping()) offset -= 1.6;

        packetSpawn.getModifier().writeDefaults();
        packetSpawn.getIntegers().write(0, eid);
        packetSpawn.getUUIDs().write(0, uuid);
        packetSpawn.getIntegers().write(1, 1);
        packetSpawn.getDoubles().write(0, player.getLocation().getX());
        packetSpawn.getDoubles().write(1, player.getLocation().getY() + offset);
        packetSpawn.getDoubles().write(2, player.getLocation().getZ());

        return packetSpawn;
    }

    @NotNull
    public static PacketContainer wrapFollowerMeta(@NotNull Player player) {
        var sn = StyleName.plugin;
        var pm = ProtocolLibrary.getProtocolManager();
        var metadata = new WrappedDataWatcher();
        var displayName = sn.getPlayerDisplayFullName(player);
        var displayNameVisible = !player.isSneaking() && sn.isFunctionEnabled();
        var optDisplayNameObject = Optional.of(WrappedChatComponent.fromText(displayName).getHandle());
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20); // invisible
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), optDisplayNameObject);
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), displayNameVisible);
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(4, WrappedDataWatcher.Registry.get(Boolean.class)), true);
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true);
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) (0x01 | 0x08 | 0x10)); //isSmall, noBasePlate, set Marker

        var eid = getFollowerEID(player);
        var uuid = getFollowerUUID(player);
        var packetMeta = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packetMeta.getModifier().writeDefaults();
        packetMeta.getIntegers().write(0, eid);
        packetMeta.getWatchableCollectionModifier().write(0, metadata.getWatchableObjects());

        return packetMeta;
    }

    @NotNull
    public static PacketContainer wrapFollowerDestroy(@NotNull Player player) {
        var sn = StyleName.plugin;
        var pm = ProtocolLibrary.getProtocolManager();
        var eid = getFollowerEID(player);
        var uuid = getFollowerUUID(player);

        var packetDestroy = pm.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        packetDestroy.getModifier().writeDefaults();
        packetDestroy.getIntLists().write(0, List.of(eid));

        return packetDestroy;
    }

    @NotNull
    public static PacketContainer wrapFollowerMove(@NotNull Player player) {
        var sn = StyleName.plugin;
        var pm = ProtocolLibrary.getProtocolManager();
        var eid = getFollowerEID(player);
        var uuid = getFollowerUUID(player);
        double offset = 2.1;
        if (!sn.getPlayerRawNameVisibility(player)) offset -= 0.3;
        if (player.isSneaking()) offset -= 0.4;
        else if (player.isGliding() || player.isSwimming()) offset -= 1.2;
        else if (player.isSleeping()) offset -= 1.6;

        var packetMove = pm.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packetMove.getModifier().writeDefaults();
        packetMove.getIntegers().write(0, eid);
        packetMove.getDoubles().write(0, player.getLocation().getX());
        packetMove.getDoubles().write(1, player.getLocation().getY() + offset);
        packetMove.getDoubles().write(2, player.getLocation().getZ());

        return packetMove;
    }

    public static final class InfoPacketAdapter extends PacketAdapter {

        private final StyleName sn;

        public InfoPacketAdapter() {
            super(
                    StyleName.plugin,
                    ListenerPriority.NORMAL,
                    PacketType.Play.Server.PLAYER_INFO
            );
            sn = StyleName.plugin;
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            PacketContainer packet;
            if (
                    this.sn.isFunctionEnabled() &&
                            ((packet = event.getPacket()) != null) &&
                            (packet.getType().equals(PacketType.Play.Server.PLAYER_INFO)) &&
                            (packet.getPlayerInfoAction().read(0).equals(EnumWrappers.PlayerInfoAction.ADD_PLAYER))
            ) {
                var nPIDList = new LinkedList<PlayerInfoData>();
                for (var rPID : packet.getPlayerInfoDataLists().read(0)) {
                    Player player;
                    WrappedGameProfile profile;
                    if (
                            (rPID != null) &&
                                    ((profile = rPID.getProfile()) != null) &&
                                    ((player = this.sn.getServer().getPlayer(profile.getUUID())) != null) &&
                                    player.isOnline()
                    ) {
                        var name = this.sn.getPlayerRawNameVisibility(player) ? player.getName() : "";
                        var nWGP = profile.withName(name);
                        nWGP.getProperties().removeAll("textures");
                        nWGP.getProperties().put("textures", sn.getPlayerDisplaySkinProperty(player));
                        nPIDList.add(new PlayerInfoData(nWGP, rPID.getLatency(), rPID.getGameMode(), rPID.getDisplayName()));
                    }
                }
                packet.getPlayerInfoDataLists().write(0, nPIDList);
            }
        }
    }

    public static final class MovePacketAdapter extends PacketAdapter {

        private final StyleName sn;
        private final ProtocolManager pm;

        public MovePacketAdapter() {
            super(
                    StyleName.plugin,
                    ListenerPriority.NORMAL,
                    PacketType.Play.Server.NAMED_ENTITY_SPAWN,
                    PacketType.Play.Server.ENTITY_DESTROY,
                    PacketType.Play.Server.ENTITY_METADATA,
                    PacketType.Play.Server.REL_ENTITY_MOVE,
                    PacketType.Play.Server.REL_ENTITY_MOVE_LOOK,
                    PacketType.Play.Server.ENTITY_TELEPORT
            );
            sn = StyleName.plugin;
            pm = ProtocolLibrary.getProtocolManager();
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            PacketContainer packet;
            if ((packet = event.getPacket()) != null) {
                if (packet.getType().equals(PacketType.Play.Server.NAMED_ENTITY_SPAWN)) {
                    var eid = packet.getIntegers().read(0);
                    var uuid = packet.getUUIDs().read(0);
                    var player = sn.getServer().getPlayer(uuid);
                    var target = event.getPlayer();
                    if (player != null && !target.getGameMode().equals(GameMode.SPECTATOR)) {
                        try {
                            var packetSpawnPlayer = PlayerPacketWrap.wrapFollowerSpawn(player);
                            var packetMetaPlayer = PlayerPacketWrap.wrapFollowerMeta(player);
                            pm.sendServerPacket(target, packetSpawnPlayer);
                            pm.sendServerPacket(target, packetMetaPlayer);
                        } catch (Exception e) {
                            StyleName.logger.warning("Unable to wrap the player follower spawn packet.");
                        }
                    }
                } else if (packet.getType().equals(PacketType.Play.Server.ENTITY_DESTROY)) {
                    var eidList = packet.getIntLists().read(0);
                    for (var eid : eidList) {
                        var player = getEIDPlayer(eid);
                        var target = event.getPlayer();
                        if (player != null && !target.getGameMode().equals(GameMode.SPECTATOR)) {
                            try {
                                var packetDestroyPlayer = wrapFollowerDestroy(player);
                                pm.sendServerPacket(target, packetDestroyPlayer);
                            } catch (Exception e) {
                                StyleName.logger.warning("Unable to wrap the player follower destroy packet.");
                            }
                        }
                    }
                } else if (packet.getType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
                    var eid = packet.getIntegers().read(0);
                    var player = getEIDPlayer(eid);
                    var target = event.getPlayer();
                    if (player != null && !target.getGameMode().equals(GameMode.SPECTATOR)) {
                        try {
                            var packetMeta = wrapFollowerMeta(player);
                            pm.sendServerPacket(target, packetMeta);
                        } catch (Exception e) {
                            StyleName.logger.warning("Unable to wrap the player follower meta packet.");
                        }
                    }
                } else if (
                        packet.getType().equals(PacketType.Play.Server.REL_ENTITY_MOVE) ||
                                packet.getType().equals(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK) ||
                                packet.getType().equals(PacketType.Play.Server.ENTITY_TELEPORT)
                ) {
                    var eid = packet.getIntegers().read(0);
                    var player = getEIDPlayer(eid);
                    var target = event.getPlayer();
                    if (player != null && !target.getGameMode().equals(GameMode.SPECTATOR)) {
                        try {
                            var packetMove = wrapFollowerMove(player);
                            pm.sendServerPacket(target, packetMove);
                        } catch (Exception e) {
                            StyleName.logger.warning("Unable to wrap the player follower move packet.");
                        }
                    }
                }
            }
        }

    }

}
