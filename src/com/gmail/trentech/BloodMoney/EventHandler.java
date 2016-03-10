package com.gmail.trentech.BloodMoney;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;

import ninja.leaping.configurate.ConfigurationNode;

public class EventHandler {

	private static HashMap<Player, Integer> killSteak = new HashMap<Player, Integer>();
	
	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event) {
	    Player player = event.getTargetEntity();
	    
	    if(player instanceof Player) {	        
	        killSteak.put(player, 0);
	    }
	}
	
    @Listener
    public void onDamageEntityEvent(DamageEntityEvent event, @First EntityDamageSource src) {
    	if(!(event.getTargetEntity() instanceof Player)) {
    		return;
    	}
    	Player player = (Player) event.getTargetEntity();
    	
		if(!player.hasPermission("BloodMoney.collect")){
			return;
		}
		
    	if(!(src.getSource() instanceof Living)) {
    		return;
    	}

    	killSteak.put(player, 0);
    }
    
    @Listener
    public void onDestructEntityEvent(DestructEntityEvent.Death event, @First EntityDamageSource src) {
    	if(!(event.getTargetEntity() instanceof Living)) {
    		return;
    	}

    	Player player;
    	
        if (src.getSource() instanceof Projectile){
        	Projectile projectile = (Projectile) src.getSource();
        	
        	Optional<UUID> optionalUUID = projectile.getCreator();
        	
        	if(!optionalUUID.isPresent()){
        		return;
        	}
        	
        	Optional<Player> optionalPlayer = BloodMoney.getGame().getServer().getPlayer(optionalUUID.get());
        	
        	if(!optionalPlayer.isPresent()){
        		return;
        	}
        	player = optionalPlayer.get();
        }else if(!(src.getSource() instanceof Player)){
        	player = (Player) src.getSource();
        }else{
        	return;
        }

		if(player.gameMode().get().equals(GameModes.CREATIVE)) {
			return;
		}
		
		if(!player.hasPermission("BloodMoney.collect")){
			return;
		}
		
		ConfigurationNode config = new ConfigManager().getConfig();
		if(config.getNode("Mobs", event.getTargetEntity().getType().getName(), "Maximum").getDouble() <= 0){
			return;
		}
		
		double multiplier = 0;		
		int kills = killSteak.get(player);		
		int killStreak = config.getNode("Options", "Kill-Streak").getInt();
		
		Builder builder = Text.builder().color(TextColors.YELLOW);

		if(kills >= killStreak && killStreak != 0) {
			multiplier = config.getNode("Options", "Kill-Streak-Multiplier").getDouble();
			Text.of(TextColors.GREEN, "Kill Streak! ");
			builder.color(TextColors.GREEN).append(Text.of("Kill Streak! "));
		}
		
		double min = config.getNode("Mobs", event.getTargetEntity().getType().getName(), "Minimum").getDouble();
		double max = config.getNode("Mobs", event.getTargetEntity().getType().getName(), "Maximum").getDouble();
		min = (min * multiplier) + min;
		max = (max * multiplier) + max;
		double amount = ThreadLocalRandom.current().nextDouble(min, max);

		if(!BloodMoney.getGame().getServiceManager().provide(EconomyService.class).isPresent()){
			return;
		}
		BloodMoney.getGame().getServiceManager().provide(EconomyService.class).get();
		
		EconomyService economy = BloodMoney.getGame().getServiceManager().provide(EconomyService.class).get();

		if(!economy.hasAccount(player.getUniqueId())){
			return;
		}
		
		UniqueAccount account = economy.getOrCreateAccount(player.getUniqueId()).get();
		
		if(account.deposit(economy.getDefaultCurrency(), new BigDecimal(amount), Cause.of(NamedCause.source(BloodMoney.getPlugin()))).getResult() == ResultType.SUCCESS){
			killSteak.put(player, kills+1);
			player.sendMessage(ChatTypes.ACTION_BAR, builder.append(Text.of(config.getNode("Options", "Representation").getString(), new DecimalFormat("#,###,##0.00").format(amount))).build());
		}
    }

}
