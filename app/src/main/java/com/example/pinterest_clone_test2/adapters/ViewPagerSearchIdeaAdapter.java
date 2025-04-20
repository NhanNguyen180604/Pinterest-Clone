package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.interfaces.SearchIdeaClickListener;

import java.util.Arrays;
import java.util.List;

public class ViewPagerSearchIdeaAdapter extends RecyclerView.Adapter<ViewPagerSearchIdeaAdapter.SearchIdeaPagerViewHolder> {
    public static class SearchIdeaPagerViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_idea_image;
        TextView tv_idea_text;
        TextView tv_idea_query;

        SearchIdeaPagerViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_idea_image = itemView.findViewById(R.id.iv_idea_image);
            tv_idea_text = itemView.findViewById(R.id.tv_idea_text);
            tv_idea_query = itemView.findViewById(R.id.tv_idea_query);

            PorterDuffColorFilter color_filter = new PorterDuffColorFilter(Color.parseColor("#88dddddd"), PorterDuff.Mode.SRC_ATOP);
            iv_idea_image.setColorFilter(color_filter);
        }
    }

    List<Integer> image_resources = Arrays.asList(
            R.drawable.serious_cat,
            R.drawable.figure_drawing_stan_prokopenko,
            R.drawable.sandalphon_burst_chain
    );
    List<String> idea_texts;
    List<String> idea_queries;
    SearchIdeaClickListener _listener;

    public ViewPagerSearchIdeaAdapter(Context context, SearchIdeaClickListener listener) {
        idea_texts = Arrays.asList(
                context.getResources().getString(R.string.you_like_cat),
                context.getResources().getString(R.string.find_human_figure_drawings_on_pinterest),
                context.getResources().getString(R.string.you_like_anime)
        );
        idea_queries = Arrays.asList(
                context.getResources().getString(R.string.funny_cat_image),
                context.getResources().getString(R.string.human_figure),
                context.getResources().getString(R.string.anime)
        );
        _listener = listener;
    }

    @NonNull
    @Override
    public SearchIdeaPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchIdeaPagerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.fragment_search_idea_pager_object, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SearchIdeaPagerViewHolder holder, int position) {
        Glide.with(holder.iv_idea_image.getContext())
                .load(image_resources.get(position))
                .placeholder(R.drawable.ic_loading)
                .fitCenter()
                .into(holder.iv_idea_image);
        holder.tv_idea_text.setText(idea_texts.get(position));
        holder.tv_idea_query.setText(idea_queries.get(position));
        holder.itemView.setOnClickListener(v -> _listener.OnClick(idea_queries.get(position)));
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
