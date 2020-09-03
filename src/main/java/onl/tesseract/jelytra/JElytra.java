package onl.tesseract.jelytra;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public final class JElytra extends JavaPlugin implements Listener {
    static public final String chatFormat = ChatColor.DARK_PURPLE + "[" + ChatColor.LIGHT_PURPLE + "Course" + ChatColor.DARK_PURPLE + "] " + ChatColor.GRAY;
    static public JElytra instance;

    @Override
    public void onEnable()
    {
        // Plugin startup logic
        instance = this;
        Objects.requireNonNull(this.getCommand("race")).setExecutor(new RaceCommand());
        Objects.requireNonNull(this.getCommand("race")).setTabCompleter(new RaceCommand());
        Bukkit.getPluginManager().registerEvents(this, this);

        load();
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
        save();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event)
    {
        for (Race race : Race.races)
        {
            if (race.getState() == Race.State.CONSTRUCTION && event.getPlayer().hasPermission("jelytra.construction")) {
                for (int i = 0; i < race.checkpoints.size(); i++)
                {
                    if (race.checkpoints.get(i).isPassing(event.getPlayer()))
                    {
                        event.getPlayer().sendTitle("", ChatColor.LIGHT_PURPLE + "Checkpoint n°" + i, 0, 20, 0);
                        return;
                    }
                }
            }
            if (! (race.getState() == Race.State.OPEN))
                continue;

            if (race.getRunners().containsKey(event.getPlayer()))
            {
                Checkpoint cp = race.getNextCheckpoint(event.getPlayer());
                if (cp != null && cp.isPassing(event.getPlayer()))
                {
                    cp.pass(event.getPlayer(), true);
                    for (int i = cp.getIndex(); i < cp.getIndex() + 2; i++)
                    {
                        if (i < race.checkpoints.size())
                            race.checkpoints.get(i).show(event.getPlayer());
                        else
                        {
                            race.getEndpoint().show(event.getPlayer());
                            break;
                        }
                    }
                } else if (cp == null)
                {
                    if (race.getEndpoint().isPassing(event.getPlayer()))
                        race.finish(event.getPlayer());
                }
                break;
            }
        }
    }

    public void load()
    {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new File("plugins/JElytra/races.yml"));
        for (String raceName : yaml.getKeys(false))
        {
            System.out.println("[JElytra] Loading race " + raceName);
            Race race = new Race(raceName);
            ConfigurationSection section = yaml.getConfigurationSection(raceName);
            assert section != null;

            race.setSpawnPoint(section.getLocation("spawnpoint"));
            race.setStartPoint(section.getLocation("startpoint"));

            // Ranking
            if (section.contains("ranking"))
            {
                for (String uuidRaw : Objects.requireNonNull(section.getConfigurationSection("ranking")).getKeys(false))
                {
                    try {
                        UUID uuid = UUID.fromString(uuidRaw);
                        if (race.ranking.containsKey(uuid)) {
                            if (race.ranking.get(uuid).toMillis() > section.getLong("ranking." + uuidRaw))
                                race.ranking.put(uuid, Duration.ofMillis(section.getLong("ranking." + uuidRaw)));
                        }
                        else
                            race.ranking.put(uuid, Duration.ofMillis(section.getLong("ranking." + uuidRaw)));
                    }catch (IllegalArgumentException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            // Checkpoints
            if (section.contains("checkpoints"))
            {
                for (String nb : Objects.requireNonNull(section.getConfigurationSection("checkpoints")).getKeys(false))
                {
                    ConfigurationSection checkpoint = section.getConfigurationSection("checkpoints." + nb);
                    assert checkpoint != null;
                    Location center = checkpoint.getLocation("center");
                    int radius = checkpoint.getInt("radius");
                    race.addCheckpoint(center, radius);
                }
            }

            Location center = section.getLocation("endpoint.center");
            int radius = section.getInt("endpoint.radius");
            race.setEndPoint(center, radius);

            if (section.contains("state"))
                race.setState(Race.State.valueOf(section.getString("state")));
            else
                race.setState(Race.State.OPEN);

            // Display
            if (section.contains("stands"))
            {
                new BukkitRunnable() {
                    @Override
                    public void run()
                    {
                        race.rankingDisplay = new RankingDisplay(race);
                        for (UUID uuid : section.getStringList("stands").stream().map(UUID::fromString).collect(Collectors.toList())) {

                            race.rankingDisplay.stands.add((ArmorStand) center.getWorld().getEntity(uuid));
                        }
                    }
                }.runTaskLater(this, 40);
            }
        }
    }

    public void save()
    {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Race race : Race.races)
        {
            ConfigurationSection section = yaml.createSection(race.getName());
            section.set("state", race.getState().toString());
            section.set("spawnpoint", race.getSpawnPoint());
            section.set("startpoint", race.getStartPoint());

            ConfigurationSection ranking = section.createSection("ranking");
            for (UUID uuid : race.ranking.keySet())
            {
                ranking.set(uuid.toString(), race.ranking.get(uuid).toMillis());
            }

            ConfigurationSection checkpoints = section.createSection("checkpoints");
            for (int i = 0; i < race.checkpoints.size(); i ++)
            {
                ConfigurationSection cp = checkpoints.createSection(i + "");
                cp.set("center", race.checkpoints.get(i).getCenter());
                cp.set("radius", race.checkpoints.get(i).getRadius());
            }

            section.set("endpoint.center", race.getEndpoint().getCenter());
            section.set("endpoint.radius", race.getEndpoint().getRadius());

            // armor stands
            if (race.rankingDisplay != null)
            {
                section.set("stands", race.rankingDisplay.stands.stream().map(s -> s.getUniqueId().toString()).collect(Collectors.toList()));
            }
        }

        try {
            yaml.save(new File("plugins/JElytra/races.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
