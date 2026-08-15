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
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Husk;
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
    private static final String COSMIC_AFTERIMAGE_TAG = "drakes_garou_cosmic_afterimage";
    private static final int TOTAL_COSMIC_LIVES = 4;
    private final Random random = new Random();
    private final List<ItemDisplay> cosmicWeapons = new ArrayList<>();
    private final List<Husk> cosmicAfterimages = new ArrayList<>();
    private int adaptationsUsed;
    private int rebirthsUsed;
    private long rebirthInvulnerableUntil;
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
        int intensity = combatIntensity();
        if (intensity == 1) {
            if (distance > 144.0D) predictiveRush(target);
            else if (random.nextBoolean()) martialCounter(target);
            else hunterCombo(target);
            return;
        }
        if (intensity == 2) {
            switch (random.nextInt(5)) {
                case 0 -> predictiveRush(target);
                case 1 -> cosmicPressure(target.getLocation());
                case 2 -> stellarMirror(target);
                case 3 -> launchCosmicArsenal(target);
                default -> ascensionBreak(target);
            }
            return;
        }
        switch (random.nextInt(intensity >= 4 ? 8 : 7)) {
            case 0 -> cosmicPressure(target.getLocation());
            case 1 -> stellarMirror(target);
            case 2 -> martialCounter(target);
            case 3 -> predictiveRush(target);
            case 4 -> hunterCombo(target);
            case 5 -> launchCosmicArsenal(target);
            case 6 -> cosmicRadiation(target.getLocation());
            default -> {
                if (intensity >= 4 && random.nextBoolean()) cosmicSingularity(target);
                else residualAssault(target);
            }
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

    /** Gives Garou three cinematic second winds; the fourth defeat remains final. */
    public boolean beginCosmicRebirth() {
        if (rebirthsUsed >= TOTAL_COSMIC_LIVES - 1 || entity.isDead()) {
            return false;
        }

        rebirthsUsed++;
        long invulnerabilitySeconds = 4L;
        rebirthInvulnerableUntil = System.currentTimeMillis() + invulnerabilitySeconds * 1_000L;
        double restoredHealth = switch (rebirthsUsed) {
            case 1 -> 0.80D;
            case 2 -> 0.68D;
            default -> 0.58D;
        };
        entity.setHealth(maxHealth * restoredHealth);
        setAttribute(Attribute.ATTACK_DAMAGE, 42.0D + rebirthsUsed * 10.0D);
        setAttribute(Attribute.MOVEMENT_SPEED, 0.42D + rebirthsUsed * 0.04D);
        entity.setGlowing(true);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, (int) invulnerabilitySeconds * 20, 10, false, false, false));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 60 * 10, 2 + rebirthsUsed, false, false, false));

        Location center = entity.getLocation().add(0, 1.0D, 0);
        center.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center, 180, 1.4D, 2.0D, 1.4D, 0.12D);
        center.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 140, 1.2D, 1.6D, 1.2D, 0.06D);
        center.getWorld().playSound(center, Sound.ENTITY_WARDEN_EMERGE, 1.6F, 0.55F + rebirthsUsed * 0.08F);
        for (Player player : combatantsNear(center, 48.0D)) {
            player.sendTitle("§5§lFASE " + (rebirthsUsed + 1), "§dGarou se adapta y renace.", 8, 50, 14);
            player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0F, 0.65F);
        }
        Bukkit.getScheduler().runTaskLater(cl.drakescraft.bosses.DrakesBosses.getInstance(), () -> {
            if (entity.isValid() && !entity.isDead()) {
                entity.setGlowing(false);
            }
        }, invulnerabilitySeconds * 20L);
        return true;
    }

    /** The damage listener uses this instead of Bukkit invulnerability for every damage source. */
    public boolean isCosmicRebirthInvulnerable() {
        return System.currentTimeMillis() < rebirthInvulnerableUntil;
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

    /**
     * La fase final convierte el suelo de la arena en un espectaculo de colapso, sin modificar
     * bloques reales. Las particulas de bloque y la oscuridad cuentan la historia; el golpe final
     * esta anunciado y da tiempo para abandonar el nucleo.
     */
    private void cosmicSingularity(Player target) {
        Location center = target.getLocation().clone().add(0.0D, 0.2D, 0.0D);
        announceAttack("Singularidad final: el vacío devora la arena");
        for (Player player : combatantsNear(center, 42.0D)) {
            player.sendTitle("§5§lSINGULARIDAD", "§dAlejate del nucleo antes del colapso.", 8, 42, 10);
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 54, 0, true, true, true));
        }
        activeSequence = new BukkitRunnable() {
            private int tick;
            private final BlockData floorFragment = Material.DEEPSLATE_TILES.createBlockData();

            @Override
            public void run() {
                if (!isEncounterActive() || tick >= 30) {
                    finishSequence();
                    cancel();
                    return;
                }
                double radius = Math.max(1.1D, 11.5D - tick * 0.34D);
                center.getWorld().spawnParticle(Particle.PORTAL, center.clone().add(0.0D, 1.1D, 0.0D), 16,
                        radius * 0.18D, 0.55D, radius * 0.18D, 0.22D);
                center.getWorld().spawnParticle(Particle.DUST, center.clone().add(0.0D, 0.5D, 0.0D), 12,
                        radius * 0.15D, 0.2D, radius * 0.15D, 0.0D, COSMIC_DUST);
                // Fragmentos puramente de cliente: nunca se cambia el bloque real de la arena.
                center.getWorld().spawnParticle(Particle.BLOCK, center.clone().add(0.0D, 0.05D, 0.0D), 8,
                        radius * 0.42D, 0.04D, radius * 0.42D, 0.18D, floorFragment);
                if (tick % 6 == 0) {
                    center.getWorld().playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.9F,
                            0.45F + tick * 0.015F);
                }
                if (tick == 24) {
                    center.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 1, 0, 0, 0, 0);
                    center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.35F, 0.55F);
                    for (Player player : combatantsNear(center, 5.25D)) {
                        player.damage(scaleArenaDamage(30.0D + combatIntensity() * 5.0D), entity);
                        Vector pull = center.toVector().subtract(player.getLocation().toVector());
                        if (pull.lengthSquared() > 0.01D) {
                            player.setVelocity(pull.normalize().multiply(0.75D).setY(0.24D));
                        }
                    }
                }
                tick++;
            }
        }.runTaskTimer(cl.drakescraft.bosses.DrakesBosses.getInstance(), 0L, 2L);
    }

    /** Creates temporary visual doubles that strike together without becoming extra bosses. */
    private void residualAssault(Player target) {
        announceAttack("Imágenes residuales: no existe un único Garou");
        clearCosmicAfterimages();
        Location center = target.getLocation().clone();
        int imageCount = Math.min(4, 1 + combatIntensity());
        for (int index = 0; index < imageCount; index++) {
            double angle = Math.PI * 2.0D * index / imageCount;
            Location imageLocation = center.clone().add(Math.cos(angle) * 4.2D, 0.0D, Math.sin(angle) * 4.2D);
            Husk image = entity.getWorld().spawn(imageLocation, Husk.class);
            // Bedrock renders real entities consistently; marker ArmorStands are unreliable there.
            image.setAI(false);
            image.setBaby(false);
            image.setSilent(true);
            image.setInvulnerable(true);
            image.setCollidable(false);
            image.setPersistent(false);
            image.setRemoveWhenFarAway(true);
            image.addScoreboardTag(COSMIC_AFTERIMAGE_TAG);
            if (image.getEquipment() != null) {
                image.getEquipment().setHelmet(new ItemStack(Material.WITHER_SKELETON_SKULL));
                image.getEquipment().setChestplate(cosmicLeather(Material.LEATHER_CHESTPLATE, Color.fromRGB(48, 12, 100)));
                image.getEquipment().setLeggings(cosmicLeather(Material.LEATHER_LEGGINGS, Color.fromRGB(78, 22, 148)));
                image.getEquipment().setBoots(cosmicLeather(Material.LEATHER_BOOTS, Color.fromRGB(22, 74, 140)));
                image.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
            }
            cosmicAfterimages.add(image);
        }

        activeSequence = new BukkitRunnable() {
            private int tick;
            private final java.util.Map<UUID, Integer> hits = new java.util.HashMap<>();

            @Override
            public void run() {
                if (!isEncounterActive() || tick >= 18) {
                    finishSequence();
                    cancel();
                    return;
                }
                for (Husk image : cosmicAfterimages) {
                    if (!image.isValid()) continue;
                    Location position = image.getLocation().add(0, 1.0D, 0);
                    position.getWorld().spawnParticle(Particle.DUST, position, 4, 0.25D, 0.65D, 0.25D, 0.02D, COSMIC_DUST);
                    if (tick != 10) continue;
                    position.getWorld().spawnParticle(Particle.SWEEP_ATTACK, position, 4, 1.5D, 0.25D, 1.5D, 0.0D);
                    for (Player player : combatantsNear(position, 2.25D)) {
                        int received = hits.getOrDefault(player.getUniqueId(), 0);
                        if (received >= 2) continue;
                        hits.put(player.getUniqueId(), received + 1);
                        player.damage(scaleArenaDamage(5.0D + combatIntensity() * 2.0D), entity);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 0, true, true, true));
                    }
                }
                tick++;
            }
        }.runTaskTimer(cl.drakescraft.bosses.DrakesBosses.getInstance(), 0L, 2L);
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

    private int combatIntensity() {
        return Math.max(currentPhase, rebirthsUsed + 1);
    }

    private void finishSequence() {
        activeSequence = null;
        clearCosmicWeapons();
        clearCosmicAfterimages();
    }

    private void clearCosmicWeapons() {
        cosmicWeapons.removeIf(display -> {
            if (display.isValid()) display.remove();
            return true;
        });
    }

    private void clearCosmicAfterimages() {
        cosmicAfterimages.removeIf(image -> {
            if (image.isValid()) image.remove();
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
