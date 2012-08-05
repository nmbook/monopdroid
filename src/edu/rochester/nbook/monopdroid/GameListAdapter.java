package edu.rochester.nbook.monopdroid;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GameListAdapter extends ArrayAdapter<GameItem> {
    private Context context = null;
    private int layout = 0;

    public GameListAdapter(Context context, int resource, List<GameItem> objects) {
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
        GameItem o = this.getItem(position);
        if (o != null) {
            TextView nameText = (TextView) v.findViewById(R.id.game_item_name);
            TextView descrText = (TextView) v.findViewById(R.id.game_item_descr);
            TextView playersText = (TextView) v.findViewById(R.id.game_item_players);
            if (nameText != null) {
                if (o.getGameId() <= 0 && o.getGameId() != -1) {
                    nameText.setText(this.context.getString(R.string.empty));
                } else {
                    nameText.setText("Game type: " + o.getTypeName() + " (" + o.getType() + ")");
                }
            }
            if (descrText != null) {
                if (o.getGameId() == 0) { // tap to refresh
                    descrText.setText(this.context.getString(R.string.game_list_ready));
                } else if (o.getGameId() == -1) { // tap to create
                    descrText.setText(this.context.getString(R.string.game_list_create));
                } else if (o.getGameId() == -2) { // loading
                    descrText.setText(this.context.getString(R.string.game_list_loading));
                } else if (o.getGameId() == -3) { // empty, tap to refresh
                    descrText.setText(this.context.getString(R.string.game_list_empty));
                } else if (o.getGameId() == -4) { // error, tap to retry
                    descrText.setText(this.context.getString(R.string.game_list_error));
                } else {
                    descrText.setText(o.getDescription());
                }
            }
            if (playersText != null) {
                if (o.getGameId() <= 0) {
                    playersText.setText(this.context.getString(R.string.empty));
                } else {
                    playersText.setText(Integer.toString(o.getPlayers()));
                }
            }
        }
        return v;
    }

    @Override
    public long getItemId(int position) {
        GameItem o = this.getItem(position);
        return o.getGameId();
    }
}
