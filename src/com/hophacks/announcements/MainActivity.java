package com.hophacks.announcements;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
 
@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {
 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
 
		TabHost tabHost = getTabHost(); 
 		
		// Announce tab
		Intent intentAnnounce = new Intent().setClass(this, AnnounceActivity.class);
		TabSpec tabSpecAnnounce = tabHost
		  .newTabSpec("Events")
		  .setIndicator("Events")
		  .setContent(intentAnnounce);
 
		// Search tab
		Intent intentSearch = new Intent().setClass(this, SearchActivity.class);
		TabSpec tabSpecSearch = tabHost
		  .newTabSpec("Search")
		  .setIndicator("Search")
		  .setContent(intentSearch);
 
		// Favorites tab
		Intent intentFavorites = new Intent().setClass(this, FavoritesActivity.class);
		TabSpec tabSpecFavorites = tabHost
		  .newTabSpec("Favorites")
		  .setIndicator("Favorites")
		  .setContent(intentFavorites.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
 
		// add all tabs 
		tabHost.addTab(tabSpecAnnounce);
		tabHost.addTab(tabSpecSearch);
		tabHost.addTab(tabSpecFavorites);
 
		//set Announcement tab as default (zero based)
		tabHost.setCurrentTab(0);
	}
 
}