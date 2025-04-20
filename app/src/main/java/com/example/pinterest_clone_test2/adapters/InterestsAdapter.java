package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinterest_clone_test2.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterestsAdapter extends RecyclerView.Adapter<InterestsAdapter.InterestViewHolder> {

    private final List<String> interestTags;
    private final List<String> selectedTags;
    private final Context context;
    private final OnTagSelectedListener listener;
    private final Map<String, String> tagToResourceMap; // Maps displayed tag to resource name

    public InterestsAdapter(Context context, List<String> interestTags, OnTagSelectedListener listener) {
        this.context = context;
        this.interestTags = interestTags;
        this.selectedTags = new ArrayList<>();
        this.listener = listener;
        this.tagToResourceMap = createTagResourceMap();
    }

    /**
     * Creates a mapping between displayed tag names and their English resource names
     * This helps find the correct drawable resource regardless of the current app language
     */
    private Map<String, String> createTagResourceMap() {
        Map<String, String> map = new HashMap<>();

        // English tag names used for resource lookup
        String[] englishTags = {
                "anime", "art", "animal", "photography", "graphic_design", "quotes",
                "football", "cars", "illustration", "technology", "celebrity", "flowers",
                "travel", "food", "fashion", "beauty", "education", "decor",
                "wedding", "landscape", "music", "science"
        };

        // Get the displayed tag name from resources for each English tag
        for (String englishTag : englishTags) {
            int resId = context.getResources().getIdentifier(englishTag, "string", context.getPackageName());
            if (resId != 0) {
                String displayName = context.getString(resId);
                map.put(displayName, englishTag);
            }
        }

        return map;
    }

    @NonNull
    @Override
    public InterestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_interest_tag, parent, false);
        return new InterestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InterestViewHolder holder, int position) {
        String tag = interestTags.get(position);
        holder.bind(tag);
    }

    @Override
    public int getItemCount() {
        return interestTags.size();
    }

    public List<String> getSelectedTags() {
        return new ArrayList<>(selectedTags);
    }

    public interface OnTagSelectedListener {
        void onTagSelected(int selectedCount);
    }

    class InterestViewHolder extends RecyclerView.ViewHolder {
        private final ImageView tagImage;
        private final TextView tagName;
        private final View itemView;
        private boolean isSelected = false;

        public InterestViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tagImage = itemView.findViewById(R.id.iv_tag_image);
            tagName = itemView.findViewById(R.id.tv_tag_name);
        }

        void bind(final String tag) {
            // Set tag name
            tagName.setText(tag);

            // Look up the English tag name for resource lookup
            String englishTag = tagToResourceMap.get(tag);
            if (englishTag == null) {
                // Fallback to direct conversion if mapping not found
                englishTag = tag.toLowerCase().replace(" ", "_");
            }

            // Determine image resource name based on English tag
            String tagImageName = "tag_" + englishTag;
            int resourceId = context.getResources().getIdentifier(tagImageName, "drawable", context.getPackageName());

            // Set image resource, or default if not found
            if (resourceId != 0) {
                tagImage.setImageResource(resourceId);
            } else {
                tagImage.setImageResource(R.drawable.ic_launcher_background); // Default image
                // Log the missing resource for debugging
                Log.d("InterestsAdapter", "Missing resource for tag: " + tag + ", tried: " + tagImageName);
            }

            // Check if this tag is selected
            isSelected = selectedTags.contains(tag);
            updateSelectedState();

            // Set click listener
            itemView.setOnClickListener(v -> {
                isSelected = !isSelected;

                if (isSelected) {
                    if (selectedTags.size() < 5) { // Limit to 5 selections
                        selectedTags.add(tag);
                    } else {
                        isSelected = false; // Revert selection
                    }
                } else {
                    selectedTags.remove(tag);
                }

                updateSelectedState();
                listener.onTagSelected(selectedTags.size());
            });
        }

        private void updateSelectedState() {
            if (isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey));
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            }
        }
    }
}