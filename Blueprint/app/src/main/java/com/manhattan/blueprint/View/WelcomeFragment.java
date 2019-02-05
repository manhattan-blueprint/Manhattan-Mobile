package com.manhattan.blueprint.View;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.manhattan.blueprint.R;

public class WelcomeFragment extends Fragment {

    private View.OnClickListener onClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup fragment = (ViewGroup) inflater.inflate(R.layout.fragment_welcome, container, false);

        Button permissionAccess = fragment.findViewById(R.id.permissionAccess);
        permissionAccess.setOnClickListener(onClickListener);

        return fragment;
    }

    public void setConfiguration(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
