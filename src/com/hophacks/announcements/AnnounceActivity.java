package com.hophacks.announcements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AnnounceActivity extends Activity {
	
	public ListView my_listview;
	public String url_content;
	public OnSwipeTouchListener swipeListener;
	public TreeMap<Date, ArrayList<Event>> events;
	public ArrayList<Event> currentlist;
	public ArrayList<Date> dates;
	public int currentindex;
	public boolean init;
	public TextView headerView;
	// dialog to show when item is clicked
	public AlertDialog.Builder info;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.announcements);
        
        url_content = grabURL();
        events = new TreeMap<Date, ArrayList<Event>>();
        info = new AlertDialog.Builder(this);
        currentindex = 0;
        init = true;
        try {
			parseData(url_content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        writeToFile();
        generateListView();
        
        swipeListener = new OnSwipeTouchListener();
        
        my_listview.setOnTouchListener(swipeListener);
        
    }
    
    public void writeToFile() {
    	try {
    		FileOutputStream fos = openFileOutput("data", Context.MODE_PRIVATE);
	    	ObjectOutputStream out = new ObjectOutputStream(fos);
	        out.writeObject(events);
	        out.flush();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    }
    
    public void parseData(String str) throws IOException, ParseException{
        int n1 = str.indexOf("baDate");
        str = str.substring(n1+1);
        while (true) {
        	String temp1 = str.substring(7, str.indexOf("</div>"));
        	Date date = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).parse(temp1);
        	n1 = str.indexOf("baDate");
	
	        if(n1 == -1){
	          temp1 = str;
	          events.put(date, readURLSource(temp1));
	          break;
	        } else {
	          temp1 = str.substring(0, n1);
	          str = str.substring(n1+1);
	           n1 = str.indexOf("baDate");
	           events.put(date, readURLSource(temp1));	
	         }
        }
        
        dates = new ArrayList<Date>(events.descendingKeySet());
    }

    

      private static ArrayList<Event> readURLSource(String str) {
     
          ArrayList<Event> events = new ArrayList<Event>();
     	   int n = str.indexOf("baListTitle");
     	   while (n != -1) {
     	       Event e = new Event();
     	       str = str.substring(n+1);
     	       n = str.indexOf("href=") + 5;
     	       str = str.substring(n+1);
     	       e.url = str.substring(0, str.indexOf("\""));
     	       e.url = Jsoup.parse(e.url).text();
     	       str = str.substring(str.indexOf("\"")+2);
     	       e.title = str.substring(0, str.indexOf("<"));
     	       e.title = Jsoup.parse(e.title).text();
     	       str = str.substring(str.indexOf("baListSum") + 11);
     	       e.des = str.substring(0, str.indexOf("</div>"));
      	       e.des = Jsoup.parse(e.des).text();
     	       n = str.indexOf("baListTitle");
     	       events.add(e);
     	   }
          return events;
     }
    
    private void clearListView() {
    	my_listview = (ListView) findViewById(R.id.id_list_view);
    	my_listview.setAdapter(null);
    }
      
      
    private void generateListView() {
		
		// make a list view (to display list items)
		my_listview = (ListView) findViewById(R.id.id_list_view);
		
		// add header to list view
		if (init) {
			headerView = new TextView(getApplicationContext());
	        headerView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50);
	        headerView.setBackgroundColor(Color.BLUE);
	        headerView.setGravity(Gravity.CENTER);
		}
		Date date = dates.get(currentindex);
		DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
        headerView.setText(dateFormatter.format(date));
        if (init) {
        	my_listview.addHeaderView(headerView);
        	init = false;
        }
        // generate a list of items from the data list (names of events)
        currentlist = events.get(dates.get(currentindex));
		String[] items = new String[currentlist.size()];
		int count = 0;
		for (Event item: currentlist) {
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
        		
        		for (Event item: currentlist) {
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
        		ArrayList<Event> favoriteslist = getFavorites();
        		if (!contains(favoriteslist, eventToAdd)) {
	        		info.setNeutralButton("Add to Favorites", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ArrayList<Event> favorites = getFavorites();
							favorites.add(0, eventToAdd);
							writeFavorites(favorites);
							Toast.makeText(AnnounceActivity.this, "Added to Favorites!", Toast.LENGTH_LONG).show();					
						}
			        });
        		} else {
        			info.setNeutralButton(null, null);
        		}
        		info.setPositiveButton("Close", null);
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
    
    public String grabURL() {
    	GrabURL grab = new GrabURL();
    	grab.execute(grab.URL);
    	while (grab.Content == null) {}
    	return grab.Content;
    }
    
    private class GrabURL extends AsyncTask<String, Void, Void> {
    	private final String URL = "http://web.jhu.edu/announcements/students/?NextStep=&Page_StartAt=1&Page_EndAt=100";
        private final HttpClient Client = new DefaultHttpClient();
        private String Content;
        
        protected void onPreExecute() {
        	Toast.makeText(AnnounceActivity.this, "Loading...", Toast.LENGTH_LONG).show();
        }

        protected Void doInBackground(String... urls) {
            try {
                HttpGet httpget = new HttpGet(urls[0]);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                Content = Client.execute(httpget, responseHandler);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            return null;
        }
        
        protected void onPostExecute(Void unused) {}
        
    }
    
    public class OnSwipeTouchListener implements OnTouchListener {

        @SuppressWarnings("deprecation")
		private final GestureDetector gestureDetector = new GestureDetector(new GestureListener());

        public boolean onTouch(final View view, final MotionEvent motionEvent) {
            return gestureDetector.onTouchEvent(motionEvent);
        }
        
        public GestureDetector getGestureDetector(){
            return gestureDetector;
        }

        private final class GestureListener extends SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
            
            public void onSwipeRight() {
            	if (currentindex - 1 >= 0) { 
            		currentindex--;
            		clearListView();
            		generateListView();
            	}
            }
            public void onSwipeLeft() {
            	if (currentindex + 1 < dates.size()) {
            		currentindex++;
            		clearListView();
            		generateListView();
            	}
            }            
            
        }


    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        swipeListener.getGestureDetector().onTouchEvent(ev); 
            return super.dispatchTouchEvent(ev);   
    }

}
