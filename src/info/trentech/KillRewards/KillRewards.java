package info.trentech.KillRewards;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class KillRewards extends JavaPlugin {
	public final Logger log = Logger.getLogger("Minecraft");
	private EventListener eventlistener;
	public Economy economy;
	public Objective objective;
	public Scoreboard board;
	
	@Override
	public void onEnable(){
		this.eventlistener = new EventListener(this);
		getServer().getPluginManager().registerEvents(this.eventlistener, this);
		if (!setupEconomy()) {
        	log.warning(String.format("[%s] Vault not found! Economy support disabled!", new Object[] {getDescription().getName()}));
		}
		getConfig().options().copyDefaults(true);
		saveConfig();
		getMobs();
		
        board = getServer().getScoreboardManager().getNewScoreboard();
        objective = board.registerNewObjective("Stats", "dummy");
        objective.setDisplayName(ChatColor.DARK_AQUA + "Stats");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}

	public void getMobs(){
		for(EntityType entity : EntityType.values()) {
			if(entity.isAlive()) {
				if(entity.name().equalsIgnoreCase("VILLAGER")){
					setMobInfo("VILLAGER");
					Profession[] jobs = Profession.values();
					for (Profession job : jobs) {
						String villager = entity.name();
						villager = villager + "-" + job.name();
						setMobInfo(villager);
					}
				}else if(entity.name().equalsIgnoreCase("ZOMBIE")){
					setMobInfo(entity.name());
					setMobInfo(entity.name() + "-VILLAGER");
					setMobInfo(entity.name() + "-BABY");
					setMobInfo(entity.name() + "-VILLAGER");
				}else if(entity.name().equalsIgnoreCase("SKELETON")){
					SkeletonType[] types = SkeletonType.values();
					for (SkeletonType type : types) {
						String skeleton = entity.name();
						skeleton = skeleton + "-" + type.name();
						setMobInfo(skeleton);
					}
				}else if(entity.name().equalsIgnoreCase("CREEPER")){
					setMobInfo(entity.name());
					setMobInfo(entity.name() + "-CHARGED");
				}else{
					setMobInfo(entity.name());
					if(entity.getClass().isAssignableFrom(Tameable.class)) {
						setMobInfo(entity.name() + "-TAME");
					}
				}
			}
		}
	}
	
	public void setMobInfo(String mob) {
		getConfig().set("Mobs." + mob + ".Minimum", 0);
		getConfig().set("Mobs." + mob + ".Maximum", 3);
		getConfig().set("Mobs." + mob + ".Bonus", 3);
		getConfig().set("Mobs." + mob + ".Level-Up", true);
		saveConfig();
	}
	
	private boolean setupEconomy() {
		Plugin plugin = getServer().getPluginManager().getPlugin("Vault");
		if(plugin != null){
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
			economy = (Economy) economyProvider.getProvider();
			return true;
		}else{
			return false;
		}
	}
}
