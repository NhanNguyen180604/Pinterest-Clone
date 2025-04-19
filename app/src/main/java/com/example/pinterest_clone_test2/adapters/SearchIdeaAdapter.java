package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.interfaces.SearchIdeaClickListener;

import java.util.Arrays;
import java.util.List;

public class SearchIdeaAdapter extends RecyclerView.Adapter<SearchIdeaAdapter.SearchIdeaViewHolder> {
    public static class SearchIdeaViewHolder extends RecyclerView.ViewHolder {
        TextView tv_idea_text;
        TextView tv_idea_query;
        ImageView iv_image_1, iv_image_2, iv_image_3, iv_image_4;

        SearchIdeaViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_idea_text = itemView.findViewById(R.id.tv_idea_text);
            tv_idea_query = itemView.findViewById(R.id.tv_idea_query);
            iv_image_1 = itemView.findViewById(R.id.iv_image_1);
            iv_image_2 = itemView.findViewById(R.id.iv_image_2);
            iv_image_3 = itemView.findViewById(R.id.iv_image_3);
            iv_image_4 = itemView.findViewById(R.id.iv_image_4);
        }
    }

    List<List<Integer>> image_groups = Arrays.asList(
            Arrays.asList(R.drawable.high_gojo, R.drawable.kaeya, R.drawable.karyl_drinks_beer, R.drawable.araragi),
            Arrays.asList(R.drawable.cat_on_sofa, R.drawable.karyl_cat, R.drawable.serious_cat, R.drawable.mewing_cat),
            Arrays.asList(R.drawable.siegfried, R.drawable.lucilius, R.drawable.sandalphon_burst_chain, R.drawable.id_burst_chain),
            Arrays.asList(R.drawable.paradise_lost, R.drawable.paradise_losto, R.drawable.sandalphon_burst_chain, R.drawable.lucilius_relink),
            Arrays.asList(R.drawable.arms_drawing_stan_prokopenko, R.drawable.figure_drawing_stan_prokopenko, R.drawable.incomplete_figure_stan_prokopenko, R.drawable.man_portrait_drawing_stan_prokopenko)
    );

    Context _context;
    SearchIdeaClickListener _listener;

    List<String> idea_texts;

    List<String> idea_queries;

    public SearchIdeaAdapter(Context context, SearchIdeaClickListener listener) {
        _context = context;
        _listener = listener;
        idea_texts = Arrays.asList(
                context.getResources().getString(R.string.idea_for_you),
                context.getResources().getString(R.string.idea_for_you),
                context.getResources().getString(R.string.idea_for_you),
                context.getResources().getString(R.string.popular_on_pinterest),
                context.getResources().getString(R.string.popular_on_pinterest)
        );
        idea_queries = Arrays.asList(
                _context.getResources().getString(R.string.anime),
                _context.getResources().getString(R.string.cat),
                _context.getResources().getString(R.string.gbf),
                _context.getResources().getString(R.string.paradise_lost),
                _context.getResources().getString(R.string.human_figure)
        );
    }

    @NonNull
    @Override
    public SearchIdeaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchIdeaViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_idea_view_holder, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SearchIdeaViewHolder holder, int position) {
        holder.tv_idea_text.setText(idea_texts.get(position));
        holder.tv_idea_query.setText(idea_queries.get(position));

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_loading);

        Glide.with(holder.iv_image_1.getContext())
                .load(image_groups.get(position).get(0))
                .apply(options)
                .into(holder.iv_image_1);

        Glide.with(holder.iv_image_2.getContext())
                .load(image_groups.get(position).get(1))
                .apply(options)
                .into(holder.iv_image_2);

        Glide.with(holder.iv_image_3.getContext())
                .load(image_groups.get(position).get(2))
                .apply(options)
                .into(holder.iv_image_3);

        Glide.with(holder.iv_image_4.getContext())
                .load(image_groups.get(position).get(3))
                .apply(options)
                .into(holder.iv_image_4);

        holder.itemView.setOnClickListener(v -> _listener.OnClick(idea_queries.get(holder.getBindingAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
