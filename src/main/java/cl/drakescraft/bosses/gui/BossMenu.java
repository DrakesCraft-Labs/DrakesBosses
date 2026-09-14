package cl.drakescraft.bosses.gui;

import cl.drakescraft.bosses.DrakesBosses;
import cl.drakescraft.bosses.boss.arena.BossArenaService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class BossMenu implements Listener, InventoryHolder {

    private static final String GUI_TITLE = ChatColor.translateAlternateColorCodes('&', "&8&l⚡ &6&lARENAS DE JEFES &8&l⚡");
    private final DrakesBosses plugin;
    private final BossArenaService arenaService;
    private final Inventory inventory;

    private static record BossEntry(
            String id,
            String displayName,
            Material icon,
            int slot,
            String difficulty,
            String health,
            String rewards,
            String description
    ) {}

    private static final List<BossEntry> BOSS_ENTRIES = List.of(
            // Tier 1: Inicial / Héroes
            new BossEntry("circe", "&d&lCirce la Hechicera", Material.BREWING_STAND, 1,
                    "&a★☆☆☆☆ &7(Fácil)", "1,200", "Pociones Arcanas, Oro, Slimefun", "Maestra de pociones y maldiciones arcanas."),
            new BossEntry("polifemo", "&2&lPolifemo el Cíclope", Material.ZOMBIE_HEAD, 3,
                    "&a★☆☆☆☆ &7(Fácil)", "1,500", "Carne Mítica, Minerales, Dragmas", "Gigante de un solo ojo con fuerza bruta aplastante."),
            new BossEntry("jax", "&7&lAjax / Jax", Material.IRON_BLOCK, 5,
                    "&a★☆☆☆☆ &7(Fácil)", "1,800", "Hierro Refinado, Núcleos, Exp", "Autómata de guerra impenetrable de la antigüedad."),
            new BossEntry("dios_corrupto", "&5&lDios Corrupto", Material.WITHER_ROSE, 7,
                    "&e★★☆☆☆ &7(Normal)", "2,500", "Esencias Oscuras, Polvo Cósmico", "Antigua deidad consumida por la corrupción abisal."),

            // Tier 2: Dioses Olímpicos
            new BossEntry("ares", "&c&lAres - Dios de la Guerra", Material.NETHERITE_SWORD, 10,
                    "&e★★☆☆☆ &7(Normal)", "3,500", "Espadas Legendarias, Lingotes, Dragmas", "Furia incansable y combate cuerpo a cuerpo feroz."),
            new BossEntry("poseidon", "&b&lPoseidón - Rey de los Mares", Material.TRIDENT, 12,
                    "&e★★☆☆☆ &7(Normal)", "4,000", "Tridentes Míticos, Corazón de Mar, XP", "Control absoluto sobre las mareas y tormentas marinas."),
            new BossEntry("loki", "&a&lLoki - Dios del Engaño", Material.ENDER_EYE, 14,
                    "&6★★★☆☆ &7(Desafiante)", "4,200", "Ilusiones, Esmeraldas, Runas", "Clones engañosos, teleportaciones y trucos mortales."),
            new BossEntry("zeus", "&e&lZeus - Rey del Olimpo", Material.LIGHTNING_ROD, 16,
                    "&6★★★☆☆ &7(Desafiante)", "5,000", "Rayos Divinos, Foco Arcano, Dragmas", "Señor de los cielos y los relámpagos devastadores."),

            // Tier 3: Mitología Nórdica y Bestias
            new BossEntry("hades", "&8&lHades - Señor del Inframundo", Material.SOUL_LANTERN, 19,
                    "&6★★★☆☆ &7(Desafiante)", "4,800", "Huesos Titánicos, Almas, Slimefun", "Comandante de las almas errantes del inframundo."),
            new BossEntry("thor", "&9&lThor - Dios del Trueno", Material.NETHERITE_AXE, 20,
                    "&6★★★☆☆ &7(Desafiante)", "5,500", "Mjölnir Shards, Hierro Puro, Rayos", "Poder atronador que sacude la tierra a su paso."),
            new BossEntry("heimdall", "&f&lHeimdall - Guardián de Bifröst", Material.SHIELD, 21,
                    "&6★★★☆☆ &7(Desafiante)", "5,200", "Escudos Divinos, Ojos de Vigía", "El centinela que todo lo ve y nada deja pasar."),
            new BossEntry("hidra", "&2&lHidra de Lerna", Material.POISONOUS_POTATO, 22,
                    "&c★★★★☆ &7(Difícil)", "6,000", "Veneno Puro, Escamas Legendarias", "Bestia policéfala cuyo veneno derrite armaduras."),
            new BossEntry("cerbero", "&4&lCerbero - Guardián Abisal", Material.FIRE_CHARGE, 23,
                    "&c★★★★☆ &7(Difícil)", "6,200", "Fuego Infernal, Piel de Magma", "El can de tres cabezas que escupe fuego abisal."),
            new BossEntry("artemisa", "&a&lArtemisa - Diosa de la Caza", Material.BOW, 24,
                    "&c★★★★☆ &7(Difícil)", "5,800", "Flechas Lunares, Arcos Sagrados", "Puntería milimétrica y flechas de luz celestial."),
            new BossEntry("prometeo", "&6&lPrometeo - Portador del Fuego", Material.BLAZE_ROD, 25,
                    "&c★★★★☆ &7(Difícil)", "6,500", "Llama Primordial, Ceniza Cósmica", "El titán rebelde que desafió a los dioses con fuego."),

            // Tier 4: Dioses Egipcios y Míticos Extremos
            new BossEntry("ra", "&e&lRa - Dios del Sol", Material.SUNFLOWER, 28,
                    "&c★★★★☆ &7(Mítico)", "7,500", "Oro Solar, Destello Divino, Dragmas", "El disco solar que calcina cualquier armadura impura."),
            new BossEntry("isis", "&d&lIsis - Reina de la Magia", Material.FEATHER, 29,
                    "&c★★★★☆ &7(Mítico)", "7,000", "Velo de la Vida, Magia Sagrada", "Hechicera suprema con artes de resurrección y curación."),
            new BossEntry("anubis", "&8&lAnubis - Juez de las Almas", Material.BONE, 30,
                    "&c★★★★☆ &7(Mítico)", "7,500", "Balanza de Osiris, Necromancia", "Pesa tu corazón contra la pluma de la verdad."),
            new BossEntry("set", "&6&lSet - Señor del Caos", Material.REDSTONE_BLOCK, 31,
                    "&c★★★★☆ &7(Mítico)", "7,800", "Arena Sangrienta, Fragmento del Caos", "Dios de las tormentas de arena y la traición."),
            new BossEntry("odin", "&9&lOdín - El Padre de Todos", Material.SPECTRAL_ARROW, 32,
                    "&4★★★★★ &7(Extremo)", "8,500", "Lanza Gungnir, Runas de Sabiduría", "El rey de Asgard, amo del conocimiento y la batalla."),
            new BossEntry("kratos", "&c&lKratos - El Fantasma de Esparta", Material.NETHERITE_INGOT, 33,
                    "&4★★★★★ &7(Extremo)", "9,000", "Hojas del Caos, Furia Espartana", "El asesino de dioses consumido por la venganza."),
            new BossEntry("coloso_end", "&5&lColoso del End", Material.END_STONE, 34,
                    "&4★★★★★ &7(Extremo)", "8,000", "Piedras del Vacío, Perlas Resonantes", "Gigante pétreo imbuido con la energía del Fin."),

            // Tier 5: Cataclismos y Final Bosses
            new BossEntry("tifon", "&4&lTifón - Padre de los Monstruos", Material.MAGMA_BLOCK, 38,
                    "&4★★★★★ &7(Cataclismo)", "12,000", "Lava Cósmica, Escamas de Tifón", "La tempestad más mortífera creada contra el Olimpo."),
            new BossEntry("dragon", "&5&lDragón Ancestral", Material.DRAGON_HEAD, 40,
                    "&5★★★★★ &7(Legendario)", "15,000", "Aliento Puro, Huevos de Dragón, SF", "El primer dragón que moldeó las dimensiones vacías."),
            new BossEntry("wither_storm", "&0&lWither Storm", Material.NETHER_STAR, 42,
                    "&5★★★★★ &7(Apocalipsis)", "25,000", "Estrellas del Vacío, Rayo Tractor", "La masa devoradora de mundos con rayo de tracción."),
            new BossEntry("garou", "&1&lGarou Cósmico", Material.BEACON, 44,
                    "&5★★★★★ &7(Dios Supremo)", "35,000", "Modo Despertar, Esencia Cósmica", "El terror absoluto que copia cualquier arte marcial.")
    );

    public BossMenu(DrakesBosses plugin, BossArenaService arenaService) {
        this.plugin = plugin;
        this.arenaService = arenaService;
        this.inventory = Bukkit.createInventory(this, 54, GUI_TITLE);
        populateInventory();
    }

    public void open(Player player) {
        player.openInventory(this.inventory);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    private void populateInventory() {
        // Marco de cristal
        ItemStack borderGlass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        ItemStack darkGlass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, borderGlass);
        }

        for (BossEntry entry : BOSS_ENTRIES) {
            double fee = arenaService.entryFee(entry.id());
            String formattedFee = String.format(Locale.ROOT, "%,.0f", fee);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&8» " + entry.description()));
            lore.add("");
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Dificultad: " + entry.difficulty()));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&c❤ Vida Estimada: &f" + entry.health() + " HP"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&e✦ Recompensas: &f" + entry.rewards()));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&6💰 Costo Entrada: &e" + formattedFee + " Dragmas"));
            lore.add("");
            lore.add(ChatColor.translateAlternateColorCodes('&', "&a▶ Clic Izquierdo: &fDesafiar en Solitario"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&e▶ Clic Derecho: &fVer info de Grupo"));

            ItemStack item = createItem(entry.icon(), ChatColor.translateAlternateColorCodes('&', entry.displayName()), lore);
            inventory.setItem(entry.slot(), item);
        }

        // Barra inferior
        ItemStack infoItem = createItem(Material.BOOK, ChatColor.translateAlternateColorCodes('&', "&6&lℹ INFORMACIÓN DE LAS ARENAS"), List.of(
                ChatColor.translateAlternateColorCodes('&', "&7Las arenas de jefes son dimensiones aisladas"),
                ChatColor.translateAlternateColorCodes('&', "&7y protegidas dentro de &fboss_arena&7."),
                "",
                ChatColor.translateAlternateColorCodes('&', "&f• No hay riesgo de interferencia ni pérdidas por grief."),
                ChatColor.translateAlternateColorCodes('&', "&f• Al vencer al jefe, las recompensas caen al suelo."),
                ChatColor.translateAlternateColorCodes('&', "&f• Si mueres, reapareces de forma segura."),
                ChatColor.translateAlternateColorCodes('&', "&f• En grupo: &e/bosswarp <jefe> grupo <amigo1> <amigo2>")
        ));
        inventory.setItem(48, infoItem);

        ItemStack closeItem = createItem(Material.BARRIER, ChatColor.translateAlternateColorCodes('&', "&c&lCerrar Menú"), List.of(
                ChatColor.translateAlternateColorCodes('&', "&7Haz clic para salir.")
        ));
        inventory.setItem(49, closeItem);

        ItemStack spectateItem = createItem(Material.SPYGLASS, ChatColor.translateAlternateColorCodes('&', "&b&lModo Espectador"), List.of(
                ChatColor.translateAlternateColorCodes('&', "&7¿Quieres ver la batalla de un amigo?"),
                "",
                ChatColor.translateAlternateColorCodes('&', "&eUsa: &f/bosswarp spectate <jugador>")
        ));
        inventory.setItem(50, spectateItem);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material mat, String name) {
        return createItem(mat, name, null);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BossMenu)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        if (slot == 49) {
            player.closeInventory();
            return;
        }

        for (BossEntry entry : BOSS_ENTRIES) {
            if (entry.slot() == slot) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);

                if (event.isRightClick()) {
                    player.closeInventory();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&6[BossArena] &ePara desafiar a " + entry.displayName() + " &een grupo, usa:"));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&f/bosswarp " + entry.id() + " grupo <jugador1> <jugador2>..."));
                    return;
                }

                // Clic izquierdo: Solitario
                player.closeInventory();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&6[BossArena] &eIniciando arena solitaria contra " + entry.displayName() + "&e..."));

                var result = arenaService.start(entry.id(), Set.of(player), false);
                if (!result.started()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&c[BossArena] " + result.error()));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                } else {
                    if (result.feePerPlayer() > 0.0D) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                "&6[BossArena] &eEntrada cobrada: &6" + String.format(Locale.ROOT, "%,.0f", result.feePerPlayer())
                                        + " Dragmas&e."));
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                }
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof BossMenu) {
            event.setCancelled(true);
        }
    }
}
