package com.gmail.trentech.BloodMoney;

import java.text.DecimalFormat;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class EventHandler {

	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event) {
	    Player player = event.getTargetEntity();
	    if(player instanceof Player) {	        
	        BloodMoney.killSteak.put(player, 0);
	    }
	}
	
    @Listener
    public void onDamageEntityEvent(DamageEntityEvent event) {
    	if(!(event.getTargetEntity() instanceof Player)) {
    		return;
    	}
    	
		Optional<EntityDamageSource> srcOptional = event.getCause().first(EntityDamageSource.class);
		if (!srcOptional.isPresent()) {
			return;
		}
		
    	if(!(srcOptional.get() instanceof Living)) {
    		return;
    	}
    	
		if(!((Player) event.getTargetEntity()).hasPermission("BloodMoney.collect")){
			return;
		}
    	
    	BloodMoney.killSteak.put(((Player) event.getTargetEntity()), 0);
    }
    
    @Listener
    public void onDestructEntityEvent(DestructEntityEvent.Death event) {
    	if(!(event.getTargetEntity() instanceof Living)) {
    		return;
    	}
    	
		Optional<EntityDamageSource> srcOptional = event.getCause().first(EntityDamageSource.class);
		if (!srcOptional.isPresent()) {
			return;
		}
		
        EntityDamageSource damageSource = srcOptional.get();
        Entity killer = damageSource.getSource();
        if (!(killer instanceof Player)) {
        	return;
        }
        
		Player player = (Player) killer;
		if(player.getGameModeData().type().get().toString().equalsIgnoreCase("CREATIVE")) {
			return;
		}
		
		if(!player.hasPermission("BloodMoney.collect")){
			return;
		}
		
		CommentedConfigurationNode config = new ConfigLoader().getConfig();
		if(config.getNode("Mobs", event.getTargetEntity().getType().getName(), "Maximum").getDouble() <= 0){
			return;
		}
		
		double multiplier = 0;		
		int kills = BloodMoney.killSteak.get(player);		
		
		TextBuilder builder = Texts.builder();	
		if(kills >= config.getNode("Options", "Kill-Streak").getInt() && config.getNode("Options", "Kill-Streak").getInt() > 0) {
			multiplier = config.getNode("Options", "Kill-Streak-Multiplier").getDouble();
			builder = Texts.of(TextColors.GREEN, "Kill Streak! ").builder();
		}
		
		double min = config.getNode("Mobs", event.getTargetEntity().getType().getName(), "Minimum").getDouble();
		double max = config.getNode("Mobs", event.getTargetEntity().getType().getName(), "Maximum").getDouble();
		min = (min * multiplier) + min;
		max = (max * multiplier) + max;
		double amount = ThreadLocalRandom.current().nextDouble(min, max);

		EconHook.deposit(player.getUniqueId().toString(), player.getWorld(), amount);
		BloodMoney.killSteak.put(player, BloodMoney.killSteak.get(player) + 1);
		DecimalFormat format = new DecimalFormat("#,###,##0.00");
		
		player.sendMessage(ChatTypes.ACTION_BAR, builder.append(Texts.of(TextColors.GREEN, config.getNode("Options", "Representation").getString(), format.format(amount))).build());
    }

}
