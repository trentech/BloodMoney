package com.gmail.trentech.BloodMoney;

import java.util.Optional;
import java.util.Random;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Texts;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class EventHandler {

	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event) {
	    Player player = event.getTargetEntity();
	    if(player instanceof Player){	        
	        BloodMoney.killSteak.put(player, 0);
	    }
	    
	    
	}
	
    @Listener
    public void onPlayerDeath(DestructEntityEvent.Death event){
    	if(!(event.getTargetEntity() instanceof Living)){
    		return;
    	}
    	
		Optional<EntityDamageSource> srcOptional = event.getCause().first(EntityDamageSource.class);
		if (!(srcOptional.isPresent())) {
			return;
		}
		
        EntityDamageSource damageSource = srcOptional.get();
        Entity killer = damageSource.getSource();
        if (!(killer instanceof Player)) {
        	return;
        }
        
		Player player = (Player) killer;
		if(player.getGameModeData().type().get().toString().equalsIgnoreCase("CREATIVE")){
			return;
		}
		
		double multiplier = 0;
		
		int kills = BloodMoney.killSteak.get(player);
		
		CommentedConfigurationNode config = new ConfigLoader().getConfig();
		
		if(kills >= config.getNode("Options", "Kill-Streak").getInt() && config.getNode("Options", "Kill-Streak").getInt() > 0){
			multiplier = config.getNode("Options", "Kill-Streak-Multiplier").getDouble();
			player.sendMessage(Texts.of("Kill Streak! " + kills));
		}
		double min = config.getNode("Mobs", event.getTargetEntity().getType().getName(), "Minimum").getDouble();
		double max = config.getNode("Mobs", event.getTargetEntity().getType().getName(), "Maximum").getDouble();		

		min = (min * multiplier) + min;
		max = (max * multiplier) + max;
		
		Random random = new Random();
		double amount = min + (max - min) * random.nextDouble();

		EconHook.deposit(player.getUniqueId().toString(), player.getWorld(), amount);
		BloodMoney.killSteak.put(player, BloodMoney.killSteak.get(player) + 1);
		player.sendMessage(Texts.of(amount));
		player.sendMessage(Texts.of(EconHook.getBalance(player.getUniqueId().toString(), player.getWorld())));
    }

}
