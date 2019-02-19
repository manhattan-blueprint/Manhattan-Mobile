package com.manhattan.blueprint.View;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.manhattan.blueprint.R;

public class PermissionFragment extends Fragment {

    private String emoji;
    private String title;
    private String description;
    private View.OnClickListener onClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup fragment = (ViewGroup) inflater.inflate(R.layout.fragment_permission, container, false);
        // Configure
//        TextView emojiText = fragment.findViewById(R.id.permissionEmoji);
        TextView titleText = fragment.findViewById(R.id.permissionTitle);
        TextView descriptionText = fragment.findViewById(R.id.permissionDescription);
        Button permissionAccess = fragment.findViewById(R.id.permissionAccess);

//        emojiText.setText(emoji);
        titleText.setText(title);
        descriptionText.setText(description);
        permissionAccess.setOnClickListener(onClickListener);

        return fragment;
    }

    public void setConfiguration(String emoji, String title, String description, View.OnClickListener onClickListener) {
        this.emoji = emoji;
        this.title = title;
        this.description = description;
        this.onClickListener = onClickListener;
    }
}
