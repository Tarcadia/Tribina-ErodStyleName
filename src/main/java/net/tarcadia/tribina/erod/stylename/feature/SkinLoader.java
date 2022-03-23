package net.tarcadia.tribina.erod.stylename.feature;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.tarcadia.tribina.erod.stylename.StyleName;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SkinLoader {

    public static final String URL_API_NAME_TO_UUID = "https://api.mojang.com/users/profiles/minecraft/";
    public static final String URL_API_UUID_TO_PROFILE = "https://sessionserver.mojang.com/session/minecraft/profile/";

    private static final Map<String, JsonObject> textureRequestJson = new HashMap<>();

    synchronized public static void loadOwnSkin(@NotNull String playerName) {
        String uuid = null;
        JsonObject tx;
        try{
            var url = new URL(URL_API_NAME_TO_UUID + playerName);
            StyleName.logger.info("Requesting: \"" + url + "\" for player UUID.");
            var https = (HttpsURLConnection) url.openConnection();
            https.setRequestMethod("GET");
            https.setRequestProperty("Content-Type", "application/json");
            https.connect();
            var code = https.getResponseCode();
            if (code >= 200 && code < 300) {
                var message = new String(https.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                //StyleName.logger.info("Response: " + message);
                uuid = JsonParser.parseString(message).getAsJsonObject().get("id").getAsString();
            } else {
                StyleName.logger.warning("Unable to fetch player " + playerName + "'s UUID.");
            }
        } catch (Exception e) {
            StyleName.logger.warning("Unable to fetch player " + playerName + "'s UUID.");
        }

        if (uuid != null) try {
            var url = new URL(URL_API_UUID_TO_PROFILE + uuid + "?unsigned=false");
            StyleName.logger.info("Requesting: \"" + url + "\" for player profile.");
            var https = (HttpsURLConnection) url.openConnection();
            https.setRequestMethod("GET");
            https.setRequestProperty("Content-Type", "application/json");
            https.connect();
            var code = https.getResponseCode();
            if (code >= 200 && code < 300) {
                var message = new String(https.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                //StyleName.logger.info("Response: " + message);
                tx = JsonParser.parseString(message).getAsJsonObject().getAsJsonArray("properties").get(0).getAsJsonObject();
                textureRequestJson.put(playerName, tx);
            } else {
                StyleName.logger.warning("Unable to fetch player " + playerName + "'s profile.");
            }
        } catch (Exception e) {
            StyleName.logger.warning("Unable to fetch player " + playerName + "'s profile.");
        }
    }

    synchronized public static void unloadOwnSkin(@NotNull Player player) {
        textureRequestJson.remove(player.getName());
    }

    @Nullable
    synchronized public static String getTextureValue(@NotNull Player player) {
        var tx = textureRequestJson.get(player.getName());
        if (tx != null && tx.has("value")) {
            return tx.get("value").getAsString();
        } else {
            return null;
        }
    }

    @Nullable
    synchronized public static String getTextureSignature(@NotNull Player player) {
        var tx = textureRequestJson.get(player.getName());
        if (tx != null && tx.has("value")) {
            return tx.get("signature").getAsString();
        } else {
            return null;
        }
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
                        var name = this.sn.getPlayerRawNameVisibility(player) ? player.getName() : "~";
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

}
