package onl.tesseract.jelytra;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static onl.tesseract.jelytra.Race.races;

public class RaceCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args)
    {
        if (! (sender instanceof Player))
            return false;

        if (args.length == 0)
            return sendHelp(sender);

        if (! checkPerm(sender, args))
        {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission de faire cela.");
            return true;
        }

        Player player = (Player) sender;
        switch (args[0])
        {
            case "create":
                if (args.length != 2)
                    return sendHelp(sender);

                if (races.stream().anyMatch(race -> race.getName().equals(args[1])))
                {
                    sender.sendMessage(ChatColor.RED + "Nom déjà pris");
                    break;
                }
                new Race(args[1], player.getLocation());
                sender.sendMessage(ChatColor.GREEN + "Course créée");
                break;
            case "removecheckpoint":
            case "addcheckpoint":
            case "insertcheckpoint":
                if (args.length < 3)
                    return sendHelp(sender);
                Race r4 = Race.fromName(args[1]);
                if (r4 == null)
                {
                    sender.sendMessage(ChatColor.RED + "Cource introuvable");
                    return true;
                }
                if (args[0].equals("addcheckpoint"))
                {
                    try {
                        int radius = Integer.parseInt(args[2]);
                        r4.addCheckpoint(player.getLocation(), radius);
                        player.sendMessage(JElytra.chatFormat + "Checkpoint placé");
                    }catch (IllegalArgumentException ignored) {}
                }else if (args[0].equals("removecheckpoint"))
                {
                    try {
                        int index = Integer.parseInt(args[2]);
                        if (index >= 0 && index < r4.checkpoints.size()) {
                            r4.removeCheckpoint(index);
                            player.sendMessage(JElytra.chatFormat + "Checkpoint retiré");
                        }else
                            player.sendMessage(JElytra.chatFormat + "Checkpoint introuvable");
                    }catch (IllegalArgumentException ignored) {}
                } else if (args[0].equals("insertcheckpoint") && args.length == 4)
                {
                    try {
                        int index = Integer.parseInt(args[3]);
                        int radius = Integer.parseInt(args[2]);
                        if (index > 0 && index < r4.checkpoints.size()) {
                            r4.insertCheckpoint(player.getLocation(), radius, index);
                            player.sendMessage(JElytra.chatFormat + "Checkpoint inséré");
                        }else
                            player.sendMessage(JElytra.chatFormat + "Checkpoint introuvable");
                    }catch (IllegalArgumentException ignored) {}
                }
                break;
            case "addendpoint":
                if (args.length != 3)
                    return sendHelp(sender);

                if (Race.fromName(args[1]) != null)
                {
                    try {
                        int radius = Integer.parseInt(args[2]);
                        Objects.requireNonNull(Race.fromName(args[1])).setEndPoint(player.getLocation(), radius);
                        player.sendMessage(JElytra.chatFormat + "Fin placée");
                    }catch (IllegalArgumentException ignored) {}
                } else
                    sender.sendMessage(ChatColor.RED + "Cource introuvable");
                break;
            case "setstate":
                if (args.length < 2)
                    return sendHelp(sender);

                Race r3 = Race.fromName(args[1]);
                if (r3 != null)
                {
                    if (args.length == 2)
                    {
                        sender.sendMessage(JElytra.chatFormat + "État actuel : " + r3.getState().toString());
                        return true;
                    }
                    try {
                        Race.State state = Race.State.valueOf(args[2].toUpperCase());
                        r3.setState(state);
                        sender.sendMessage(JElytra.chatFormat + "État changé.");
                    }catch (IllegalArgumentException ignored) {}
                } else
                    sender.sendMessage(ChatColor.RED + "Cource introuvable");

                break;
            case "spawn":
                if (args.length < 2)
                    return sendHelp(sender);

                if (Race.fromName(args[1]) != null)
                {
                    player.teleport(Objects.requireNonNull(Race.fromName(args[1])).getSpawnPoint());
                    sender.sendMessage(ChatColor.GREEN + "Vous avez été téléporté à la course.");
                } else
                    sender.sendMessage(ChatColor.RED + "Cource introuvable");
                break;
            case "start":
                if (args.length < 2)
                    return sendHelp(sender);

                // Leave previous race
                for (Race race : races)
                    if (race.runners.containsKey(player))
                        race.leave(player);

                Race r = Race.fromName(args[1]);
                if (r != null)
                {
                    if (r.getState() == Race.State.OPEN)
                    {
                        assert player.getEquipment() != null;
                        if (player.getEquipment().getChestplate() != null && player.getEquipment().getChestplate().getType() == Material.ELYTRA)
                            r.start(player);
                        else
                            sender.sendMessage(ChatColor.RED + "Equipez vous d'abord de vos ailes depuis le menu /equipement.");
                    }else
                        sender.sendMessage(ChatColor.RED + "Cette course n'est pas disponible.");
                } else
                    sender.sendMessage(ChatColor.RED + "Cource introuvable");
                break;
            case "setstartpoint":
                if (args.length < 2)
                    return sendHelp(sender);

                if (Race.fromName(args[1]) != null)
                {
                    Objects.requireNonNull(Race.fromName(args[1])).setStartPoint(player.getLocation());
                    sender.sendMessage(ChatColor.GREEN + "Done.");
                } else
                    sender.sendMessage(ChatColor.RED + "Cource introuvable");
                break;
            case "setspawn":
                if (args.length < 2)
                    return sendHelp(sender);

                if (Race.fromName(args[1]) != null)
                {
                    Objects.requireNonNull(Race.fromName(args[1])).setSpawnPoint(player.getLocation());
                    sender.sendMessage(ChatColor.GREEN + "Done.");
                } else
                    sender.sendMessage(ChatColor.RED + "Cource introuvable");
                break;
            case "delete":
                if (args.length < 2)
                    return sendHelp(sender);

                if (Race.fromName(args[1]) != null)
                {
                    Objects.requireNonNull(Race.fromName(args[1])).delete();
                    sender.sendMessage(ChatColor.GREEN + "Course supprimée.");
                } else
                    sender.sendMessage(ChatColor.RED + "Cource introuvable.");
                break;
            case "list":
                sender.sendMessage(JElytra.chatFormat + "Courses :");
                races.forEach(race -> {
                    if (race.getState() != Race.State.OPEN && player.hasPermission("jelytra.animation"))
                        sender.sendMessage(ChatColor.GRAY + " • " + race.getName());
                    else if (race.getState() == Race.State.OPEN)
                        sender.sendMessage(ChatColor.GRAY + " • " + race.getName());
                });
                break;
            case "leave":
                for (Race race : races) {
                    if (race.runners.containsKey(player)) {
                        race.leave(player);
                        player.teleport(race.getSpawnPoint());
                        player.sendMessage(JElytra.chatFormat + "Vous avez quitté la course.");
                    }
                }
                break;
            case "top":
                if (args.length < 2)
                    return sendHelp(sender);

                if (Race.fromName(args[1]) != null)
                {
                    List<OfflinePlayer> ranking = Race.fromName(args[1]).getOrderedRanking();
                    int page = 0;
                    if (args.length == 3)
                    {
                        try {
                            page = Integer.parseInt(args[2]);
                        }catch (IllegalArgumentException ignored) {}
                    }
                    player.sendMessage(JElytra.chatFormat + "Classement de la course " + args[1]);
                    for (int i = page * 15; i < (page + 1) * 15 && i < ranking.size(); i++)
                        player.sendMessage(JElytra.chatFormat + " " + (i + 1) + " " + ranking.get(i).getName() + " : " + Util.durationToString(Race.fromName(args[1]).ranking.get(ranking.get(i).getUniqueId())));
                } else
                    sender.sendMessage(ChatColor.RED + "Cource introuvable");
                break;
            case "rankingBoard":
                if (args.length < 2)
                    return sendHelp(sender);

                if (Race.fromName(args[1]) != null)
                {
                    Objects.requireNonNull(Race.fromName(args[1])).createBoard(player.getLocation());
                } else
                    sender.sendMessage(ChatColor.RED + "Cource introuvable");
                break;
            case "resetRanking":
                if (args.length < 2)
                    return sendHelp(sender);

                Race r2 = Race.fromName(args[1]);
                if (r2 != null)
                {
                    if (args.length == 3)
                    {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(args[2]);
                        r2.ranking.remove(p.getUniqueId());
                    }
                    else
                        r2.ranking.clear();
                    player.sendMessage(JElytra.chatFormat + "Classement réinitialisé.");
                } else
                    sender.sendMessage(ChatColor.RED + "Cource introuvable");
                break;
        }
        return true;
    }

    boolean sendHelp(CommandSender sender)
    {
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race list : " + ChatColor.GRAY + "Afficher la liste des courses.");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race spawn {nom} : " + ChatColor.GRAY + "Se téléporter au spawn d'une course.");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race start {nom} : " + ChatColor.GRAY + "Commencer une course.");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race leave : " + ChatColor.GRAY + "Abandonner la course en cours.");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race top {nom} : " + ChatColor.GRAY + "Afficher le classement d'une course.");
        if (sender.hasPermission("jelytra.animation") || sender.hasPermission("jelytra.construction"))
        {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race rankingBoard {nom} : " + ChatColor.GRAY + "Placer un tableau des scores.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race resetRanking {nom} [pseudo] : " + ChatColor.GRAY + "Réinitialiser le score d'un joueur ou de tout le classement.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race setspawn {nom} : " + ChatColor.GRAY + "Définir le point de spawn.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race setstate {nom} {OPEN/CLOSED/CONSTRUCTION} : " + ChatColor.GRAY + "Changer l'état d'une course.");
        }
        if (sender.hasPermission("jelytra.construction"))
        {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race create {nom} : " + ChatColor.GRAY + "Créer une course.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race delete {nom} : " + ChatColor.GRAY + "Supprimer une course.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race setstartpoint {nom} : " + ChatColor.GRAY + "Définir le point de départ.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race addendpoint {nom} {rayon} : " + ChatColor.GRAY + "Placer le point d'arrivé.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race addcheckpoint {nom} {rayon} : " + ChatColor.GRAY + "Placer un checkpoint.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race insertcheckpoint {nom} {rayon} {index} : " + ChatColor.GRAY + "Insérer un checkpoint.");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/race removecheckpoint {nom} {index}: " + ChatColor.GRAY + "Retirer un checkpoint.");
        }
        return true;
    }

    boolean checkPerm(CommandSender sender, String[] args)
    {
        switch (args[0])
        {
            case "setstate":
            case "setspawn":
            case "rankingBoard":
            case "resetRanking":
                if (sender.hasPermission("jelytra.animation"))
                    return true;
            case "create":
            case "addcheckpoint":
            case "insertcheckpoint":
            case "removecheckpoint":
            case "addendpoint":
            case "setstart":
            case "delete":
                return sender.hasPermission("jelytra.construction");
            default:
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        if (args.length == 1 && sender.isOp())
            return List.of("create", "delete", "setstate", "setstartpoint", "addcheckpoint", "insertcheckpoint", "removecheckpoint", "addendpoint", "setspawn", "spawn", "start", "leave", "list", "top", "rankingBoard", "resetRanking").stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        else if (args.length == 1)
            return List.of("spawn", "start", "leave", "list", "top").stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());

        switch (args[0])
        {
            case "create":
            case "resetRanking":
            case "delete":
            case "setstartpoint":
            case "spawn":
            case "start":
            case "setspawn":
            case "rankingBoard":
            case "top":
                return races.stream().map(Race::getName).collect(Collectors.toList()).stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
            case "addcheckpoint":
            case "addendpoint":
                if (args.length == 2)
                    return races.stream().map(Race::getName).collect(Collectors.toList()).stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
                else return List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
            case "removecheckpoint":
                if (args.length == 2)
                    return races.stream().map(Race::getName).collect(Collectors.toList()).stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
                else if (args.length == 3) {
                    Race race = Race.fromName(args[1]);
                    if (race == null)
                        return List.of("");
                    AtomicInteger i = new AtomicInteger();
                    return race.checkpoints.stream().map(cp -> i.getAndIncrement() + "").collect(Collectors.toList());
                }
                break;
            case "insertcheckpoint":
                if (args.length == 2)
                    return races.stream().map(Race::getName).collect(Collectors.toList()).stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
                else if (args.length == 3)
                    return List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
                else if (args.length == 4) {
                    Race race = Race.fromName(args[1]);
                    if (race == null)
                        return List.of("");
                    AtomicInteger i = new AtomicInteger();
                    return race.checkpoints.stream().map(cp -> i.getAndIncrement() + "").collect(Collectors.toList());
                }
                break;
            case "setstate":
                if (args.length == 2)
                    return races.stream().map(Race::getName).collect(Collectors.toList()).stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
                else return List.of("OPEN", "CLOSED", "CONSTRUCTION");
        }
        return null;
    }
}
