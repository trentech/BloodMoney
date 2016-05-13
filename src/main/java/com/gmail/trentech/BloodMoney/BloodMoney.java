package com.gmail.trentech.BloodMoney;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

import me.flibio.updatifier.Updatifier;

@Updatifier(repoName = "BloodMoney", repoOwner = "TrenTech", version = Resource.VERSION)
@Plugin(id = Resource.ID, name = Resource.NAME, version = Resource.VERSION, authors = Resource.AUTHOR, url = Resource.URL, description = Resource.DESCRIPTION, dependencies = {@Dependency(id = "Updatifier", optional = true)})
public class BloodMoney {

	private static Game game;
	private static Logger log;
	private static PluginContainer plugin;	

	@Listener
    public void onPreInitializationEvent(GamePreInitializationEvent event) {
		game = Sponge.getGame();
		plugin = getGame().getPluginManager().getPlugin(Resource.ID).get();
		log = getPlugin().getLogger();
	}

	@Listener
	public void onStartedServerEvent(GameStartedServerEvent event) {
		new ConfigManager().init();
		
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
}
