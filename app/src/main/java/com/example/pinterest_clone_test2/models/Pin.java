package com.example.pinterest_clone_test2.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.pinterest_clone_test2.BR;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pin extends BaseObservable implements Parcelable {
    public enum PinType {
        IMAGE,
        GIF,
        VIDEO
    }

    private String id;
    private String mediaUrl;
    private String thumbnailUrl;  // for image
    private String authorId;
    PinType type;
    boolean isLiked;
    int likeCount;
    private String name = "";
    private String nameNormalized = "";
    private String description = "";
    private String descriptionNormalized = "";
    private boolean allowComment;
    private long createdAt;
    private List<String> tags;

    public Pin() {

    }

    public String getId() {
        return id;
    }

    public Pin setId(String id) {
        this.id = id;
        return this;
    }

    @Bindable
    public String getMediaUrl() {
        return mediaUrl;
    }

    public Pin setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
        notifyPropertyChanged(BR.mediaUrl);
        return this;
    }

    private boolean selected;

    @Bindable
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        notifyPropertyChanged(BR.selected);
    }

    @Bindable
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Pin setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        notifyPropertyChanged(BR.thumbnailUrl);
        return this;
    }

    public String getAuthorId() {
        return authorId;
    }

    public Pin setAuthorId(String authorId) {
        this.authorId = authorId;
        return this;
    }

    public PinType getType() {
        return type;
    }

    public Pin setType(PinType type) {
        this.type = type;
        return this;
    }

    @Bindable
    public boolean getIsLiked() {
        return isLiked;
    }

    public Pin setIsLiked(boolean liked) {
        isLiked = liked;
        notifyPropertyChanged(BR.isLiked);
        return this;
    }

    @Bindable
    public int getLikeCount() {
        return likeCount;
    }

    public Pin setLikeCount(int likeCount) {
        this.likeCount = likeCount;
        notifyPropertyChanged(BR.likeCount);
        notifyPropertyChanged(BR.likeCountString);
        return this;
    }

    @Bindable
    public String getLikeCountString() {
        return Integer.toString(likeCount);
    }

    @Bindable
    public String getName() {
        return name;
    }

    public Pin setName(@Nullable String name) {
        this.name = Objects.requireNonNullElse(name, "");
        setNameNormalized(this.name.toLowerCase());
        notifyPropertyChanged(BR.name);
        notifyPropertyChanged(BR.titleVisibility);
        return this;
    }

    @Bindable
    public int getTitleVisibility() {
        return name != null && !name.isBlank() ? View.VISIBLE : View.GONE;
    }

    public String getNameNormalized() {
        return nameNormalized;
    }

    public void setNameNormalized(String nameNormalized) {
        this.nameNormalized = nameNormalized;
    }

    @Bindable
    public String getDescription() {
        return description;
    }

    public Pin setDescription(String description) {
        this.description = Objects.requireNonNullElse(description, "");
        setDescriptionNormalized(this.description.toLowerCase());
        notifyPropertyChanged(BR.description);
        notifyPropertyChanged(BR.descriptionVisibility);
        return this;
    }

    public String getDescriptionNormalized() {
        return descriptionNormalized;
    }

    public void setDescriptionNormalized(String descriptionNormalized) {
        this.descriptionNormalized = descriptionNormalized;
    }

    @Bindable
    public int getDescriptionVisibility() {
        return description != null && !description.isBlank() ? View.VISIBLE : View.GONE;
    }

    @Bindable
    public boolean getAllowComment() {
        return allowComment;
    }

    public Pin setAllowComment(boolean allowComment) {
        this.allowComment = allowComment;
        notifyPropertyChanged(BR.allowComment);
        notifyPropertyChanged(BR.commentVisibility);
        return this;
    }

    @Bindable
    public int getCommentVisibility() {
        return allowComment ? View.VISIBLE : View.GONE;
    }

    @Bindable
    public long getCreatedAt() {
        return createdAt;
    }

    public Pin setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        notifyPropertyChanged(BR.createdAt);
        return this;
    }

    @Bindable
    public List<String> getTags() {
        return tags;
    }

    public Pin setTags(List<String> tags) {
        this.tags = tags;
        notifyPropertyChanged(BR.tags);
        return this;
    }

    public Pin(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static final Parcelable.Creator<Pin> CREATOR = new Parcelable.Creator<>() {

        @Override
        public Pin createFromParcel(Parcel source) {
            return new Pin(source);
        }

        @Override
        public Pin[] newArray(int size) {
            return new Pin[size];
        }
    };

    public void readFromParcel(Parcel in) {
        id = in.readString();
        setMediaUrl(in.readString());
        authorId = in.readString();
        setThumbnailUrl(in.readString());
        type = (PinType) in.readSerializable();
        setIsLiked(in.readBoolean());
        setLikeCount(in.readInt());
        setName(in.readString());
        setDescription(in.readString());
        allowComment = in.readBoolean();
        createdAt = in.readLong();
        tags = new ArrayList<>();
        in.readStringList(tags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(mediaUrl);
        dest.writeString(authorId);
        dest.writeString(thumbnailUrl);
        dest.writeSerializable(type);
        dest.writeBoolean(isLiked);
        dest.writeInt(likeCount);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeBoolean(allowComment);
        dest.writeLong(createdAt);
        dest.writeStringList(tags);
    }
}
