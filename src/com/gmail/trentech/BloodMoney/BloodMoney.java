package com.gmail.trentech.BloodMoney;

import java.util.Collection;
import java.util.HashMap;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;

@Plugin(id = "BloodMoney", name = "BloodMoney", dependencies = "after:EconomyLite;after:TotalEconomy", version = "0.1.5")
public class BloodMoney {

	private static Game game;
	private static Logger log;
	private static PluginContainer plugin;
	public static HashMap<Player, Integer> killSteak = new HashMap<Player, Integer>();

	@Listener
    public void onPreInit(GamePreInitializationEvent event) {
		game = event.getGame();
		plugin = event.getGame().getPluginManager().getPlugin("BloodMoney").get();
		log = event.getGame().getPluginManager().getLogger(plugin);
	}

	@Listener
	public void onServerInit(GameInitializationEvent event) {
		if(!EconHook.initialize()){
			log.error("Economy plugin not found!");
			return;
		}
		new ConfigLoader().initConfig();
		game.getEventManager().registerListeners(this, new EventHandler());
	}
	
	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		getMobs();
		log.info("BloodMoney has started!");
	}

	public static Game getGame() {
		return game;
	}

	public static Logger getLog() {
		return log;
	}

	public static PluginContainer getPlugin() {
		return plugin;
	}
	
	private void getMobs(){
		Collection<EntityType> entities = game.getRegistry().getAllOf(EntityType.class);
		ConfigLoader loader = new ConfigLoader();
		for(EntityType entityType : entities) {
			if(Living.class.isAssignableFrom(entityType.getEntityClass())){
				CommentedConfigurationNode config = loader.getConfig();
				if(config.getNode("Mobs", entityType.getName()) != null){
					config.getNode("Mobs", entityType.getName(), "Minimum").setValue(1);
					config.getNode("Mobs", entityType.getName(), "Maximum").setValue(3);
					loader.saveConfig();
				}
			}
		}
	}
}
