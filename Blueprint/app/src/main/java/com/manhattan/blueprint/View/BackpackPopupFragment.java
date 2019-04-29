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

public class BackpackPopupFragment extends Fragment {
    private static final String MODELID = "modelID";
    private static final String QUANTITY = "quantity";
    private int modelID;
    private int quantity;

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

        // Renderer doesn't function correctly on anything lower than O
        // Show an image view instead
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SurfaceView surface = new SurfaceView(getContext());
            surface.setFrameRate(60.0);
            surface.setRenderMode(ISurface.RENDERMODE_CONTINUOUSLY);

            ModelRenderer renderer = new ModelRenderer(getContext(), modelID);
            surface.setSurfaceRenderer(renderer);
            layout.addView(surface);
        } else {
            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            // TODO: Replace with centered sprite
            imageView.setImageBitmap(SpriteManager.getInstance(getContext()).fetch(modelID));
            layout.addView(imageView);
        }

        modelName.setText(ItemManager.getInstance(getContext()).getName(modelID).withDefault("Resource"));
        modelQuantity.setText(String.format(getString(R.string.inventory_popup_quantity), quantity));

        return view;
    }
}
