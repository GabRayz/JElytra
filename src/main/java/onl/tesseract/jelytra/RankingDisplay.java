package onl.tesseract.jelytra;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class RankingDisplay {
    Race race;
    Location origin;
    List<ArmorStand> stands = new ArrayList<>();
    BukkitTask task;

    public RankingDisplay(Race race, Location origin)
    {
        this.race = race;
        this.origin = origin;

        ArmorStand title = origin.getWorld().spawn(origin, ArmorStand.class);
        title.setVisible(false);
        title.setInvulnerable(true);
        title.setGravity(false);
        title.setCustomName(ChatColor.DARK_PURPLE + race.getName());
        title.setCustomNameVisible(true);
        stands.add(title);

        spawn();
    }

    public RankingDisplay(Race race)
    {
        this.race = race;

        this.task = new BukkitRunnable() {
            @Override
            public void run()
            {
                update();
            }
        }.runTaskTimer(JElytra.instance, 5*20, 5*20);
    }

    void spawn()
    {
        List<OfflinePlayer> ranking = race.getOrderedRanking();
        for (int i = 0; i < 10; i++)
        {
            ArmorStand s = origin.getWorld().spawn(origin.clone().subtract(0, (i + 1) * 0.25, 0), ArmorStand.class);
            s.setVisible(false);
            s.setInvulnerable(true);
            s.setGravity(false);
            if (i < ranking.size())
                s.setCustomName(ChatColor.GOLD + "" + (i + 1) + " " + ChatColor.LIGHT_PURPLE + ranking.get(i).getName() + " > " + ChatColor.GOLD + Util.durationToString(race.ranking.get(ranking.get(i).getUniqueId())));
            else
                s.setCustomName(ChatColor.GOLD + "" + (i + 1) + " " + ChatColor.GRAY + "--- > -h -m -s:---ms");
            s.setCustomNameVisible(true);
            stands.add(s);
        }

        this.task = new BukkitRunnable() {
            @Override
            public void run()
            {
                update();
            }
        }.runTaskTimer(JElytra.instance, 5*20, 5*20);
    }

    public void update()
    {
        List<OfflinePlayer> ranking = race.getOrderedRanking();
        for (int i = 0; i < 10; i++)
        {
            if (i < ranking.size())
                stands.get(i + 1).setCustomName(ChatColor.GOLD + "" + (i + 1) + " " + ChatColor.LIGHT_PURPLE + ranking.get(i).getName() + " > " + ChatColor.GOLD + Util.durationToString(race.ranking.get(ranking.get(i).getUniqueId())));
            else
                stands.get(i + 1).setCustomName(ChatColor.GOLD + "" + (i + 1) + " " + ChatColor.GRAY + "--- > -h -m -s:---ms");
        }
    }

    public void move(Location origin)
    {
        for (int i = 0; i < stands.size(); i++)
            stands.get(i).teleport(origin.clone().subtract(0, i * 0.25, 0));
    }

    public void delete()
    {
        stands.forEach(ArmorStand::remove);
        stands.clear();
        task.cancel();
    }
}
