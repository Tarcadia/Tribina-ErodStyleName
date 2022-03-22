package net.tarcadia.tribina.erod.stylename.util.run;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.tarcadia.tribina.erod.stylename.StyleName;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class PlayerFollowerId {

    static final Map<Player, PlayerFollowerId> pfi = new HashMap<>();

    synchronized public static PlayerFollowerId getPlayerFollowerId(Player player) {
        pfi.putIfAbsent(player, new PlayerFollowerId());
        return pfi.get(player);
    }

    private final UUID uuid;
    private final int eid;

    private PlayerFollowerId() {
        this.uuid = UUID.randomUUID();
        this.eid = new Random().nextInt(Integer.MAX_VALUE);
    }

    public UUID uuid() { return this.uuid; }
    public int eid() { return this.eid; }

}

class PlayerFollowerViewer extends PacketAdapter {

    static final Map<Player, PlayerFollowerViewer> pvm = new HashMap<>();

    synchronized public static PlayerFollowerViewer getPlayerFollowerViewer(Player player) {
        if (!pvm.containsKey(player)) {
            var pv = new PlayerFollowerViewer(player);
            pvm.put(player, pv);
            return pv;
        } else {
            return pvm.get(player);
        }
    }

    synchronized public static Collection<Player> getPlayers() {
        return new LinkedList<>(pvm.keySet());
    }

    synchronized public static Collection<PlayerFollowerViewer> getFollowerViewers() {
        return new LinkedList<>(pvm.values());
    }

    private final Player player;
    private final Set<Player> playerViewedBy = new HashSet<>();
    private final Set<Player> followerViewedBy = new HashSet<>();
    private boolean canViewFollower = false;

    private PlayerFollowerViewer(Player player) {
        super(
                StyleName.plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.NAMED_ENTITY_SPAWN,
                PacketType.Play.Server.ENTITY_DESTROY
        );
        this.player = player;
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    public void end() {
        ProtocolLibrary.getProtocolManager().removePacketListener(this);
    }

    synchronized public void canViewFollower(boolean canViewFollower) {
        this.canViewFollower = canViewFollower;
        for (var viewer : playerViewedBy) this.viewInOutUpdateFollower(viewer);
        for (var follower : getFollowerViewers()) follower.viewInOutUpdateFollower(this.player);
    }

    synchronized public boolean canViewFollower() {
        return this.canViewFollower;
    }

    synchronized private void viewIn(@NotNull Player viewer) {
        this.playerViewedBy.add(viewer);
        this.viewInOutUpdateFollower(viewer);
    }

    synchronized private void viewOut(@NotNull Player viewer) {
        this.playerViewedBy.remove(viewer);
        this.viewInOutUpdateFollower(viewer);
    }

    synchronized public void viewMoveUpdateFollowerAll() {
        try {
            var pm = ProtocolLibrary.getProtocolManager();
            var movePacket = this.getMovePacket();
            for (var viewer : this.followerViewedBy) if (viewer != this.player) {
                pm.sendServerPacket(viewer, movePacket);
            }
        } catch (Exception e) {
            StyleName.logger.severe("Unable to move follower of " + this.player.getName() + " in view of all.");
        }
    }

    synchronized public void viewMetaUpdateFollowerAll() {
        try {
            var pm = ProtocolLibrary.getProtocolManager();
            var metaPacket = this.getMetaPacket();
            for (var viewer : this.followerViewedBy) if (viewer != this.player) {
                pm.sendServerPacket(viewer, metaPacket);
            }
        } catch (Exception e) {
            StyleName.logger.severe("Unable to meta follower of " + this.player.getName() + " in view of all.");
        }
    }

    synchronized public void viewMoveMetaUpdateFollowerAll() {
        try {
            var pm = ProtocolLibrary.getProtocolManager();
            var movePacket = this.getMovePacket();
            var metaPacket = this.getMetaPacket();
            for (var viewer : this.followerViewedBy) if (viewer != this.player) {
                pm.sendServerPacket(viewer, movePacket);
                pm.sendServerPacket(viewer, metaPacket);
            }
        } catch (Exception e) {
            StyleName.logger.severe("Unable to move and meta follower of " + this.player.getName() + " in view of all.");
        }
    }

    synchronized public void viewMetaUpdateFollower(@NotNull Player viewer) {
        if (viewer != this.player && this.followerViewedBy.contains(viewer)) {
            try {
                var pm = ProtocolLibrary.getProtocolManager();
                var metaPacket = this.getMetaPacket();
                pm.sendServerPacket(viewer, metaPacket);
                this.followerViewedBy.add(viewer);
            } catch (Exception e) {
                StyleName.logger.severe("Unable to meta follower of " + this.player.getName() + " in view of " + viewer.getName() + ".");
            }
        }
    }

    synchronized public void viewMoveUpdateFollower(@NotNull Player viewer) {
        if (viewer != this.player && this.followerViewedBy.contains(viewer)) {
            try {
                var pm = ProtocolLibrary.getProtocolManager();
                var movePacket = this.getMovePacket();
                pm.sendServerPacket(viewer, movePacket);
                this.followerViewedBy.add(viewer);
            } catch (Exception e) {
                StyleName.logger.severe("Unable to move follower of " + this.player.getName() + " in view of " + viewer.getName() + ".");
            }
        }
    }

    synchronized public void viewMoveMetaUpdateFollower(@NotNull Player viewer) {
        if (viewer != this.player && this.followerViewedBy.contains(viewer)) {
            try {
                var pm = ProtocolLibrary.getProtocolManager();
                var movePacket = this.getMovePacket();
                var metaPacket = this.getMetaPacket();
                pm.sendServerPacket(viewer, movePacket);
                pm.sendServerPacket(viewer, metaPacket);
                this.followerViewedBy.add(viewer);
            } catch (Exception e) {
                StyleName.logger.severe("Unable to move and meta follower of " + this.player.getName() + " in view of " + viewer.getName() + ".");
            }
        }
    }

    synchronized public void viewInOutUpdateFollower(@NotNull Player viewer) {
        if (viewer != this.player && !this.followerViewedBy.contains(viewer) && (this.playerViewedBy.contains(viewer) && this.canViewFollower && getPlayerFollowerViewer(viewer).canViewFollower())) {
            try {
                var pm = ProtocolLibrary.getProtocolManager();
                var spawnPacket = this.getSpawnPacket();
                var metaPacket = this.getMetaPacket();
                pm.sendServerPacket(viewer, spawnPacket);
                pm.sendServerPacket(viewer, metaPacket);
                this.followerViewedBy.add(viewer);
            } catch (Exception e) {
                StyleName.logger.severe("Unable to spawn follower of " + this.player.getName() + " in view of " + viewer.getName() + ".");
            }
        } else if (viewer != this.player && this.followerViewedBy.contains(viewer) && (!this.playerViewedBy.contains(viewer) || !this.canViewFollower || !getPlayerFollowerViewer(viewer).canViewFollower())) {
            try {
                var pm = ProtocolLibrary.getProtocolManager();
                var destroyPacket = this.getDestroyPacket();
                pm.sendServerPacket(viewer, destroyPacket);
                this.followerViewedBy.remove(viewer);
            } catch (Exception e) {
                StyleName.logger.severe("Unable to destroy follower of " + this.player.getName() + " in view of " + viewer.getName() + ".");
            }
        }

    }

    private double getFollowerNameTagOffset() {
        var sn = StyleName.plugin;
        double offset;
        var vehicle = this.player.getVehicle();
        if (vehicle instanceof Strider) offset = this.player.getHeight() + 1.16;
        else if (vehicle instanceof Horse) offset = this.player.getHeight() + 0.85;
        else if (vehicle instanceof Llama) offset = this.player.getHeight() + 0.772;
        else if (vehicle instanceof Pig) offset = this.player.getHeight() + 0.325;
        else if (vehicle instanceof Boat) offset = this.player.getHeight() - 0.45;
        else offset = this.player.getHeight();
        if (sn.getPlayerRawNameVisibility(player)) offset += 0.3;
        return offset;
    }

    @NotNull
    private PacketContainer getSpawnPacket() {
        var pm = ProtocolLibrary.getProtocolManager();
        var id = PlayerFollowerId.getPlayerFollowerId(this.player);
        var packetSpawn = pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        var loc = this.player.getLocation();
        var offset = this.getFollowerNameTagOffset();

        packetSpawn.getModifier().writeDefaults();
        packetSpawn.getIntegers().write(0, id.eid());
        packetSpawn.getUUIDs().write(0, id.uuid());
        packetSpawn.getIntegers().write(1, 1);
        packetSpawn.getDoubles().write(0, loc.getX());
        packetSpawn.getDoubles().write(1, loc.getY() + offset);
        packetSpawn.getDoubles().write(2, loc.getZ());

        return packetSpawn;
    }

    @NotNull
    private PacketContainer getMetaPacket() {
        var sn = StyleName.plugin;
        var pm = ProtocolLibrary.getProtocolManager();
        var metadata = new WrappedDataWatcher();
        var displayName = sn.getPlayerDisplayFullName(this.player);
        var displayNameVisible = !this.player.isSneaking() && sn.isFunctionEnabled();
        var optDisplayNameObject = Optional.of(WrappedChatComponent.fromText(displayName).getHandle());
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20); // invisible
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), optDisplayNameObject);
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), displayNameVisible);
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(4, WrappedDataWatcher.Registry.get(Boolean.class)), true);
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true);
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) (0x01 | 0x08 | 0x10)); //isSmall, noBasePlate, set Marker

        var id = PlayerFollowerId.getPlayerFollowerId(this.player);
        var packetMeta = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packetMeta.getModifier().writeDefaults();
        packetMeta.getIntegers().write(0, id.eid());
        packetMeta.getWatchableCollectionModifier().write(0, metadata.getWatchableObjects());

        return packetMeta;
    }

    @NotNull
    private PacketContainer getMovePacket() {
        var pm = ProtocolLibrary.getProtocolManager();
        var id = PlayerFollowerId.getPlayerFollowerId(player);
        var loc = this.player.getLocation();
        var offset = this.getFollowerNameTagOffset();

        var packetMove = pm.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packetMove.getModifier().writeDefaults();
        packetMove.getIntegers().write(0, id.eid());
        packetMove.getDoubles().write(0, loc.getX());
        packetMove.getDoubles().write(1, loc.getY() + offset);
        packetMove.getDoubles().write(2, loc.getZ());

        return packetMove;
    }

    @NotNull
    private PacketContainer getDestroyPacket() {
        var pm = ProtocolLibrary.getProtocolManager();
        var id = PlayerFollowerId.getPlayerFollowerId(player);

        var packetDestroy = pm.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        packetDestroy.getModifier().writeDefaults();
        packetDestroy.getIntLists().write(0, List.of(id.eid()));

        return packetDestroy;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet;
        if ((packet = event.getPacket()) != null) {
            if (packet.getType().equals(PacketType.Play.Server.NAMED_ENTITY_SPAWN)) {
                var eid = packet.getIntegers().read(0);
                var uuid = packet.getUUIDs().read(0);
                if (this.player.getEntityId() == eid && this.player.getUniqueId().equals(uuid)) {
                    var viewer = event.getPlayer();
                    this.viewIn(viewer);
                }
            } else if (packet.getType().equals(PacketType.Play.Server.ENTITY_DESTROY)) {
                var eidList = packet.getIntLists().read(0);
                for (var eid : eidList) {
                    if (this.player.getEntityId() == eid) {
                        var viewer = event.getPlayer();
                        this.viewOut(viewer);
                    }
                }
            }
        }
    }

}

public class PlayerFollower extends BukkitRunnable {

    private Location lastLoc = null;
    private GameMode lastGM = null;
    private boolean lastSneaking = false;

    private final Player player;
    private final PlayerFollowerViewer follower;

    public PlayerFollower(@NotNull Player player) {
        super();
        this.player = player;
        this.follower = PlayerFollowerViewer.getPlayerFollowerViewer(player);
        this.runTaskTimerAsynchronously(StyleName.plugin, 0, 1);
    }

    public void end() {
        this.follower.end();
        this.cancel();
    }

    @Override
    public void run() {
        if (this.player.isOnline()) {
            var nowGM = this.player.getGameMode();
            if (this.lastGM == null) this.follower.canViewFollower(!nowGM.equals(GameMode.SPECTATOR));
            else if (this.lastGM.equals(GameMode.SPECTATOR) && !nowGM.equals(GameMode.SPECTATOR))
                this.follower.canViewFollower(true);
            else if (!this.lastGM.equals(GameMode.SPECTATOR) && nowGM.equals(GameMode.SPECTATOR))
                this.follower.canViewFollower(false);
            var nowLoc = this.player.getLocation().clone();
            var nowSneaking = this.player.isSneaking();
            boolean ifMove = (this.lastLoc == null || this.lastLoc.getX() != nowLoc.getX() || this.lastLoc.getY() != nowLoc.getY() || this.lastLoc.getZ() != nowLoc.getZ());
            boolean ifMeta = (this.lastSneaking != nowSneaking);
            if (ifMove && ifMeta) this.follower.viewMoveMetaUpdateFollowerAll();
            else if (ifMove) this.follower.viewMoveUpdateFollowerAll();
            else if (ifMeta) this.follower.viewMetaUpdateFollowerAll();
            this.lastLoc = nowLoc;
            this.lastGM = nowGM;
            this.lastSneaking = nowSneaking;
        } else {
            this.end();
        }
    }
}