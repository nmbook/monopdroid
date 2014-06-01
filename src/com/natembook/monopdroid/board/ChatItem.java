package com.natembook.monopdroid.board;

import java.util.Calendar;

import com.natembook.monopdroid.board.surface.BoardViewOverlay;

import android.content.Context;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.LeadingMarginSpan;

/**
 * A line of chat.
 * @author Nate
 *
 */
public class ChatItem {
    private CharSequence text;
    private Calendar timestamp;
    private String realText = null;
    private int color;
    private BoardViewOverlay overlayType;
    private int objectId;
    private boolean clickable;
    private boolean firstOfDay;
    
    private static Calendar lastDate; 
    
    public ChatItem(String text, int color, Calendar timestamp) {
        this(text, color, timestamp, BoardViewOverlay.NONE, -1);
    }

    public ChatItem(String text, int color, Calendar timestamp, BoardViewOverlay overlayType, int objectId) {
        this.timestamp = timestamp;
        this.realText = text;
        this.color = color;
        this.overlayType = overlayType;
        this.objectId = objectId;
        this.clickable = overlayType != BoardViewOverlay.NONE;
        
        this.firstOfDay = false;
        if (lastDate != null) {
            if (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != timestamp.get(Calendar.DAY_OF_YEAR) ||
                    Calendar.getInstance().get(Calendar.YEAR) != timestamp.get(Calendar.YEAR)) {
                this.firstOfDay = true;
            }
        }
        lastDate = timestamp;
    }

    public CharSequence getText(boolean showTimestamps, boolean firstTimestamp, Context context) {
        //if (this.text == null) {
            SpannableStringBuilder formattedText = (SpannableStringBuilder) Html.fromHtml(getTimestamp(showTimestamps, firstTimestamp, context) + realText, null, BoardActivity.tagHandler);
            formattedText.setSpan(new LeadingMarginSpan.Standard(0, 30), 0, formattedText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            this.text = formattedText; 
        //}
        return this.text;
    }

    private String getTimestamp(boolean showTimestamps, boolean firstTimestamp, Context context) {
        if (showTimestamps) {
            String timeString = DateFormat.getTimeFormat(context).format(timestamp.getTime());
            if (firstTimestamp) {
                timeString += " on " + DateFormat.getDateFormat(context).format(timestamp.getTime());
            }
            return "<small><font color=\"#ffffff\">(" +
                    timeString +
                    ") </font></small>";
        } else {
            return "";
        }
    }

    public int getColor() {
        return this.color;
    }
    
    public BoardViewOverlay getOverlayType() {
        return this.overlayType;
    }

    public int getObjectId() {
        return this.objectId;
    }

    public boolean isClickable() {
        return this.clickable;
    }

    public boolean isFirstOfDay() {
        return this.firstOfDay;
    }
}
