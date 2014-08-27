package info.trentech.KillRewards;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Score;

public class EventListener implements Listener {

	HashMap<Player, String> players = new HashMap<Player, String>();
	private KillRewards plugin;
	public EventListener(KillRewards plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLoginEvent(PlayerJoinEvent event){
		Player player = event.getPlayer();
		players.put(player, "0;0;0");
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeathEvent(EntityDeathEvent  event){
		LivingEntity entity = event.getEntity();
		Entity killer = entity.getKiller();
		if(killer instanceof Player){
			Player player = (Player) killer;
			if(player.getGameMode() != GameMode.CREATIVE){
				String mob = entity.getType().name();
				if(player.hasPermission("KillRewards.collect")){			
			        if(entity instanceof Zombie){
			            Zombie zombie = (Zombie) entity;
			            if(zombie.isBaby()){
			            	mob = mob + "-BABY";
			            	if(zombie.isVillager()){
			            		mob = mob + "-VILLAGER";
			            	}
			            }
			        }else if(entity instanceof Creeper){
			        	Creeper creeper = (Creeper) entity;
			        	if(creeper.isPowered()){
			        		mob = mob + "-CHARGED";
			        	}
			        }else if(entity instanceof Tameable){
			        	mob = mob + "-TAME";
			        }else if(entity instanceof Skeleton){
			        	Skeleton skeleton = (Skeleton) entity;
			        	mob = mob + "-" + skeleton.getSkeletonType().name();
			        }else if(entity instanceof Villager){
			        	Villager villager = (Villager) entity;
			        	Profession job = villager.getProfession();
			        	if(job != null) {
							mob = mob + "-" + job.name();
						}
			        }

				}
				double addedMoney = 0;
				if(player.hasPermission("KillRewards.levels") && plugin.getConfig().getBoolean("Advanced-Mode") && plugin.getConfig().getBoolean("Mobs." + mob + ".Level-Up")){
					String[] stats = players.get(player).split(";");			
					int maxLevel = plugin.getConfig().getInt("Levels.Max-Level");
					double multiplier = plugin.getConfig().getInt("Levels.Payment-Multipler");
					int level = Integer.parseInt(stats[0]);
					int killsPerLevel = Integer.parseInt(stats[1]) + 1;
					int killsPerLevelConfig = plugin.getConfig().getInt("Levels.Kills-Per-Level");
					int killsTotal = Integer.parseInt(stats[2]) + 1;
					if(level > 1){
						killsPerLevelConfig = killsPerLevelConfig + killsPerLevelConfig * (level * plugin.getConfig().getInt("Levels.Kills-Per-Level-Multiplier"));
					}
					if(killsPerLevel >= killsPerLevelConfig){
						killsPerLevel = 0;
						if(level < maxLevel){
							level++;
							if(level == maxLevel){
								player.sendMessage(ChatColor.DARK_GREEN + "You have reached the maximum level possible! What a slayer!");
							}else{
								player.sendMessage(ChatColor.DARK_GREEN + "You've reached level " + level + "!");
							}		
						}
					}
					players.put(player, Integer.toString(level) + ";" + Integer.toString(killsPerLevel) + ";" + Integer.toString(killsTotal));
					addedMoney = multiplier * level;
					Score bLevel = plugin.objective.getScore("Level:");
					Score bTotalKills = plugin.objective.getScore("Total Kills:");
					bLevel.setScore(level);
					bTotalKills.setScore(killsTotal);
					player.setScoreboard(plugin.board);
				}
				double min = plugin.getConfig().getDouble("Mobs." + mob + ".Minimum");
				double max = plugin.getConfig().getDouble("Mobs." + mob + ".Maximum") + addedMoney;
				Random random = new Random();
				double amount = min + (max - min) * random.nextDouble();
				DecimalFormat format = new DecimalFormat("#.00");
				player.sendMessage(ChatColor.DARK_GREEN + "$" + format.format(amount));
				plugin.economy.depositPlayer(player, Double.parseDouble(format.format(amount)));
			}
		}	
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerItemHeldEvent(PlayerItemHeldEvent event){
		Player player = event.getPlayer();
		if(player.getGameMode() != GameMode.CREATIVE){
			if(player.getInventory().getItem(event.getNewSlot()) != null){
				Material material = player.getInventory().getItem(event.getNewSlot()).getType();
				if(isValidTool(material)){
					String[] stats = players.get(player).split(";");
					int killsTotal = Integer.parseInt(stats[2]);
					int level = Integer.parseInt(stats[0]);
					Score bLevel = plugin.objective.getScore("Level:");
					Score bTotalKills = plugin.objective.getScore("Total Kills:");
					bLevel.setScore(level);
					bTotalKills.setScore(killsTotal);
					player.setScoreboard(plugin.board);
				}
			}else{
				player.setScoreboard(plugin.getServer().getScoreboardManager().getNewScoreboard());
			}
		}
	}

	public boolean isValidTool(Material material) {
		boolean b = false;
		for(String weapon : plugin.getConfig().getStringList("Levels.Valid-Weapons")){
			if(material.name().equalsIgnoreCase(weapon)){
				b = true;
				break;
			}
		}
		return b;
	}
	
	
}
