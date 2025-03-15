package com.example.pinterest_clone_test2.adapters;

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

import com.example.pinterest_clone_test2.R;

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
            R.drawable.cow,
            R.drawable.conversation,
            R.drawable.kaeya,
            R.drawable.araragi
    );
    List<String> idea_texts = Arrays.asList(
            "You like animals?",
            "You like memes?",
            "Cursed images are funny",
            "Cool anime angles"
    );
    List<String> idea_queries = Arrays.asList(
            "Animal drawn with Microsoft Paint",
            "Memes made with Microsoft Paint",
            "Weird cursed images",
            "Anime head low angles"
    );

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
        holder.iv_idea_image.setImageResource(image_resources.get(position));
        holder.tv_idea_text.setText(idea_texts.get(position));
        holder.tv_idea_query.setText(idea_queries.get(position));
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
