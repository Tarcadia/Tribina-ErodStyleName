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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerPacketWrap {

    private final ProtocolManager pm;
    private final StyleName sn;
    private final Map<Integer, Player> eidPlayer = new HashMap<>();
    private final Map<Integer, Player> eidFollower = new HashMap<>();
    private final Map<String, Integer> followerEID = new HashMap<>();
    private final Map<String, UUID> followerUUID = new HashMap<>();

    public final PacketAdapter playerInfoListener;
    public final PacketAdapter playerMoveListener;

    public int getFollowerEID(@NotNull Player player) {
        if (!followerEID.containsKey(player.getName()) || followerEID.get(player.getName()) == null) {
            var eid = new Random().nextInt(Integer.MAX_VALUE);
            followerEID.put(player.getName(), eid);
            eidFollower.put(eid, player);
            return eid;
        } else {
            return followerEID.get(player.getName());
        }
    }

    @NotNull
    public UUID getFollowerUUID(@NotNull Player player) {
        if (!followerUUID.containsKey(player.getName()) || followerUUID.get(player.getName()) == null) {
            var uuid = UUID.randomUUID();
            followerUUID.put(player.getName(), uuid);
            return uuid;
        } else {
            return followerUUID.get(player.getName());
        }
    }

    @Nullable
    public Player getEIDFollower(int eid) {
        if (eidFollower.containsKey(eid) && eidFollower.get(eid) != null) {
            return eidFollower.get(eid);
        } else {
            return null;
        }
    }

    public void updateEIDPlayer(@NotNull Player player) {
        eidPlayer.put(player.getEntityId(), player);
    }

    @Nullable
    public Player getEIDPlayer(int eid) {
        if (eidPlayer.containsKey(eid) && eidPlayer.get(eid) != null) {
            var player = eidPlayer.get(eid);
            if (player.getEntityId() == eid) {
                return player;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @NotNull
    public PacketContainer wrapFollowerSpawn(@NotNull Player player) {
        var eid = getFollowerEID(player);
        var uuid = getFollowerUUID(player);
        var packetSpawn = pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        packetSpawn.getModifier().writeDefaults();
        packetSpawn.getIntegers().write(0, eid);
        packetSpawn.getUUIDs().write(0, uuid);
        packetSpawn.getIntegers().write(1, 1);
        packetSpawn.getDoubles().write(0, player.getLocation().getX());
        packetSpawn.getDoubles().write(1, player.getLocation().getY() + 2.4);
        packetSpawn.getDoubles().write(2, player.getLocation().getZ());

        return packetSpawn;
    }

    @NotNull
    public PacketContainer wrapFollowerMeta(@NotNull Player player) {
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
    public PacketContainer wrapFollowerDestroy(@NotNull Player player) {
        var eid = getFollowerEID(player);
        var uuid = getFollowerUUID(player);

        var packetDestroy = pm.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        packetDestroy.getModifier().writeDefaults();
        packetDestroy.getIntLists().write(0, List.of(eid));

        return packetDestroy;
    }

    @NotNull
    public PacketContainer wrapFollowerMove(@NotNull Player player) {
        var eid = getFollowerEID(player);
        var uuid = getFollowerUUID(player);

        var packetMove = pm.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packetMove.getModifier().writeDefaults();
        packetMove.getIntegers().write(0, eid);
        packetMove.getDoubles().write(0, player.getLocation().getX());
        packetMove.getDoubles().write(1, player.getLocation().getY() + 2.4);
        packetMove.getDoubles().write(2, player.getLocation().getZ());

        return packetMove;
    }

    public PlayerPacketWrap() {
        pm = ProtocolLibrary.getProtocolManager();
        sn = StyleName.plugin;

        playerInfoListener = new PacketAdapter(
                sn,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.PLAYER_INFO
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet;
                if (
                        sn.isFunctionEnabled() &&
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
                                ((player = sn.getServer().getPlayer(profile.getUUID())) != null) &&
                                player.isOnline()
                        ) {
                            var name = sn.getPlayerStringName(player);
                            var nWGP = profile.withName(name);
                            nWGP.getProperties().removeAll("textures");
                            nWGP.getProperties().put("textures", sn.getPlayerDisplaySkinProperty(player));
                            nPIDList.add(new PlayerInfoData(nWGP, rPID.getLatency(), rPID.getGameMode(), rPID.getDisplayName()));
                        }
                    }
                    packet.getPlayerInfoDataLists().write(0, nPIDList);
                }
            }
        };


        playerMoveListener = new PacketAdapter(
                sn,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.NAMED_ENTITY_SPAWN,
                PacketType.Play.Server.ENTITY_DESTROY,
                PacketType.Play.Server.ENTITY_METADATA,
                PacketType.Play.Server.REL_ENTITY_MOVE,
                PacketType.Play.Server.REL_ENTITY_MOVE_LOOK,
                PacketType.Play.Server.ENTITY_TELEPORT
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet;
                if ((packet = event.getPacket()) != null) {
                    if (packet.getType().equals(PacketType.Play.Server.NAMED_ENTITY_SPAWN)) {
                        var eid = packet.getIntegers().read(0);
                        var uuid = packet.getUUIDs().read(0);
                        var player = sn.getServer().getPlayer(uuid);
                        if (player != null) {
                            updateEIDPlayer(player);
                            var packetSpawn = wrapFollowerSpawn(player);
                            var packetMeta = wrapFollowerMeta(player);
                            try {
                                var target = event.getPlayer();
                                pm.sendServerPacket(target, packetSpawn);
                                pm.sendServerPacket(target, packetMeta);
                            } catch (Exception e) {
                                StyleName.logger.warning("Unable to wrap the player follower spawn packet.");
                            }
                        }
                    } else if (packet.getType().equals(PacketType.Play.Server.ENTITY_DESTROY)) {
                        var eidList = packet.getIntLists().read(0);
                        for (var eid : eidList) {
                            var player = getEIDPlayer(eid);
                            if (player != null) {
                                sn.getLogger().info("ENTITY DESTROY: " + player.getName());
                                var packetDestroy = wrapFollowerDestroy(player);
                                try {
                                    var target = event.getPlayer();
                                    pm.sendServerPacket(target, packetDestroy);
                                } catch (Exception e) {
                                    StyleName.logger.warning("Unable to wrap the player follower destroy packet.");
                                }
                            }
                        }
                    } else if (packet.getType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
                        var eid = packet.getIntegers().read(0);
                        var player = getEIDPlayer(eid);
                        if (player != null) {
                            var packetMeta = wrapFollowerMeta(player);
                            try {
                                var target = event.getPlayer();
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
                        if (player != null) {
                            var packetMove = wrapFollowerMove(player);
                            try {
                                var target = event.getPlayer();
                                pm.sendServerPacket(target, packetMove);
                            } catch (Exception e) {
                                StyleName.logger.warning("Unable to wrap the player follower move packet.");
                            }
                        }
                    }
                }
            }
        };

        pm.addPacketListener(playerInfoListener);
        pm.addPacketListener(playerMoveListener);
    }

}
