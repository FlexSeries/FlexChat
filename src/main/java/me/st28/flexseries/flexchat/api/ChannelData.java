package me.st28.flexseries.flexchat.api;

import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.Map.Entry;

/**
 * Holds configuration data for a {@link me.st28.flexseries.flexchat.api.ConfigurableChannel}.
 */
public final class ChannelData {

    private YamlFileManager file;

    private String name;
    private String shortName;
    private ChatColor color;
    private String logFormat;

    /**
     * <b>Structure:</b> <code>group, format</code>
     */
    private final Map<String, String> formats = new HashMap<>();

    /**
     * A list of banned chatter identifiers.
     */
    private final List<String> banned = new ArrayList<>();

    private final Map<String, Object> options = new HashMap<>();

    ChannelData(YamlFileManager file) {
        Validate.notNull(file, "File cannot be null.");
        this.file = file;
        FileConfiguration config = file.getConfig();

        name = config.getString("name");
        Validate.notNull(name, "Name cannot be null.");

        Validate.isTrue(ChannelManager.CHANNEL_NAME_PATTERN.matcher(name).matches(), "Channel name must not consist of only numbers.");

        shortName = config.getString("short name");
        color = ChatColor.valueOf(config.getString("color", "WHITE"));

        logFormat = config.getString("log format");
        Validate.notNull(logFormat, "Log format cannot be null.");

        ConfigurationSection formatSec = config.getConfigurationSection("formats");
        if (formatSec != null) {
            for (String group : formatSec.getKeys(false)) {
                formats.put(group.toLowerCase(), formatSec.getString(group));
            }
        }

        if (!formats.containsKey("default")) {
            throw new IllegalArgumentException("No default format is defined.");
        }

        ConfigurationSection optionSec = config.getConfigurationSection("options");
        if (optionSec != null) {
            for (String option : optionSec.getKeys(false)) {
                options.put(option.toLowerCase(), optionSec.get(option));
            }
        }
    }

    public final void save() {
        FileConfiguration config = file.getConfig();

        config.set("banned", banned);

        ConfigurationSection optionSec = config.createSection("options");
        for (Entry<String, Object> entry : options.entrySet()) {
            optionSec.set(entry.getKey(), entry.getValue());
        }

        file.save();
    }

    /**
     * @return the display name of the channel.
     */
    public final String getName() {
        return name;
    }

    /**
     * @return the short identifier of the channel.
     */
    public final String getShortName() {
        return shortName;
    }

    /**
     * @return the default color for the channel.
     */
    public final ChatColor getColor() {
        return color;
    }

    /**
     * @return a read-only map of the chat formats for this channel.
     */
    public final Map<String, String> getFormats() {
        return Collections.unmodifiableMap(formats);
    }

    /**
     * @return the chat format for a specified group.
     */
    public final String getFormat(String group) {
        Validate.notNull(group, "Group cannot be null.");
        group = group.toLowerCase();
        return formats.containsKey(group) ? formats.get(group) : formats.get("default");
    }

    /**
     * @return the format to use for logging purposes.
     */
    public final String getLogFormat() {
        return logFormat;
    }

    /**
     * @return the options map.  Values should be ConfigurationSerializable.
     */
    public final Map<String, Object> getOptions() {
        return options;
    }

    public final <T> T getOption(String name, T defaultValue) {
        Validate.notNull(name, "Name cannot be null.");

        name = name.toLowerCase();

        return (T) (options.containsKey(name) ? options.get(name) : defaultValue);
    }

}