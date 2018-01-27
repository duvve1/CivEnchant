package com.gmail.sharpcastle33.listeners;

import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.sharpcastle33.enchantments.CustomEnchantment;
import com.gmail.sharpcastle33.enchantments.CustomEnchantmentManager;
import com.gmail.sharpcastle33.util.Util;

import net.md_5.bungee.api.ChatColor;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

public class BlockListener implements Listener{
	
	final static String TIMBER_REINFORCEMENT = ChatColor.RED + "Timber enchantment is disabled for reinforcements.";
	final static Material[] crystals = {Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.QUARTZ_ORE};
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		
		Player player = event.getPlayer();
		Block block = event.getBlock();
		ItemStack mainHand = event.getPlayer().getInventory().getItemInMainHand();
		
		boolean demolished = false;
		boolean smelt = false;
		
		if (mainHand.hasItemMeta()) {
			
			Map<CustomEnchantment, Integer> enchants = CustomEnchantmentManager.getCustomEnchantments(mainHand);
			
			//PICKAXE
			if(Util.isPickaxe(mainHand)) {
				//AUTO SMELT
				if (enchants.containsKey(CustomEnchantment.AUTO_SMELT)) {
					
					smelt = true;
					
					if (event.getBlock().getType().equals(Material.GOLD_ORE)) {
						
						event.setDropItems(false);
						event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.GOLD_INGOT));
						event.getBlock().getWorld().spawn(event.getBlock().getLocation(), ExperienceOrb.class).setExperience(2 * enchants.get(CustomEnchantment.AUTO_SMELT));
						
					}
					
					if (event.getBlock().getType().equals(Material.IRON_ORE)) {
						
						event.setDropItems(false);
						event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.IRON_INGOT));
						event.getBlock().getWorld().spawn(event.getBlock().getLocation(), ExperienceOrb.class).setExperience(1 * enchants.get(CustomEnchantment.AUTO_SMELT));
						
					}
					
				}
				//DEMOLISHING
				if(enchants.containsKey(CustomEnchantment.DEMOLISHING)) {
					demolished = true;
					if(block.getType() == Material.STONE) {
						event.setDropItems(false);
						if(Util.chance(25*enchants.get(CustomEnchantment.DEMOLISHING), 100)) {
							event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.GRAVEL));				
						}
					}
				}
				//CRYSTAL ATTUNEMENT
				if(enchants.containsKey(CustomEnchantment.CRYSTAL_ATTUNEMENT)) {
					for(Material m : crystals) {
						if(block.getType() == m) {
							Util.replacePotionEffect(player, new PotionEffect(PotionEffectType.FAST_DIGGING, 0, 2*enchants.get(CustomEnchantment.CRYSTAL_ATTUNEMENT)));
						}
					}
				}
				//EMERALD RESONANCE
				//is silk touch a problem? Don't think so.
				if(enchants.containsKey(CustomEnchantment.EMERALD_RESONANCE)) {
					if(block.getType() == Material.EMERALD_ORE) {
						int lvl = enchants.get(CustomEnchantment.EMERALD_RESONANCE);
						//dissonance
						if(Util.chance(20 + (10*lvl), 100)) {
							event.setDropItems(false);
							block.getLocation().getWorld().playSound(block.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);
							
							Random rand = new Random();
							
							int amount = 0;
							
							if(lvl == 1) {
								amount += rand.nextInt(1);
							}else if(lvl == 2) {
								amount += rand.nextInt(2);
							}else {
								amount += rand.nextInt(1);
								
								if(Util.chance(50, 100)) {
									amount+=1;
								}
								
								if(Util.chance(5, 100)) {
									amount+=1;
								}
							}
							

							event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), Util.generateItem("EMERALD_FRAGMENT", amount));				
							int exp = event.getExpToDrop();
							event.setExpToDrop((int) (exp*(2.5 + (0.5*lvl))));
						
						}else {
							//resonance

							int exp = event.getExpToDrop();
							
							if(!(enchants.containsKey(CustomEnchantment.SILK_TOUCH))) {
								event.setExpToDrop(((int) (exp*(1.1+(0.15*lvl)))));
								block.getLocation().getWorld().playSound(block.getLocation(), Sound.BLOCK_NOTE_CHIME, 1f, 1f);

							}
							
							
							
							
						}
					}
				}
				
				//PROFICIENT
				if(enchants.containsKey(CustomEnchantment.PROFICIENT)) {
					if(block.getType() == Material.STONE) {
						if(Util.chance(2 + enchants.get(CustomEnchantment.PROFICIENT), 100)) {
							event.setExpToDrop(1);
						}
					}
				}
				//STONEMASON
				if(enchants.containsKey(CustomEnchantment.STONEMASON)) {
					if(!(demolished)) {
						if(block.getType() == Material.STONE) {
							byte data = block.getData();
							
							if(data == 0) {
								if(Util.chance(5*enchants.get(CustomEnchantment.STONEMASON), 100)){
									event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.COBBLESTONE));				
								}
							}else{
								if(Util.chance(20*enchants.get(CustomEnchantment.STONEMASON), 100)){
									event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.STONE,data));				
								}
							}
						}
					}
				}
				//IRON AFFINITY
				if(enchants.containsKey(CustomEnchantment.IRON_AFFINITY)){
					//How do we want to deal with players placing ore?
					//Drop special iron ore fragments every break.
					if(block.getType() == Material.IRON_ORE) {
						
					}
		
				}
				//GOLD AFFINITY
				if (enchants.containsKey(CustomEnchantment.GOLD_AFFINITY)) {
					if(block.getType() == Material.GOLD_ORE) {
						
					}
				}
			}
			
			//AXE
			if(Util.isAxe(mainHand)) {
				//CARPENTRY
				if(enchants.containsKey(CustomEnchantment.CARPENTRY)) {
					//LOGS
					if (block.getType() == Material.LOG) {
						//STICK DROP
						if(Util.chance(5*enchants.get(CustomEnchantment.CARPENTRY), 100)) {
							Random rand = new Random();
							int amt = rand.nextInt(enchants.get(CustomEnchantment.CARPENTRY))+1;		
							event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.STICK,amt));				
						}
						//FENCE DROP
						if(Util.chance(2*enchants.get(CustomEnchantment.CARPENTRY), 100)) {
							Byte data = block.getData();
							Random rand = new Random();
							int amt = rand.nextInt(enchants.get(CustomEnchantment.CARPENTRY)/2)+1;		
							event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.FENCE,amt,data));				
						}
						
						
					//LOG_2s	
					}else if (block.getType() == Material.LOG_2) {
						//STICK DROP
						if(Util.chance(5*enchants.get(CustomEnchantment.CARPENTRY), 100)) {
							Random rand = new Random();
							int amt = rand.nextInt(enchants.get(CustomEnchantment.CARPENTRY))+1;		
							event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.STICK,amt));				
						}
						//FENCE DROP
						if(Util.chance(2*enchants.get(CustomEnchantment.CARPENTRY), 100)) {
							Byte data = block.getData();
							Random rand = new Random();
							int amt = rand.nextInt(enchants.get(CustomEnchantment.CARPENTRY)/2)+1;		
							event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.FENCE,amt,data));				
						}
					}
					
					
				}
				//TIMBER
				if (enchants.containsKey(CustomEnchantment.TIMBER)) {
					if (block.getType() == Material.LOG || block.getType() == Material.LOG_2) {
						
						
						
						double x = block.getX();
						double y = block.getY();
						double z = block.getZ();
						
						for (double i = y; i < (y + (enchants.get(CustomEnchantment.TIMBER) * 3)); i++) {
							
							Location location = new Location(block.getWorld(), x, i, z);
							Reinforcement reinfrocement = Citadel.getCitadelDatabase().getReinforcement(location);
							
							if (!(reinfrocement == null)) {
								
								player.sendMessage(TIMBER_REINFORCEMENT);
								
								break;
								
							}
							
							else {
							
								if (location.getBlock().getType() == Material.LOG || location.getBlock().getType() == Material.LOG_2) {
								
									location.getBlock().breakNaturally();
								
									if (enchants.containsKey(CustomEnchantment.UNBREAKING)) {
									
										double random = Math.random();
										double unbreakingLvl = enchants.get(CustomEnchantment.UNBREAKING);
									
										if (random < (1 / unbreakingLvl)) {
										
											mainHand.setDurability((short) (mainHand.getDurability() + 1));
										
										}
									}
								
									else {
									
										mainHand.setDurability((short) (mainHand.getDurability() + 1));
									
									}
								}
								
								else {
									break;
								}
							}
							
						}
						
					}
				}	
			}
			
			if(Util.isShovel(mainHand)) {
				
			}
			
			if(Util.isHoe(mainHand)) {
				
			}
			
			
			
			

			
		}
		
	}

}