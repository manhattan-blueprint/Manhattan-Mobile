package com.manhattan.blueprint.View;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.manhattan.blueprint.R;

import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.RajawaliSurfaceView;

public class ModelFragment extends Fragment {
    private static final String MODELID = "modelID";

   ModelRenderer renderer;

    private int modelID;

    public ModelFragment() {
        // Required empty public constructor
    }

    public static ModelFragment newInstance(int modelID) {
        ModelFragment fragment = new ModelFragment();
        Bundle args = new Bundle();
        args.putInt(MODELID, modelID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            modelID = getArguments().getInt(MODELID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_model, container, false);
        FrameLayout layout = view.findViewById(R.id.model_layout);

        RajawaliSurfaceView surface = new RajawaliSurfaceView(getContext());
        surface.setFrameRate(60.0);
        surface.setRenderMode(IRajawaliSurface.RENDERMODE_WHEN_DIRTY);
        layout.addView(surface);

        renderer = new ModelRenderer(getContext(), modelID);
        surface.setSurfaceRenderer(renderer);

        return view;
    }
}
