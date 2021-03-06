package com.natembook.monopdroid.dialogs;

import com.natembook.monopdroid.R;
import com.natembook.monopdroid.board.BoardActivity;
import com.natembook.monopdroid.board.TradeOfferType;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;

public class MonopolyDialog extends DialogFragment {
    private MonopolyDialogListener listener = null;
    private EditText editText = null;
    
    private void setMonopolyDialogListener(MonopolyDialogListener listener) {
        this.listener = listener;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = null;
        Context context = getActivity();
        final Bundle arguments = getArguments();
        int dialogType = arguments.getInt("dialogType");
        String title = arguments.getString("title");
        String message = arguments.getString("message");
        boolean allowBack = arguments.getBoolean("allowBack");
        Log.v("monopd", "dialog create: " + arguments.toString());
        
        if (getActivity() instanceof BoardActivity) {
            // if this is a BoardActivity, it can listen to callbacks
            // the GameListActivity doesn't have any (info and error are ok-only)
            setMonopolyDialogListener((BoardActivity) getActivity());
        }
        
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        int layoutId = -1;
        switch (dialogType) {
        default:
            break;
        case R.id.dialog_type_prompt_name:
            layoutId = R.layout.dialog_prompt_name;
            break;
        case R.id.dialog_type_prompt_money:
            layoutId = R.layout.dialog_prompt_money;
            break;
        }
        if (layoutId >= 0) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(layoutId, null);
            b.setView(v);
        }
        
        b.setTitle(title);
        
        if (allowBack) {
            b.setNeutralButton(R.string.previous_step, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onDialogPreviousStep(arguments);
                    dismiss();
                }
            });
        }
        
        final View view = v;
        
        switch (dialogType) {
        case R.id.dialog_type_prompt_name:
            editText = (EditText) view.findViewById(R.id.dialog_edit_field);
            final int maxLength = arguments.getInt("maxLength");
            if (maxLength > 0) {
                editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxLength) });
            }
            // TODO something something empty text box
            String defaultName = arguments.getString("default");
            if (defaultName != null) {
                editText.setText(defaultName);
                editText.selectAll();
            }
            b.setMessage(message);
            b.setPositiveButton(android.R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (editText.length() > 0) {
                        listener.onDialogEnterName(editText.getText().toString(), arguments);
                    }
                    dismiss();
                }
            });
            b.setNegativeButton(android.R.string.cancel, new DismissingOnClickListener());
            b.setIcon(android.R.drawable.ic_dialog_info);
            break;
        case R.id.dialog_type_prompt_money:
            editText = (EditText) view.findViewById(R.id.dialog_edit_field);
            // final int minMoney = arguments.getInt("min");
            // TODO validate
            int defaultMoney = arguments.getInt("default");
            String defaultMoneyS = Integer.toString(defaultMoney);
            editText.setText(defaultMoneyS);
            editText.selectAll();
            b.setMessage(message);
            b.setPositiveButton(android.R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        int intValue = Integer.parseInt(editText.getText().toString());
                        listener.onDialogEnterMoney(intValue, arguments);
                    } catch (NumberFormatException nfex) { }
                    dismiss();
                }
            });
            b.setNegativeButton(android.R.string.cancel, new DismissingOnClickListener());
            b.setIcon(android.R.drawable.ic_dialog_info);
            break;
        case R.id.dialog_type_prompt_tradetype:
            final TradeOfferType[] types = new TradeOfferType[] { TradeOfferType.MONEY, TradeOfferType.ESTATE, TradeOfferType.CARD };
            ArrayAdapter<TradeOfferType> adapter = new ArrayAdapter<TradeOfferType>(
                    context,
                    android.R.layout.select_dialog_item,
                    types);
            b.setAdapter(adapter, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onDialogChooseTradeType(types[which], arguments);
                    dismiss();
                }
            });
            b.setNegativeButton(android.R.string.cancel, new DismissingOnClickListener());
            b.setIcon(android.R.drawable.ic_dialog_info);
            break;
        case R.id.dialog_type_prompt_objectlist:
            final int itemCount = arguments.getInt("itemCount");
            if (itemCount == 0) {
                b.setMessage(message);
            } else {
                final MonopolyDialogObjectItem[] objects = new MonopolyDialogObjectItem[itemCount];
                for (int i = 0; i < itemCount; i++) {
                    objects[i] = new MonopolyDialogObjectItem(arguments.getInt("itemId_" + i),
                            arguments.getString("itemName_" + i),
                            arguments.getString("itemSubtext_" + i));
                }
                MonopolyDialogObjectAdapter objectAdapter =
                        new MonopolyDialogObjectAdapter(context, R.layout.game_item, objects);
                b.setAdapter(objectAdapter, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogChooseItem(objects[which].getObjectId(), arguments);
                        dismiss();
                    }
                });
            }
            b.setNegativeButton(android.R.string.cancel, new DismissingOnClickListener());
            b.setIcon(android.R.drawable.ic_dialog_info);
            break;
        case R.id.dialog_type_info:
            b.setMessage(message);
            b.setPositiveButton(android.R.string.ok, new DismissingOnClickListener());
            b.setIcon(android.R.drawable.ic_dialog_info);
            break;
        case R.id.dialog_type_error:
            b.setMessage(message);
            b.setNegativeButton(android.R.string.ok, new DismissingOnClickListener());
            b.setIcon(android.R.drawable.ic_dialog_alert);
            break;
        case R.id.dialog_type_reconnect:
            b.setMessage(message);
            b.setPositiveButton(R.string.reconnect, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onDialogReconnect(arguments);
                    dismiss();
                }
            });
            b.setNegativeButton(R.string.quit, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onDialogQuit(arguments);
                    dismiss();
                }
            });
            b.setCancelable(false);
            b.setIcon(android.R.drawable.ic_dialog_alert);
            break;
        case R.id.dialog_type_confirmquit:
            b.setMessage(message);
            b.setPositiveButton(android.R.string.yes, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onDialogConfirmQuit(arguments);
                    dismiss();
                }
            });
            b.setNegativeButton(android.R.string.no, new DismissingOnClickListener());
            b.setIcon(android.R.drawable.ic_dialog_alert);
            break;
        }
        return b.create();
    }
    
    @Override
    public void onDismiss(DialogInterface dialog) {
        if (editText != null) {
            hideKeyboard(editText);
        }
        if (listener != null) {
            listener.onDialogDismiss();
        }
    }
    
    @Override
    public void onCancel(DialogInterface dialog) {
        dismiss();
    }

    /**
     * Call to auto-hide the keyboard when closing the dialog.
     * @param editText The text field.
     */
    private void hideKeyboard(EditText editText) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }
    
    /**
     * Shows a new dialog on the given Activity. Call this when you want to show a dialog.
     * <p>
     * Required arguments:<br>
     * int "dialogType": A R.id.dialog_type_* value identifying how to display the dialog.<br>
     * String "title": The text to display in the title.<br>
     * String "message": The text to display in the message/prompt area.
     * <p>
     * Optional arguments for {@link R.id.dialog_type_enter_value}:<br>
     * String "defaultValue": The value to put in the text box when it is opened.
     * @param dialogHost The hosting activity.
     * @param args The arguments. See above. 
     * @param listener An optional listener to call when the user submits information through the dialog.
     */
    public static MonopolyDialog showNewDialog(MonopolyDialogHost dialogHost,
            Bundle args) {
        Log.v("monopd", "dialog new: " + args.toString());
        MonopolyDialog dialog = dialogHost.getCurrentDialog();
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = new MonopolyDialog();
        dialog.setArguments(args);
        if (dialogHost.isRunning()) {
            dialog.show(dialogHost.getSupportFragmentManager(), null);
        }
        return dialog;
    }
    
    private class DismissingOnClickListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dismiss();
        }
    }
}
