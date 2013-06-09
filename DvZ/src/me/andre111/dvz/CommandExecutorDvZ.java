package me.andre111.dvz;

import java.io.File;
import java.util.Map;
import java.util.Random;

import me.andre111.dvz.dragon.PlayerDragon;
import me.andre111.dvz.utils.Item3DHandler.Item3DRunnable;
import me.andre111.dvz.utils.ItemHandler;
import me.andre111.dvz.utils.Slapi;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

//import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
//import pgDev.bukkit.DisguiseCraft.disguise.Disguise;
//import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;

public class CommandExecutorDvZ implements CommandExecutor {
	private DvZ plugin;

	public CommandExecutorDvZ(DvZ plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		//trick to make /dvz start work like /dvz_start
		if(command.getName().equalsIgnoreCase("dvz")) {
			if(args.length>0) {
				if(sender instanceof Player) {
					StringBuilder argstring = new StringBuilder();
					for(int i=1; i<args.length; i++) {
						argstring.append(" " + args[i]);
					}
					
					((Player) sender).performCommand("dvz_"+args[0]+argstring.toString());
					return true;
				}
			} 
			return false;
		//normal command
		} else {
			return onCommandIntern(sender, command, label, args);
		}
	}
	//World test = null;
	private boolean onCommandIntern(CommandSender sender, Command command, String label, String[] args) {
		int gameID = -1;
		try {
			if(args.length>0) gameID = Integer.parseInt(args[0].replace("+", ""));
		} catch (Exception e) {}
		
		//Only testing some stuff
		if (command.getName().equalsIgnoreCase("dvztest")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Y U NO PLAYER??!111");
				return true;
			}
			Player player = (Player)sender;
			if(!sender.hasPermission("dvz.test")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}
			
			//plugin.resetMainWorld();
			
			//Spellcontroller.spellEnderChest(plugin.game, player);
			
			/*player.getInventory().clear();
			plugin.getPlayerGame(player.getName()).setPlayerState(player.getName(), 3);
			plugin.getPlayerGame(player.getName()).addMonsterItems(player);*/
			/*if(test==null) {
				test = DvZWorldProvider.generateNewWorld();
			}
			player.teleport(test.getSpawnLocation());*/
			
			//Spellcontroller.spellItemTrow(player, player);
			
			//(new Dragon(DragonTyp.FIRE)).spawn(player.getLocation().add(0, 20, 0));
			//(new DragonThrow()).castShot(player.getLocation().add(0, 5, 0), Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
			
			//Fireworks.spawnTest(player.getLocation());
			
			/*if(args.length>0)
				DvZ.dragonAtManager.castFromPlayer(player, Integer.parseInt(args[0]));
			else
				player.sendMessage("Please specify Dragonattck ID");*/
			//QuarryGenerator.generateQuarry(player.getLocation().clone().subtract(0, 1, 0), 12, 60);
			
			final int itemID = (args.length>0) ? Integer.parseInt(args[0]) : Material.DIAMOND_SWORD.getId();
			//final String rand = ""+ (new Random()).nextInt(100);
			DvZ.item3DHandler.spawnAroundBlock(player, player.getLocation().clone().add(1, 0, 0), itemID, new Item3DRunnable() {
				@Override
				public void run(Player player) {
					player.getWorld().dropItemNaturally(player.getLocation().clone().add(0, 1, 0), new ItemStack(itemID));
				}
			});
			
			//DvZ.disguiseP(player, new Disguise(0, "", DisguiseType.Spider));
			return true;
		}
		//Start the Game
		if (command.getName().equalsIgnoreCase("dvz_start")) {
			if(!sender.hasPermission("dvz.start")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}
			
			Game game = getGameFromID(gameID, sender);
			
			if(game!=null) {
				int time = 1;
				if(args.length>1)
					time = Integer.parseInt(args[1].replace("+", ""));
				else
					game.broadcastMessage(DvZ.getLanguage().getString("string_starting_instant","Starting instantly!"));
				
				boolean assasins = false;
				int minutes = 10;
				int count = 1;
				int maxdeaths = 0;
				if(args.length>2) {
					assasins = true;
					minutes = Integer.parseInt(args[2].replace("+", ""));
					if(args.length>3) {
						count = Integer.parseInt(args[3].replace("+", ""));
					}
					if(args.length>4) {
						maxdeaths = Integer.parseInt(args[4].replace("+", ""));
					}
				}
				
				game.start(time);
				if(assasins) {
					game.assasins(minutes, count, maxdeaths);
				}
			}
			return true;
		}
		//Set the Dwarf Spawnpoint
		if (command.getName().equalsIgnoreCase("dvz_dwarf")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Y U NO PLAYER??!111");
				return true;
			}
			Player player = (Player)sender;
			if(!sender.hasPermission("dvz.setspawn")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}
			
			if(gameID==-1) {
				plugin.getDummy().spawnDwarves = player.getLocation();
				sender.sendMessage(DvZ.getLanguage().getString("string_using_dummy","Using dummy Game"));
			} else {
				Game game = plugin.getGame(gameID);
				if(game!=null) sender.sendMessage(DvZ.getLanguage().getString("string_using_game","Using Game ID -0-").replace("-0-", ""+gameID));
				else sender.sendMessage(DvZ.getLanguage().getString("string_not_game","Game ID -0- does not exist/is not activated!").replace("-0-", ""+0));
				
				if(game!=null) game.spawnDwarves = player.getLocation();
			}
			
			player.sendMessage(DvZ.getLanguage().getString("string_setspawn_dwarf","Set Dwarf Spawn to your current Location!"));
			
			String path = Bukkit.getServer().getWorldContainer().getPath()+"/"+player.getWorld().getName()+"/dvz/dvz_spawn_d.dat";
			Location loc = player.getLocation();
			try {
				Slapi.save(loc.getX()+":"+loc.getY()+":"+loc.getZ()+":"+loc.getPitch()+":"+loc.getYaw(), path);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return true;
		}
		//Set the Monster Spawnpoint
		if (command.getName().equalsIgnoreCase("dvz_monster")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Y U NO PLAYER??!111");
				return true;
			}
			Player player = (Player)sender;
			if(!sender.hasPermission("dvz.setspawn")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}
			
			if(gameID==-1) {
				plugin.getDummy().spawnMonsters = player.getLocation();
				sender.sendMessage(DvZ.getLanguage().getString("string_using_dummy","Using dummy Game"));
			} else {
				Game game = plugin.getGame(gameID);
				if(game!=null) sender.sendMessage(DvZ.getLanguage().getString("string_using_game","Using Game ID -0-").replace("-0-", ""+gameID));
				else sender.sendMessage(DvZ.getLanguage().getString("string_not_game","Game ID -0- does not exist/is not activated!").replace("-0-", ""+0));
				
				if(game!=null) game.spawnMonsters = player.getLocation();
			}
			
			player.sendMessage(DvZ.getLanguage().getString("string_setspawn_monster","Set Monster Spawn to your current Location!"));
			
			String path = Bukkit.getServer().getWorldContainer().getPath()+"/"+player.getWorld().getName()+"/dvz/dvz_spawn_m.dat";
			Location loc = player.getLocation();
			try {
				Slapi.save(loc.getX()+":"+loc.getY()+":"+loc.getZ()+":"+loc.getPitch()+":"+loc.getYaw(), path);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return true;
		}
		//get some information about the game
		if (command.getName().equalsIgnoreCase("dvz_info")) {
			if(!sender.hasPermission("dvz.info")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}
			
			Game game = getGameFromID(gameID, sender);
			
			if(game!=null) {
				int gameS = game.getState();
				if(gameS==1) {
					if(!game.getStarting()) {
						sender.sendMessage(DvZ.getLanguage().getString("string_game_notrunning","No Game running!"));
					} else {
						sender.sendMessage(DvZ.getLanguage().getString("string_game_start","Game starting in -0- Seconds!").replace("-0-", ""+game.getStartTime()));
					}
				} else {
					int dwarf = 0;
					int dwarfoff = 0;
					int mons = 0;
					int monsoff = 0;
					
					for(Map.Entry<String, Integer> e : game.playerstate.entrySet()){
						boolean online = false;
						Player player = Bukkit.getServer().getPlayerExact(e.getKey());
						if (player!=null) online = true;
						
						if (game.isDwarf(e.getKey(), true)) {
							if (online) dwarf++; else dwarfoff++;
						}
						if (game.isMonster(e.getKey())) {
							if (online) mons++; else monsoff++;
						}
					}
					
					
					int hours = 0;
					int minutes = 0;
					int seconds = game.getDauer();
					hours = (int)Math.floor(seconds/60.0/60.0);
					minutes = (int)Math.floor((seconds-(hours*60*60))/60.0);
					seconds = seconds-(minutes*60)-(hours*60*60);
					
					sender.sendMessage(DvZ.getLanguage().getString("string_game_running","Game running for -0- Hours -1- Minutes -2- Seconds!").replace("-0-", ""+hours).replace("-1-", ""+minutes).replace("-2-", ""+seconds));
					sender.sendMessage(DvZ.getLanguage().getString("string_game_count","-0- (-1- Offline) Dwarves and -2-(-3- Offline) Monsters!").replace("-0-", ""+dwarf).replace("-1-", ""+dwarfoff).replace("-2-", ""+mons).replace("-3-", ""+monsoff));
				}
			}
			return true;
		}
		//reset all gamestates
		if (command.getName().equalsIgnoreCase("dvz_reset")) {
			if(!sender.hasPermission("dvz.rest")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}
			
			Game game = getGameFromID(gameID, sender);
			
			if(game!=null) {
				game.reset(true);
			}
			
			return true;
		}
		//create the dwarf monument
		if (command.getName().equalsIgnoreCase("dvz_monument")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Y U NO PLAYER??!111");
				return true;
			}
			Player player = (Player)sender;
			if(!sender.hasPermission("dvz.rest")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}
			
			if(gameID==-1) {
				sender.sendMessage(DvZ.getLanguage().getString("string_using_dummy","Using dummy Game"));
				
				plugin.getDummy().monument = player.getLocation();
				plugin.getDummy().monumentexists = true;
				player.teleport(new Location(plugin.getDummy().monument.getWorld(), plugin.getDummy().monument.getBlockX(), plugin.getDummy().monument.getBlockY()+4, plugin.getDummy().monument.getBlockZ()));
				plugin.getDummy().createMonument(true);
			} else {
				Game game = plugin.getGame(gameID);
				if(game!=null) sender.sendMessage(DvZ.getLanguage().getString("string_using_game","Using Game ID -0-").replace("-0-", ""+gameID));
				else sender.sendMessage(DvZ.getLanguage().getString("string_not_game","Game ID -0- does not exist/is not activated!").replace("-0-", ""+0));
				
				if(game!=null) {
					game.monument = player.getLocation();
					game.monumentexists = true;
					player.teleport(new Location(game.monument.getWorld(), game.monument.getBlockX(), game.monument.getBlockY()+4, game.monument.getBlockZ()));
					game.createMonument(true);
				}
			}
			
			String path = Bukkit.getServer().getWorldContainer().getPath()+"/"+player.getWorld().getName()+"/dvz/dvz_mon.dat";
			try {
				Slapi.save(player.getLocation().getBlockX()+":"+player.getLocation().getBlockY()+":"+player.getLocation().getBlockZ(), path);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return true;
		}
		//become the dragen
		if (command.getName().equalsIgnoreCase("dvz_dragon")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Y U NO PLAYER??!111");
				return true;
			}
			Player player = (Player)sender;
			if(!sender.hasPermission("dvz.dragon")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}
			
			//int type = 1;
			//if(args.length>0)
			//	type = Integer.parseInt(args[0].replace("+", ""));
			Game game = plugin.getPlayerGame(player.getName());

			if(game != null) {
				int dID = -1;
				if(args.length>0) dID = Integer.parseInt(args[0].replace("+", ""));
				if(dID<0 || dID>DvZ.dragonAtManager.getMaxDragonCount()) {
					dID = (new Random()).nextInt(DvZ.dragonAtManager.getMaxDragonCount()+1);
					player.sendMessage(DvZ.getLanguage().getString("string_random_dragon", "Choosing a random Dragon!"));
				}
				player.sendMessage(DvZ.getLanguage().getString("string_become_dragon", "You became the -0-!").replace("-0-", DvZ.dragonAtManager.getDragon(dID).getName()));
				
				PlayerDragon dragon = new PlayerDragon(player);
				
				dragon.setID(dID);
				dragon.init();
				
				game.setDragon(dragon);
			} else {
				player.sendMessage(DvZ.getLanguage().getString("string_need_game", "You have to be a player in a game to become the Dragon!"));
			}
			/*DvZ.disguiseP(player, new Disguise(DvZ.api.newEntityID(), "", DisguiseType.EnderDragon));
			player.setAllowFlight(true);
			player.setFlying(true);
			player.setMaxHealth(200);
			player.setHealth(200);
			plugin.getGame(0).setPlayerState(player.getName(), 100);*/
			
			return true;
		}
		//Add a new Player to the game
		if (command.getName().equalsIgnoreCase("dvz_add")) {
			if(!sender.hasPermission("dvz.add")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}
			
			Game game = getGameFromID(gameID, sender);
			
			if(game!=null) {
				Player player = null;
				if(args.length>1)
					player = Bukkit.getServer().getPlayerExact(args[1]);
				
				if(player!=null) {
					game.setPlayerState(player.getName(), 2);
					player.teleport(Bukkit.getServer().getWorld(plugin.getConfig().getString("world_prefix", "DvZ_")+"Main"+gameID).getSpawnLocation());
					player.getInventory().clear();
					game.addDwarfItems(player);
					
					player.sendMessage(DvZ.getLanguage().getString("string_self_added","You have been added to the game!"));
					sender.sendMessage(DvZ.getLanguage().getString("string_player_added","Added -0- to the game!").replace("-0-", args[1]));
				} else {
					sender.sendMessage(DvZ.getLanguage().getString("string_noplayer","No Player found with that Name!"));
				}
			}
			
			return true;
		}
		//Chose Assasins
		if (command.getName().equalsIgnoreCase("dvz_assasin")) {
			if(!sender.hasPermission("dvz.assasin")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}
			
			Game game = getGameFromID(gameID, sender);
			
			if(game!=null) {
				int count = 1;
				if(args.length>1)
					count = Integer.parseInt(args[1].replace("+", ""));
				
				game.addAssasins(count);
			}
			
			return true;
		}
		//Join the game
		if (command.getName().equalsIgnoreCase("dvz_join")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Y U NO PLAYER??!111");
				return true;
			}
			Player player = (Player)sender;
			if(!sender.hasPermission("dvz.join")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}

			Game game = getGameFromIDSearchFree(gameID, sender);
			
			if(game!=null) {
				if(!game.isPlayer(player.getName())) {
					if(ItemHandler.isInvEmpty(player)) {
						plugin.joinGame(player, game);
					}
					else {
						player.sendMessage(DvZ.getLanguage().getString("string_join_items", "Warning! Items in your inventory will be deleted, please emtpy it first. To ignore this use /dvz_joini"));
					}
				}
			}

			return true;
		}
		//Join the game ignoring items
		if (command.getName().equalsIgnoreCase("dvz_joini")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Y U NO PLAYER??!111");
				return true;
			}
			Player player = (Player)sender;
			if(!sender.hasPermission("dvz.join")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}

			Game game = getGameFromIDSearchFree(gameID, sender);
			
			if(game!=null) {
				if(!game.isPlayer(player.getName())) {
					plugin.joinGame(player, game);
				}
			}

			return true;
		}
		//Leave the game
		if (command.getName().equalsIgnoreCase("dvz_leave")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Y U NO PLAYER??!111");
				return true;
			}
			Player player = (Player)sender;
			if(!sender.hasPermission("dvz.leave")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}

			if(plugin.getConfig().getString("dedicated_mode","false")!="true") {
				Game game = plugin.getPlayerGame(player.getName());
				
				if(game!=null) {
					if(game.getState()==1 && game.getPlayerState(player.getName())==1) {
						game.removePlayer(player.getName());

						if(DvZ.getStaticConfig().getString("use_lobby", "true").equals("true"))
							player.teleport(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
					}
				}
			}

			return true;
		}
		//Save a Backup of the DvZ_Main world
		if (command.getName().equalsIgnoreCase("dvz_saveworld")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Y U NO PLAYER??!111");
				return true;
			}
			Player player = (Player)sender;
			if(!sender.hasPermission("dvz.save")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}
			
			plugin.saveWorld(sender, player.getWorld().getName());
			
			return true;
		}
		//Save a Automap of the world
		if (command.getName().equalsIgnoreCase("dvz_createworld")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Y U NO PLAYER??!111");
				return true;
			}
			Player player = (Player)sender;
			if(!sender.hasPermission("dvz.save")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}

			plugin.createWorld(sender, player.getWorld().getName());
			
			return true;
		}
		//Release the monsters
		if (command.getName().equalsIgnoreCase("dvz_release")) {
			if(!sender.hasPermission("dvz.release")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}

			Game game = getGameFromID(gameID, sender);
			
			if(game!=null) {
				game.release();
			}
			
			return true;
		}
		//Give special items
		if (command.getName().equalsIgnoreCase("dvz_give")) {
			if(!sender.hasPermission("dvz.give")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}

			//get the player
			if(args.length>0) {
				Player player = Bukkit.getServer().getPlayerExact(args[0]);
				
				if(player!=null) {
					//recombine all other arguments
					String itemSt = "";
					int ii = 1;
					while(args.length>ii) {
						itemSt = itemSt + " " + args[ii];
						ii++;
					}
					
					//get the tem
					ItemStack it = ItemHandler.decodeItem(itemSt);
					if(it!=null) {
						player.getInventory().addItem(it);
						
						return true;
					} else {
						sender.sendMessage("Could not decode Itemstring: "+itemSt);
						return false;
					}
				} else {
					sender.sendMessage("Player "+args[0]+" not found!");
					return false;
				}
			} else {
				sender.sendMessage("Please specify a player to give the item to!");
				return false;
			}
		}
		
		//Give special items
		if (command.getName().equalsIgnoreCase("dvz_itemstand")) {
			if(!sender.hasPermission("dvz.itemstand")) {
				sender.sendMessage("You don't have the Permission to do that!");
				return false;
			}
			if (!(sender instanceof Player)) {
				sender.sendMessage("Y U NO PLAYER??!111");
				return true;
			}
			Player player = (Player)sender;

			//get the player
			if(args.length>0) {
				if(args.length>1) {
					if(args.length>2) {
						boolean once = Boolean.parseBoolean(args[0]);
						int itemID = Integer.parseInt(args[1]);
						
						//recombine all other arguments
						String itemSt = "";
						int ii = 2;
						while(args.length>ii) {
							itemSt = itemSt + " " + args[ii];
							ii++;
						}
						
						//get the item
						if(ItemHandler.decodeItem(itemSt)!=null) {
							DvZ.itemStandManager.createAndSaveStand(new File(Bukkit.getServer().getWorldContainer().getPath()+"/"+player.getWorld().getName()+"/dvz/itemstands/"), player.getLocation(), itemID, once, itemSt);
						} else {
							sender.sendMessage("Could not decode Itemstring: "+itemSt);
							return false;
						}
					} else {
						sender.sendMessage("Please specify a formated Item to give!");
						return false;
					}
				} else {
					sender.sendMessage("Please specify a display Item ID!");
					return false;
				}
			} else {
				sender.sendMessage("Please specify a if it should be once per Player!");
				return false;
			}
		}
		
		return false;
	}
	
	private Game getGameFromID(int gameID, CommandSender sender) {
		Game game;
		if(gameID==-1) {
			game = plugin.getGame(0);
			
			if(DvZ.getStaticConfig().getString("show_game_id","true")=="true")
				sender.sendMessage(DvZ.getLanguage().getString("string_using_game","Using Game ID -0-").replace("-0-", ""+0));
		} else {
			game = plugin.getGame(gameID);
			if(DvZ.getStaticConfig().getString("show_game_id","true")=="true")
				if(game!=null) sender.sendMessage(DvZ.getLanguage().getString("string_using_game","Using Game ID -0-").replace("-0-", ""+gameID));
				else sender.sendMessage(DvZ.getLanguage().getString("string_not_game","Game ID -0- does not exist/is not activated!").replace("-0-", ""+0));
		}
		
		return game;
	}
	
	private Game getGameFromIDSearchFree(int gameID, CommandSender sender) {
		Game game;
		if(gameID==-1) {
			int id = 0;
			game = plugin.getGame(0);
			
			//searching for a game in the "Lobby state"
			if(DvZ.getStaticConfig().getString("join_free_game", "true").equals("true")) {
				if(game.getState()>1) {
					for(int i=0; i<10; i++) {
						Game g = plugin.getGame(i);
						if(g!=null) {
							if(g.getState()<2) {
								id = i;
								game = g;
								break;
							}
						}
					}
				}
			}
			
			if(DvZ.getStaticConfig().getString("show_game_id","true")=="true")
				sender.sendMessage(DvZ.getLanguage().getString("string_using_game","Using Game ID -0-").replace("-0-", ""+id));
		} else {
			game = plugin.getGame(gameID);
			if(DvZ.getStaticConfig().getString("show_game_id","true")=="true")
				if(game!=null) sender.sendMessage(DvZ.getLanguage().getString("string_using_game","Using Game ID -0-").replace("-0-", ""+gameID));
				else sender.sendMessage(DvZ.getLanguage().getString("string_not_game","Game ID -0- does not exist/is not activated!").replace("-0-", ""+0));
		}
		
		return game;
	}
}