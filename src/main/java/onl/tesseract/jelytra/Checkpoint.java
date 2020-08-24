package onl.tesseract.jelytra;

import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Checkpoint {
    Set<UUID> passed = new HashSet<>();
    HashMap<Player, Circle> circles = new HashMap<>();
    Race race;
    Location center;
    int radius;

    public Checkpoint(Race race, Location center, int radius)
    {
        this.race = race;
        this.center = center;
        this.radius = radius;
    }

    public Location getCenter()
    {
        return center;
    }

    public int getRadius()
    {
        return radius;
    }

    public void pass(Player player, boolean passed)
    {
        if (passed) {
            this.passed.add(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            Firework fw = player.getWorld().spawn(center.clone().add(0, radius + 3, 0), Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().withColor(Color.LIME)
                    .flicker(true).with(FireworkEffect.Type.BALL).withFade(Color.YELLOW).build());
            fw.setFireworkMeta(meta);
            fw.detonate();
            if (circles.containsKey(player))
                circles.get(player).setColor(Color.LIME);
            else
                circles.put(player, new Circle(player, center, radius, Color.LIME));

            if (this instanceof Endpoint) {
                player.setCompassTarget(player.getWorld().getSpawnLocation());
                return;
            }

            Checkpoint next = race.getNextCheckpoint(player);
            if (next != null)
                player.setCompassTarget(next.getCenter());
            else
                player.setCompassTarget(race.getEndpoint().getCenter());

            player.sendTitle("", ChatColor.LIGHT_PURPLE + "" + (race.checkpoints.indexOf(this) + 1) + "/" + (race.checkpoints.size() + 1));
        }else {
            this.passed.remove(player.getUniqueId());
            if (circles.containsKey(player))
                circles.get(player).remove();
            circles.remove(player);
        }
    }

    public boolean hasPassed(Player player)
    {
        return passed.contains(player.getUniqueId());
    }

    public void show(Player player)
    {
        circles.put(player, new Circle(player, center, radius, Color.AQUA));
    }

    public boolean isPassing(Player player)
    {
        return player.getWorld().equals(center.getWorld()) && player.getLocation().distance(center) <= radius;
    }

    public void hide(Player player)
    {
        if (circles.containsKey(player))
            circles.get(player).remove();
    }

    public void hideAll()
    {
        for (Player p : circles.keySet())
            circles.get(p).remove();
        circles.clear();
    }
}
