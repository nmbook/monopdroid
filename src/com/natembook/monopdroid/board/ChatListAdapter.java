package com.natembook.monopdroid.board;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.natembook.monopdroid.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * The adapter to view the list of {@link ChatItem}s.
 * @author Nate
 *
 */
public class ChatListAdapter extends ArrayAdapter<ChatItem> {
    private Context context = null;
    private int layout = 0;

    public ChatListAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
        this.layout = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(this.layout, null);
        }
        ChatItem o = super.getItem(position);
        if (o != null) {
            TextView chat = (TextView) v.findViewById(R.id.chat);
            chat.setText(o.getText(getTimestamp()));
            chat.setTextColor(o.getColor());
            chat.setClickable(!o.isClickable());
            chat.setFocusable(!o.isClickable());
            chat.setEnabled(!o.isClickable());
        }
        return v;
    }
    
    private String getTimestamp() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("gameboard_timestamps", false)) {
            Date d = new Date();
            return "<small><font color=\"#ffffff\">(" + DateFormat.getTimeInstance().format(d) + ")</font></small> ";
        } else {
            return "";
        }
    }

    public ArrayList<ChatItem> saveState() {
        ArrayList<ChatItem> items = new ArrayList<ChatItem>();
        for (int i = 0; i < this.getCount(); i++) {
            items.add(this.getItem(i));
        }
        return items;
    }

    public void restoreState(ArrayList<ChatItem> chatItems) {
        for (ChatItem item : chatItems) {
            add(item);
        }
    }
}
