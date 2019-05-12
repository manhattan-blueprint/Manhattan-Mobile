package com.manhattan.blueprint.View;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.Utils.SpriteManager;

import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.SurfaceView;

public class HelpPopupFragment extends Fragment {
    View.OnClickListener onClickListener;

    public HelpPopupFragment() { }

    public HelpPopupFragment(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_popup, container, false);
        if (onClickListener != null) {
            view.findViewById(R.id.help_close_button).setOnClickListener(onClickListener);
        }
        return view;
    }
}
