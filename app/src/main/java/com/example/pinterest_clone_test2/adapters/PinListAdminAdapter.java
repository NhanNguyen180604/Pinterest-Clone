package com.example.pinterest_clone_test2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.Pin;

import java.util.List;

public class PinListAdminAdapter extends RecyclerView.Adapter<PinListAdminAdapter.PinViewHolder> {

    private List<Pin> pinList;
    private PinAdminActionListener listener;

    public PinListAdminAdapter(List<Pin> pinList, PinAdminActionListener listener) {
        this.pinList = pinList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_pin_view_holder, parent, false);
        return new PinViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PinViewHolder holder, int position) {
        Pin pin = pinList.get(position);

        // Bind the data to the views
        holder.imageView.setImageResource(pin.getMediaURL());
        holder.pinIdText.setText(pin.getId());
        holder.authorIdText.setText(pin.getAuthorId());

        // Handle Edit button click
        holder.editButton.setOnClickListener(v -> listener.onEditClick(pin));

        // Handle Delete button click
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(pin));
    }

    @Override
    public int getItemCount() {
        return pinList.size();
    }

    // ViewHolder class
    public static class PinViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView pinIdText, authorIdText;
        TextView editButton, deleteButton;

        public PinViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.pin_image);
            pinIdText = itemView.findViewById(R.id.pin_id);
            authorIdText = itemView.findViewById(R.id.author_id);
            editButton = itemView.findViewById(R.id.edit_button); // Now it's a TextView instead of ImageView
            deleteButton = itemView.findViewById(R.id.delete_button); // Now it's a TextView instead of ImageView
        }
    }

    // Interface to handle edit and delete actions
    public interface PinAdminActionListener {
        void onEditClick(Pin pin);

        void onDeleteClick(Pin pin);
    }
}
