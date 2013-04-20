package me.andre111.dvz.listeners;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import me.andre111.dvz.DvZ;
import me.andre111.dvz.Game;
import me.andre111.dvz.monster.CustomMonster;
import me.andre111.dvz.utils.ItemHandler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import pgDev.bukkit.DisguiseCraft.api.PlayerUndisguiseEvent;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;

public class Listener_Player implements Listener  {
	private DvZ plugin;

	public Listener_Player(DvZ plugin){
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		//if not dedicated and the player is not in the game->ignore
		if(event.getPlayer().getLocation().getWorld()==Bukkit.getServer().getWorld(plugin.getConfig().getString("world_prefix", "DvZ_")+"Lobby") && (plugin.getPlayerGame(event.getPlayer().getName())==null && plugin.getConfig().getString("dedicated_mode","false")!="true"))
			event.getPlayer().teleport(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
		
		if(!plugin.getConfig().getString("dedicated_mode","false").equals("true") && plugin.getPlayerGame(event.getPlayer().getName())==null) return;
		
		event.setJoinMessage("");
		Player player = event.getPlayer();
		//TODO - maybe change to not always join game 0
		if (plugin.getPlayerGame(player.getName())==null) {
			plugin.getGame(0).addPlayer(player.getName());
			player.sendMessage(DvZ.getLanguage().getString("string_motd","Welcome to this �1DvZ�f Server!"));
			player.sendMessage("--------------------------------");
			player.sendMessage("Plugin by andre111");
			
			event.setJoinMessage(DvZ.getLanguage().getString("string_welcome","Welcome -0- to the Game!").replaceAll("-0-", player.getDisplayName()));
			
			//dedicated mode and game not started -> teleport to the lobby
			if(plugin.getGame(0).getState()==1 && plugin.getConfig().getString("dedicated_mode","false")=="true") {
				plugin.getGame(0).setPlayerState(player.getName(), 1);
				ItemHandler.clearInv(player);
				player.teleport(Bukkit.getServer().getWorld(plugin.getConfig().getString("world_prefix", "DvZ_")+"Lobby").getSpawnLocation());
			}
			//autoadd player
			if(plugin.getGame(0).getState()==2) {
				if (plugin.getConfig().getString("autoadd_players","false")=="true") {
					if(!plugin.getGame(0).released) {
						plugin.getGame(0).setPlayerState(player.getName(), 2);
						ItemHandler.clearInv(player);
						player.sendMessage(DvZ.getLanguage().getString("string_choose","Choose your class!"));
						plugin.getGame(0).addDwarfItems(player);
						
						plugin.getGame(0).broadcastMessage(DvZ.getLanguage().getString("string_autoadd","Autoadded -0- as a Dwarf to the Game!").replaceAll("-0-", player.getDisplayName()));
					} else {
						plugin.getGame(0).setPlayerState(player.getName(), 3);
						ItemHandler.clearInv(player);
						player.sendMessage(DvZ.getLanguage().getString("string_choose","Choose your class!"));
						plugin.getGame(0).addMonsterItems(player);
						
						plugin.getGame(0).broadcastMessage(DvZ.getLanguage().getString("string_autoadd_m","Autoadded -0- as a Monster to the Game!").replaceAll("-0-", player.getDisplayName()));
					}
				}
			}
		} else {
			int pstate = plugin.getPlayerGame(player.getName()).getPlayerState(player.getName());
			//redisguise
			CustomMonster cm = DvZ.monsterManager.getMonster(pstate-Game.monsterMin);
			if(cm!=null) {
				DvZ.disguiseP(player, new Disguise(DvZ.api.newEntityID(), "", cm.getDisguise()));
				player.sendMessage(DvZ.getLanguage().getString("string_redisguise","Redisguised you as a -0-!").replaceAll("-0-", cm.getName()));
			}
			//player leave during start
			if(pstate==1 && plugin.getPlayerGame(player.getName()).getState()>1) {
				plugin.joinGame(player, plugin.getPlayerGame(player.getName()));
			}
		}
	}

	/*@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location playerp = player.getLocation();
		
	}*/
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		//if not dedicated and the player is not in the game->ignore
		if(!plugin.getConfig().getString("dedicated_mode","false").equals("true") && plugin.getPlayerGame(event.getPlayer().getName())==null) return;
		
		Player player = event.getPlayer();
		Game game = plugin.getPlayerGame(player.getName());
		
		if (game!=null) {
			//disable rightclick items during class selection
			if(game.getPlayerState(player.getName())==Game.pickDwarf || game.getPlayerState(player.getName())==Game.pickMonster) {
				event.setCancelled(true);
			}
			
			Action action = event.getAction();
			ItemStack item = event.getItem();
			if (action==Action.RIGHT_CLICK_AIR) {
				game.playerRC(player, item, null);
			}
			else if(action==Action.RIGHT_CLICK_BLOCK) {
				game.playerRC(player, item, event.getClickedBlock());
			}
			else if(action==Action.LEFT_CLICK_AIR) {
				game.playerLC(player, item, null);
			}
			else if(action==Action.LEFT_CLICK_BLOCK) {
				game.playerLC(player, item, event.getClickedBlock());
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		//if not dedicated and the player is not in the game->ignore
		if(!plugin.getConfig().getString("dedicated_mode","false").equals("true") && plugin.getPlayerGame(event.getPlayer().getName())==null) return;
		
		Player player = event.getPlayer();
		Game game = plugin.getPlayerGame(player.getName());
		
		if (game!=null) {
			Entity entity = event.getRightClicked();
			ItemStack item = event.getPlayer().getItemInHand();
			if(entity instanceof Player) {
				game.playerRCPlayer(player, item, (Player)entity);
			}
			
			//disable rightclick items during class selection
			if(game.getPlayerState(player.getName())==Game.pickDwarf || game.getPlayerState(player.getName())==Game.pickMonster) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerPickUpItem(PlayerPickupItemEvent event) {
		//if not dedicated and the player is not in the game->ignore
		if(!plugin.getConfig().getString("dedicated_mode","false").equals("true") && plugin.getPlayerGame(event.getPlayer().getName())==null) return;
		if(plugin.getPlayerGame(event.getPlayer().getName())==null) return;
		
		Player player = event.getPlayer();
		//wenn nicht ingame -> aufsammeln und droppen verbieten
		if (plugin.getPlayerGame(event.getPlayer().getName())==null  && !player.isOp()) {
			event.setCancelled(true);
			return;
		}
		Game game = plugin.getPlayerGame(player.getName());
		if (game!=null) {
			if(game.getPlayerState(player.getName())<10) {
				event.setCancelled(true);
				return;
			}
			//monster k�nnen nichts aufheben
			if(game.isMonster(player.getName())) {
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler
	public void onPlayerItemDrop(PlayerDropItemEvent event) {
		//if not dedicated and the player is not in the game->ignore
		if(!plugin.getConfig().getString("dedicated_mode","false").equals("true") && plugin.getPlayerGame(event.getPlayer().getName())==null) return;
		
		Player player = event.getPlayer();
		//wenn nicht ingame -> aufsammeln und droppen verbieten
		if (plugin.getPlayerGame(event.getPlayer().getName())==null && !player.isOp()) {
			event.setCancelled(true);
			return;
		}
		Game game = plugin.getPlayerGame(player.getName());
		if (game!=null) {
			if(game.getPlayerState(player.getName())<10 && !player.isOp()) {
				event.setCancelled(true);
				return;
			}
			//monster k�nnen nichts droppen
			if(game.isMonster(player.getName()) && !player.isOp()) {
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		//if not dedicated and the player is not in the game->ignore
		if(!plugin.getConfig().getString("dedicated_mode","false").equals("true") && plugin.getPlayerGame(event.getPlayer().getName())==null) return;
		
		final Player player = event.getPlayer();
		final Game game = plugin.getPlayerGame(player.getName());
		
		if (game!=null) {
			//Redisguise no longer needed->disguiscraft fix
			//game.redisguisePlayers();

			for(PotionEffect pet : player.getActivePotionEffects()) {
				player.removePotionEffect(pet.getType());
			}
			
			int pstate = game.getPlayerState(player.getName());
			if((pstate>=Game.dwarfMin && pstate<=Game.monsterMax) || pstate==Game.assasinState || pstate==Game.dragonWarrior) {
				
				//respawn at spawnpoint of dvzworld
				final World w = Bukkit.getServer().getWorld(plugin.getConfig().getString("world_prefix", "DvZ_")+"Main"+plugin.getGameID(game)+"");
				if(game.spawnMonsters!=null) event.setRespawnLocation(game.spawnMonsters);
				else event.setRespawnLocation(w.getSpawnLocation());
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						if(game.spawnMonsters!=null) player.teleport(game.spawnMonsters);
						else player.teleport(w.getSpawnLocation());
					}
				}, 1);
				
				game.setPlayerState(player.getName(), 3);
				player.sendMessage(DvZ.getLanguage().getString("string_choose","Choose your class!"));
	
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						game.addMonsterItems(player);
					}
				}, 1);
				
				ItemHandler.clearInv(player);
				
				//deaths
				if(game.isDwarf(player.getName())) {
					game.deaths++;
				}
			}
			
			if (DvZ.api.isDisguised(player)) 
				DvZ.api.undisguisePlayer(player);
		}
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if(event.isCancelled()) return;
		//if not dedicated and the player is not in the game->ignore
		if(!plugin.getConfig().getString("dedicated_mode","false").equals("true") && plugin.getPlayerGame(event.getPlayer().getName())==null) return;
		
		Player player = event.getPlayer();
		Game game = plugin.getPlayerGame(player.getName());
		
		if (game!=null) {
			//default
			//event.setFormat("�r<%1$s> %2$s");
			
			int pstate = game.getPlayerState(player.getName());
			if(pstate>=Game.dwarfMin && pstate<=Game.dwarfMax) {
				int did = pstate - (Game.dwarfMin-1);

				String prefix = DvZ.getClassFile().getString("custom_d"+did+"_chatprefix","");
				String suffix = DvZ.getClassFile().getString("custom_d"+did+"_chatsuffix","");
				
				event.setFormat("�r<"+prefix+"%1$s"+suffix+"> %2$s");
			}
			if(game.isMonster(player.getName())) {
				int did = pstate - Game.monsterMin;
				
				String prefix = DvZ.monsterManager.getMonster(did).getPrefix();
				String suffix = DvZ.monsterManager.getMonster(did).getSuffix();
				
				event.setFormat("�r<"+prefix+"%1$s"+suffix+"> %2$s");
				//event.setMessage(DvZ.getLanguage().getString("string_chat_monster","�5Monster�f>")+" "+event.getMessage());
			}
			
			//game dedicated chat
			if(plugin.getConfig().getString("dedicated_chat", "true")=="true") {
				Set<Player> playerset = event.getRecipients();
				Iterator<Player> playerit = playerset.iterator();
				try{
					while(playerit.hasNext()) {
						Player p = playerit.next();
						if(!game.isPlayer(p.getName()) && !player.isOp()) {
							playerit.remove();
						}
					}
				} catch(ConcurrentModificationException e) {
				} catch(UnsupportedOperationException e) {
				}
			}
			//
		}
	}
	
	@EventHandler
	public void onPlayerPortal(PlayerPortalEvent event) {
		if (event.isCancelled()) return;
		
		event.setCancelled(plugin.getLobby().portalEvent(event));
	}
	
	@EventHandler
    public void onPLayerDeath(PlayerDeathEvent event)
    {
		Player p = event.getEntity();
		
		//is killer a player?
		if(event.getEntity().getKiller()==null) return;
		Player k;
		if(event.getEntity().getKiller() instanceof Player) {
			k = (Player)event.getEntity().getKiller();
		} else {
			return;
		}
		
		
		Game game = plugin.getPlayerGame(p.getName());
		if(game!=null) {
			//Is dwarv
			if (game.isDwarf(p.getName())) {
				//Is killer monster
				if (game.isMonster(k.getName())) {
					//TODO - change monstername
					if (plugin.getConfig().getString("change_death_message", "true").equals("true")) {
						event.setDeathMessage(ChatColor.YELLOW+DvZ.getLanguage().getString("string_chat_death", "-0- was killed by a monster!").replaceAll("-0-", p.getName()));
					}
				}
			//is Monster
			} else if(game.isMonster(p.getName())) {
				//Is killer dwarv
				if (game.isDwarf(k.getName())) {
					//Item stats
					//----
					if (plugin.getConfig().getString("item_stats", "true").equals("true")) {
						if (k.getItemInHand().getAmount() > 0) {
							ItemStack it = k.getItemInHand();
							ItemMeta im = it.getItemMeta();
							if(im.hasLore()) {
								String lore1 = im.getLore().get(0);
								if(lore1.startsWith(DvZ.getLanguage().getString("string_stats_killed_monsters","Killed Monsters: "))) {
									int count = Integer.parseInt(lore1.replaceAll(DvZ.getLanguage().getString("string_stats_killed_monsters","Killed Monsters: "), ""));
									count += 1;
									ArrayList<String> li = new ArrayList<String>();
									li.add(DvZ.getLanguage().getString("string_stats_killed_monsters","Killed Monsters: ")+count);
									im.setLore(li);
								}
							} else {
								ArrayList<String> li = new ArrayList<String>();
								li.add(DvZ.getLanguage().getString("string_stats_killed_monsters","Killed Monsters: ")+1);
								im.setLore(li);
							}
							it.setItemMeta(im);
							k.setItemInHand(it);
							DvZ.updateInventory(k);
						}
					}
					//----
				}
			}
		}
    }
	
	//Item Monster Damage listener maybe
	/* @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Player p = null;
        if (event.getDamager() instanceof Player) {
            p = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Arrow && ((Arrow) event.getDamager()).getShooter() instanceof Player) {
            p = (Player) ((Arrow) event.getDamager()).getShooter();
        }
        if (p != null) {
            if (p.getItemInHand().getAmount() > 0*/
	
	//TODO - remove temporary workaround when disguisecraft fixes it
	//cancel the event if the player is monster or dragon
	//to circuvent the disguisecraft permissions
	@EventHandler
	public void onPlayerUndisguise(PlayerUndisguiseEvent event) {
		Player p = event.getPlayer();
		Game game = plugin.getPlayerGame(p.getName());
		if(game!=null) {
			if(game.getState()>1)
			if(game.isMonster(p.getName()) || game.getPlayerState(p.getName())>Game.dragonMin) {
				event.setCancelled(true);
			}
		}
	}
	
	//Crafting
	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent event) {
		boolean disable = false;
		List<HumanEntity> hList = event.getViewers();
		for(HumanEntity he : hList) {
			if(plugin.getPlayerGame(he.getName())!=null) {
				disable = true;
				break;
			}
		}
		
		if(disable) {
			ItemStack result = event.getRecipe().getResult();
			int id = result.getTypeId();
			
			if(DvZ.instance.isCraftDisabled(id)) {
				event.getInventory().setResult(new ItemStack(Material.AIR));
			}
		}
	}
	@EventHandler
	public void onCraft(CraftItemEvent event) {
		if(event.isCancelled()) return;
		
		boolean disable = false;
		List<HumanEntity> hList = event.getViewers();
		for(HumanEntity he : hList) {
			if(plugin.getPlayerGame(he.getName())!=null) {
				disable = true;
				break;
			}
		}
		
		if(disable) {
			ItemStack result = event.getRecipe().getResult();
			int id = result.getTypeId();
			
			if(DvZ.instance.isCraftDisabled(id)) {
				event.setCancelled(true);
			}
		}
	}
}