package net.tarcadia.tribina.erod.stylename.util;

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
import java.util.Map;

public class SkinLoad {

    public static final String URL_API_NAME_TO_UUID = "https://api.mojang.com/users/profiles/minecraft/";
    public static final String URL_API_UUID_TO_PROFILE = "https://sessionserver.mojang.com/session/minecraft/profile/";

    private static final Map<String, JsonObject> textureRequestJson = new HashMap<>();

    synchronized public static void loadOwnSkin(@NotNull Player player) {
        String uuid = null;
        JsonObject tx;
        try{
            var url = new URL(URL_API_NAME_TO_UUID + player.getName());
            StyleName.logger.info("Requesting: \"" + url + "\" for player UUID.");
            var https = (HttpsURLConnection) url.openConnection();
            https.setRequestMethod("GET");
            https.setRequestProperty("Content-Type", "application/json");
            https.connect();
            var code = https.getResponseCode();
            if (code >= 200 && code < 300) {
                var message = new String(https.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                StyleName.logger.info("Response: " + message);
                uuid = JsonParser.parseString(message).getAsJsonObject().get("id").getAsString();
            } else {
                StyleName.logger.warning("Unable to fetch player " + player.getName() + "'s UUID.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            StyleName.logger.warning("Unable to fetch player " + player.getName() + "'s UUID.");
        }

        if (uuid != null) try {
            var url = new URL(URL_API_UUID_TO_PROFILE + uuid);
            StyleName.logger.info("Requesting: \"" + url + "\" for player profile.");
            var https = (HttpsURLConnection) url.openConnection();
            https.setRequestMethod("GET");
            https.setRequestProperty("Content-Type", "application/json");
            https.connect();
            var code = https.getResponseCode();
            if (code >= 200 && code < 300) {
                var message = new String(https.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                StyleName.logger.info("Response: " + message);
                tx = JsonParser.parseString(message).getAsJsonObject().getAsJsonArray("properties").get(0).getAsJsonObject();
                textureRequestJson.put(player.getName(), tx);
            } else {
                StyleName.logger.warning("Unable to fetch player " + player.getName() + "'s profile.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            StyleName.logger.warning("Unable to fetch player " + player.getName() + "'s profile.");
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

}
