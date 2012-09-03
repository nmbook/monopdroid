package edu.rochester.nbook.monopdroid.board;

import java.util.ArrayList;
import java.util.List;

import edu.rochester.nbook.monopdroid.R;
import edu.rochester.nbook.monopdroid.board.BoardActivity.BoardActivityState;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
            chat.setText(o.getText());
            chat.setTextColor(o.getColor());
            chat.setClickable(!o.isClickable());
            chat.setFocusable(!o.isClickable());
            chat.setEnabled(!o.isClickable());
        }
        return v;
    }
    
    public List<ChatItem> saveState() {
        List<ChatItem> items = new ArrayList<ChatItem>();
        for (int i = 0; i < this.getCount(); i++) {
            items.add(this.getItem(i));
        }
        return items;
    }

    public void restoreState(BoardActivityState state) {
        List<ChatItem> items = state.chat;
        for (ChatItem item : items) {
            add(item);
        }
    }
}
