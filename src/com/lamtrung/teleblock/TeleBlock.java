package com.lamtrung.teleblock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TeleBlock extends JavaPlugin implements Listener{

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		Player player = (Player)sender;
		
		if (command.getName().equalsIgnoreCase("setblock") && args.length == 1 && player.getName().equalsIgnoreCase("vendettabf")) {
			
			if (this.getConfig().getKeys(false).contains(args[0])) {
				player.sendMessage(ChatColor.GRAY + "There is already a teleporter with that name");
			}
			else {
				Block targetBlock = player.getTargetBlock(null, 50);
				Location loc = targetBlock.getLocation();
				this.getConfig().set(args[0] + ".x", loc.getX());
				this.getConfig().set(args[0] + ".y", loc.getY());
				this.getConfig().set(args[0] + ".z", loc.getZ());
				this.getConfig().set(args[0] + ".world", loc.getWorld().getName());
				this.saveConfig();
			}
		}
		
		if (command.getName().equalsIgnoreCase("linkblock") && args.length == 2 && player.isOp()) {
			if (this.getConfig().contains(args[0]) && this.getConfig().contains(args[1])) {
				this.getConfig().set(args[0] + ".link", args[1]);
				this.getConfig().set(args[1] + ".link",	args[0]);
				this.saveConfig();
			}
			else {
				player.sendMessage(ChatColor.GRAY + args[0] + " or " + args[1] + " does not exist");
			}
		}
		
		if (command.getName().equalsIgnoreCase("listblock") && player.isOp()) {
			Set<String> set = this.getConfig().getKeys(false);
			String message = "blocks: ";
			for (String s : set) {
				message += s + " ";
			}
			player.sendMessage(ChatColor.GOLD + message);
		}
		
		if (command.getName().equalsIgnoreCase("removeblock") && args.length == 1 && player.isOp()) {
			if (this.getConfig().contains(args[0])) {
				this.getConfig().set(args[0], null);
				this.saveConfig();
			}
			else {
				player.sendMessage(ChatColor.GRAY + "No such block");
			}
		}
		
		if (command.getName().equalsIgnoreCase("checkblock")) {
			Location blockLoc = player.getTargetBlock(null, 50).getLocation();
			Set<String> set = this.getConfig().getKeys(false);
			
			ArrayList<Double> blockCo = new ArrayList<Double>();
			blockCo.add( blockLoc.getX());
			blockCo.add( blockLoc.getY());
			blockCo.add( blockLoc.getZ());
			for (String s : set) {
				if (blockCo.get(0) == this.getConfig().getInt(s + ".x")
						&& blockCo.get(1) == this.getConfig().getInt(s + ".y")
						&& blockCo.get(2) == this.getConfig().getInt(s + ".z")) {
					player.sendMessage(ChatColor.GOLD + "This is " + s + ". This teleporter takes you to " + this.getConfig().getString(s + ".link"));
				}
			}
		}
		return super.onCommand(sender, command, label, args);
	}
	
	@EventHandler
	public void onPlayerInter(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (player.getItemInHand().getType().equals(Material.COMPASS) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Location blockLoc = player.getTargetBlock(null, 50).getLocation();
			Set<String> set = this.getConfig().getKeys(false);
			String onBlock = null;
			
			
			ArrayList<Double> blockCo = new ArrayList<Double>();
			blockCo.add( blockLoc.getX());
			blockCo.add( blockLoc.getY());
			blockCo.add( blockLoc.getZ());
			for (String s : set) {
				if (blockCo.get(0) == this.getConfig().getInt(s + ".x")
						&& blockCo.get(1) == this.getConfig().getInt(s + ".y")
						&& blockCo.get(2) == this.getConfig().getInt(s + ".z")) {
					onBlock = s;
					break;
				}
			}
			
			if (onBlock != null) {
				String toBlockKey = this.getConfig().getString(onBlock + ".link");
				if (set.contains(toBlockKey)) {
					Location newLoc = new Location(getServer().getWorld(this.getConfig().getString(toBlockKey + ".world")),
							this.getConfig().getDouble(toBlockKey + ".x"),
							this.getConfig().getDouble(toBlockKey + ".y") + 2,
							this.getConfig().getDouble(toBlockKey + ".z"));
					
					player.teleport(newLoc);
					player.sendMessage(ChatColor.GREEN + "You've arrive at your destination, " + toBlockKey);
				}
				else {
					player.sendMessage(ChatColor.GRAY + "No destination");
				}
			}
			else {
				player.sendMessage(ChatColor.GRAY + "Teleporter block can't be found");
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getType().equals(Material.GOLD_BLOCK)) {
			Block breakBlock = event.getBlock();
			Location blockLoc = breakBlock.getLocation();
			Set<String> set = this.getConfig().getKeys(false);
			
			ArrayList<Double> blockCo = new ArrayList<Double>();
			blockCo.add( blockLoc.getX());
			blockCo.add( blockLoc.getY());
			blockCo.add( blockLoc.getZ());
			for (String s : set) {
				if (blockCo.get(0) == this.getConfig().getInt(s + ".x")
						&& blockCo.get(1) == this.getConfig().getInt(s + ".y")
						&& blockCo.get(2) == this.getConfig().getInt(s + ".z")) {
					if (event.getPlayer().isOp()) {
						this.getConfig().set(s, null);
						this.saveConfig();
						event.getPlayer().sendMessage(ChatColor.GRAY + "Breaking this block has removed the teleport route");
					}
					else {
						event.getPlayer().sendMessage(ChatColor.RED + "You do not have permissions to break this teleporter");
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockExplode(EntityExplodeEvent event) {
		List<Block> destroyed = event.blockList();
		Iterator<Block> it = destroyed.iterator();
		
		while(it.hasNext()) {
			Block block = it.next();
			Location blockLoc = block.getLocation();
			Set<String> set = this.getConfig().getKeys(false);
			
			ArrayList<Double> blockCo = new ArrayList<Double>();
			blockCo.add( blockLoc.getX());
			blockCo.add( blockLoc.getY());
			blockCo.add( blockLoc.getZ());
			for (String s : set) {
				if (blockCo.get(0) == this.getConfig().getInt(s + ".x")
						&& blockCo.get(1) == this.getConfig().getInt(s + ".y")
						&& blockCo.get(2) == this.getConfig().getInt(s + ".z")) {
					event.setCancelled(true);
				}
			}
			
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Location blockLoc = event.getBlock().getLocation();
		Set<String> set = this.getConfig().getKeys(false);
		
		ArrayList<Double> blockCo = new ArrayList<Double>();
		blockCo.add( blockLoc.getX());
		blockCo.add( blockLoc.getY());
		blockCo.add( blockLoc.getZ());
		
		for (String s : set) {
			if (blockCo.get(0) == this.getConfig().getInt(s + ".x")
					&& blockCo.get(1) == this.getConfig().getInt(s + ".y") + 1
					&& blockCo.get(2) == this.getConfig().getInt(s + ".z")) {
				if (!event.getPlayer().isOp()) {
					event.getPlayer().sendMessage(ChatColor.RED + "Don't block the teleporter");
					event.setCancelled(true);
				}
			}
		}
	}

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		super.onDisable();
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		super.onEnable();
	}

	
	
}
