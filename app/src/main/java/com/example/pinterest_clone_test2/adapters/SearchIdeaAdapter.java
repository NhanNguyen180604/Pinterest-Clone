package com.example.pinterest_clone_test2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.interfaces.SearchIdeaClickListener;

import java.util.ArrayList;
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
            Arrays.asList(R.drawable.karyl, R.drawable.kaeya, R.drawable.cow, R.drawable.araragi),
            Arrays.asList(R.drawable.kaeya, R.drawable.araragi, R.drawable.conversation, R.drawable.karyl),
            Arrays.asList(R.drawable.conversation, R.drawable.karyl, R.drawable.araragi, R.drawable.cow),
            Arrays.asList(R.drawable.cow, R.drawable.karyl, R.drawable.conversation, R.drawable.araragi),
            Arrays.asList(R.drawable.araragi, R.drawable.karyl, R.drawable.kaeya, R.drawable.conversation)
    );

    List<String> idea_texts = Arrays.asList(
            "Ideas for you",
            "Ideas for you",
            "Ideas for you",
            "Popular on Pinterest",
            "Popular on Pinterest"
    );

    List<String> idea_queries = Arrays.asList(
            "Lmao",
            "Gomen Amanai",
            "What the hell",
            "It's linking time",
            "Paradise Lost"
    );

    SearchIdeaClickListener _listener;

    public SearchIdeaAdapter(SearchIdeaClickListener listener) {
        _listener = listener;
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
        holder.iv_image_1.setImageResource(image_groups.get(position).get(0));
        holder.iv_image_2.setImageResource(image_groups.get(position).get(1));
        holder.iv_image_3.setImageResource(image_groups.get(position).get(2));
        holder.iv_image_4.setImageResource(image_groups.get(position).get(3));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _listener.OnClick(idea_queries.get(holder.getBindingAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
