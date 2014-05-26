package com.natembook.monopdroid.board;

import com.natembook.monopdroid.board.surface.BoardViewOverlay;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;

/**
 * A line of chat.
 * @author Nate
 *
 */
public class ChatItem {
    private CharSequence text;
    private String realText = null;
    private int color;
    private BoardViewOverlay overlayType;
    private int objectId;
    private boolean clickable;
    
    public ChatItem(String text, int color) {
        this(text, color, BoardViewOverlay.NONE, -1);
    }

    public ChatItem(String text, int color, BoardViewOverlay overlayType, int objectId) {
        this.realText = text;
        this.color = color;
        this.overlayType = overlayType;
        this.objectId = objectId;
        this.clickable = overlayType != BoardViewOverlay.NONE;
    }

    public CharSequence getText(String timestamp) {
        if (this.text == null) {
            SpannableStringBuilder formattedText = (SpannableStringBuilder) Html.fromHtml(timestamp + realText, null, BoardActivity.tagHandler);
            formattedText.setSpan(new LeadingMarginSpan.Standard(0, 30), 0, formattedText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            this.text = formattedText; 
        }   
        return this.text;
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
}
