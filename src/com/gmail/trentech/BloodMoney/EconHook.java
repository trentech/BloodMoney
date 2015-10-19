package com.gmail.trentech.BloodMoney;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.World;

import com.erigitic.service.TEService;
import com.greatmancode.craftconomy3.Common;

import me.Flibio.EconomyLite.API.EconomyLiteAPI;

public class EconHook {

	private static TEService totalEconomy;
	private static EconomyLiteAPI economyLite;
	private static Common craftConomy;

	public static boolean initialize(){
		Optional<PluginContainer> plugin = BloodMoney.getGame().getPluginManager().getPlugin("EconomyLite");
		if(plugin.isPresent()) {
			economyLite = BloodMoney.getGame().getServiceManager().provide(EconomyLiteAPI.class).get();
			return true;
		}
		plugin = BloodMoney.getGame().getPluginManager().getPlugin("TotalEconomy");
		if(plugin.isPresent()) {
			totalEconomy = BloodMoney.getGame().getServiceManager().provide(TEService.class).get();
			return true;
		}
		plugin = BloodMoney.getGame().getPluginManager().getPlugin("Craftconomy3");
		if(plugin.isPresent()) {
			craftConomy = BloodMoney.getGame().getServiceManager().provide(Common.class).get();
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public static void deposit(String uuid, World world, double amount) {
		if(economyLite != null) {
			economyLite.getPlayerAPI().addCurrency(uuid, (int) amount);
		}else if(totalEconomy != null) {
			totalEconomy.addToBalance(UUID.fromString(uuid), BigDecimal.valueOf(amount), false);
		}else if(craftConomy != null) {
			craftConomy.getAccountManager().getAccount(uuid, false).deposit(amount, world.getName(), "Dollars");
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void withdraw(String uuid, World world, double amount) {
		if(economyLite != null) {
			economyLite.getPlayerAPI().removeCurrency(uuid, (int) amount);
		}else if(totalEconomy != null) {
			totalEconomy.removeFromBalance(UUID.fromString(uuid), BigDecimal.valueOf(amount));
		}else if(craftConomy != null) {
			craftConomy.getAccountManager().getAccount(uuid, false).withdraw(amount, world.toString(), "Dollars");
		}
	}
	
	public static double getBalance(String uuid, World world) {
		if(economyLite != null) {
			return economyLite.getPlayerAPI().getBalance(uuid);
		} else if(totalEconomy != null) {
			return totalEconomy.getBalance(UUID.fromString(uuid)).doubleValue();
		}else if(craftConomy != null) {
			return craftConomy.getAccountManager().getAccount(uuid, false).getBalance(world.getName(), "Dollars");
		}
		return 0;
	}
	
	@SuppressWarnings("deprecation")
	public static void setBalance(String uuid, World world, double amount) {
		if(economyLite != null) {
			economyLite.getPlayerAPI().setBalance(uuid, (int) amount);
		}else if(totalEconomy != null) {
			totalEconomy.setBalance(UUID.fromString(uuid), BigDecimal.valueOf(amount));
		}else if(craftConomy != null) {
			craftConomy.getAccountManager().getAccount(uuid, false).set(amount, world.getName(), "Dollars");
		}
	}
}
