package cl.drakescraft.bosses.boss.instances;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import cl.drakescraft.bosses.boss.OdysseyBoss;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Garou Cosmico no depende de comandos ni destruye terreno: lee la distancia del
 * objetivo, alterna posturas y aumenta su presion en cada fase.
 */
public final class GarouCosmicoBoss extends OdysseyBoss {

    private static final Particle.DustOptions COSMIC_DUST = new Particle.DustOptions(Color.fromRGB(122, 76, 255), 1.8F);
    private static final String COSMIC_WEAPON_TAG = "drakes_garou_cosmic_weapon";
    private final Random random = new Random();
    private final List<ItemDisplay> cosmicWeapons = new ArrayList<>();
    private int adaptationsUsed;
    private BukkitTask activeSequence;

    public GarouCosmicoBoss(LivingEntity entity) {
        super(entity, "garou_cosmico", "§5§lGarou Cósmico §7§l- §dCazador de Héroes", 4200.0D, BarColor.PURPLE, BarStyle.SEGMENTED_20);

        setAttribute(Attribute.ATTACK_DAMAGE, 42.0D);
        setAttribute(Attribute.ARMOR, 18.0D);
        setAttribute(Attribute.KNOCKBACK_RESISTANCE, 1.0D);
        setAttribute(Attribute.FOLLOW_RANGE, 72.0D);
        setAttribute(Attribute.MOVEMENT_SPEED, 0.42D);
        setAttribute(Attribute.SCALE, 1.18D);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 60 * 20, 0, false, false, false));
        equipCosmicAppearance();
    }

    @Override
    public void executeSkillsRotation() {
        if (entity.isDead() || activeSequence != null) return;
        Player target = findNearestPlayer(52.0D);
        if (target == null) return;

        double distance = entity.getLocation().distanceSquared(target.getLocation());
        if (currentPhase == 1) {
            if (distance > 144.0D) predictiveRush(target);
            else if (random.nextBoolean()) martialCounter(target);
            else hunterCombo(target);
            return;
        }
        if (currentPhase == 2) {
            switch (random.nextInt(5)) {
                case 0 -> predictiveRush(target);
                case 1 -> cosmicPressure(target.getLocation());
                case 2 -> stellarMirror(target);
                case 3 -> launchCosmicArsenal(target);
                default -> ascensionBreak(target);
            }
            return;
        }
        switch (random.nextInt(7)) {
            case 0 -> cosmicPressure(target.getLocation());
            case 1 -> stellarMirror(target);
            case 2 -> martialCounter(target);
            case 3 -> predictiveRush(target);
            case 4 -> hunterCombo(target);
            case 5 -> launchCosmicArsenal(target);
            default -> cosmicRadiation(target.getLocation());
        }
    }

    @Override
    protected void onPhaseChange(int phase) {
        super.onPhaseChange(phase);
        adaptToCombat(phase);
    }

    @Override
    public void tickAura() {
        super.tickAura();
        if (currentPhase < 2 || entity.isDead()) return;
        Location center = entity.getLocation().add(0, 1.15D, 0);
        entity.getWorld().spawnParticle(Particle.DUST, center, currentPhase * 6, 0.7D, 1.0D, 0.7D, 0.01D, COSMIC_DUST);
        if (currentPhase == 3) {
            entity.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 8, 0.65D, 1.0D, 0.65D, 0.03D);
        }
    }

    /** Pone presion al jugador lejano con un salto fisico, no con teletransporte inseguro. */
    private void predictiveRush(Player target) {
        Location origin = entity.getLocation();
        Vector direction = target.getVelocity().clone().multiply(7.0D)
                .add(target.getLocation().toVector().subtract(origin.toVector()));
        if (direction.lengthSquared() < 0.01D) return;
        entity.setVelocity(direction.normalize().multiply(1.35D + currentPhase * 0.15D).setY(0.32D));
        entity.getWorld().spawnParticle(Particle.DUST, origin.add(0, 1, 0), 28, 0.45D, 0.6D, 0.45D, 0.08D, COSMIC_DUST);
        entity.getWorld().playSound(origin, Sound.ENTITY_ENDERMAN_SCREAM, 0.8F, 0.55F);
        announceAttack("Caza predicativa");
    }

    /** Castiga el cuerpo a cuerpo sin bloquear al jugador ni alterar bloques. */
    private void martialCounter(Player target) {
        Location center = entity.getLocation().add(0, 1.0D, 0);
        announceAttack("Arte marcial cósmico");
        entity.getWorld().spawnParticle(Particle.SWEEP_ATTACK, center, 8, 1.7D, 0.4D, 1.7D, 0.0D);
        entity.getWorld().playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2F, 0.55F);
        for (Player player : findPlayersInRange(6.0D)) {
            Vector knockback = player.getLocation().toVector().subtract(entity.getLocation().toVector());
            if (knockback.lengthSquared() < 0.01D) knockback = new Vector(0.2D, 0.0D, 0.2D);
            player.setVelocity(knockback.normalize().multiply(1.15D).setY(0.38D));
            player.damage(scaleArenaDamage(14.0D + currentPhase * 4.0D), entity);
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 45, Math.max(0, currentPhase - 2), true, true, true));
        }
    }

    /** Deja una zona de daño breve y legible, sin fuego, lava ni cambios del mundo. */
    private void cosmicPressure(Location target) {
        Location center = target.clone().add(0, 0.15D, 0);
        announceAttack("Colapso de energía cósmica");
        for (int point = 0; point < 30; point++) {
            double angle = Math.PI * 2.0D * point / 30.0D;
            center.getWorld().spawnParticle(Particle.DUST, center.clone().add(Math.cos(angle) * 5.5D, 0.1D, Math.sin(angle) * 5.5D),
                    1, 0, 0, 0, 0, COSMIC_DUST);
        }
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 1.2F, 0.45F);
        for (Player player : combatantsNear(center, 5.5D)) {
            player.damage(scaleArenaDamage(18.0D + currentPhase * 3.0D), entity);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 50, 1, true, true, true));
        }
    }

    /** Replica una linea de ataque visible para impedir que el rango sea una zona segura. */
    private void stellarMirror(Player target) {
        Location start = entity.getLocation().add(0, 1.2D, 0);
        Vector path = target.getLocation().add(0, 1.0D, 0).toVector().subtract(start.toVector());
        if (path.lengthSquared() < 0.01D) return;
        Vector step = path.normalize().multiply(0.65D);
        announceAttack("Puño que imita las estrellas");
        for (double traveled = 0.0D; traveled < Math.min(34.0D, path.length()); traveled += 0.65D) {
            Location point = start.clone().add(step.clone().multiply(traveled / 0.65D));
            point.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, COSMIC_DUST);
            for (Player player : combatantsNear(point, 1.25D)) {
                player.damage(scaleArenaDamage(17.0D + currentPhase * 3.0D), entity);
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 50, 0, true, true, true));
            }
        }
        start.getWorld().playSound(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.75F, 1.45F);
    }

    /** Tres golpes guiados que siguen al objetivo, con pausas cortas para poder esquivarlos. */
    private void hunterCombo(Player target) {
        announceAttack("Combo de cazador de héroes");
        activeSequence = new BukkitRunnable() {
            private int hit;

            @Override
            public void run() {
                if (!isEncounterActive() || !target.isOnline() || target.isDead() || hit >= 3) {
                    finishSequence();
                    cancel();
                    return;
                }
                Location center = target.getLocation();
                center.getWorld().spawnParticle(Particle.SWEEP_ATTACK, center.add(0, 1.0D, 0), 5, 1.4D, 0.3D, 1.4D, 0.0D);
                center.getWorld().playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.1F, 0.65F + hit * 0.15F);
                for (Player player : combatantsNear(center, 4.5D)) {
                    player.damage(scaleArenaDamage(8.0D + currentPhase * 2.0D + hit * 2.0D), entity);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 18, 0, true, true, true));
                }
                hit++;
            }
        }.runTaskTimer(cl.drakescraft.bosses.DrakesBosses.getInstance(), 0L, 9L);
    }

    /** Lanza al objetivo y remata su posicion, pero concede caida lenta para que no muera por terreno. */
    private void ascensionBreak(Player target) {
        announceAttack("Ascensión y ruptura celestial");
        target.setFallDistance(0.0F);
        target.setVelocity(target.getVelocity().setY(1.15D));
        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 18, 1, true, true, true));
        target.getWorld().spawnParticle(Particle.REVERSE_PORTAL, target.getLocation().add(0, 1, 0), 40, 0.5D, 0.8D, 0.5D, 0.08D);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.9F, 0.65F);
        activeSequence = Bukkit.getScheduler().runTaskLater(cl.drakescraft.bosses.DrakesBosses.getInstance(), () -> {
            activeSequence = null;
            if (!isEncounterActive() || !target.isOnline() || target.isDead()) return;
            target.setFallDistance(0.0F);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, true, true, true));
            target.damage(scaleArenaDamage(22.0D + currentPhase * 3.0D), entity);
            target.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation(), 3, 0.4D, 0.4D, 0.4D, 0.0D);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.9F, 1.3F);
        }, 24L);
    }

    /** Daño por pulsos explícitos y limitados; una sola secuencia evita acumulaciones de tareas. */
    private void cosmicRadiation(Location target) {
        Location center = target.clone().add(0, 0.2D, 0);
        announceAttack("Radiación de la estrella caída");
        activeSequence = new BukkitRunnable() {
            private int pulse;

            @Override
            public void run() {
                if (!isEncounterActive() || pulse >= 4) {
                    finishSequence();
                    cancel();
                    return;
                }
                double radius = 5.0D + pulse;
                drawCosmicRing(center, radius);
                for (Player player : combatantsNear(center, radius)) {
                    player.damage(scaleArenaDamage(3.0D + currentPhase), entity);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 35, 0, true, true, true));
                }
                center.getWorld().playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.8F, 0.65F + pulse * 0.08F);
                pulse++;
            }
        }.runTaskTimer(cl.drakescraft.bosses.DrakesBosses.getInstance(), 0L, 20L);
    }

    /** Armas visuales orbitan al boss y despues salen hacia el objetivo una sola vez. */
    private void launchCosmicArsenal(Player target) {
        announceAttack("Arsenal que gira alrededor del cazador");
        clearCosmicWeapons();
        List<ItemStack> arsenal = List.of(
                new ItemStack(Material.NETHERITE_SWORD), new ItemStack(Material.MACE),
                new ItemStack(Material.TRIDENT), new ItemStack(Material.NETHERITE_AXE));
        Location center = entity.getLocation().add(0, 1.25D, 0);
        for (ItemStack weapon : arsenal) {
            ItemDisplay display = entity.getWorld().spawn(center, ItemDisplay.class);
            display.setItemStack(weapon);
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            display.setInvulnerable(true);
            display.setPersistent(false);
            display.addScoreboardTag(COSMIC_WEAPON_TAG);
            cosmicWeapons.add(display);
        }
        entity.getWorld().playSound(center, Sound.ITEM_TRIDENT_THROW, 1.1F, 0.7F);

        activeSequence = new BukkitRunnable() {
            private int tick;
            private final java.util.Set<UUID> hitTargets = new java.util.HashSet<>();

            @Override
            public void run() {
                if (!isEncounterActive() || !target.isOnline() || target.isDead() || tick >= 46) {
                    finishSequence();
                    cancel();
                    return;
                }
                if (tick < 24) {
                    orbitWeapons(tick);
                } else {
                    launchWeaponsAt(target, hitTargets);
                }
                tick++;
            }
        }.runTaskTimer(cl.drakescraft.bosses.DrakesBosses.getInstance(), 0L, 2L);
    }

    private void orbitWeapons(int tick) {
        Location center = entity.getLocation().add(0, 1.35D, 0);
        for (int index = 0; index < cosmicWeapons.size(); index++) {
            ItemDisplay display = cosmicWeapons.get(index);
            if (!display.isValid()) continue;
            double angle = tick * 0.34D + (Math.PI * 2.0D * index / cosmicWeapons.size());
            Location position = center.clone().add(Math.cos(angle) * 3.1D, Math.sin(angle * 2.0D) * 0.55D, Math.sin(angle) * 3.1D);
            display.teleport(position);
            display.setRotation((float) Math.toDegrees(-angle), (float) (Math.sin(angle) * 40.0D));
            position.getWorld().spawnParticle(Particle.DUST, position, 1, 0.02D, 0.02D, 0.02D, 0.0D, COSMIC_DUST);
        }
    }

    private void launchWeaponsAt(Player target, java.util.Set<UUID> hitTargets) {
        Location destination = target.getLocation().add(0, 1.0D, 0);
        for (ItemDisplay display : cosmicWeapons) {
            if (!display.isValid()) continue;
            Vector path = destination.toVector().subtract(display.getLocation().toVector());
            if (path.lengthSquared() > 0.01D) {
                display.teleport(display.getLocation().add(path.normalize().multiply(1.55D)));
            }
            Location impact = display.getLocation();
            impact.getWorld().spawnParticle(Particle.DUST, impact, 2, 0.08D, 0.08D, 0.08D, 0.01D, COSMIC_DUST);
            for (Player player : combatantsNear(impact, 1.15D)) {
                if (!hitTargets.add(player.getUniqueId())) continue;
                player.damage(scaleArenaDamage(19.0D + currentPhase * 3.0D), entity);
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0, true, true, true));
                player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1.0F, 0.7F);
            }
        }
    }

    private void drawCosmicRing(Location center, double radius) {
        for (int point = 0; point < 24; point++) {
            double angle = Math.PI * 2.0D * point / 24.0D;
            center.getWorld().spawnParticle(Particle.DUST, center.clone().add(Math.cos(angle) * radius, 0.1D, Math.sin(angle) * radius),
                    1, 0, 0, 0, 0, COSMIC_DUST);
        }
    }

    /** Equipa una silueta cosmica propia sin depender de perfiles web ni de texturas externas. */
    private void equipCosmicAppearance() {
        if (entity.getEquipment() == null) return;
        ItemStack helmet = new ItemStack(Material.WITHER_SKELETON_SKULL);
        ItemStack chestplate = cosmicLeather(Material.LEATHER_CHESTPLATE, Color.fromRGB(34, 10, 76));
        ItemStack leggings = cosmicLeather(Material.LEATHER_LEGGINGS, Color.fromRGB(58, 18, 128));
        ItemStack boots = cosmicLeather(Material.LEATHER_BOOTS, Color.fromRGB(18, 66, 122));
        entity.getEquipment().setHelmet(helmet);
        entity.getEquipment().setChestplate(chestplate);
        entity.getEquipment().setLeggings(leggings);
        entity.getEquipment().setBoots(boots);
        entity.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        entity.getEquipment().setHelmetDropChance(0.0F);
        entity.getEquipment().setChestplateDropChance(0.0F);
        entity.getEquipment().setLeggingsDropChance(0.0F);
        entity.getEquipment().setBootsDropChance(0.0F);
        entity.getEquipment().setItemInMainHandDropChance(0.0F);
    }

    private ItemStack cosmicLeather(Material material, Color color) {
        ItemStack armor = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            meta.setUnbreakable(true);
            armor.setItemMeta(meta);
        }
        return armor;
    }

    /** Excluye observadores y staff en creativo de ataques con centro movil. */
    private List<Player> combatantsNear(Location center, double radius) {
        double radiusSquared = radius * radius;
        return center.getWorld().getPlayers().stream()
                .filter(player -> player.isOnline() && !player.isDead())
                .filter(player -> player.getGameMode() != org.bukkit.GameMode.CREATIVE)
                .filter(player -> player.getGameMode() != org.bukkit.GameMode.SPECTATOR)
                .filter(player -> player.getLocation().distanceSquared(center) <= radiusSquared)
                .toList();
    }

    private boolean isEncounterActive() {
        return entity.isValid() && !entity.isDead();
    }

    private void finishSequence() {
        activeSequence = null;
        clearCosmicWeapons();
    }

    private void clearCosmicWeapons() {
        cosmicWeapons.removeIf(display -> {
            if (display.isValid()) display.remove();
            return true;
        });
    }

    @Override
    public void cleanup() {
        if (activeSequence != null) activeSequence.cancel();
        finishSequence();
        super.cleanup();
    }

    /** Una adaptación por fase: resistente y visual, no inmunidad repetitiva. */
    private void adaptToCombat(int phase) {
        if (adaptationsUsed >= phase) return;
        adaptationsUsed = phase;
        Location center = entity.getLocation().add(0, 1, 0);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, Math.min(4, phase + 1), false, false, false));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 10, phase, false, false, false));
        center.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center, 80, 1.2D, 1.7D, 1.2D, 0.08D);
        center.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 90, 1.0D, 1.4D, 1.0D, 0.05D);
        center.getWorld().playSound(center, Sound.ENTITY_WARDEN_EMERGE, 1.4F, 0.65F);
        speak("He observado suficiente. Ahora conozco vuestro ritmo.");
    }

    private void setAttribute(Attribute attribute, double value) {
        var instance = entity.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(value);
    }
}
