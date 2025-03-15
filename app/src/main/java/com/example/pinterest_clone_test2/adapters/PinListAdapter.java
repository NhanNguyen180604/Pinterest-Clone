package com.example.pinterest_clone_test2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.interfaces.ImageClickListener;
import com.example.pinterest_clone_test2.models.Pin;

import java.util.List;

public class PinListAdapter extends RecyclerView.Adapter<PinListAdapter.PinViewHolder> {
    public static class PinViewHolder extends RecyclerView.ViewHolder {
        ImageView iv;

        public PinViewHolder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.image_view_holder);
        }
    }

    List<Pin> pins;
    ImageClickListener listener;

    public PinListAdapter(List<Pin> pins, ImageClickListener listener) {
        this.pins = pins;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PinViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.pin_view_holder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PinViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .load(pins.get(position).getMediaURL())
                .placeholder(R.drawable.karyl)
                .fitCenter()
                .into(holder.iv);
        holder.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnClick(holder.getBindingAdapterPosition(), v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pins.size();
    }
}
