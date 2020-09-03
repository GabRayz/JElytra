package onl.tesseract.jelytra;

import org.bukkit.*;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class Race {
    static public Set<Race> races = new HashSet<>();
    public HashMap<UUID, Duration> ranking = new HashMap<>();

    String name;
    Location spawnPoint;
    Location startPoint;
    List<Checkpoint> checkpoints = new ArrayList<>();
    Endpoint endpoint;

    HashMap<Player, Instant> runners = new HashMap<>();
    RankingDisplay rankingDisplay;
    State state;

    public enum State {
        OPEN, CLOSED, CONSTRUCTION
    }

    public Race(String name)
    {
        this.name = name;
        races.add(this);
    }

    public Race(String name, Location creationLocation)
    {
        this.name = name;
        races.add(this);
        setStartPoint(creationLocation);
        setSpawnPoint(creationLocation);
        setEndPoint(creationLocation, 2);
        setState(State.CONSTRUCTION);
        endpoint.hideAll();
    }

    public void addCheckpoint(Location center, int radius)
    {
        Checkpoint cp = new Checkpoint(this, center, radius);
        checkpoints.add(cp);
        if (state == State.CONSTRUCTION)
            cp.show(null);
    }

    public void insertCheckpoint(Location center, int radius, int index)
    {
        for (Checkpoint cp : checkpoints)
            cp.hideAll();
        checkpoints.add(index, new Checkpoint(this, center, radius));
        for (Checkpoint cp : checkpoints)
            cp.show(null);
    }

    public void removeCheckpoint(int index)
    {
        checkpoints.get(index).hideAll();
        checkpoints.remove(index);
    }

    public void setEndPoint(Location center, int radius)
    {
        if (endpoint != null)
            endpoint.hideAll();
        this.endpoint = new Endpoint(this, center, radius);
        if (state == State.CONSTRUCTION)
            endpoint.show(null);
    }

    public void start(Player player)
    {
        player.sendMessage(ChatColor.AQUA + "La course commence !.");
        player.teleport(startPoint);
        player.sendTitle(ChatColor.BOLD + "" + ChatColor.GREEN + "GO !", "", 0, 40, 10);
        checkpoints.get(0).show(player);
        if (checkpoints.size() > 1)
            checkpoints.get(1).show(player, Color.RED);
        else
            endpoint.show(player);
        runners.put(player, Instant.now());
        player.setGliding(true);

        if (checkpoints.size() > 0)
            player.setCompassTarget(checkpoints.get(0).getCenter());
        else
            player.setCompassTarget(endpoint.getCenter());
    }

    public void finish(Player player)
    {
        for (Checkpoint checkpoint : checkpoints) {
            checkpoint.pass(player, false);
            checkpoint.hide(player);
        }
        endpoint.hide(player);
        Duration duration = Duration.between(runners.get(player), Instant.now());
        runners.remove(player);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        player.sendMessage(JElytra.chatFormat + ChatColor.LIGHT_PURPLE + "Vous avez fini la course avec un temps de " + ChatColor.GOLD + Util.durationToString(duration));
        if (ranking.containsKey(player.getUniqueId()) && ranking.get(player.getUniqueId()).toMillis() > duration.toMillis())
            player.sendMessage(JElytra.chatFormat + ChatColor.LIGHT_PURPLE + "C'est votre nouveau record !");
        if (getRecord() == null || duration.toMillis() < getRecord().toMillis())
            Bukkit.getOnlinePlayers().forEach(p ->
                    p.sendMessage(JElytra.chatFormat + ChatColor.BOLD + "" + ChatColor.GOLD + player.getName() + ChatColor.LIGHT_PURPLE + " a battu le record de la course " + name + " avec un temps de " + ChatColor.GOLD + Util.durationToString(duration)));
        if (! ranking.containsKey(player.getUniqueId()) || duration.toMillis() < ranking.get(player.getUniqueId()).toMillis())
            ranking.put(player.getUniqueId(), duration);
    }

    public void leave(Player player)
    {
        for (Checkpoint checkpoint : checkpoints) {
            checkpoint.pass(player, false);
            checkpoint.hide(player);
        }
        endpoint.hide(player);
    }

    public void createBoard(Location origin)
    {
        if (this.rankingDisplay != null)
            rankingDisplay.move(origin);
        else
            this.rankingDisplay = new RankingDisplay(this, origin);
    }

    public List<OfflinePlayer> getOrderedRanking()
    {
        return ranking.keySet().stream().sorted((p1, p2) -> {
            if (ranking.get(p1).toMillis() < ranking.get(p2).toMillis())
                return -1;
            else if (ranking.get(p1).toMillis() > ranking.get(p2).toMillis())
                return 1;
            return 0;
        }).map(Bukkit::getOfflinePlayer).collect(Collectors.toList());
    }

    public int getRankPlace(Player player)
    {
        return getOrderedRanking().indexOf(player);
    }

    public Duration getRecord()
    {
        if (ranking.size() > 0)
            return ranking.get(getOrderedRanking().get(0).getUniqueId());
        else return null;
    }

    public String getName()
    {
        return name;
    }

    public Location getSpawnPoint()
    {
        return spawnPoint;
    }

    public void setStartPoint(Location startPoint)
    {
        this.startPoint = startPoint;
    }

    public Location getStartPoint()
    {
        return startPoint;
    }

    public void setSpawnPoint(Location spawnPoint)
    {
        this.spawnPoint = spawnPoint;
    }

    public HashMap<Player, Instant> getRunners()
    {
        return runners;
    }

    public Checkpoint getNextCheckpoint(Player player)
    {
        for (Checkpoint checkpoint : checkpoints)
            if (! checkpoint.hasPassed(player))
                return checkpoint;
        return null;
    }

    public Endpoint getEndpoint()
    {
        return endpoint;
    }

    public void delete()
    {
        for (Checkpoint cp : checkpoints)
            cp.hideAll();
        endpoint.hideAll();
        races.remove(this);
        if (rankingDisplay != null)
            rankingDisplay.delete();
        this.rankingDisplay = null;
    }

    public void reset()
    {
        for (Checkpoint cp : checkpoints)
            cp.hideAll();
        endpoint.hideAll();
        runners.clear();
    }

    public State getState()
    {
        return state;
    }

    public void setState(State state)
    {
        reset();
        this.state = state;
        if (state == State.CONSTRUCTION)
        {
            for (Checkpoint cp : checkpoints)
                cp.show(null);
            endpoint.show(null);
        }
        if (state == State.CLOSED && rankingDisplay != null) {
            rankingDisplay.delete();
            rankingDisplay = null;
        }
    }

    static public Race fromName(String name)
    {
        for (Race race : races)
            if (race.getName().equals(name))
                return race;
        return null;
    }
}
