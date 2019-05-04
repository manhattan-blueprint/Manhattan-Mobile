package com.manhattan.blueprint.View;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.manhattan.blueprint.Model.ItemSchema;
import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.Utils.SpriteManager;
import com.manhattan.blueprint.View.Adapter.ComponentItemAdapter;

public class BlueprintDetailFragment extends Fragment {
    private static final String ITEM_ID = "itemID";
    private int itemID;

    public BlueprintDetailFragment() {
        // Required empty public constructor
    }

    public static BlueprintDetailFragment newInstance(int itemID) {
        BlueprintDetailFragment fragment = new BlueprintDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ITEM_ID, itemID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemID = getArguments().getInt(ITEM_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_blueprint_detail, container, false);

        TextView title = v.findViewById(R.id.blueprintDetailTitle);
        ImageView image = v.findViewById(R.id.blueprintDetailImage);
        TextView primaryResource = v.findViewById(R.id.blueprintDetailPrimaryResourceText);
        TextView machine = v.findViewById(R.id.blueprintDetailMachineText);
        TextView recipe = v.findViewById(R.id.blueprintDetailRecipeText);
        RecyclerView componentRecycler = v.findViewById(R.id.blueprintDetailComponentRecycler);

        // Fetch item if exists and populate UI with data
        ItemManager.getInstance(getContext()).getItem(itemID).ifPresent(item -> {
            title.setText(item.getName());
            image.setImageBitmap(SpriteManager.getInstance(getContext()).fetch(item.getItemID()));

            if (item.getItemType() == ItemSchema.ItemType.PrimaryResource) {
                componentRecycler.setVisibility(View.INVISIBLE);
                recipe.setVisibility(View.INVISIBLE);
            } else {
                int columns = item.getBlueprint().size() + item.getRecipe().size();
                componentRecycler.setLayoutManager(new GridLayoutManager(this.getContext(), columns));
                componentRecycler.setAdapter(new ComponentItemAdapter(this.getContext(), item.getItemID()));
                primaryResource.setVisibility(View.INVISIBLE);

            }

            if (item.getItemType() != ItemSchema.ItemType.MachineCraftedComponent) {
                machine.setVisibility(View.INVISIBLE);
            } else {
                String machineName = ItemManager.getInstance(getContext())
                        .getItem(item.getMachineID())
                        .map(ItemSchema.Item::getName)
                        .withDefault("a machine");
                machine.setText(String.format("Requires: %s", machineName));
            }
        });

        return v;
    }
}
