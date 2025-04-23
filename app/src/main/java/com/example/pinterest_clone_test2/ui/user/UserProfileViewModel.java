package com.example.pinterest_clone_test2.ui.user;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public class UserProfileViewModel extends ViewModel {
    private final SavedStateHandle savedStateHandle;

    // Keys for saved state
    private static final String KEY_SOURCE = "source";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_NAME = "name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_AVATAR_URL = "avatarUrl";
    private static final String KEY_IS_FOLLOWING = "isFollowing";
    private static final String KEY_FOLLOWERS_COUNT = "followersCount";
    private static final String KEY_FOLLOWING_COUNT = "followingCount";
    private static final String KEY_SELECTED_TAB = "selectedTab";

    public UserProfileViewModel(SavedStateHandle savedStateHandle) {
        this.savedStateHandle = savedStateHandle;
    }

    // Source methods
    public void setSource(String source) {
        savedStateHandle.set(KEY_SOURCE, source);
    }

    public String getSource() {
        return savedStateHandle.get(KEY_SOURCE);
    }

    // UserId methods
    public void setUserId(String userId) {
        savedStateHandle.set(KEY_USER_ID, userId);
    }

    public String getUserId() {
        return savedStateHandle.get(KEY_USER_ID);
    }

    // Name methods
    public void setName(String name) {
        savedStateHandle.set(KEY_NAME, name);
    }

    public String getName() {
        return savedStateHandle.get(KEY_NAME);
    }

    public void setUserName(String name) {
        savedStateHandle.set(KEY_USERNAME, name);
    }

    public String getUserName() {
        return savedStateHandle.get(KEY_USERNAME);
    }

    // AvatarURL methods
    public void setAvatarUrl(String avatarUrl) {
        savedStateHandle.set(KEY_AVATAR_URL, avatarUrl);
    }

    public String getAvatarUrl() {
        return savedStateHandle.get(KEY_AVATAR_URL);
    }

    // Following status methods
    public void setIsFollowing(boolean isFollowing) {
        savedStateHandle.set(KEY_IS_FOLLOWING, isFollowing);
    }

    public Boolean getIsFollowing() {
        return savedStateHandle.get(KEY_IS_FOLLOWING);
    }

    // Follower count methods
    public void setFollowersCount(int count) {
        savedStateHandle.set(KEY_FOLLOWERS_COUNT, count);
    }

    public Integer getFollowersCount() {
        return savedStateHandle.get(KEY_FOLLOWERS_COUNT);
    }

    // Following count methods
    public void setFollowingCount(int count) {
        savedStateHandle.set(KEY_FOLLOWING_COUNT, count);
    }

    public Integer getFollowingCount() {
        return savedStateHandle.get(KEY_FOLLOWING_COUNT);
    }

    // Selected tab methods
    public void setSelectedTab(int tabPosition) {
        savedStateHandle.set(KEY_SELECTED_TAB, tabPosition);
    }

    public Integer getSelectedTab() {
        return savedStateHandle.get(KEY_SELECTED_TAB);
    }
}