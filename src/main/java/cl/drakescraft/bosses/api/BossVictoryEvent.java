package cl.drakescraft.bosses.api;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Immutable result published after a boss has been defeated and rewards are resolved. */
public final class BossVictoryEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID bossInstanceId;
    private final String bossId;
    private final Location location;
    private final Map<UUID, Double> contributions;
    private final Instant defeatedAt;

    public BossVictoryEvent(UUID bossInstanceId, String bossId, Location location,
                            Map<UUID, Double> contributions, Instant defeatedAt) {
        this.bossInstanceId = bossInstanceId;
        this.bossId = bossId;
        this.location = location.clone();
        this.contributions = Map.copyOf(new LinkedHashMap<>(contributions));
        this.defeatedAt = defeatedAt;
    }

    public UUID getBossInstanceId() { return bossInstanceId; }
    public String getBossId() { return bossId; }
    public Location getLocation() { return location.clone(); }
    public Map<UUID, Double> getContributions() { return contributions; }
    public Instant getDefeatedAt() { return defeatedAt; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
