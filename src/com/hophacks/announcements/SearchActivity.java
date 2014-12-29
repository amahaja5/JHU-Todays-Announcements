package com.hophacks.announcements;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity {

   public TreeMap<Date, ArrayList<Event>> events;
   public ListView my_listview;
   public ArrayList<Event> searched;
   public int currentindex;
   public boolean init;
   public TextView headerView;
   // dialog to show when item is clicked
   public AlertDialog.Builder info;
	
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.search);
       
       openFile();
       info = new AlertDialog.Builder(this);
       init = true;
       
       Button search = (Button) findViewById(R.id.button1);
		search.setOnClickListener(new OnClickListener() {
           public void onClick(View v) {
        	   EditText e = (EditText) findViewById(R.id.editText1);
        	   searched = search(events, e.getText().toString());
        	   InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        	   imm.hideSoftInputFromWindow(e.getWindowToken(), 0);
        	   if (!init) {
        		   clearListView();
        	   }
        	   generateListView();
           }
       });
   }
   
   private void openFile() {
	   try {
   			FileInputStream fis = openFileInput("data");
   			ObjectInputStream in = new ObjectInputStream(fis);
	        events = (TreeMap<Date, ArrayList<Event>>) in.readObject();
	   	} catch(IOException e) {
	   		e.printStackTrace();
	   	} catch(ClassNotFoundException e) {
	   		e.printStackTrace();
	   	}
   }
   
   private static ArrayList<Event> search(TreeMap<Date, ArrayList<Event>> tree, String s) {

	   ArrayList<Event> events = new ArrayList<Event>();
	  

       for(Date d: tree.descendingKeySet()) {
           for(Event e: tree.get(d)) {
               int n= e.title.toLowerCase().indexOf(s.toLowerCase());
               int n1 = e.des.toLowerCase().indexOf(s.toLowerCase());
               if(n!= -1 || n1 != -1) {
                  events.add(e);
               }
           }
       }
       return events;
   }
   
   private void clearListView() {
	   my_listview = (ListView) findViewById(R.id.id_search_view);
	   my_listview.setAdapter(null);
   }
   
   private void generateListView() {
		
		// make a list view (to display list items)
		my_listview = (ListView) findViewById(R.id.id_search_view);
		
		// add header to list view
		if (init) {
			headerView = new TextView(getApplicationContext());
	        headerView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50);
	        headerView.setGravity(Gravity.CENTER);
		}
       headerView.setText("Search Results");
       if (init) {
       	my_listview.addHeaderView(headerView);
       	init = false;
       }
       // generate a list of items from the data list (names of events)
        if (searched.size() == 0) {
        	Toast.makeText(SearchActivity.this, "No search results found.", Toast.LENGTH_LONG).show();
        }
		String[] items = new String[searched.size()];
		int count = 0;
		for (Event item: searched) {
			items[count++] = item.title;
		} 
		
		// set up the list view to display
	    ArrayAdapter<String> adapter =
	      new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
	    my_listview.setAdapter(adapter);
	    
	    // make list items clickable
       my_listview.setOnItemClickListener(new OnItemClickListener() {
       	public void onItemClick(AdapterView<?> parent, View view,
       		int position, long id) {
       		
       		String selected = ((TextView) view).getText().toString();
       		
       		Event selecteddata = null;
       		
       		for (Event item: searched) {
       			if (item.title.equals(selected)) {
       				selecteddata = item;
       				break;
       			}
       		}
       		
       		if (selecteddata == null)
       			return;
       		
       		final Event eventToAdd = selecteddata;
       	
       		info.setTitle(selecteddata.title);
       		String message = selecteddata.des + "<br/><br/>";
       		message += "URL: " + selecteddata.url + "<br/><br/>";
       		info.setMessage(message);
       		info.setPositiveButton("Close", null);
       		ArrayList<Event> favoriteslist = getFavorites();
    		if (!contains(favoriteslist, eventToAdd)) {
        		info.setNeutralButton("Add to Favorites", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ArrayList<Event> favorites = getFavorites();
						favorites.add(0, eventToAdd);
						writeFavorites(favorites);
						Toast.makeText(SearchActivity.this, "Added to Favorites!", Toast.LENGTH_LONG).show();					
					}
		        });
    		} else {
    			info.setNeutralButton(null, null);
    		}
       		info.setCancelable(true);
       		AlertDialog dialog = info.create();
       		dialog.show();
       		
       		// make URLs in the dialogs clickable
       		TextView t = (TextView) dialog.findViewById(android.R.id.message);
       		t.setText(Html.fromHtml(message));
       		t.setAutoLinkMask(Linkify.WEB_URLS);
       		t.setMovementMethod(LinkMovementMethod.getInstance());
       		
         }
       });
	}
   
   public boolean contains(ArrayList<Event> favorites, Event evt) {
   	for (Event e: favorites) {
   		if (e.des.equals(evt.des) && e.title.equals(evt.title)) {
   			return true;
   		}
   	}
   	return false;
   }
   
   public void writeFavorites(ArrayList<Event> favorites) {
   	try {
	    	FileOutputStream fos = openFileOutput("favorites", Context.MODE_PRIVATE);
	   		ObjectOutputStream out = new ObjectOutputStream(fos);
	   		out.writeObject(favorites);
	   		generateListView();
   	} catch(IOException e) {
   		e.printStackTrace();
   	}
   }
      
   public ArrayList<Event> getFavorites() {
   	ArrayList<Event> favorites_list = null;
   	try {
  			FileInputStream fis = openFileInput("favorites");
  			ObjectInputStream in = new ObjectInputStream(fis);
	        favorites_list = (ArrayList<Event>) in.readObject();
	   	} catch(IOException e) {
	   		try {
		   		FileOutputStream fos = openFileOutput("favorites", Context.MODE_PRIVATE);
		   		ObjectOutputStream out = new ObjectOutputStream(fos);
		   		out.writeObject(new ArrayList<Event>());
		   		favorites_list = new ArrayList<Event>();
	   		} catch(IOException e1) {
	   			e1.printStackTrace();
	   		}
	   	} catch(ClassNotFoundException e) {
	   		e.printStackTrace();
	   	}
   	return favorites_list;
   }
}
