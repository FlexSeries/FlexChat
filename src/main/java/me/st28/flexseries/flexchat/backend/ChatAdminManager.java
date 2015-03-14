package me.st28.flexseries.flexchat.backend;

import me.st28.flexseries.flexchat.FlexChat;
import me.st28.flexseries.flexchat.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.events.PlayerJoinLoadedEvent;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import me.st28.flexseries.flexcore.utils.StringConverter;
import me.st28.flexseries.flexcore.utils.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatAdminManager extends FlexModule<FlexChat> implements Listener {

    public final static Pattern COMMAND_LABEL_PATTERN = Pattern.compile("(?i)^/([^ ]+)");

    private String spyOutput;
    private final List<String> spyCommands = new ArrayList<>();

    private Set<UUID> chatSpies = new HashSet<>();
    private YamlFileManager spyFile;

    public ChatAdminManager(FlexChat plugin) {
        super(plugin, "chat_admin", "Manages chat administration features", true);
    }

    @Override
    protected void handleLoad() throws Exception {
        spyFile = new YamlFileManager(getDataFolder() + File.separator + "chatSpies.yml");
        for (String rawUuid : spyFile.getConfig().getStringList("enabled")) {
            UUID uuid;
            try {
                uuid = UUID.fromString(rawUuid);
            } catch (Exception ex) {
                LogHelper.warning(this, "Invalid UUID in spyingPlayers.yml '" + rawUuid + "'");
                continue;
            }

            chatSpies.add(uuid);
        }
    }

    @Override
    protected void handleReload() {
        FileConfiguration config = getConfig();

        spyOutput = StringEscapeUtils.unescapeJava(config.getString("spy output", "&c[&7SPY&c] &7{SENDER}&7: &f{COMMAND}"));

        spyCommands.clear();
        spyCommands.addAll(StringUtils.collectionToStringList(config.getStringList("spy commands"), new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return object.toLowerCase();
            }
        }));
    }

    @Override
    protected void handleSave(boolean async) {
        spyFile.getConfig().set("enabled", StringUtils.collectionToStringList(chatSpies, new StringConverter<UUID>() {
            @Override
            public String toString(UUID object) {
                return object.toString();
            }
        }));

        spyFile.save();
    }

    public Collection<UUID> getChatSpies() {
        return Collections.unmodifiableCollection(chatSpies);
    }

    /**
     * Toggles a player's chat spy mode.
     *
     * @return True if chat spy was enabled for the player.<br />
     *         False if chat spy was disabled for the player.
     */
    public boolean toggleChatSpyMode(Player player) {
        Validate.notNull(player, "Player cannot be null.");
        UUID uuid = player.getUniqueId();
        if (chatSpies.remove(uuid)) {
            return false;
        } else {
            chatSpies.add(uuid);
            return true;
        }
    }

    private void sendSpyMessage(String sender, String message) {
        String sendMessage = ChatColor.translateAlternateColorCodes('&', spyOutput.replace("{SENDER}", sender)).replace("{COMMAND}", ChatColor.stripColor(message));

        for (UUID spy : chatSpies) {
            Player spyPlayer = Bukkit.getPlayer(spy);
            if (spyPlayer != null && !spyPlayer.getName().equalsIgnoreCase(sender)) {
                spyPlayer.sendMessage(sendMessage);
            }
        }
    }

    @EventHandler
    public void onPlayerJoinLoaded(PlayerJoinLoadedEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (chatSpies.contains(uuid) && !PermissionNodes.CHAT_SPY_PERSISTENT.isAllowed(e.getPlayer())) {
            chatSpies.remove(uuid);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent e) {
        String message = e.getMessage();
        Matcher matcher = COMMAND_LABEL_PATTERN.matcher(message);
        if (matcher.matches()) {
            if (spyCommands.contains(matcher.group(1).toLowerCase())) {
                sendSpyMessage(e.getPlayer().getName(), message);
            }
        }
    }

}