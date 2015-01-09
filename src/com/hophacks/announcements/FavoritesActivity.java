package com.hophacks.announcements;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FavoritesActivity extends Activity {

    ArrayList<Event> favorites;
    public ListView my_listview;
    public int currentindex;
    public boolean init;
    public TextView headerView;
    // dialog to show when item is clicked
    public AlertDialog.Builder info;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites);
        favorites = getFavorites();
        info = new AlertDialog.Builder(this);
        init = true;
        generateListView();
    }

    public ArrayList<Event> getFavorites() {
        ArrayList<Event> favorites_list = null;
        try {
            FileInputStream fis = openFileInput("favorites");
            ObjectInputStream in = new ObjectInputStream(fis);
            favorites_list = (ArrayList<Event>) in.readObject();
        } catch (IOException e) {
            try {
                FileOutputStream fos = openFileOutput("favorites",
                        Context.MODE_PRIVATE);
                ObjectOutputStream out = new ObjectOutputStream(fos);
                out.writeObject(new ArrayList<Event>());
                favorites_list = new ArrayList<Event>();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return favorites_list;
    }

    private void generateListView() {

        // make a list view (to display list items)
        my_listview = (ListView) findViewById(R.id.id_favorites_view);

        // add header to list view
        if (init) {
            headerView = new TextView(getApplicationContext());
            headerView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50);
            headerView.setGravity(Gravity.CENTER);
        }
        headerView.setText("Favorites");
        if (init) {
            my_listview.addHeaderView(headerView);
            init = false;
        }
        // generate a list of items from the data list (names of events)
        String[] items = new String[favorites.size()];
        int count = 0;
        for (Event item : favorites) {
            items[count++] = item.title;
        }

        // set up the list view to display
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);
        my_listview.setAdapter(adapter);

        // make list items clickable
        my_listview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {

                String selected = ((TextView) view).getText().toString();

                Event selecteddata = null;

                for (Event item : favorites) {
                    if (item.title.equals(selected)) {
                        selecteddata = item;
                        break;
                    }
                }

                if (selecteddata == null)
                    return;

                final Event toRemove = selecteddata;

                info.setTitle(selecteddata.title);
                String message = selecteddata.des + "<br/><br/>";
                message += "URL: " + selecteddata.url + "<br/><br/>";
                info.setMessage(message);
                info.setNeutralButton("Remove from Favorites",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                Toast.makeText(FavoritesActivity.this,
                                        "Removed from Favorites!",
                                        Toast.LENGTH_LONG).show();
                                removeFavorite(toRemove);
                            }
                        });
                info.setPositiveButton("Close", null);
                info.setCancelable(true);
                AlertDialog dialog = info.create();
                dialog.show();

                // make URLs in the dialogs clickable
                TextView t = (TextView) dialog
                        .findViewById(android.R.id.message);
                t.setText(Html.fromHtml(message));
                t.setAutoLinkMask(Linkify.WEB_URLS);
                t.setMovementMethod(LinkMovementMethod.getInstance());

            }
        });
    }

    public void removeFavorite(Event e) {
        favorites.remove(e);
        writeFavorites(favorites);
    }

    public void writeFavorites(ArrayList<Event> favorites) {
        try {
            FileOutputStream fos = openFileOutput("favorites",
                    Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(favorites);
            generateListView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
