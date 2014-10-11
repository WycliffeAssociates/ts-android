package com.door43.translationstudio.dialogs;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.door43.translationstudio.R;
import com.door43.translationstudio.SettingsActivity;
import com.door43.translationstudio.util.MainContext;
import com.door43.translationstudio.util.ModalDialog;

/**
 * This menu provides some advanced tools
 */
public class AdvancedSettingsDialog extends ModalDialog {
    private AdvancedSettingsDialog me = this;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_advanced_settings, container, false);

        // hook up buttons
        Button closeBtn = (Button)v.findViewById(R.id.close_advanced_settings_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                me.dismiss();
            }
        });

        Button keysBtn = (Button)v.findViewById(R.id.regenerate_keys_btn);
        keysBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainContext.getContext().generateKeys();
                me.dismiss();
            }
        });

        return v;
    }
}
