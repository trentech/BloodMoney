package com.gmail.trentech.BloodMoney;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class ConfigLoader {

	private File file;
	private CommentedConfigurationNode config;
	private ConfigurationLoader<CommentedConfigurationNode> loader;
	
	public ConfigLoader(String folder, String fileName) {
        if (!new File(folder).isDirectory()) {
        	new File(folder).mkdirs();
        }
		this.file = new File(folder, fileName);
		create();
	}
	
	public ConfigLoader() {
        if (!new File("config/BloodMoney/").isDirectory()) {
        	new File("config/BloodMoney/").mkdirs();
        }
		this.file = new File("config/BloodMoney/", "config.conf");
		create();
	}
	
	public CommentedConfigurationNode getConfig() {
    	loader = HoconConfigurationLoader.builder().setFile(file).build();
		try {
			config = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return config;
	}
	
	public boolean saveConfig() {
		if(config == null) {
			getConfig();
		}
		try {
			loader.save(config);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			Logger.getGlobal().severe("Failed to save Config!");
			return false;
		}
	}
	
	private void create() {
		if(!file.exists()) {
			try {
				file.createNewFile();		
			} catch (IOException e) {
				e.printStackTrace();
				BloodMoney.getLog().error("Failed to create config file!");
			}
		}
	}
	
	public void initConfig() {
		CommentedConfigurationNode config = getConfig();
        if(config.getNode("Options", "Kill-Streak").getString() == null) {
        	config.getNode("Options", "Kill-Streak").setValue(10);
        }
        if(config.getNode("Options", "Kill-Streak-Multiplier").getString() == null) {
        	config.getNode("Options", "Kill-Streak-Multiplier").setValue(2.00);
        }
        if(config.getNode("Options", "Representation").getString() == null) {
        	config.getNode("Options", "Representation").setValue("$");
        }
        saveConfig();
	}

}
