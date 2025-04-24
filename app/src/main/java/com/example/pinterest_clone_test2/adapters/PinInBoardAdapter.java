package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PinInBoardAdapter extends RecyclerView.Adapter<PinInBoardAdapter.BoardViewHolder> {
    private final Context context;
    private final List<Pin> pinList;
    private final PinClickListener onPinClickListener;

    public PinInBoardAdapter(Context context, List<Pin> pinList, PinClickListener listener) {
        this.context = context;
        this.pinList = pinList;
        this.onPinClickListener = listener;
    }

    public static class BoardViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPin;
        ShapeableImageView btnAccount;
        TextView tvAccountName;
        CardView cvPinItem;
        FrameLayout framePinItem;
        CheckBox checkboxSelect;

        public BoardViewHolder(View itemView) {
            super(itemView);
            imgPin = itemView.findViewById(R.id.image_view_holder);
            btnAccount = itemView.findViewById(R.id.btn_account);
            tvAccountName = itemView.findViewById(R.id.tv_account_name);
            cvPinItem = itemView.findViewById(R.id.cv_pin_item);
            framePinItem = itemView.findViewById(R.id.frame_pin_item);
            checkboxSelect = itemView.findViewById(R.id.checkbox_select);
        }

        public void bind(Pin pin, boolean isSelected) {
            framePinItem.setBackgroundResource(isSelected
                    ? R.drawable.bg_pin_border_selected
                    : 0);

            checkboxSelect.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            checkboxSelect.setChecked(isSelected);
        }
    }

    @NonNull
    @Override
    public PinInBoardAdapter.BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pin_in_board_item, parent, false);
        return new PinInBoardAdapter.BoardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PinInBoardAdapter.BoardViewHolder holder, int position) {
        Pin pin = pinList.get(position);
        RequestOptions glideOptions = new RequestOptions()
                .placeholder(R.drawable.ic_loading);

        Glide.with(holder.imgPin.getContext())
                .load(pin.getMediaUrl())
                .apply(glideOptions)
                .into(holder.imgPin);
        FirebaseUserService.getUserById(pin.getAuthorId() ,task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    RequestOptions avatarGlideOptions = new RequestOptions()
                            .placeholder(R.drawable.ic_loading)
                            .error(R.drawable.ic_account_circle);

                    holder.tvAccountName.setText(document.getString("name"));
                    Glide.with(holder.btnAccount.getContext())
                            .load(document.getString("avatarUrl"))
                            .apply(avatarGlideOptions)
                            .into(holder.btnAccount);


                } else {
                    Log.d("Firestore", "No such user");
                }
            } else {
                Log.e("Firestore", "Error fetching user", task.getException());
            }
        });

        holder.bind(pin, pin.isSelected());

        holder.itemView.setOnClickListener(v -> {
            onPinClickListener.OnClick(position, holder.itemView);
        });
    }

    @Override
    public int getItemCount() {
        return pinList != null ? pinList.size() : 0;
    }

    public void moveItem(int fromPosition, int toPosition) {
        Pin fromPin = pinList.get(fromPosition);
        pinList.remove(fromPosition);
        pinList.add(toPosition, fromPin);
        notifyItemMoved(fromPosition, toPosition);
    }

}
