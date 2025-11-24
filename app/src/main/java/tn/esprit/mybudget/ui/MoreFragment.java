package tn.esprit.mybudget.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tn.esprit.mybudget.R;

public class MoreFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvMoreOptions = view.findViewById(R.id.rvMoreOptions);
        rvMoreOptions.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3 columns

        List<MoreOption> options = new ArrayList<>();
        // Add options based on the image
        options.add(new MoreOption("Messages", android.R.drawable.ic_dialog_email));
        options.add(new MoreOption("Objectifs d'épargne", android.R.drawable.ic_menu_my_calendar));
        options.add(new MoreOption("Récurrent", android.R.drawable.ic_menu_recent_history));
        options.add(new MoreOption("Rappels", android.R.drawable.ic_popup_reminder));
        options.add(new MoreOption("Acheter Premium", android.R.drawable.star_on));
        options.add(new MoreOption("Devise/Taux", android.R.drawable.ic_menu_rotate));
        options.add(new MoreOption("Catégories", android.R.drawable.ic_menu_sort_by_size));
        options.add(new MoreOption("Membres", android.R.drawable.ic_menu_myplaces));
        options.add(new MoreOption("Budget", android.R.drawable.ic_menu_manage));
        options.add(new MoreOption("Livres", android.R.drawable.ic_menu_agenda));
        options.add(new MoreOption("Comptes", android.R.drawable.ic_lock_lock));
        options.add(new MoreOption("Rechercher", android.R.drawable.ic_menu_search));
        options.add(new MoreOption("Sauvegarde", android.R.drawable.ic_menu_save));
        options.add(new MoreOption("Exporter", android.R.drawable.ic_menu_share));
        options.add(new MoreOption("Évaluer", android.R.drawable.btn_star));

        MoreAdapter adapter = new MoreAdapter(options);
        rvMoreOptions.setAdapter(adapter);
    }

    private class MoreAdapter extends RecyclerView.Adapter<MoreAdapter.ViewHolder> {
        private List<MoreOption> options;

        public MoreAdapter(List<MoreOption> options) {
            this.options = options;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_more_option, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MoreOption option = options.get(position);
            holder.tvTitle.setText(option.getTitle());
            holder.ivIcon.setImageResource(option.getIconResId());
            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Clicked: " + option.getTitle(), Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            ImageView ivIcon;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                ivIcon = itemView.findViewById(R.id.ivIcon);
            }
        }
    }
}
