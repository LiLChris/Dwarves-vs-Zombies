package me.andre111.dvz.item.spell;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import me.andre111.dvz.Game;
import me.andre111.dvz.item.ItemSpell;

public class ItemExplode extends ItemSpell {
	private float power = 6F;
	private boolean kill = true;
	
	@Override
	public void setCastVar(int id, double var) {
		if(id==0) power = (float) var;
		else if(id==1) kill = (var==1);
	}
	
	@Override
	public boolean cast(Game game, Player player) {	
		World w = player.getWorld();
		Location loc = player.getLocation();
		
		if(kill) {
			player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
			player.damage((double) 10000);
		}
		
		w.createExplosion(loc, power);
		w.createExplosion(loc, power);
		
		return true;
	}
	
	@Override
	public boolean cast(Game game, Player player, Block block) {
		return cast(game, player);
	}
	@Override
	public boolean cast(Game game, Player player, Player target) {
		return cast(game, player);
	}
	
	@Override
	public boolean cast(Game game, Player player, Location target) {
		World w = target.getWorld();
		
		if(kill) {
			player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
			player.damage((double) 10000);
		}
		
		w.createExplosion(target, power);
		w.createExplosion(target, power);
		
		return true;
	}
}
