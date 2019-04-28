package com.manhattan.blueprint.View;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.R;

import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.RajawaliSurfaceView;

public class BackpackPopupFragment extends Fragment {
    private static final String MODELID = "modelID";
    private static final String QUANTITY = "quantity";
    private int modelID;
    private int quantity;
    private RajawaliSurfaceView surface;

    public static BackpackPopupFragment newInstance(int modelID, int quantity) {
        BackpackPopupFragment fragment = new BackpackPopupFragment();
        Bundle args = new Bundle();
        args.putInt(MODELID, modelID);
        args.putInt(QUANTITY, quantity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            modelID = getArguments().getInt(MODELID);
            quantity = getArguments().getInt(QUANTITY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory_popup, container, false);
        RelativeLayout layout = view.findViewById(R.id.model_layout);
        TextView modelName = view.findViewById(R.id.model_title);
        TextView modelQuantity = view.findViewById(R.id.model_subtitle);


        surface = new RajawaliSurfaceView(getContext());
        surface.setFrameRate(60.0);
        surface.setRenderMode(IRajawaliSurface.RENDERMODE_WHEN_DIRTY);
        layout.addView(surface);

        ModelRenderer renderer = new ModelRenderer(getContext(), modelID);
        surface.setSurfaceRenderer(renderer);

        modelName.setText(ItemManager.getInstance(getContext()).getName(modelID).withDefault("Resource"));
        modelQuantity.setText(String.format(getString(R.string.inventory_popup_quantity), quantity));

        return view;
    }
}
