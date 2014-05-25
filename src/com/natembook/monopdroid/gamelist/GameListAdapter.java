package com.natembook.monopdroid.gamelist;

import java.util.ArrayList;

import com.natembook.monopdroid.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GameListAdapter extends ArrayAdapter<GameItem> {
    private Context context = null;
    private int layout = 0;

    public GameListAdapter(Context context, int resource, ArrayList<GameItem> objects) {
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
                switch (o.getItemType()) {
                case RECONNECT:
                    nameText.setText("#" + o.getGameId() + ": " + o.getTypeName() + " (" + o.getType() + "): you may be able to rejoin this game");
                    break;
                case CREATE:
                    nameText.setText(o.getTypeName() + " (" + o.getType() + ")");
                    break;
                case JOIN:
                    nameText.setText("#" + o.getGameId() + ": " + o.getTypeName() + " (" + o.getType() + ")");
                    break;
                default:
                case LOADING:
                case ERROR:
                case EMPTY:
                case READY:
                    nameText.setText(this.context.getString(R.string.empty));
                    break;
                }
            }
            if (descrText != null) {
                switch (o.getItemType()) {
                case READY:
                    descrText.setTextColor(Color.LTGRAY);
                    descrText.setText(this.context.getString(R.string.game_list_ready));
                    break;
                case CREATE:
                    descrText.setTextColor(Color.LTGRAY);
                    descrText.setText(this.context.getString(R.string.game_list_create));
                    break;
                case LOADING:
                    descrText.setTextColor(Color.LTGRAY);
                    descrText.setText(this.context.getString(R.string.game_list_loading));
                    break;
                case EMPTY:
                    descrText.setTextColor(Color.LTGRAY);
                    descrText.setText(this.context.getString(R.string.game_list_empty));
                    break;
                case ERROR:
                    descrText.setTextColor(Color.RED);
                    descrText.setText(this.context.getString(R.string.game_list_error));
                    break;
                case RECONNECT:
                    descrText.setTextColor(Color.GREEN);
                    descrText.setText(o.getDescription());
                    break;
                default:
                case JOIN:
                    descrText.setTextColor(Color.WHITE);
                    descrText.setText(o.getDescription());
                    break;
                }
            }
            if (playersText != null) {
                switch (o.getItemType()) {
                case JOIN:
                case RECONNECT:
                    playersText.setText(Integer.toString(o.getPlayers()));
                    break;
                default:
                case CREATE:
                case EMPTY:
                case ERROR:
                case LOADING:
                case READY:
                    playersText.setText(this.context.getString(R.string.empty));
                    break;
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
