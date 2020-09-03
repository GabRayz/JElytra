package onl.tesseract.jelytra;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Circle {
    BukkitTask task;
    ParticleBuilder builder;

    public Circle(Player player, Location center, int radius, Color color)
    {
        this.builder = new ParticleBuilder(Particle.REDSTONE);
        if (player != null)
            builder.receivers(player);
        builder.color(color);
        builder.offset(0.2, 0.2, 0.2);
        builder.count(10);
        this.task = new BukkitRunnable() {
            double theta = 0;
            @Override
            public void run()
            {
                double x1 = radius * (Math.cos(theta) * Math.cos(Math.toRadians(center.getYaw())));
                double z1 = radius * (Math.cos(theta) * Math.sin(Math.toRadians(center.getYaw())));
                double y1 = radius * Math.sin(theta);
                double x2 = radius * (Math.cos(theta + 2.0943951024) * Math.cos(Math.toRadians(center.getYaw())));
                double z2 = radius * (Math.cos(theta + 2.0943951024) * Math.sin(Math.toRadians(center.getYaw())));
                double y2 = radius * Math.sin(theta + 2.0943951024);
                double x3 = radius * (Math.cos(theta - 2.0943951024) * Math.cos(Math.toRadians(center.getYaw())));
                double z3 = radius * (Math.cos(theta - 2.0943951024) * Math.sin(Math.toRadians(center.getYaw())));
                double y3 = radius * Math.sin(theta - 2.0943951024);
                Location loc1 = center.clone().add(x1, y1, z1);
                Location loc2 = center.clone().add(x2, y2, z2);
                Location loc3 = center.clone().add(x3, y3, z3);

                builder.location(loc1);
                builder.spawn();
                builder.location(loc2).spawn();
                builder.location(loc3).spawn();
                theta += 0.05;
            }
        }.runTaskTimer(JElytra.instance, 0, 1);
    }

    public Circle()
    {
    }

    public void setColor(Color color) {
        builder.color(color);
    }

    public void remove() {
        System.out.println("test");
        task.cancel();
    }
}
