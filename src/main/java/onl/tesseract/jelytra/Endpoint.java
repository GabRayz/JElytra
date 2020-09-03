package onl.tesseract.jelytra;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Endpoint extends Checkpoint{
    public Endpoint(Race race, Location center, int radius)
    {
        super(race, center, radius);
    }

    @Override
    public void show(Player player)
    {
        if (circles.containsKey(player))
            circles.get(player).remove();
        circles.put(player, new EndCircle(player, center, radius));
    }
}
