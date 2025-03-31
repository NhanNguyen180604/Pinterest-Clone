package com.example.pinterest_clone_test2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
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
    PinClickListener listener;

    public PinListAdapter(List<Pin> pins, PinClickListener listener) {
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
        Pin pin = pins.get(position);

        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_loading)
                .fitCenter()
                .error(R.drawable.turtle_huh);

        if (pin.getType() == Pin.PinType.IMAGE) {
            Glide.with(holder.itemView.getContext())
                    .load(pins.get(position).getThumbnailUrl())
                    .fitCenter()
                    .apply(options)
                    .into(holder.iv);
        } else if (pin.getType() == Pin.PinType.GIF) {
            Glide.with(holder.itemView.getContext())
                    .asGif()
                    .load(pins.get(position).getThumbnailUrl())
                    .fitCenter()
                    .apply(options)
                    .into(holder.iv);
        } else {
            //TODO: load video
        }

        holder.iv.setOnClickListener(v -> listener.OnClick(holder.getBindingAdapterPosition(), v));
    }

    @Override
    public int getItemCount() {
        return pins.size();
    }
}
