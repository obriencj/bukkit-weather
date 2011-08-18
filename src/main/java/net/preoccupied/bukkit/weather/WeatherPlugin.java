package net.preoccupied.bukkit.weather;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import net.preoccupied.bukkit.permissions.PermissionCommand;


/**
   A very simple weather plugin for Bukkit

   @author Christopher O'Brien <obriencj@gmail.com>
 */
public class WeatherPlugin extends JavaPlugin {


    private Map<String, WeatherSetting> worldSettings = null;



    public void onEnable() {
	System.out.println("Weather.onEnable()");

	loadWorlds();

	PluginManager pm = getServer().getPluginManager();

	EventExecutor ee;
	ee = new EventExecutor() {
		public void execute(Listener ignored, Event e) {
		    onPlayerInteract((PlayerInteractEvent) e);
		}
	    };
	pm.registerEvent(Event.Type.PLAYER_INTERACT, null, ee, Priority.Normal, this);

	setupCommands();

	delayBegin();
    }



    public void onDisable() {
	;
    }



    private void loadWorlds() {
	worldSettings = new HashMap<String, WeatherSetting>();

	if(getDataFolder().mkdirs())
	    return;

	for(File f : getDataFolder().listFiles()) {
	    String wn = f.getName();
	    if(wn.endsWith(".yml")) {
		wn = wn.substring(0, wn.length() - 4);

		Configuration conf = new Configuration(f);
		conf.load();

		WeatherSetting setting = new WeatherSetting(wn);
		setting.load(conf);

		worldSettings.put(wn, setting);
	    }
	}
    }



    private void delayBegin() {
	/* Simple trick to make all of this happen long after all
	   other plugins have finished their business. */

	Runnable task = new Runnable() {
		public void run() {
		    begin();
		}
	    };

	getServer().getScheduler().scheduleSyncDelayedTask(this, task);
    }
    

    
    private void begin() {
	for(Map.Entry<String,WeatherSetting> entry : worldSettings.entrySet()) {
	    WeatherSetting setting = entry.getValue();
	    World w = getServer().getWorld(setting.getWorld());
	    if(w != null) {
		w.setWeatherDuration(0);
		w.setThunderDuration(0);
		w.setStorm(false);
		w.setThundering(false);

		scheduleRain(setting);
		scheduleThunder(setting);
	    }
	}
    }



    private void scheduleRain(WeatherSetting setting) {
	scheduleRain(setting, true, setting.getRainScheduleTicks());
    }


    
    private void scheduleRain(final WeatherSetting setting, final boolean raining, int ticks) {
	if(ticks <= 0) return;

	Runnable task = new Runnable() {
		public void run() {
		    World w = getServer().getWorld(setting.getWorld());
		    if(w != null) {
			w.setStorm(raining);

			int changeticks = 0;
			if(raining) {
			    changeticks = setting.getRainDurationTicks();
			} else {
			    changeticks = setting.getRainScheduleTicks();
			}

			scheduleRain(setting, !raining, changeticks);
		    }
		}
	    };

	getServer().getScheduler().scheduleSyncDelayedTask(this, task, ticks);
    }



    private void scheduleThunder(WeatherSetting setting) {
	scheduleThunder(setting, true, setting.getThunderScheduleTicks());
    }


    
    private void scheduleThunder(final WeatherSetting setting, final boolean thundering, int ticks) {
	if(ticks <= 0) return;

	Runnable task = new Runnable() {
		public void run() {
		    World w = getServer().getWorld(setting.getWorld());
		    if(w != null) {
			w.setThundering(thundering);

			int changeticks = 0;
			if(thundering) {
			    changeticks = setting.getThunderDurationTicks();
			} else {
			    changeticks = setting.getThunderScheduleTicks();
			}

			scheduleThunder(setting, !thundering, changeticks);
		    }
		}
	    };

	getServer().getScheduler().scheduleSyncDelayedTask(this, task, ticks);
    }


    
    private void onPlayerInteract(PlayerInteractEvent pie) {
	if((pie.getAction() == Action.RIGHT_CLICK_AIR) &&
	   (pie.hasItem()) &&
	   (pie.getItem().getTypeId() == 317)) {

	    Player p = pie.getPlayer();
	    if(p.hasPermission("preoccupied.weather.wand")) {
		Block b = p.getTargetBlock(null, 500);
		if(b != null) {
		    if(p.hasPermission("preoccupied.weather.damage")) {
			p.getWorld().strikeLightning(b.getLocation());
		    } else {
			p.getWorld().strikeLightningEffect(b.getLocation());
		    }
		}
	    }
	}
    }



    private enum WeatherKeyword {
	CLEAR,
	    STORM, STORMY, STORMING,
	    RAIN, RAINY, RAINING,
	    THUNDER, THUNDERING, LIGHTNING,
	    DAWN, SUNRISE, MORNING,
	    DAY, NOON,
	    AFTERNOON, SUNSET,
	    NIGHT, MIDNIGHT,
    };



    private void setupCommands() {

	new PermissionCommand(this, "weather") {
	    public boolean run(Player p, String[] args) {
		World w = p.getWorld();

		for(String a : args) {
		    try {
			a = a.toUpperCase();

			switch(WeatherKeyword.valueOf(a)) {
			case CLEAR:
			    w.setStorm(false);
			    w.setThundering(false);
			    break;

			case STORM:
			case STORMY:
			case STORMING:
			case RAIN:
			case RAINY:
			case RAINING:
			    w.setStorm(true);
			    break;

			case THUNDER:
			case THUNDERING:
			case LIGHTNING:
			    w.setThundering(true);
			    break;

			case DAWN:
			case SUNRISE:
			case MORNING:
			    w.setTime(23000);
			    break;

			case DAY:
			case NOON:
			    w.setTime(6000);
			    break;

			case AFTERNOON:
			case SUNSET:
			    w.setTime(12000);
			    break;

			case NIGHT:
			case MIDNIGHT:
			    w.setTime(18000);
			    break;
			}

		    } catch(Exception e) {
			;
		    }
		}

		return true;
	    }
	};

    }



}


/* The end. */
