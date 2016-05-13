package com.gmail.trentech.BloodMoney;

import java.io.File;
import java.io.IOException;

import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class ConfigManager {

	private File file;
	private CommentedConfigurationNode config;
	private ConfigurationLoader<CommentedConfigurationNode> loader;

	public ConfigManager(String folder, String configName) {
		folder = "config" + File.separator + "bloodmoney" + File.separator + folder;
        if (!new File(folder).isDirectory()) {
        	new File(folder).mkdirs();
        }
		file = new File(folder, configName);
		
		create();
		load();
	}
	
	public ConfigManager(String configName) {
		String folder = "config" + File.separator + "bloodmoney";
        if (!new File(folder).isDirectory()) {
        	new File(folder).mkdirs();
        }
		file = new File(folder, configName);
		
		create();
		load();
	}
	
	public ConfigManager() {
		String folder = "config" + File.separator + "bloodmoney";
        if (!new File(folder).isDirectory()) {
        	new File(folder).mkdirs();
        }
		file = new File(folder, "config.conf");
		
		create();
		load();
	}
	
	public ConfigurationLoader<CommentedConfigurationNode> getLoader() {
		return loader;
	}

	public CommentedConfigurationNode getConfig() {
		return config;
	}

	private void create(){
		if(!file.exists()) {
			try {
				BloodMoney.getLog().info("Creating new " + file.getName() + " file...");
				file.createNewFile();		
			} catch (IOException e) {				
				BloodMoney.getLog().error("Failed to create new config file");
				e.printStackTrace();
			}
		}
	}
	
	public void init() {
        if(config.getNode("Options", "Kill-Streak").isVirtual()) {
        	config.getNode("Options", "Kill-Streak").setValue(10);
        }
        if(config.getNode("Options", "Kill-Streak-Multiplier").isVirtual()) {
        	config.getNode("Options", "Kill-Streak-Multiplier").setValue(2.00);
        }
        if(config.getNode("Options", "Representation").isVirtual()) {
        	config.getNode("Options", "Representation").setValue("$");
        }
		for(EntityType entityType : BloodMoney.getGame().getRegistry().getAllOf(EntityType.class)) {
			if(Living.class.isAssignableFrom(entityType.getEntityClass())){				
				if(config.getNode("Mobs", entityType.getName()).isVirtual()){
					config.getNode("Mobs", entityType.getName(), "Minimum").setValue(1);
					config.getNode("Mobs", entityType.getName(), "Maximum").setValue(3);
				}
			}
		}
		save();
	}
	
	private void load(){
		loader = HoconConfigurationLoader.builder().setFile(file).build();
		try {
			config = loader.load();
		} catch (IOException e) {
			BloodMoney.getLog().error("Failed to load config");
			e.printStackTrace();
		}
	}
	
	public void save(){
		try {
			loader.save(config);
		} catch (IOException e) {
			BloodMoney.getLog().error("Failed to save config");
			e.printStackTrace();
		}
	}
}
