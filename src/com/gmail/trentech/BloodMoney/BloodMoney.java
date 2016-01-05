package com.gmail.trentech.BloodMoney;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

import ninja.leaping.configurate.ConfigurationNode;

@Plugin(id = Resource.ID, name = Resource.NAME, version = Resource.VERSION)
public class BloodMoney {

	private static Game game;
	private static Logger log;
	private static PluginContainer plugin;	

	@Listener
    public void onPreInitializationEvent(GamePreInitializationEvent event) {
		game = Sponge.getGame();
		plugin = getGame().getPluginManager().getPlugin(Resource.ID).get();
		log = getGame().getPluginManager().getLogger(plugin);
	}

	@Listener
	public void onStartedServerEvent(GameStartedServerEvent event) {
		getMobs();
		
		if(!getGame().getServiceManager().provide(EconomyService.class).isPresent()){
			getLog().error("Economy plugin not found!");
			return;
		}
		
		getGame().getEventManager().registerListeners(this, new EventHandler());
		
		getLog().info("BloodMoney has started!");
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
		ConfigManager configManager = new ConfigManager();
		ConfigurationNode config = configManager.getConfig();
		
		for(EntityType entityType : getGame().getRegistry().getAllOf(EntityType.class)) {
			if(Living.class.isAssignableFrom(entityType.getEntityClass())){				
				if(config.getNode("Mobs", entityType.getName()) != null){
					config.getNode("Mobs", entityType.getName(), "Minimum").setValue(1);
					config.getNode("Mobs", entityType.getName(), "Maximum").setValue(3);
					configManager.save();
				}
			}
		}
	}
}
