package com.natembook.monopdroid.dialogs;

import com.natembook.monopdroid.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MonopolyDialogObjectAdapter extends
        ArrayAdapter<MonopolyDialogObjectItem> {
    private Context context = null;
    private int layout = 0;

    public MonopolyDialogObjectAdapter(Context context, int resource, MonopolyDialogObjectItem[] objects) {
        super(context, resource, objects);
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
        MonopolyDialogObjectItem o = this.getItem(position);
        if (o != null) {
            TextView nameText = (TextView) v.findViewById(R.id.game_item_descr);
            TextView subtextText = (TextView) v.findViewById(R.id.game_item_name);
            TextView rightSideText = (TextView) v.findViewById(R.id.game_item_players);
            nameText.setText(o.getName());
            subtextText.setText(o.getSubtext());
            rightSideText.setText("");
        }
        return v;
    }

    @Override
    public long getItemId(int position) {
        MonopolyDialogObjectItem o = this.getItem(position);
        return o.getObjectId();
    }
}
