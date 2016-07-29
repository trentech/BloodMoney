package com.gmail.trentech.bloodmoney;

import java.util.Optional;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

import me.flibio.updatifier.Updatifier;

@Updatifier(repoName = Resource.NAME, repoOwner = Resource.AUTHOR, version = Resource.VERSION)
@Plugin(id = Resource.ID, name = Resource.NAME, version = Resource.VERSION, description = Resource.DESCRIPTION, authors = Resource.AUTHOR, url = Resource.URL, dependencies = { @Dependency(id = "Updatifier", optional = true) })
public class BloodMoney {

	private static Game game;
	private static Logger log;
	private static PluginContainer plugin;
	private static EconomyService economy;

	@Listener
	public void onPreInitializationEvent(GamePreInitializationEvent event) {
		game = Sponge.getGame();
		plugin = getGame().getPluginManager().getPlugin(Resource.ID).get();
		log = getPlugin().getLogger();
	}

	@Listener
	public void onPostInitializationEvent(GamePostInitializationEvent event) {
		new ConfigManager().init();

		Optional<EconomyService> optionalEconomy = getGame().getServiceManager().provide(EconomyService.class);

		if (!optionalEconomy.isPresent()) {
			getLog().error("Economy plugin not found!");
			return;
		}
		economy = optionalEconomy.get();

		getGame().getEventManager().registerListeners(this, new EventHandler());
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

	public static EconomyService getEconomy() {
		return economy;
	}
}
