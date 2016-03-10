package com.gmail.trentech.BloodMoney;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class ConfigManager {

	private File file;
	private CommentedConfigurationNode config;
	private ConfigurationLoader<CommentedConfigurationNode> loader;
	
	public ConfigManager(String folder, String configName) {
		folder = "config/bloodmoney/" + folder + "/";
        if (!new File(folder).isDirectory()) {
        	new File(folder).mkdirs();
        }
		file = new File(folder + configName);
		
		create();
		load();
		init();
	}
	
	public ConfigManager(String configName) {
		String folder = "config/bloodmoney/";
        if (!new File(folder).isDirectory()) {
        	new File(folder).mkdirs();
        }
		file = new File(folder + configName);
		
		create();
		load();
		init();
	}
	
	public ConfigManager() {
		String folder = "config/bloodmoney/";
        if (!new File(folder).isDirectory()) {
        	new File(folder).mkdirs();
        }
		file = new File(folder, "config.conf");
		
		create();
		load();
		init();
	}
	
	public ConfigurationLoader<CommentedConfigurationNode> getLoader() {
		return loader;
	}

	public CommentedConfigurationNode getConfig() {
		return config;
	}

	public void save(){
		try {
			loader.save(config);
		} catch (IOException e) {
			BloodMoney.getLog().error("Failed to save config");
			e.printStackTrace();
		}
	}
	
	private void init() {
        if(config.getNode("Options", "Kill-Streak").getString() == null) {
        	config.getNode("Options", "Kill-Streak").setValue(10);
        }
        if(config.getNode("Options", "Kill-Streak-Multiplier").getString() == null) {
        	config.getNode("Options", "Kill-Streak-Multiplier").setValue(2.00);
        }
        if(config.getNode("Options", "Representation").getString() == null) {
        	config.getNode("Options", "Representation").setValue("$");
        }
		save();
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
	
	private void load(){
		loader = HoconConfigurationLoader.builder().setFile(file).build();
		try {
			config = loader.load();
		} catch (IOException e) {
			BloodMoney.getLog().error("Failed to load config");
			e.printStackTrace();
		}
	}

	public boolean removeCuboidLocation(String locationName){
		for(Entry<Object, ? extends ConfigurationNode> node : config.getNode("Cuboids").getChildrenMap().entrySet()){
			String uuid = node.getKey().toString();
			
			List<String> list = config.getNode("Cuboids", uuid, "Locations").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());

	    	if(!list.contains(locationName)){
	    		continue;
	    	}

			for(String loc : list){
	        	String[] info = loc.split("\\.");

	        	Location<World> location = BloodMoney.getGame().getServer().getWorld(info[0]).get().getLocation(Integer.parseInt(info[1]), Integer.parseInt(info[2]), Integer.parseInt(info[3]));

            	if(location.getBlockType().equals(BlockTypes.FLOWING_WATER)){
            		BlockState block = BlockState.builder().blockType(BlockTypes.AIR).build();
            		location.setBlock(block);
            	}
			}
			
			config.getNode("Cuboids", uuid).setValue(null);
			save();
			
			return true;
		}
		return false;
	}

	public Location<World> getCuboid(String locationName){
		for(Entry<Object, ? extends ConfigurationNode> node : config.getNode("Cuboids").getChildrenMap().entrySet()){
			String uuid = node.getKey().toString();

	    	List<String> list = config.getNode("Cuboids", uuid, "Locations").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());

	    	if(!list.contains(locationName)){
	    		continue;
	    	}
	    	
			String worldName = config.getNode("Cuboids", uuid, "World").getString();
			
			if(!BloodMoney.getGame().getServer().getWorld(worldName).isPresent()){
				continue;
			}
			World world = BloodMoney.getGame().getServer().getWorld(worldName).get();
			
			int x = world.getSpawnLocation().getBlockX();
			int y = world.getSpawnLocation().getBlockY();
			int z = world.getSpawnLocation().getBlockZ();
			
			if(config.getNode("Cuboids", uuid, "X").getString() != null && config.getNode("Cuboids", uuid, "Y").getString() != null && config.getNode("Cuboids", uuid, "Z").getString() != null){
				x = config.getNode("Cuboids", uuid, "X").getInt();
				y = config.getNode("Cuboids", uuid, "Y").getInt();
				z = config.getNode("Cuboids", uuid, "Z").getInt();
			}

			return BloodMoney.getGame().getServer().getWorld(worldName).get().getLocation(x, y, z);
		}
		return null;
	}

}
