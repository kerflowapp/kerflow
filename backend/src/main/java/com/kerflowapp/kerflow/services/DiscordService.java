package com.kerflowapp.kerflow.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kerflowapp.kerflow.BaseConfiguration.CommonsProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DiscordService {

    private static final String DISCORD_API = "https://discord.com/api/v10";
    private static final int GUILD_TEXT = 0;
    private static final int GUILD_CATEGORY = 4;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private static final int COLOR_GREEN = 0x2ECC71;

    private final CommonsProperties commonsProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private final Map<String, String> channelIds = new ConcurrentHashMap<>();
    private volatile boolean enabled;

    public DiscordService(CommonsProperties commonsProperties, ObjectMapper objectMapper) {
        this.commonsProperties = commonsProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    @PostConstruct
    public void init() {
        if (commonsProperties.discord() == null
            || commonsProperties.discord().botToken() == null
            || commonsProperties.discord().botToken().isBlank()) {
            LOGGER.warn("Discord bot token not configured, notifications disabled");
            this.enabled = false;
            return;
        }

        // Initialize channels in a virtual thread to avoid blocking app startup
        Thread.startVirtualThread(() -> {
            try {
                initChannels();
                this.enabled = true;
                LOGGER.info("Discord service initialized with channels: {}", channelIds.keySet());
            } catch (Exception e) {
                LOGGER.error("Failed to initialize Discord channels — notifications disabled", e);
                this.enabled = false;
            }
        });
    }

    // ── Channel initialization ──────────────────────────────────────────

    private String prefix() {
        String p = commonsProperties.discord().channelPrefix();
        return p != null ? p : "";
    }

    private void initChannels() {
        String guildId = commonsProperties.discord().guildId();
        JsonNode existingChannels = getGuildChannels(guildId);

        // Find or create category
        String categoryName = prefix() + "kerflow-notifications";
        String categoryId = findChannelId(existingChannels, categoryName, GUILD_CATEGORY);
        if (categoryId == null) {
            categoryId = createChannel(guildId, categoryName, GUILD_CATEGORY, null);
        }

        // Find or create text channels under the category
        for (String name : new String[]{"registrations"}) {
            String prefixedName = prefix() + name;
            String id = findChannelId(existingChannels, prefixedName, GUILD_TEXT);
            if (id == null) {
                id = createChannel(guildId, prefixedName, GUILD_TEXT, categoryId);
            }
            if (id != null) {
                channelIds.put(name, id);
            }
        }
    }

    private JsonNode getGuildChannels(String guildId) {
        try {
            HttpRequest request = authorizedRequest(DISCORD_API + "/guilds/" + guildId + "/channels")
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return objectMapper.readTree(response.body());
            }
            LOGGER.error("Failed to get guild channels: {} {}", response.statusCode(), response.body());
        } catch (Exception e) {
            LOGGER.error("Error fetching guild channels", e);
        }
        return objectMapper.createArrayNode();
    }

    private String findChannelId(JsonNode channels, String name, int type) {
        if (channels == null || !channels.isArray()) {
            return null;
        }
        for (JsonNode channel : channels) {
            if (name.equals(channel.path("name").asText())
                && channel.path("type").asInt() == type) {
                return channel.path("id").asText();
            }
        }
        return null;
    }

    private String createChannel(String guildId, String name, int type, String parentId) {
        try {
            ObjectNode body = objectMapper.createObjectNode()
                .put("name", name)
                .put("type", type);

            if (parentId != null) {
                body.put("parent_id", parentId);
            }

            HttpRequest request = authorizedRequest(DISCORD_API + "/guilds/" + guildId + "/channels")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201) {
                JsonNode created = objectMapper.readTree(response.body());
                String id = created.path("id").asText();
                LOGGER.info("Created Discord channel #{} (id={})", name, id);
                return id;
            }
            LOGGER.error("Failed to create Discord channel #{}: {} {}", name, response.statusCode(), response.body());
        } catch (Exception e) {
            LOGGER.error("Error creating Discord channel #{}", name, e);
        }
        return null;
    }

    // ── Public notification methods ─────────────────────────────────────

    public void sendRegistrationNotification(String title, String description) {
        sendEmbed("registrations", title, description, COLOR_GREEN);
    }

    // ── Internal ────────────────────────────────────────────────────────

    private void sendEmbed(String channelName, String title, String description, int color) {
        if (!enabled) {
            return;
        }

        String channelId = channelIds.get(channelName);
        if (channelId == null) {
            LOGGER.warn("Discord channel #{} not initialized, skipping notification", channelName);
            return;
        }

        try {
            ObjectNode embed = objectMapper.createObjectNode()
                .put("title", truncate(title, 256))
                .put("description", truncate(description, 4096))
                .put("color", color)
                .put("timestamp", Instant.now().toString());

            ArrayNode embeds = objectMapper.createArrayNode().add(embed);

            ObjectNode body = objectMapper.createObjectNode();
            body.set("embeds", embeds);

            HttpRequest request = authorizedRequest(DISCORD_API + "/channels/" + channelId + "/messages")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

            // Fire-and-forget: never block the calling thread
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        LOGGER.error("Failed to send Discord message to #{}: {} {}",
                            channelName, response.statusCode(), response.body());
                    }
                })
                .exceptionally(e -> {
                    LOGGER.error("Error sending Discord notification to #{}", channelName, e);
                    return null;
                });
        } catch (Exception e) {
            LOGGER.error("Error building Discord notification for #{}", channelName, e);
        }
    }

    private HttpRequest.Builder authorizedRequest(String url) {
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(REQUEST_TIMEOUT)
            .header("Authorization", "Bot " + commonsProperties.discord().botToken())
            .header("Content-Type", "application/json");
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() > maxLength ? value.substring(0, maxLength - 3) + "..." : value;
    }
}
