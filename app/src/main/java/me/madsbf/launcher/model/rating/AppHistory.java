package me.madsbf.launcher.model.rating;

import android.content.SharedPreferences;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Calendar;

import me.madsbf.launcher.model.entities.AppHistoryEntry;

public class AppHistory {

    final Multimap<Integer, AppHistoryEntry> hourEntries;
    final Multimap<Integer, AppHistoryEntry> dayEntries;

    public AppHistory(SharedPreferences sharedPreferences) {
        hourEntries = HashMultimap.create();
        dayEntries = HashMultimap.create();
    }

    private void appOpened(AppHistoryEntry entry) {
        hourEntries.put(entry.getDate().get(Calendar.HOUR_OF_DAY), entry);
        dayEntries.put(entry.getDate().get(Calendar.DAY_OF_MONTH), entry);
    }

    public void appOpened(String packageName, Calendar date) {
        appOpened(new AppHistoryEntry(packageName, date));
    }
}
