package net.tarcadia.tribina.erod.stylename.util.data;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationOptions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class Configuration implements org.bukkit.configuration.Configuration {

    static class ToSave extends Thread{

        private final Configuration thisConfig;

        ToSave(Configuration thisConfig) {
            super();
            thisConfig.saving = true;
            this.thisConfig = thisConfig;
        }

        @Override
        public void run() {
            try {
                sleep(thisConfig.ttl);
            } catch (InterruptedException ignored) {}
            thisConfig.saving = false;
            thisConfig.save(thisConfig.file);
        }
    }

    private static final Map<File, Configuration> configs = new HashMap<>();

    private final File file;
    private final long ttl;
    private long timeFile;
    private long timeUpdate;

    private boolean saving = false;

    private org.bukkit.configuration.Configuration def;
    private YamlConfiguration configBuff;

    public synchronized static Configuration getConfiguration(@NotNull File file) {
        return Objects.requireNonNullElseGet(
                Configuration.configs.get(file),
                () -> {
                    var c = new Configuration(file);
                    Configuration.configs.put(file, c);
                    return c;
                }
        );
    }

    private Configuration(@NotNull File file, org.bukkit.configuration.Configuration def, long ttl) {
        this.file = file;
        this.ttl = ttl;
        this.def = def;

        this.instUpdate();
    }

    private Configuration(@NotNull File file, long ttl) {
        this.file = file;
        this.ttl = ttl;
        this.def = null;

        this.instUpdate();
    }

    private Configuration(@NotNull File file, org.bukkit.configuration.Configuration def) {
        this.file = file;
        this.ttl = 5000;
        this.def = def;

        this.instUpdate();
    }

    private Configuration(@NotNull File file) {
        this.file = file;
        this.ttl = 5000;
        this.def = null;

        this.instUpdate();
    }

    public synchronized void load(@NotNull File file) {
        this.timeFile = file.lastModified();
        this.configBuff = YamlConfiguration.loadConfiguration(file);
        if (def != null) {
            this.configBuff.setDefaults(def);
        }
    }

    public synchronized void save(@NotNull File file) {
        try {
            this.configBuff.save(file);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot save " + file, ex);
        }
        this.timeFile = file.lastModified();
    }

    public synchronized void tryUpdate() {
        if (System.currentTimeMillis() - this.timeUpdate > this.ttl) {
            if (this.file.lastModified() > this.timeFile) {
                this.load(this.file);
            }
        }
        this.timeUpdate = System.currentTimeMillis();
    }

    public synchronized void instUpdate() {
        this.load(this.file);
        this.timeUpdate = System.currentTimeMillis();
    }

    public synchronized void didUpdate() {
        if (!this.saving) {
            var toSave = new ToSave(this);
            toSave.start();
        }
    }

    public synchronized void addAllDefault() {
        for (var key : this.def.getKeys(true)) {
            this.tryUpdate();
            this.configBuff.set(key, this.def.get(key));
            this.didUpdate();
        }
    }

    /**
     * Gets a set containing all keys in this section.
     * <p>
     * If deep is set to true, then this will contain all the keys within any
     * child {@link ConfigurationSection}s (and their children, etc). These
     * will be in a valid path notation for you to use.
     * <p>
     * If deep is set to false, then this will contain only the keys of any
     * direct children, and not their own children.
     *
     * @param deep Whether or not to get a deep list, as opposed to a shallow
     *             list.
     * @return Set of keys contained within this ConfigurationSection.
     */
    @Override
    @NotNull
    public synchronized Set<String> getKeys(boolean deep) {
        this.tryUpdate();
        return this.configBuff.getKeys(deep);
    }

    /**
     * Gets a Map containing all keys and their values for this section.
     * <p>
     * If deep is set to true, then this will contain all the keys and values
     * within any child {@link ConfigurationSection}s (and their children,
     * etc). These keys will be in a valid path notation for you to use.
     * <p>
     * If deep is set to false, then this will contain only the keys and
     * values of any direct children, and not their own children.
     *
     * @param deep Whether or not to get a deep list, as opposed to a shallow
     *             list.
     * @return Map of keys and values of this section.
     */
    @Override
    @NotNull
    public synchronized Map<String, Object> getValues(boolean deep) {
        this.tryUpdate();
        return this.configBuff.getValues(deep);
    }

    /**
     * Checks if this {@link ConfigurationSection} contains the given path.
     * <p>
     * If the value for the requested path does not exist but a default value
     * has been specified, this will return true.
     *
     * @param path Path to check for existence.
     * @return True if this section contains the requested path, either via
     * default or being set.
     * @throws IllegalArgumentException Thrown when path is null.
     */
    @Override
    public synchronized boolean contains(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.contains(path);
    }

    /**
     * Checks if this {@link ConfigurationSection} contains the given path.
     * <p>
     * If the value for the requested path does not exist, the boolean parameter
     * of true has been specified, a default value for the path exists, this
     * will return true.
     * <p>
     * If a boolean parameter of false has been specified, true will only be
     * returned if there is a set value for the specified path.
     *
     * @param path          Path to check for existence.
     * @param ignoreDefault Whether or not to ignore if a default value for the
     *                      specified path exists.
     * @return True if this section contains the requested path, or if a default
     * value exist and the boolean parameter for this method is true.
     * @throws IllegalArgumentException Thrown when path is null.
     */
    @Override
    public synchronized boolean contains(@NotNull String path, boolean ignoreDefault) {
        this.tryUpdate();
        return this.configBuff.contains(path, ignoreDefault);
    }

    /**
     * Checks if this {@link ConfigurationSection} has a value set for the
     * given path.
     * <p>
     * If the value for the requested path does not exist but a default value
     * has been specified, this will still return false.
     *
     * @param path Path to check for existence.
     * @return True if this section contains the requested path, regardless of
     * having a default.
     * @throws IllegalArgumentException Thrown when path is null.
     */
    @Override
    public synchronized boolean isSet(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.isSet(path);
    }

    /**
     * Gets the path of this {@link ConfigurationSection} from its root {@link
     * org.bukkit.configuration.Configuration}
     * <p>
     * For any {@link org.bukkit.configuration.Configuration} themselves, this will return an empty
     * string.
     * <p>
     * If the section is no longer contained within its root for any reason,
     * such as being replaced with a different value, this may return null.
     * <p>
     * To retrieve the single name of this section, that is, the final part of
     * the path returned by this method, you may use {@link #getName()}.
     *
     * @return Path of this section relative to its root
     */
    @Override
    public synchronized String getCurrentPath() {
        this.tryUpdate();
        return this.configBuff.getCurrentPath();
    }

    /**
     * Gets the name of this individual {@link ConfigurationSection}, in the
     * path.
     * <p>
     * This will always be the final part of {@link #getCurrentPath()}, unless
     * the section is orphaned.
     *
     * @return Name of this section
     */
    @Override
    @NotNull
    public synchronized String getName() {
        this.tryUpdate();
        return this.configBuff.getName();
    }

    /**
     * Gets the root {@link org.bukkit.configuration.Configuration} that contains this {@link
     * ConfigurationSection}
     * <p>
     * For any {@link org.bukkit.configuration.Configuration} themselves, this will return its own
     * object.
     * <p>
     * If the section is no longer contained within its root for any reason,
     * such as being replaced with a different value, this may return null.
     *
     * @return Root configuration containing this section.
     */
    @Override
    public synchronized org.bukkit.configuration.Configuration getRoot() {
        this.tryUpdate();
        return this.configBuff.getRoot();
    }

    /**
     * Gets the parent {@link ConfigurationSection} that directly contains
     * this {@link ConfigurationSection}.
     * <p>
     * For any {@link org.bukkit.configuration.Configuration} themselves, this will return null.
     * <p>
     * If the section is no longer contained within its parent for any reason,
     * such as being replaced with a different value, this may return null.
     *
     * @return Parent section containing this section.
     */
    @Override
    public synchronized ConfigurationSection getParent() {
        this.tryUpdate();
        return this.configBuff.getParent();
    }

    /**
     * Gets the requested Object by path.
     * <p>
     * If the Object does not exist but a default value has been specified,
     * this will return the default value. If the Object does not exist and no
     * default value was specified, this will return null.
     *
     * @param path Path of the Object to get.
     * @return Requested Object.
     */
    @Override
    public synchronized Object get(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.get(path);
    }

    /**
     * Gets the requested Object by path, returning a default value if not
     * found.
     * <p>
     * If the Object does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link org.bukkit.configuration.Configuration}.
     *
     * @param path Path of the Object to get.
     * @param def  The default value to return if the path is not found.
     * @return Requested Object.
     */
    @Override
    public synchronized Object get(@NotNull String path, Object def) {
        this.tryUpdate();
        return this.configBuff.get(path, def);
    }

    /**
     * Sets the specified path to the given value.
     * <p>
     * If value is null, the entry will be removed. Any existing entry will be
     * replaced, regardless of what the new value is.
     * <p>
     * Some implementations may have limitations on what you may store. See
     * their individual javadocs for details. No implementations should allow
     * you to store {@link org.bukkit.configuration.Configuration}s or {@link ConfigurationSection}s,
     * please use {@link #createSection(String)} for that.
     *
     * @param path  Path of the object to set.
     * @param value New value to set the path to.
     */
    @Override
    public synchronized void set(@NotNull String path, Object value) {
        this.tryUpdate();
        this.configBuff.set(path, value);
        this.didUpdate();
    }

    /**
     * Creates an empty {@link ConfigurationSection} at the specified path.
     * <p>
     * Any value that was previously set at this path will be overwritten. If
     * the previous value was itself a {@link ConfigurationSection}, it will
     * be orphaned.
     *
     * @param path Path to create the section at.
     * @return Newly created section
     */
    @Override
    @NotNull
    public synchronized ConfigurationSection createSection(@NotNull String path) {
        this.tryUpdate();
        var ret = this.configBuff.createSection(path);
        this.didUpdate();
        return ret;
    }

    /**
     * Creates a {@link ConfigurationSection} at the specified path, with
     * specified values.
     * <p>
     * Any value that was previously set at this path will be overwritten. If
     * the previous value was itself a {@link ConfigurationSection}, it will
     * be orphaned.
     *
     * @param path Path to create the section at.
     * @param map  The values to used.
     * @return Newly created section
     */
    @Override
    @NotNull
    public synchronized ConfigurationSection createSection(@NotNull String path, @NotNull Map<?, ?> map) {
        this.tryUpdate();
        var ret = this.configBuff.createSection(path, map);
        this.didUpdate();
        return ret;
    }

    /**
     * Gets the requested String by path.
     * <p>
     * If the String does not exist but a default value has been specified,
     * this will return the default value. If the String does not exist and no
     * default value was specified, this will return null.
     *
     * @param path Path of the String to get.
     * @return Requested String.
     */
    @Override
    public synchronized String getString(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getString(path);
    }

    /**
     * Gets the requested String by path, returning a default value if not
     * found.
     * <p>
     * If the String does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link org.bukkit.configuration.Configuration}.
     *
     * @param path Path of the String to get.
     * @param def  The default value to return if the path is not found or is
     *             not a String.
     * @return Requested String.
     */
    @Override
    public synchronized String getString(@NotNull String path, String def) {
        this.tryUpdate();
        return this.configBuff.getString(path, def);
    }

    /**
     * Checks if the specified path is a String.
     * <p>
     * If the path exists but is not a String, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a String and return appropriately.
     *
     * @param path Path of the String to check.
     * @return Whether or not the specified path is a String.
     */
    @Override
    public synchronized boolean isString(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.isString(path);
    }

    /**
     * Gets the requested int by path.
     * <p>
     * If the int does not exist but a default value has been specified, this
     * will return the default value. If the int does not exist and no default
     * value was specified, this will return 0.
     *
     * @param path Path of the int to get.
     * @return Requested int.
     */
    @Override
    public synchronized int getInt(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getInt(path);
    }

    /**
     * Gets the requested int by path, returning a default value if not found.
     * <p>
     * If the int does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link org.bukkit.configuration.Configuration}.
     *
     * @param path Path of the int to get.
     * @param def  The default value to return if the path is not found or is
     *             not an int.
     * @return Requested int.
     */
    @Override
    public synchronized int getInt(@NotNull String path, int def) {
        this.tryUpdate();
        return this.configBuff.getInt(path, def);
    }

    /**
     * Checks if the specified path is an int.
     * <p>
     * If the path exists but is not a int, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a int and return appropriately.
     *
     * @param path Path of the int to check.
     * @return Whether or not the specified path is an int.
     */
    @Override
    public synchronized boolean isInt(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.isInt(path);
    }

    /**
     * Gets the requested boolean by path.
     * <p>
     * If the boolean does not exist but a default value has been specified,
     * this will return the default value. If the boolean does not exist and
     * no default value was specified, this will return false.
     *
     * @param path Path of the boolean to get.
     * @return Requested boolean.
     */
    @Override
    public synchronized boolean getBoolean(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getBoolean(path);
    }

    /**
     * Gets the requested boolean by path, returning a default value if not
     * found.
     * <p>
     * If the boolean does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link org.bukkit.configuration.Configuration}.
     *
     * @param path Path of the boolean to get.
     * @param def  The default value to return if the path is not found or is
     *             not a boolean.
     * @return Requested boolean.
     */
    @Override
    public synchronized boolean getBoolean(@NotNull String path, boolean def) {
        this.tryUpdate();
        return this.configBuff.getBoolean(path);
    }

    /**
     * Checks if the specified path is a boolean.
     * <p>
     * If the path exists but is not a boolean, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a boolean and return appropriately.
     *
     * @param path Path of the boolean to check.
     * @return Whether or not the specified path is a boolean.
     */
    @Override
    public synchronized boolean isBoolean(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.isBoolean(path);
    }

    /**
     * Gets the requested double by path.
     * <p>
     * If the double does not exist but a default value has been specified,
     * this will return the default value. If the double does not exist and no
     * default value was specified, this will return 0.
     *
     * @param path Path of the double to get.
     * @return Requested double.
     */
    @Override
    public synchronized double getDouble(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getDouble(path);
    }

    /**
     * Gets the requested double by path, returning a default value if not
     * found.
     * <p>
     * If the double does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link org.bukkit.configuration.Configuration}.
     *
     * @param path Path of the double to get.
     * @param def  The default value to return if the path is not found or is
     *             not a double.
     * @return Requested double.
     */
    @Override
    public synchronized double getDouble(@NotNull String path, double def) {
        this.tryUpdate();
        return this.configBuff.getDouble(path, def);
    }

    /**
     * Checks if the specified path is a double.
     * <p>
     * If the path exists but is not a double, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a double and return appropriately.
     *
     * @param path Path of the double to check.
     * @return Whether or not the specified path is a double.
     */
    @Override
    public boolean isDouble(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.isDouble(path);
    }

    /**
     * Gets the requested long by path.
     * <p>
     * If the long does not exist but a default value has been specified, this
     * will return the default value. If the long does not exist and no
     * default value was specified, this will return 0.
     *
     * @param path Path of the long to get.
     * @return Requested long.
     */
    @Override
    public synchronized long getLong(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getLong(path);
    }

    /**
     * Gets the requested long by path, returning a default value if not
     * found.
     * <p>
     * If the long does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link org.bukkit.configuration.Configuration}.
     *
     * @param path Path of the long to get.
     * @param def  The default value to return if the path is not found or is
     *             not a long.
     * @return Requested long.
     */
    @Override
    public synchronized long getLong(@NotNull String path, long def) {
        this.tryUpdate();
        return this.configBuff.getLong(path, def);
    }

    /**
     * Checks if the specified path is a long.
     * <p>
     * If the path exists but is not a long, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a long and return appropriately.
     *
     * @param path Path of the long to check.
     * @return Whether or not the specified path is a long.
     */
    @Override
    public synchronized boolean isLong(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.isLong(path);
    }

    /**
     * Gets the requested List by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return null.
     *
     * @param path Path of the List to get.
     * @return Requested List.
     */
    @Override
    public synchronized List<?> getList(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getList(path);
    }

    /**
     * Gets the requested List by path, returning a default value if not
     * found.
     * <p>
     * If the List does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link org.bukkit.configuration.Configuration}.
     *
     * @param path Path of the List to get.
     * @param def  The default value to return if the path is not found or is
     *             not a List.
     * @return Requested List.
     */
    @Override
    public synchronized List<?> getList(@NotNull String path, List<?> def) {
        this.tryUpdate();
        return this.configBuff.getList(path, def);
    }

    /**
     * Checks if the specified path is a List.
     * <p>
     * If the path exists but is not a List, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a List and return appropriately.
     *
     * @param path Path of the List to check.
     * @return Whether or not the specified path is a List.
     */
    @Override
    public synchronized boolean isList(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.isList(path);
    }

    /**
     * Gets the requested List of String by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a String if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of String.
     */
    @Override
    @NotNull
    public synchronized List<String> getStringList(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getStringList(path);
    }

    /**
     * Append to the requested List by path.
     * <p>
     * If the List does not exist and no value is attached, this will create and
     * append to a new empty list. If the List does not exist but a value is
     * attached, this will turn the value into a list.
     *
     * @param path Path of the List to get.
     */
    public synchronized void addStringList(@NotNull String path, @NotNull String value) {
        this.tryUpdate();
        var lst = this.configBuff.getStringList(path);
        var val = this.configBuff.getString(path);
        List<String> added;
        if (!lst.isEmpty() && !lst.contains(value)) {
            added = lst;
            added.add(value);
        } else if (!lst.isEmpty() && lst.contains(value)) {
            added = lst;
        } else if ((val != null) && !Objects.equals(val, value)) {
            added = List.of(val, value);
        } else {
            added = List.of(value);
        }
        this.configBuff.set(path, added);
        this.didUpdate();
    }

    /**
     * Remove the value from the requested List by path.
     * <p>
     * If the List does not exist, this will do nothing.
     *
     * @param path Path of the List to get.
     */
    public synchronized void subStringList(@NotNull String path, @NotNull String value) {
        this.tryUpdate();
        var lst = this.configBuff.getStringList(path);
        lst.remove(value);
        this.configBuff.set(path, lst);
        this.didUpdate();
    }

    /**
     * Gets the requested List of Integer by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Integer if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Integer.
     */
    @Override
    @NotNull
    public synchronized List<Integer> getIntegerList(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getIntegerList(path);
    }

    /**
     * Append to the requested List by path.
     * <p>
     * If the List does not exist and no value is attached, this will create and
     * append to a new empty list. If the List does not exist but a value is
     * attached, this will turn the value into a list.
     *
     * @param path Path of the List to get.
     */
    public synchronized void addIntegerList(@NotNull String path, int value) {
        this.tryUpdate();
        var lst = this.configBuff.getIntegerList(path);
        var val = this.configBuff.getInt(path);
        List<Integer> added;
        if (!lst.isEmpty() && !lst.contains(value)) {
            added = lst;
            added.add(value);
        } else if (!Objects.equals(val, value)) {
            added = List.of(val, value);
        } else {
            added = List.of(value);
        }
        this.configBuff.set(path, added);
        this.didUpdate();
    }

    /**
     * Gets the requested List of Boolean by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Boolean if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Boolean.
     */
    @Override
    @NotNull
    public synchronized List<Boolean> getBooleanList(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getBooleanList(path);
    }

    /**
     * Append to the requested List by path.
     * <p>
     * If the List does not exist and no value is attached, this will create and
     * append to a new empty list. If the List does not exist but a value is
     * attached, this will turn the value into a list.
     *
     * @param path Path of the List to get.
     */
    public synchronized void addBooleanList(@NotNull String path, boolean value) {
        this.tryUpdate();
        var lst = this.configBuff.getBooleanList(path);
        var val = this.configBuff.getBoolean(path);
        List<Boolean> added;
        if (!lst.isEmpty() && !lst.contains(value)) {
            added = lst;
            added.add(value);
        } else if (!Objects.equals(val, value)) {
            added = List.of(val, value);
        } else {
            added = List.of(value);
        }
        this.configBuff.set(path, added);
        this.didUpdate();
    }

    /**
     * Gets the requested List of Double by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Double if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Double.
     */
    @Override
    @NotNull
    public synchronized List<Double> getDoubleList(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getDoubleList(path);
    }

    /**
     * Append to the requested List by path.
     * <p>
     * If the List does not exist and no value is attached, this will create and
     * append to a new empty list. If the List does not exist but a value is
     * attached, this will turn the value into a list.
     *
     * @param path Path of the List to get.
     */
    public synchronized void addDoubleList(@NotNull String path, double value) {
        this.tryUpdate();
        var lst = this.configBuff.getDoubleList(path);
        var val = this.configBuff.getDouble(path);
        List<Double> added;
        if (!lst.isEmpty() && !lst.contains(value)) {
            added = lst;
            added.add(value);
        } else if (!Objects.equals(val, value)) {
            added = List.of(val, value);
        } else {
            added = List.of(value);
        }
        this.configBuff.set(path, added);
        this.didUpdate();
    }

    /**
     * Gets the requested List of Float by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Float if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Float.
     */
    @Override
    @NotNull
    public synchronized List<Float> getFloatList(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getFloatList(path);
    }

    /**
     * Append to the requested List by path.
     * <p>
     * If the List does not exist and no value is attached, this will create and
     * append to a new empty list. If the List does not exist but a value is
     * attached, this will turn the value into a list.
     *
     * @param path Path of the List to get.
     */
    public synchronized void addFloatList(@NotNull String path, float value) {
        this.tryUpdate();
        var lst = this.configBuff.getFloatList(path);
        var val = this.configBuff.getDouble(path);
        List<Float> added;
        if (!lst.isEmpty() && !lst.contains(value)) {
            added = lst;
            added.add(value);
        } else if (!Objects.equals(val, value)) {
            added = List.of((float) val, value);
        } else {
            added = List.of(value);
        }
        this.configBuff.set(path, added);
        this.didUpdate();
    }

    /**
     * Gets the requested List of Long by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Long if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Long.
     */
    @Override
    @NotNull
    public synchronized List<Long> getLongList(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getLongList(path);
    }

    /**
     * Append to the requested List by path.
     * <p>
     * If the List does not exist and no value is attached, this will create and
     * append to a new empty list. If the List does not exist but a value is
     * attached, this will turn the value into a list.
     *
     * @param path Path of the List to get.
     */
    public synchronized void addLongList(@NotNull String path, long value) {
        this.tryUpdate();
        var lst = this.configBuff.getLongList(path);
        var val = this.configBuff.getLong(path);
        List<Long> added;
        if (!lst.isEmpty() && !lst.contains(value)) {
            added = lst;
            added.add(value);
        } else if (!Objects.equals(val, value)) {
            added = List.of(val, value);
        } else {
            added = List.of(value);
        }
        this.configBuff.set(path, added);
        this.didUpdate();
    }

    /**
     * Gets the requested List of Byte by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Byte if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Byte.
     */
    @Override
    @NotNull
    public synchronized List<Byte> getByteList(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getByteList(path);
    }

    /**
     * Append to the requested List by path.
     * <p>
     * If the List does not exist and no value is attached, this will create and
     * append to a new empty list. If the List does not exist but a value is
     * attached, this will turn the value into a list.
     *
     * @param path Path of the List to get.
     */
    public synchronized void addByteList(@NotNull String path, byte value) {
        this.tryUpdate();
        var lst = this.configBuff.getByteList(path);
        var val = this.configBuff.getInt(path);
        List<Byte> added;
        if (!lst.isEmpty() && !lst.contains(value)) {
            added = lst;
            added.add(value);
        } else if (!Objects.equals(val, value)) {
            added = List.of((byte) val, value);
        } else {
            added = List.of(value);
        }
        this.configBuff.set(path, added);
        this.didUpdate();
    }

    /**
     * Gets the requested List of Character by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Character if
     * possible, but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Character.
     */
    @Override
    @NotNull
    public synchronized List<Character> getCharacterList(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getCharacterList(path);
    }

    /**
     * Append to the requested List by path.
     * <p>
     * If the List does not exist and no value is attached, this will create and
     * append to a new empty list. If the List does not exist but a value is
     * attached, this will turn the value into a list.
     *
     * @param path Path of the List to get.
     */
    public synchronized void addCharacterList(@NotNull String path, char value) {
        this.tryUpdate();
        var lst = this.configBuff.getCharacterList(path);
        var val = this.configBuff.getInt(path);
        List<Character> added;
        if (!lst.isEmpty() && !lst.contains(value)) {
            added = lst;
            added.add(value);
        } else if (!Objects.equals(val, value)) {
            added = List.of((char) val, value);
        } else {
            added = List.of(value);
        }
        this.configBuff.set(path, added);
        this.didUpdate();
    }

    /**
     * Gets the requested List of Short by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Short if possible,
     * but may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Short.
     */
    @Override
    @NotNull
    public synchronized List<Short> getShortList(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getShortList(path);
    }

    /**
     * Append to the requested List by path.
     * <p>
     * If the List does not exist and no value is attached, this will create and
     * append to a new empty list. If the List does not exist but a value is
     * attached, this will turn the value into a list.
     *
     * @param path Path of the List to get.
     */
    public synchronized void addShortList(@NotNull String path, short value) {
        this.tryUpdate();
        var lst = this.configBuff.getShortList(path);
        var val = this.configBuff.getInt(path);
        List<Short> added;
        if (!lst.isEmpty() && !lst.contains(value)) {
            added = lst;
            added.add(value);
        } else if (!Objects.equals(val, value)) {
            added = List.of((short) val, value);
        } else {
            added = List.of(value);
        }
        this.configBuff.set(path, added);
        this.didUpdate();
    }

    /**
     * Gets the requested List of Maps by path.
     * <p>
     * If the List does not exist but a default value has been specified, this
     * will return the default value. If the List does not exist and no
     * default value was specified, this will return an empty List.
     * <p>
     * This method will attempt to cast any values into a Map if possible, but
     * may miss any values out if they are not compatible.
     *
     * @param path Path of the List to get.
     * @return Requested List of Maps.
     */
    @Override
    @NotNull
    public synchronized List<Map<?, ?>> getMapList(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getMapList(path);
    }

    /**
     * Gets the requested object at the given path.
     * <p>
     * If the Object does not exist but a default value has been specified, this
     * will return the default value. If the Object does not exist and no
     * default value was specified, this will return null.
     *
     * <b>Note:</b> For example #getObject(path, String.class) is <b>not</b>
     * equivalent to {@link #getString(String) #getString(path)} because
     * {@link #getString(String) #getString(path)} converts internally all
     * Objects to Strings. However, #getObject(path, Boolean.class) is
     * equivalent to {@link #getBoolean(String) #getBoolean(path)} for example.
     *
     * @param path  the path to the object.
     * @param clazz the type of the requested object
     * @return Requested object
     */
    @Override
    public synchronized <T> T getObject(@NotNull String path, @NotNull Class<T> clazz) {
        this.tryUpdate();
        return this.configBuff.getObject(path, clazz);
    }

    /**
     * Gets the requested object at the given path, returning a default value if
     * not found
     * <p>
     * If the Object does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link org.bukkit.configuration.Configuration}.
     *
     * <b>Note:</b> For example #getObject(path, String.class, def) is
     * <b>not</b> equivalent to
     * {@link #getString(String, String) #getString(path, def)} because
     * {@link #getString(String, String) #getString(path, def)} converts
     * internally all Objects to Strings. However, #getObject(path,
     * Boolean.class, def) is equivalent to {@link #getBoolean(String, boolean) #getBoolean(path,
     * def)} for example.
     *
     * @param path  the path to the object.
     * @param clazz the type of the requested object
     * @param def   the default object to return if the object is not present at
     *              the path
     * @return Requested object
     */
    @Override
    public synchronized <T> T getObject(@NotNull String path, @NotNull Class<T> clazz, T def) {
        this.tryUpdate();
        return this.configBuff.getObject(path, clazz, def);
    }

    /**
     * Gets the requested {@link ConfigurationSerializable} object at the given
     * path.
     * <p>
     * If the Object does not exist but a default value has been specified, this
     * will return the default value. If the Object does not exist and no
     * default value was specified, this will return null.
     *
     * @param path  the path to the object.
     * @param clazz the type of {@link ConfigurationSerializable}
     * @return Requested {@link ConfigurationSerializable} object
     */
    @Override
    public synchronized <T extends ConfigurationSerializable> T getSerializable(@NotNull String path, @NotNull Class<T> clazz) {
        this.tryUpdate();
        return this.configBuff.getSerializable(path, clazz);
    }

    /**
     * Gets the requested {@link ConfigurationSerializable} object at the given
     * path, returning a default value if not found
     * <p>
     * If the Object does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link org.bukkit.configuration.Configuration}.
     *
     * @param path  the path to the object.
     * @param clazz the type of {@link ConfigurationSerializable}
     * @param def   the default object to return if the object is not present at
     *              the path
     * @return Requested {@link ConfigurationSerializable} object
     */
    @Override
    public synchronized <T extends ConfigurationSerializable> T getSerializable(@NotNull String path, @NotNull Class<T> clazz, T def) {
        this.tryUpdate();
        return this.configBuff.getSerializable(path, clazz, def);
    }

    /**
     * Gets the requested Vector by path.
     * <p>
     * If the Vector does not exist but a default value has been specified,
     * this will return the default value. If the Vector does not exist and no
     * default value was specified, this will return null.
     *
     * @param path Path of the Vector to get.
     * @return Requested Vector.
     */
    @Override
    public synchronized Vector getVector(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getVector(path);
    }

    /**
     * Gets the requested {@link Vector} by path, returning a default value if
     * not found.
     * <p>
     * If the Vector does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link org.bukkit.configuration.Configuration}.
     *
     * @param path Path of the Vector to get.
     * @param def  The default value to return if the path is not found or is
     *             not a Vector.
     * @return Requested Vector.
     */
    @Override
    public synchronized Vector getVector(@NotNull String path, Vector def) {
        this.tryUpdate();
        return this.configBuff.getVector(path, def);
    }

    /**
     * Checks if the specified path is a Vector.
     * <p>
     * If the path exists but is not a Vector, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a Vector and return appropriately.
     *
     * @param path Path of the Vector to check.
     * @return Whether or not the specified path is a Vector.
     */
    @Override
    public synchronized boolean isVector(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.isVector(path);
    }

    /**
     * Gets the requested OfflinePlayer by path.
     * <p>
     * If the OfflinePlayer does not exist but a default value has been
     * specified, this will return the default value. If the OfflinePlayer
     * does not exist and no default value was specified, this will return
     * null.
     *
     * @param path Path of the OfflinePlayer to get.
     * @return Requested OfflinePlayer.
     */
    @Override
    public synchronized OfflinePlayer getOfflinePlayer(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getOfflinePlayer(path);
    }

    /**
     * Gets the requested {@link OfflinePlayer} by path, returning a default
     * value if not found.
     * <p>
     * If the OfflinePlayer does not exist then the specified default value
     * will returned regardless of if a default has been identified in the
     * root {@link org.bukkit.configuration.Configuration}.
     *
     * @param path Path of the OfflinePlayer to get.
     * @param def  The default value to return if the path is not found or is
     *             not an OfflinePlayer.
     * @return Requested OfflinePlayer.
     */
    @Override
    public synchronized OfflinePlayer getOfflinePlayer(@NotNull String path, OfflinePlayer def) {
        this.tryUpdate();
        return this.configBuff.getOfflinePlayer(path, def);
    }

    /**
     * Checks if the specified path is an OfflinePlayer.
     * <p>
     * If the path exists but is not a OfflinePlayer, this will return false.
     * If the path does not exist, this will return false. If the path does
     * not exist but a default value has been specified, this will check if
     * that default value is a OfflinePlayer and return appropriately.
     *
     * @param path Path of the OfflinePlayer to check.
     * @return Whether or not the specified path is an OfflinePlayer.
     */
    @Override
    public synchronized boolean isOfflinePlayer(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.isOfflinePlayer(path);
    }

    /**
     * Gets the requested ItemStack by path.
     * <p>
     * If the ItemStack does not exist but a default value has been specified,
     * this will return the default value. If the ItemStack does not exist and
     * no default value was specified, this will return null.
     *
     * @param path Path of the ItemStack to get.
     * @return Requested ItemStack.
     */
    @Override
    public synchronized ItemStack getItemStack(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getItemStack(path);
    }

    /**
     * Gets the requested {@link ItemStack} by path, returning a default value
     * if not found.
     * <p>
     * If the ItemStack does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link org.bukkit.configuration.Configuration}.
     *
     * @param path Path of the ItemStack to get.
     * @param def  The default value to return if the path is not found or is
     *             not an ItemStack.
     * @return Requested ItemStack.
     */
    @Override
    public synchronized ItemStack getItemStack(@NotNull String path, ItemStack def) {
        this.tryUpdate();
        return this.configBuff.getItemStack(path, def);
    }

    /**
     * Checks if the specified path is an ItemStack.
     * <p>
     * If the path exists but is not a ItemStack, this will return false. If
     * the path does not exist, this will return false. If the path does not
     * exist but a default value has been specified, this will check if that
     * default value is a ItemStack and return appropriately.
     *
     * @param path Path of the ItemStack to check.
     * @return Whether or not the specified path is an ItemStack.
     */
    @Override
    public synchronized boolean isItemStack(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.isItemStack(path);
    }

    /**
     * Gets the requested Color by path.
     * <p>
     * If the Color does not exist but a default value has been specified,
     * this will return the default value. If the Color does not exist and no
     * default value was specified, this will return null.
     *
     * @param path Path of the Color to get.
     * @return Requested Color.
     */
    @Override
    public synchronized Color getColor(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getColor(path);
    }

    /**
     * Gets the requested {@link Color} by path, returning a default value if
     * not found.
     * <p>
     * If the Color does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link org.bukkit.configuration.Configuration}.
     *
     * @param path Path of the Color to get.
     * @param def  The default value to return if the path is not found or is
     *             not a Color.
     * @return Requested Color.
     */
    @Override
    public synchronized Color getColor(@NotNull String path, Color def) {
        this.tryUpdate();
        return this.configBuff.getColor(path, def);
    }

    /**
     * Checks if the specified path is a Color.
     * <p>
     * If the path exists but is not a Color, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a Color and return appropriately.
     *
     * @param path Path of the Color to check.
     * @return Whether or not the specified path is a Color.
     */
    @Override
    public synchronized boolean isColor(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.isColor(path);
    }

    /**
     * Gets the requested Location by path.
     * <p>
     * If the Location does not exist but a default value has been specified,
     * this will return the default value. If the Location does not exist and no
     * default value was specified, this will return null.
     *
     * @param path Path of the Location to get.
     * @return Requested Location.
     */
    @Override
    public synchronized Location getLocation(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getLocation(path);
    }

    /**
     * Gets the requested {@link Location} by path, returning a default value if
     * not found.
     * <p>
     * If the Location does not exist then the specified default value will
     * returned regardless of if a default has been identified in the root
     * {@link org.bukkit.configuration.Configuration}.
     *
     * @param path Path of the Location to get.
     * @param def  The default value to return if the path is not found or is not
     *             a Location.
     * @return Requested Location.
     */
    @Override
    public synchronized Location getLocation(@NotNull String path, Location def) {
        this.tryUpdate();
        return this.configBuff.getLocation(path, def);
    }

    /**
     * Checks if the specified path is a Location.
     * <p>
     * If the path exists but is not a Location, this will return false. If the
     * path does not exist, this will return false. If the path does not exist
     * but a default value has been specified, this will check if that default
     * value is a Location and return appropriately.
     *
     * @param path Path of the Location to check.
     * @return Whether or not the specified path is a Location.
     */
    @Override
    public synchronized boolean isLocation(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.isLocation(path);
    }

    /**
     * Gets the requested ConfigurationSection by path.
     * <p>
     * If the ConfigurationSection does not exist but a default value has been
     * specified, this will return the default value. If the
     * ConfigurationSection does not exist and no default value was specified,
     * this will return null.
     *
     * @param path Path of the ConfigurationSection to get.
     * @return Requested ConfigurationSection.
     */
    @Override
    public synchronized ConfigurationSection getConfigurationSection(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getConfigurationSection(path);
    }

    /**
     * Checks if the specified path is a ConfigurationSection.
     * <p>
     * If the path exists but is not a ConfigurationSection, this will return
     * false. If the path does not exist, this will return false. If the path
     * does not exist but a default value has been specified, this will check
     * if that default value is a ConfigurationSection and return
     * appropriately.
     *
     * @param path Path of the ConfigurationSection to check.
     * @return Whether or not the specified path is a ConfigurationSection.
     */
    @Override
    public synchronized boolean isConfigurationSection(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.isConfigurationSection(path);
    }

    /**
     * Gets the equivalent {@link ConfigurationSection} from the default
     * {@link org.bukkit.configuration.Configuration} defined in {@link #getRoot()}.
     * <p>
     * If the root contains no defaults, or the defaults doesn't contain a
     * value for this path, or the value at this path is not a {@link
     * ConfigurationSection} then this will return null.
     *
     * @return Equivalent section in root configuration
     */
    @Override
    public synchronized ConfigurationSection getDefaultSection() {
        this.tryUpdate();
        return this.configBuff.getDefaultSection();
    }

    /**
     * Sets the default value of the given path as provided.
     * <p>
     * If no source {@link org.bukkit.configuration.Configuration} was provided as a default
     * collection, then a new {@link MemoryConfiguration} will be created to
     * hold the new default value.
     * <p>
     * If value is null, the value will be removed from the default
     * Configuration source.
     *
     * @param path  Path of the value to set.
     * @param value Value to set the default to.
     * @throws IllegalArgumentException Thrown if path is null.
     */
    @Override
    public synchronized void addDefault(@NotNull String path, Object value) {
        this.configBuff.addDefault(path, value);
        this.def.set(path, value);
    }

    /**
     * Gets the requested comment list by path.
     * <p>
     * If no comments exist, an empty list will be returned. A null entry
     * represents an empty line and an empty String represents an empty comment
     * line.
     *
     * @param path Path of the comments to get.
     * @return A unmodifiable list of the requested comments, every entry
     * represents one line.
     */
    @Override
    @NotNull
    public synchronized List<String> getComments(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getComments(path);
    }

    /**
     * Gets the requested inline comment list by path.
     * <p>
     * If no comments exist, an empty list will be returned. A null entry
     * represents an empty line and an empty String represents an empty comment
     * line.
     *
     * @param path Path of the comments to get.
     * @return A unmodifiable list of the requested comments, every entry
     * represents one line.
     */
    @Override
    @NotNull
    public synchronized List<String> getInlineComments(@NotNull String path) {
        this.tryUpdate();
        return this.configBuff.getInlineComments(path);
    }

    /**
     * Sets the comment list at the specified path.
     * <p>
     * If value is null, the comments will be removed. A null entry is an empty
     * line and an empty String entry is an empty comment line. If the path does
     * not exist, no comments will be set. Any existing comments will be
     * replaced, regardless of what the new comments are.
     * <p>
     * Some implementations may have limitations on what persists. See their
     * individual javadocs for details.
     *
     * @param path     Path of the comments to set.
     * @param comments New comments to set at the path, every entry represents
     */
    @Override
    public synchronized void setComments(@NotNull String path, List<String> comments) {
        this.tryUpdate();
        this.configBuff.setComments(path, comments);
        this.didUpdate();
    }

    /**
     * Sets the inline comment list at the specified path.
     * <p>
     * If value is null, the comments will be removed. A null entry is an empty
     * line and an empty String entry is an empty comment line. If the path does
     * not exist, no comment will be set. Any existing comments will be
     * replaced, regardless of what the new comments are.
     * <p>
     * Some implementations may have limitations on what persists. See their
     * individual javadocs for details.
     *
     * @param path     Path of the comments to set.
     * @param comments New comments to set at the path, every entry represents
     */
    @Override
    public synchronized void setInlineComments(@NotNull String path, List<String> comments) {
        this.tryUpdate();
        this.configBuff.setInlineComments(path, comments);
        this.didUpdate();
    }

    /**
     * Sets the default values of the given paths as provided.
     * <p>
     * If no source {@link org.bukkit.configuration.Configuration} was provided as a default
     * collection, then a new {@link MemoryConfiguration} will be created to
     * hold the new default values.
     *
     * @param defaults A map of Path{@literal ->}Values to add to defaults.
     * @throws IllegalArgumentException Thrown if defaults is null.
     */
    @Override
    public synchronized void addDefaults(@NotNull Map<String, Object> defaults) {
        this.configBuff.addDefaults(defaults);
//        for (var kv : defaults.entrySet()) {
//            var key = kv.getKey();
//            var val = kv.getValue();
//            this.def.set(key, val);
//        }
    }

    /**
     * Sets the default values of the given paths as provided.
     * <p>
     * If no source {@link org.bukkit.configuration.Configuration} was provided as a default
     * collection, then a new {@link MemoryConfiguration} will be created to
     * hold the new default value.
     * <p>
     * This method will not hold a reference to the specified Configuration,
     * nor will it automatically update if that Configuration ever changes. If
     * you require this, you should set the default source with {@link
     * #setDefaults(org.bukkit.configuration.Configuration)}.
     *
     * @param defaults A configuration holding a list of defaults to copy.
     * @throws IllegalArgumentException Thrown if defaults is null or this.
     */
    @Override
    public synchronized void addDefaults(@NotNull org.bukkit.configuration.Configuration defaults) {
        this.configBuff.addDefaults(defaults);
//        for (var kv : defaults.getValues(true).entrySet()) {
//            var key = kv.getKey();
//            var val = kv.getValue();
//            this.def.set(key, val);
//        }
    }

    /**
     * Sets the source of all default values for this {@link org.bukkit.configuration.Configuration}.
     * <p>
     * If a previous source was set, or previous default values were defined,
     * then they will not be copied to the new source.
     *
     * @param defaults New source of default values for this configuration.
     * @throws IllegalArgumentException Thrown if defaults is null or this.
     */
    @Override
    public synchronized void setDefaults(@NotNull org.bukkit.configuration.Configuration defaults) {
        this.configBuff.setDefaults(defaults);
        this.def = defaults;
    }

    /**
     * Gets the source {@link org.bukkit.configuration.Configuration} for this configuration.
     * <p>
     * If no configuration source was set, but default values were added, then
     * a {@link MemoryConfiguration} will be returned. If no source was set
     * and no defaults were set, then this method will return null.
     *
     * @return Configuration source for default values, or null if none exist.
     */
    @Override
    public synchronized org.bukkit.configuration.Configuration getDefaults() {
        return this.def;
    }

    /**
     * Gets the {@link ConfigurationOptions} for this {@link org.bukkit.configuration.Configuration}.
     * <p>
     * All setters through this method are chainable.
     *
     * @return Options for this configuration
     */
    @Override
    @NotNull
    public synchronized ConfigurationOptions options() {
        this.tryUpdate();
        return this.configBuff.options();
    }
}
