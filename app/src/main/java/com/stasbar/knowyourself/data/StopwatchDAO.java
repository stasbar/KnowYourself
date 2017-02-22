package com.stasbar.knowyourself.data;

/**
 * Created by admin1 on 25.01.2017.
 */

import android.content.Context;
import android.content.SharedPreferences;

import com.stasbar.knowyourself.data.Stopwatch.State;
import com.stasbar.knowyourself.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.stasbar.knowyourself.data.Stopwatch.State.RESET;


/**
 * This class encapsulates the transfer of data between {@link Stopwatch} and {@link Lap} domain
 * objects and their permanent storage in {@link SharedPreferences}.
 */
public final class StopwatchDAO {

    /** Key to a preference that stores the state of the stopwatch. */
    private static final String STATE = "sw_state";

    /** Key to a preference that stores the last start time of the stopwatch. */
    private static final String LAST_START_TIME = "sw_start_time";

    /** Key to a preference that stores the epoch time when the stopwatch last started. */
    private static final String LAST_WALL_CLOCK_TIME = "sw_wall_clock_time";

    /** Key to a preference that stores the accumulated elapsed time of the stopwatch. */
    private static final String ACCUMULATED_TIME = "sw_accum_time";

    /** Prefix for a key to a preference that stores the number of recorded laps. */
    private static final String LAP_COUNT = "sw_lap_num";

    /** Prefix for a key to a preference that stores accumulated time at the end of a lap. */
    private static final String LAP_ACCUMULATED_TIME = "sw_lap_time_";

    private StopwatchDAO() {}

    /**
     * @return the stopwatch from permanent storage or a reset stopwatch if none exists
     */
    static Stopwatch getStopwatch(Context context) {
        final SharedPreferences prefs = Utils.getDefaultSharedPreferences(context);
        final int stateIndex = prefs.getInt(STATE, RESET.ordinal());
        final State state = State.values()[stateIndex];
        final long lastStartTime = prefs.getLong(LAST_START_TIME, Stopwatch.UNUSED);
        final long lastWallClockTime = prefs.getLong(LAST_WALL_CLOCK_TIME, Stopwatch.UNUSED);
        final long accumulatedTime = prefs.getLong(ACCUMULATED_TIME, 0);
        return new Stopwatch(state, lastStartTime, lastWallClockTime, accumulatedTime);
    }

    /**
     * @param stopwatch the last state of the stopwatch
     */
    static void setStopwatch(Context context, Stopwatch stopwatch) {
        final SharedPreferences prefs = Utils.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();

        if (stopwatch.isReset()) {
            editor.remove(STATE)
                    .remove(LAST_START_TIME)
                    .remove(ACCUMULATED_TIME);
        } else {
            editor.putInt(STATE, stopwatch.getState().ordinal())
                    .putLong(LAST_START_TIME, stopwatch.getLastStartTime())
                    .putLong(ACCUMULATED_TIME, stopwatch.getAccumulatedTime());
        }

        editor.apply();
    }

    /**
     * @return a list of recorded laps for the stopwatch
     */
    static List<Lap> getLaps(Context context) {
        final SharedPreferences prefs = Utils.getDefaultSharedPreferences(context);

        // Prepare the container to be filled with laps.
        final int lapCount = prefs.getInt(LAP_COUNT, 0);
        final List<Lap> laps = new ArrayList<>(lapCount);

        long prevAccumulatedTime = 0;

        // Lap numbers are 1-based and so the are corresponding shared preference keys.
        for (int lapNumber = 1; lapNumber <= lapCount; lapNumber++) {
            // Look up the accumulated time for the lap.
            final String lapAccumulatedTimeKey = LAP_ACCUMULATED_TIME + lapNumber;
            final long accumulatedTime = prefs.getLong(lapAccumulatedTimeKey, 0);

            // Lap time is the delta between accumulated time of this lap and prior lap.
            final long lapTime = accumulatedTime - prevAccumulatedTime;

            // Create the lap instance from the data.
            laps.add(new Lap(lapNumber, lapTime, accumulatedTime));

            // Update the accumulated time of the previous lap.
            prevAccumulatedTime = accumulatedTime;
        }

        // Laps are stored in the order they were recorded; display order is the reverse.
        Collections.reverse(laps);

        return laps;
    }

    /**
     * @param newLapCount the number of laps including the new lap
     * @param accumulatedTime the amount of time accumulate by the stopwatch at the end of the lap
     */
    static void addLap(Context context, int newLapCount, long accumulatedTime) {
        final SharedPreferences prefs = Utils.getDefaultSharedPreferences(context);
        prefs.edit()
                .putInt(LAP_COUNT, newLapCount)
                .putLong(LAP_ACCUMULATED_TIME + newLapCount, accumulatedTime)
                .apply();
    }

    /**
     * Remove the recorded laps for the stopwatch
     */
    public static void clearLaps(Context context) {
        final SharedPreferences prefs = Utils.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();

        final int lapCount = prefs.getInt(LAP_COUNT, 0);
        for (int lapNumber = 1; lapNumber <= lapCount; lapNumber++) {
            editor.remove(LAP_ACCUMULATED_TIME + lapNumber);
        }
        editor.remove(LAP_COUNT);

        editor.apply();
    }
}