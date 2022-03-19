package net.tarcadia.tribina.erod.stylename;

import com.comphenix.protocol.wrappers.*;
import net.tarcadia.tribina.erod.stylename.util.Skin;
import net.tarcadia.tribina.erod.stylename.util.Style;
import net.tarcadia.tribina.erod.stylename.util.Tag;
import net.tarcadia.tribina.erod.stylename.util.data.Configuration;
import net.tarcadia.tribina.erod.stylename.util.wrap.PlayerPacketWrap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public final class StyleName extends JavaPlugin implements TabExecutor, Listener {

    public static StyleName plugin = null;
    public static Configuration config = null;
    public static PluginDescriptionFile descrp = null;
    public static Logger logger = null;
    public static String dataPath = null;

    public static final String PATH_CONFIG_DEFAULT = "config.yml";
    public static final String PATH_CONFIG = "Erod/StyleName.yml";

    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_PLAYERS = "players.";
    public static final String KEY_PLAYERS_NAME = ".name";
    public static final String KEY_PLAYERS_SKIN = ".skin";
    public static final String KEY_PLAYERS_SKIN_LIST = ".skin-list";
    public static final String KEY_PLAYERS_TAG = ".tag";
    public static final String KEY_PLAYERS_TAG_LIST = ".tag-list";
    public static final String KEY_PLAYERS_TAG_VISIBLE = ".tag-visible";
    public static final String KEY_PLAYERS_STYLE = ".style";
    public static final String KEY_PLAYERS_STYLE_LIST = ".style-list";

    public static final String KEY_TEXT_FUNCTION_ENABLE = "texts.function-enable";
    public static final String KEY_TEXT_FUNCTION_DISABLE = "texts.function-disable";
    public static final String KEY_TEXT_FUNCTION_FAIL = "texts.function-fail";
    public static final String KEY_TEXT_LIST_FAIL = "texts.list-fail";
    public static final String KEY_TEXT_SET_NAME = "texts.set-name";
    public static final String KEY_TEXT_SET_NAME_FAIL = "texts.set-name-fail";
    public static final String KEY_TEXT_ADD_SKIN = "texts.add-skin";
    public static final String KEY_TEXT_ADD_SKIN_FAIL = "texts.add-skin-fail";
    public static final String KEY_TEXT_SET_SKIN = "texts.set-skin";
    public static final String KEY_TEXT_SET_SKIN_FAIL = "texts.set-skin-fail";
    public static final String KEY_TEXT_ADD_TAG = "texts.add-tag";
    public static final String KEY_TEXT_ADD_TAG_FAIL = "texts.add-tag-fail";
    public static final String KEY_TEXT_SET_TAG = "texts.set-tag";
    public static final String KEY_TEXT_SET_TAG_FAIL = "texts.set-tag-fail";
    public static final String KEY_TEXT_SET_TAG_VISIBLE = "texts.set-tag-visible";
    public static final String KEY_TEXT_SET_TAG_INVISIBLE = "texts.set-tag-invisible";
    public static final String KEY_TEXT_SET_TAG_VISIBILITY_FAIL = "texts.set-tag-visibility-fail";
    public static final String KEY_TEXT_ADD_STYLE = "texts.add-style";
    public static final String KEY_TEXT_ADD_STYLE_FAIL = "texts.add-style-fail";
    public static final String KEY_TEXT_SET_STYLE = "texts.set-style";
    public static final String KEY_TEXT_SET_STYLE_FAIL = "texts.set-style-fail";

    public static final String CMD_SN = "erodstylename";
    public static final String CMD_SN_ARG_ENABLE = "enable";
    public static final String CMD_SN_ARG_DISABLE = "disable";
    public static final String CMD_SN_ARG_LIST = "list";
    public static final String CMD_SN_ARG_SET_NAME = "set-name";
    public static final String CMD_SN_ARG_ADD_SKIN = "add-skin";
    public static final String CMD_SN_ARG_SET_SKIN = "set-skin";
    public static final String CMD_SN_ARG_ADD_TAG = "add-tag";
    public static final String CMD_SN_ARG_SET_TAG = "set-tag";
    public static final String CMD_SN_ARG_SET_TAG_VISIBLE = "set-tag-visible";
    public static final String CMD_SN_ARG_SET_TAG_INVISIBLE = "set-tag-invisible";
    public static final String CMD_SN_ARG_ADD_STYLE = "add-style";
    public static final String CMD_SN_ARG_SET_STYLE = "set-style";

    private PlayerPacketWrap ppw;

    public boolean isFunctionEnabled() {
        return config.getBoolean(KEY_ENABLED);
    }

    public void functionEnable() {
        config.set(KEY_ENABLED, true);
        for (var p : plugin.getServer().getOnlinePlayers()) this.updatePlayerDisplay(p);
        logger.info("Plugin functional enabled.");
    }

    public void functionDisable() {
        config.set(KEY_ENABLED, false);
        for (var p : plugin.getServer().getOnlinePlayers()) this.updatePlayerDisplay(p);
        logger.info("Plugin functional disabled.");
    }

    @Override
    public void onLoad() {
        plugin = this;
        config = Configuration.getConfiguration(new File(PATH_CONFIG));
        config.setDefaults(YamlConfiguration.loadConfiguration(Objects.requireNonNull(this.getTextResource(PATH_CONFIG_DEFAULT))));
        descrp = this.getDescription();
        logger = this.getLogger();
        dataPath = this.getDataFolder().getPath() + "/";
        logger.info("Loaded " + descrp.getName() + " v" + descrp.getVersion() + ".");
    }

    @Override
    public void onEnable() {
        var commandSN = this.getCommand(CMD_SN);
        if (commandSN != null) {
            commandSN.setExecutor(this);
            commandSN.setTabCompleter(this);
        }
        this.getServer().getPluginManager().registerEvents(this, this);
        ppw = new PlayerPacketWrap();
        logger.info("Enabled " + descrp.getName() + " v" + descrp.getVersion() + ".");

    }

    @Override
    public void onDisable() {
        logger.info("Disabled " + descrp.getName() + " v" + descrp.getVersion() + ".");
    }

    public boolean setPlayerName(@NotNull Player player, @NotNull String name) {
        if (!name.contains("§") && name.getBytes(StandardCharsets.UTF_8).length <= 16) {
            config.set(KEY_PLAYERS + player.getName() + KEY_PLAYERS_NAME, name);
            return true;
        } else {
            return false;
        }
    }

    public void addPlayerSkin(@NotNull Player player, @NotNull String skin) {
        config.addStringList(KEY_PLAYERS + player.getName() + KEY_PLAYERS_SKIN_LIST, skin);
    }

    public boolean setPlayerSkin(@NotNull Player player, @NotNull String skin) {
        var skinLst = config.getStringList(KEY_PLAYERS + player.getName() + KEY_PLAYERS_SKIN_LIST);
        if (skinLst.contains(skin)) {
            config.set(KEY_PLAYERS + player.getName() + KEY_PLAYERS_SKIN, skin);
            return true;
        } else {
            return false;
        }
    }

    public void addPlayerTag(@NotNull Player player, @NotNull String tag) {
        config.addStringList(KEY_PLAYERS + player.getName() + KEY_PLAYERS_TAG_LIST, tag);
    }

    public boolean setPlayerTag(@NotNull Player player, @NotNull String tag) {
        var tagLst = config.getStringList(KEY_PLAYERS + player.getName() + KEY_PLAYERS_TAG_LIST);
        if (tagLst.contains(tag)) {
            config.set(KEY_PLAYERS + player.getName() + KEY_PLAYERS_TAG, tag);
            return true;
        } else {
            return false;
        }
    }

    public void setPlayerTagVisibility(@NotNull Player player, boolean visibility) {
        config.set(KEY_PLAYERS + player.getName() + KEY_PLAYERS_TAG_VISIBLE, visibility);
    }

    public void addPlayerStyle(@NotNull Player player, @NotNull String style) {
        config.addStringList(KEY_PLAYERS + player.getName() + KEY_PLAYERS_STYLE_LIST, style);
    }

    public boolean setPlayerStyle(@NotNull Player player, @NotNull String style) {
        var styleLst = config.getStringList(KEY_PLAYERS + player.getName() + KEY_PLAYERS_STYLE_LIST);
        if (styleLst.contains(style)) {
            config.set(KEY_PLAYERS + player.getName() + KEY_PLAYERS_STYLE, style);
            return true;
        } else {
            return false;
        }
    }



    @NotNull
    public String getPlayerStringName(@NotNull Player player) {
        return config.getString(KEY_PLAYERS + player.getName() + KEY_PLAYERS_NAME, player.getName());
    }

    @NotNull
    public String getPlayerStringSkin(@NotNull Player player) {
        var skin = config.getString(KEY_PLAYERS + player.getName() + KEY_PLAYERS_SKIN, "(NONE)");
        var ret = "";
        try {
            var theSkin = Skin.valueOf(skin);
            ret = theSkin.name();
        } catch (IllegalArgumentException e) {
            ret = skin + " (NOT SUPPORTED)";
        }
        return ret;
    }

    @NotNull
    public List<String> getPlayerListSkin(@NotNull Player player) {
        var lst = config.getStringList(KEY_PLAYERS + player.getName() + KEY_PLAYERS_SKIN_LIST);
        var ret = new LinkedList<String>();
        for (var skin : lst) {
            try {
                Skin.valueOf(skin);
                ret.add(skin);
            } catch (IllegalArgumentException e) {
                ret.add(skin + " (NOT SUPPORTED)");
            }
        }
        return ret;
    }

    @NotNull
    public String getPlayerStringTag(@NotNull Player player) {
        var tag = config.getString(KEY_PLAYERS + player.getName() + KEY_PLAYERS_TAG, "(NONE)");
        var ret = "";
        try {
            var theTag = Tag.valueOf(tag);
            ret = theTag.tag();
        } catch (IllegalArgumentException e) {
            ret = tag + " (NOT SUPPORTED)";
        }
        return ret;
    }

    @NotNull
    public List<String> getPlayerListTag(@NotNull Player player) {
        var lst = config.getStringList(KEY_PLAYERS + player.getName() + KEY_PLAYERS_TAG_LIST);
        var ret = new LinkedList<String>();
        for (var tag : lst) {
            try {
                var theTag = Tag.valueOf(tag);
                ret.add(theTag.tag());
            } catch (IllegalArgumentException e) {
                ret.add(tag + " (NOT SUPPORTED)");
            }
        }
        return ret;
    }

    @NotNull
    public String getPlayerStringStyle(@NotNull Player player) {
        var style = config.getString(KEY_PLAYERS + player.getName() + KEY_PLAYERS_STYLE, "(NONE)");
        var ret = "";
        try {
            var theStyle = Style.valueOf(style);
            ret = theStyle.styled(style);
        } catch (IllegalArgumentException e) {
            ret = style + " (NOT SUPPORTED)";
        }
        return ret;
    }

    @NotNull
    public List<String> getPlayerListStyle(@NotNull Player player) {
        var lst = config.getStringList(KEY_PLAYERS + player.getName() + KEY_PLAYERS_STYLE_LIST);
        var ret = new LinkedList<String>();
        for (var style : lst) {
            try {
                var theStyle = Style.valueOf(style);
                ret.add(theStyle.styled(style));
            } catch (IllegalArgumentException e) {
                ret.add(style + " (NOT SUPPORTED)");
            }
        }
        return ret;
    }

    @NotNull
    public String getPlayerDisplayName(@NotNull Player player) {
        var name = config.getString(KEY_PLAYERS + player.getName() + KEY_PLAYERS_NAME, player.getName());
        var style = config.getString(KEY_PLAYERS + player.getName() + KEY_PLAYERS_STYLE, "Normal");
        var styleLst = config.getStringList(KEY_PLAYERS + player.getName() + KEY_PLAYERS_STYLE_LIST);
        Style theStyle;
        try {
            if (styleLst.contains(style)) {
                theStyle = Style.valueOf(style);
            } else {
                theStyle = Style.Normal;
            }
        } catch (IllegalArgumentException e) {
            logger.warning("Style " + style + " load failed.");
            this.setPlayerStyle(player, "Normal");
            theStyle = Style.Normal;
        }
        return theStyle.styled(name);
    }

    @NotNull
    public String getPlayerDisplayTag(@NotNull Player player) {
        var tag = config.getString(KEY_PLAYERS + player.getName() + KEY_PLAYERS_TAG, "NullTag");
        var tagVisible = config.getBoolean(KEY_PLAYERS + player.getName() + KEY_PLAYERS_TAG_VISIBLE);
        var tagLst = config.getStringList(KEY_PLAYERS + player.getName() + KEY_PLAYERS_TAG_LIST);
        Tag theTag;
        try {
            if (tagLst.contains(tag)) {
                theTag = Tag.valueOf(tag);
            } else {
                theTag = Tag.NullTag;
            }
        } catch (IllegalArgumentException e) {
            logger.warning("Tag " + tag + " load failed.");
            this.setPlayerStyle(player, "NullTag");
            theTag = Tag.NullTag;
        }
        if (tagVisible) return theTag.tag();
        else return "";
    }

    @NotNull
    public WrappedSignedProperty getPlayerDisplaySkinProperty(@NotNull Player player) {
        var skin = config.getString(KEY_PLAYERS + player.getName() + KEY_PLAYERS_SKIN, "Default");
        var skinLst = config.getStringList(KEY_PLAYERS + player.getName() + KEY_PLAYERS_SKIN_LIST);
        Skin theSkin;
        try {
            if (skinLst.contains(skin)) {
                theSkin = Skin.valueOf(skin);
            } else {
                theSkin = Skin.Default;
            }
        } catch (IllegalArgumentException e) {
            logger.warning("Skin " + skin + " load failed.");
            this.setPlayerStyle(player, "Default");
            theSkin = Skin.Default;
        }
        return new WrappedSignedProperty("textures", theSkin.skinValue(player), theSkin.skinSignature(player));
    }

    @NotNull
    public String getPlayerDisplayFullName(@NotNull Player player) {
        String name = this.getPlayerDisplayName(player);
        String tag = this.getPlayerDisplayTag(player);
        if (tag.equals("")) {
            return name;
        } else {
            return tag + name;
        }
    }

    public void updatePlayerDisplay(@NotNull Player player) {
        var skinLst = config.getStringList(KEY_PLAYERS + player.getName() + KEY_PLAYERS_SKIN_LIST);
        var tagLst = config.getStringList(KEY_PLAYERS + player.getName() + KEY_PLAYERS_TAG_LIST);
        var styleLst = config.getStringList(KEY_PLAYERS + player.getName() + KEY_PLAYERS_STYLE_LIST);
        if (skinLst.isEmpty()) {
            this.addPlayerSkin(player, "Default");
            this.setPlayerSkin(player, "Default");
        }
        if (tagLst.isEmpty()) {
            this.addPlayerTag(player, "NullTag");
            this.setPlayerTag(player, "NullTag");
        }
        if (styleLst.isEmpty()) {
            this.addPlayerStyle(player, "Normal");
            this.setPlayerStyle(player, "Normal");
        }
        if (this.isFunctionEnabled()) {
            player.setCustomName(getPlayerDisplayName(player));
            player.setDisplayName(getPlayerDisplayFullName(player));
            player.setPlayerListName(getPlayerDisplayName(player));
        } else {
            player.setCustomName(null);
            player.setDisplayName(null);
            player.setPlayerListName(null);
        }
        for (var p : player.getServer().getOnlinePlayers()) if (p != player) {
            p.hidePlayer(this, player);
            p.showPlayer(this, player);
        }
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        var player = event.getPlayer();
        this.updatePlayerDisplay(player);
        Skin.loadOwnSkin(player);
        ppw.loadEIDPlayer(player);
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        var player = event.getPlayer();
        Skin.unloadOwnSkin(player);
        ppw.unloadEIDPlayer(player);
    }

    @EventHandler
    public void onPlayerGameMode(@NotNull PlayerGameModeChangeEvent event) {
        var player = event.getPlayer();
        this.updatePlayerDisplay(player);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals(CMD_SN)) {
            if ((args.length == 1) && (args[0].equals(CMD_SN_ARG_ENABLE))) {
                if (sender.isOp()) {
                    this.functionEnable();
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_FUNCTION_ENABLE), ""));
                } else {
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_FUNCTION_FAIL), ""));
                }
                return true;
            } else if ((args.length == 1) && (args[0].equals(CMD_SN_ARG_DISABLE))) {
                if (sender.isOp()) {
                    this.functionDisable();
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_FUNCTION_DISABLE), ""));
                } else {
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_FUNCTION_FAIL), ""));
                }
                return true;
            } else if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_LIST))) {
                var player = this.getServer().getPlayer(args[1]);
                if ((sender.isOp() || sender.equals(player)) && (player != null)) {
                    this.updatePlayerDisplay(player);
                    sender.sendMessage(player.getName() + ":\n");
                    sender.sendMessage("name:  " + this.getPlayerStringName(player) + '\n');
                    sender.sendMessage("skin:  " + this.getPlayerStringSkin(player) + '\n');
                    sender.sendMessage("tag:   " + this.getPlayerStringTag(player) + '\n');
                    sender.sendMessage("style: " + this.getPlayerStringStyle(player) + '\n');
                    sender.sendMessage("skin list:\n");
                    for (var s : this.getPlayerListSkin(player)) sender.sendMessage("  - " + s + '\n');
                    sender.sendMessage("tag list:\n");
                    for (var s : this.getPlayerListTag(player)) sender.sendMessage("  - " + s + '\n');
                    sender.sendMessage("style list:\n");
                    for (var s : this.getPlayerListStyle(player)) sender.sendMessage("  - " + s + '\n');
                } else {
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_LIST_FAIL), ""));
                }
                return true;
            } else if ((args.length == 3) && (args[0].equals(CMD_SN_ARG_SET_NAME))) {
                var player = this.getServer().getPlayer(args[1]);
                if (sender.isOp() && (player != null) && this.setPlayerName(player, args[2])) {
                    this.updatePlayerDisplay(player);
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_SET_NAME), "").replace("$player$", player.getName()).replace("$name$", args[2]));
                } else {
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_SET_NAME_FAIL), ""));
                }
                return true;
            } else if ((args.length == 3) && (args[0].equals(CMD_SN_ARG_ADD_SKIN))) {
                var player = this.getServer().getPlayer(args[1]);
                if (sender.isOp() && (player != null)) {
                    this.addPlayerSkin(player, args[2]);
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_ADD_SKIN), "").replace("$player$", player.getName()).replace("$skin$", args[2]));
                } else {
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_ADD_SKIN_FAIL), ""));
                }
                return true;
            } else if ((args.length == 3) && (args[0].equals(CMD_SN_ARG_ADD_TAG))) {
                var player = this.getServer().getPlayer(args[1]);
                if (sender.isOp() && (player != null)) {
                    this.addPlayerTag(player, args[2]);
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_ADD_TAG), "").replace("$player$", player.getName()).replace("$tag$", args[2]));
                } else {
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_ADD_TAG_FAIL), ""));
                }
                return true;
            } else if ((args.length == 3) && (args[0].equals(CMD_SN_ARG_ADD_STYLE))) {
                var player = this.getServer().getPlayer(args[1]);
                if (sender.isOp() && (player != null)) {
                    this.addPlayerStyle(player, args[2]);
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_ADD_STYLE), "").replace("$player$", player.getName()).replace("$style$", args[2]));
                } else {
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_ADD_STYLE_FAIL), ""));
                }
                return true;
            } else if ((args.length == 3) && (args[0].equals(CMD_SN_ARG_SET_SKIN))) {
                var player = this.getServer().getPlayer(args[1]);
                if ((sender.isOp() || sender.equals(player)) && (player != null) && this.setPlayerSkin(player, args[2])) {
                    this.updatePlayerDisplay(player);
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_SET_SKIN), "").replace("$player$", player.getName()).replace("$skin$", args[2]));
                } else {
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_SET_SKIN_FAIL), ""));
                }
                return true;
            } else if ((args.length == 3) && (args[0].equals(CMD_SN_ARG_SET_TAG))) {
                var player = this.getServer().getPlayer(args[1]);
                if ((sender.isOp() || sender.equals(player)) && (player != null) && this.setPlayerTag(player, args[2])) {
                    this.updatePlayerDisplay(player);
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_SET_TAG), "").replace("$player$", player.getName()).replace("$tag$", args[2]));
                } else {
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_SET_TAG_FAIL), ""));
                }
                return true;
            } else if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_SET_TAG_VISIBLE))) {
                var player = this.getServer().getPlayer(args[1]);
                if ((sender.isOp() || sender.equals(player)) && (player != null)) {
                    this.setPlayerTagVisibility(player, true);
                    this.updatePlayerDisplay(player);
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_SET_TAG_VISIBLE), "").replace("$player$", player.getName()));
                } else {
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_SET_TAG_VISIBILITY_FAIL), ""));
                }
                return true;
            } else if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_SET_TAG_INVISIBLE))) {
                var player = this.getServer().getPlayer(args[1]);
                if ((sender.isOp() || sender.equals(player)) && (player != null)) {
                    this.setPlayerTagVisibility(player, false);
                    this.updatePlayerDisplay(player);
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_SET_TAG_INVISIBLE), "").replace("$player$", player.getName()));
                } else {
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_SET_TAG_VISIBILITY_FAIL), ""));
                }
                return true;
            } else if ((args.length == 3) && (args[0].equals(CMD_SN_ARG_SET_STYLE))) {
                var player = this.getServer().getPlayer(args[1]);
                if ((sender.isOp() || sender.equals(player)) && (player != null) && this.setPlayerStyle(player, args[2])) {
                    this.updatePlayerDisplay(player);
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_SET_STYLE), "").replace("$player$", player.getName()).replace("$style$", args[2]));
                } else {
                    sender.sendMessage(Objects.requireNonNullElse(config.getString(KEY_TEXT_SET_STYLE_FAIL), ""));
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals(CMD_SN)) {
            List<String> ret = new LinkedList<>();
            if ((args.length == 1) && sender.isOp() && !this.isFunctionEnabled()) ret.add(CMD_SN_ARG_ENABLE);
            if ((args.length == 1) && sender.isOp() && this.isFunctionEnabled()) ret.add(CMD_SN_ARG_DISABLE);
            if ((args.length == 1)) ret.add(CMD_SN_ARG_LIST);
            if ((args.length == 1) && sender.isOp()) ret.add(CMD_SN_ARG_SET_NAME);
            if ((args.length == 1) && sender.isOp()) ret.add(CMD_SN_ARG_ADD_SKIN);
            if ((args.length == 1) && sender.isOp()) ret.add(CMD_SN_ARG_ADD_TAG);
            if ((args.length == 1) && sender.isOp()) ret.add(CMD_SN_ARG_ADD_STYLE);
            if ((args.length == 1)) ret.add(CMD_SN_ARG_SET_SKIN);
            if ((args.length == 1)) ret.add(CMD_SN_ARG_SET_TAG);
            if ((args.length == 1)) ret.add(CMD_SN_ARG_SET_TAG_VISIBLE);
            if ((args.length == 1)) ret.add(CMD_SN_ARG_SET_TAG_INVISIBLE);
            if ((args.length == 1)) ret.add(CMD_SN_ARG_SET_STYLE);
            if ((args.length == 3) && sender.isOp() && (args[0].equals(CMD_SN_ARG_SET_NAME))) ret.add("<name>");
            if ((args.length == 3) && sender.isOp() && (args[0].equals(CMD_SN_ARG_ADD_SKIN))) ret.add("<skin>");
            if ((args.length == 3) && sender.isOp() && (args[0].equals(CMD_SN_ARG_ADD_TAG))) ret.add("<tag>");
            if ((args.length == 3) && sender.isOp() && (args[0].equals(CMD_SN_ARG_ADD_STYLE))) ret.add("<style>");
            if ((args.length == 3) && (args[0].equals(CMD_SN_ARG_SET_SKIN)) && (sender instanceof Player)) ret.addAll(this.getPlayerListSkin((Player) sender));
            if ((args.length == 3) && (args[0].equals(CMD_SN_ARG_SET_SKIN))) ret.add("<skin>");
            if ((args.length == 3) && (args[0].equals(CMD_SN_ARG_SET_TAG)) && (sender instanceof Player)) ret.addAll(this.getPlayerListTag((Player) sender));
            if ((args.length == 3) && (args[0].equals(CMD_SN_ARG_SET_TAG))) ret.add("<tag>");
            if ((args.length == 3) && (args[0].equals(CMD_SN_ARG_SET_STYLE)) && (sender instanceof Player)) ret.addAll(this.getPlayerListStyle((Player) sender));
            if ((args.length == 3) && (args[0].equals(CMD_SN_ARG_SET_STYLE))) ret.add("<style>");
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_LIST)) && (sender instanceof Player)) ret.add(sender.getName());
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_LIST)) && sender.isOp()) ret.add("<player>");
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_SET_NAME)) && (sender instanceof Player)) ret.add(sender.getName());
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_SET_NAME)) && sender.isOp()) ret.add("<player>");
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_ADD_SKIN)) && (sender instanceof Player)) ret.add(sender.getName());
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_ADD_SKIN)) && sender.isOp()) ret.add("<player>");
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_SET_SKIN)) && (sender instanceof Player)) ret.add(sender.getName());
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_SET_SKIN)) && sender.isOp()) ret.add("<player>");
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_ADD_TAG)) && (sender instanceof Player)) ret.add(sender.getName());
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_ADD_TAG)) && sender.isOp()) ret.add("<player>");
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_SET_TAG)) && (sender instanceof Player)) ret.add(sender.getName());
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_SET_TAG)) && sender.isOp()) ret.add("<player>");
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_ADD_STYLE)) && (sender instanceof Player)) ret.add(sender.getName());
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_ADD_STYLE)) && sender.isOp()) ret.add("<player>");
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_SET_STYLE)) && (sender instanceof Player)) ret.add(sender.getName());
            if ((args.length == 2) && (args[0].equals(CMD_SN_ARG_SET_STYLE)) && sender.isOp()) ret.add("<player>");
            return ret;
        } else {
            return null;
        }
    }

}
