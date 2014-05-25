package com.natembook.monopdroid.dialogs;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;

import com.natembook.monopdroid.board.BoardActivity;

public class MonopolyDialogObjectItem {
    private int objectId;
    private CharSequence name;
    private CharSequence subtext = "";

    public MonopolyDialogObjectItem(int objectId, String name, String subtext) {
        // TODO stop using HTML formatter
        this.objectId = objectId;
        
        SpannableStringBuilder formattedName = (SpannableStringBuilder) Html.fromHtml(name, null, BoardActivity.tagHandler);
        formattedName.setSpan(new LeadingMarginSpan.Standard(0, 30), 0, formattedName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        this.name = formattedName;
        
        if (subtext != null) {
            SpannableStringBuilder formattedSubtext = (SpannableStringBuilder) Html.fromHtml(subtext, null, BoardActivity.tagHandler);
            formattedSubtext.setSpan(new LeadingMarginSpan.Standard(0, 30), 0, formattedSubtext.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            this.subtext = formattedSubtext;
        }
    }

    public int getObjectId() {
        return objectId;
    }

    public CharSequence getName() {
        return name;
    }

    public CharSequence getSubtext() {
        return subtext;
    }
}
