package net.preoccupied.bukkit.weather;

import java.util.Random;
import org.bukkit.util.config.Configuration;


class WeatherSetting {

    Random rand;

    String world;

    int rain_schedule = 0;
    int rain_schedule_fudge = 0;
    int rain_duration = 0;
    int rain_duration_fudge = 0;

    int thunder_schedule = 0;
    int thunder_schedule_fudge = 0;
    int thunder_duration = 0;
    int thunder_duration_fudge = 0;


    WeatherSetting(String worldname) {
	this.world = worldname;
	this.rand = new Random();
    }


    String getWorld() {
	return world;
    }


    private int fudge(int a, int b) {
	return a + ((b <= 0)? 0: rand.nextInt(b));
    }


    int getRainScheduleTicks() {
	return fudge(rain_schedule, rain_schedule_fudge) * 20;
    }


    int getRainDurationTicks() {
	return fudge(rain_duration, rain_duration_fudge) * 20;
    }


    int getThunderScheduleTicks() {
	return fudge(thunder_schedule, thunder_schedule_fudge) * 20;
    }


    int getThunderDurationTicks() {
	return fudge(thunder_duration, thunder_duration_fudge) * 20;
    }


    void load(Configuration conf) {
	rain_schedule = conf.getInt("rain.schedule", rain_schedule);
	rain_schedule_fudge = conf.getInt("rain.schedulefudge", rain_schedule_fudge);
	rain_duration = conf.getInt("rain.duration", rain_duration);
	rain_duration_fudge = conf.getInt("rain.durationfudge", rain_duration_fudge);

	thunder_schedule = conf.getInt("thunder.schedule", thunder_schedule);
	thunder_schedule_fudge = conf.getInt("thunder.schedulefudge", thunder_schedule_fudge);
	thunder_duration = conf.getInt("thunder.duration", thunder_duration);
	thunder_duration_fudge = conf.getInt("thunder.durationfudge", thunder_duration_fudge);
    }

}


/* The end. */
