package com.example.pinterest_clone_test2.ui.admin.manage_user;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.example.pinterest_clone_test2.models.User;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ManageUserViewModel extends ViewModel {
    private static final String TAG = "ManageUserViewModel";

    // LiveData cho các danh sách người dùng
    private final MutableLiveData<List<User>> allUsers = new MutableLiveData<>();
    private final MutableLiveData<List<User>> bannedUsers = new MutableLiveData<>();
    private final MutableLiveData<List<User>> normalUsers = new MutableLiveData<>();
    private final MutableLiveData<List<User>> filteredUsers = new MutableLiveData<>();

    // LiveData cho trạng thái loading
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Lưu trữ dữ liệu gốc (để phục vụ việc filter mà không cần query lại)
    private List<User> originalBannedUsers = new ArrayList<>();
    private List<User> originalNormalUsers = new ArrayList<>();
    private List<User> originalAllUsers = new ArrayList<>();

    // Biến để lưu trạng thái filter
    private User.Role roleFilter = null;
    private String searchQuery = "";
    private boolean isBannedSelected = false;

    // Getters cho LiveData
    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<List<User>> getBannedUsers() {
        return bannedUsers;
    }

    public LiveData<List<User>> getNormalUsers() {
        return normalUsers;
    }

    public LiveData<List<User>> getFilteredUsers() {
        return filteredUsers;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Getter cho roleFilter
    public User.Role getRoleFilter() {
        return roleFilter;
    }

    public void setIsBannedSelected(boolean isBanned) {
        this.isBannedSelected = isBanned;
        // Reset filters when switching tabs
        resetFilters();
    }

    /**
     * Lấy tất cả người dùng từ Firestore
     */
    public void fetchAllUsers() {
        isLoading.setValue(true);
        FirebaseUserService.getAllUsers(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                List<User> users = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    User user = doc.toObject(User.class);

                    // Đảm bảo userId được set đúng từ document ID
                    String userId = doc.getId();
                    user.setUserId(userId);

                    users.add(user);
                }
                originalAllUsers = new ArrayList<>(users);
                allUsers.setValue(users);

                Log.d(TAG, "Fetched all users: " + users.size());
            } else {
                String message = "Failed to fetch all users: " + task.getException().getMessage();
                Log.e(TAG, message);
                errorMessage.setValue(message);
            }
        });
    }

    /**
     * Lấy danh sách người dùng bị cấm
     */
    public void fetchBannedUsers() {
        isLoading.setValue(true);
        FirebaseUserService.getBannedUsers(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                List<User> users = task.getResult();
                originalBannedUsers = new ArrayList<>(users);

                // Áp dụng các bộ lọc hiện tại
                applyFilters(users, bannedUsers);

                Log.d(TAG, "Fetched banned users: " + users.size());
            } else {
                String message = "Failed to fetch banned users: " + task.getException().getMessage();
                Log.e(TAG, message);
                errorMessage.setValue(message);
            }
        });
    }

    /**
     * Lấy danh sách người dùng bình thường (không bị cấm)
     */
    public void fetchNormalUsers() {
        isLoading.setValue(true);
        FirebaseUserService.getNormalUsers(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                List<User> users = task.getResult();
                originalNormalUsers = new ArrayList<>(users);

                // Áp dụng các bộ lọc hiện tại
                applyFilters(users, normalUsers);

                Log.d(TAG, "Fetched normal users: " + users.size());

                // Debug: In thông tin user để kiểm tra userId
                for (User user : users) {
                    Log.d(TAG, "Normal User: " + user.getName() + ", ID: " + user.getUserId());
                }
            } else {
                String message = "Failed to fetch normal users: " + task.getException().getMessage();
                Log.e(TAG, message);
                errorMessage.setValue(message);
            }
        });
    }

    /**
     * Cấm một người dùng
     */
    public void banUser(String userId, Runnable onSuccess, Consumer<Exception> onError) {
        if (userId == null || userId.isEmpty()) {
            onError.accept(new IllegalArgumentException("userId cannot be empty"));
            return;
        }

        isLoading.setValue(true);
        FirebaseUserService.addBannedUser(userId,
                documentReference -> {
                    isLoading.setValue(false);
                    // Refresh danh sách
                    fetchBannedUsers();
                    fetchNormalUsers();
                    onSuccess.run();
                    Log.d(TAG, "User banned successfully: " + userId);
                },
                e -> {
                    isLoading.setValue(false);
                    onError.accept(e);
                    Log.e(TAG, "Failed to ban user: " + e.getMessage());
                }
        );
    }

    /**
     * Hủy cấm một người dùng
     */
    public void unbanUser(String userId, Runnable onSuccess, Consumer<Exception> onError) {
        if (userId == null || userId.isEmpty()) {
            onError.accept(new IllegalArgumentException("userId cannot be empty"));
            return;
        }

        isLoading.setValue(true);
        FirebaseUserService.removeBannedUser(userId,
                unused -> {
                    isLoading.setValue(false);
                    // Refresh danh sách
                    fetchBannedUsers();
                    fetchNormalUsers();
                    onSuccess.run();
                    Log.d(TAG, "User unbanned successfully: " + userId);
                },
                e -> {
                    isLoading.setValue(false);
                    onError.accept(e);
                    Log.e(TAG, "Failed to unban user: " + e.getMessage());
                }
        );
    }

    /**
     * Thay đổi role của người dùng
     */
    public void changeUserRole(String userId, String newRole, Runnable onSuccess, Consumer<Exception> onError) {
        if (userId == null || userId.isEmpty()) {
            onError.accept(new IllegalArgumentException("userId cannot be empty"));
            return;
        }

        isLoading.setValue(true);
        FirebaseUserService.editRoleUser(userId, newRole,
                unused -> {
                    isLoading.setValue(false);
                    // Refresh all lists
                    fetchAllUsers();
                    fetchBannedUsers();
                    fetchNormalUsers();
                    onSuccess.run();
                    Log.d(TAG, "User role changed successfully: " + userId + " to " + newRole);
                },
                e -> {
                    isLoading.setValue(false);
                    onError.accept(e);
                    Log.e(TAG, "Failed to change user role: " + e.getMessage());
                }
        );
    }

    /**
     * Thêm người dùng mới
     */
    public void addUser(User user, Runnable onSuccess, Consumer<Exception> onError) {
        isLoading.setValue(true);
        FirebaseUserService.addUser(user,
                unused -> {
                    isLoading.setValue(false);
                    // Refresh lists
                    fetchAllUsers();
                    fetchNormalUsers();
                    onSuccess.run();
                    Log.d(TAG, "User added successfully: " + user.getName());
                },
                e -> {
                    isLoading.setValue(false);
                    onError.accept(e);
                    Log.e(TAG, "Failed to add user: " + e.getMessage());
                }
        );
    }

    /**
     * Đặt query tìm kiếm và áp dụng filter
     */
    public void setSearchQuery(String query) {
        this.searchQuery = query;
        applyFilters();
    }

    /**
     * Đặt filter role và áp dụng filter
     */
    public void setRoleFilter(User.Role role) {
        this.roleFilter = role;
        applyFilters();
    }

    /**
     * Reset tất cả các filters
     */
    public void resetFilters() {
        searchQuery = "";
        roleFilter = null;

        // Khôi phục danh sách gốc
        if (isBannedSelected) {
            bannedUsers.setValue(originalBannedUsers);
        } else {
            normalUsers.setValue(originalNormalUsers);
        }
    }

    /**
     * Áp dụng tất cả các filters hiện tại
     */
    private void applyFilters() {
        List<User> sourceList;

        // Chọn danh sách nguồn phù hợp
        if (isBannedSelected) {
            sourceList = new ArrayList<>(originalBannedUsers);
            applyFilters(sourceList, bannedUsers);
        } else {
            sourceList = new ArrayList<>(originalNormalUsers);
            applyFilters(sourceList, normalUsers);
        }
    }

    /**
     * Áp dụng các bộ lọc cho danh sách người dùng và cập nhật LiveData
     */
    private void applyFilters(List<User> sourceList, MutableLiveData<List<User>> targetLiveData) {
        // Nếu không có filter nào được áp dụng
        if ((searchQuery == null || searchQuery.isEmpty()) && roleFilter == null) {
            targetLiveData.setValue(sourceList);
            return;
        }

        // Lọc danh sách
        List<User> filteredList = sourceList.stream()
                .filter(user -> filterBySearchQuery(user) && filterByRole(user))
                .collect(Collectors.toList());

        // Cập nhật LiveData
        targetLiveData.setValue(filteredList);
        Log.d(TAG, "Applied filters - Results: " + filteredList.size());
    }

    /**
     * Kiểm tra nếu user khớp với query tìm kiếm
     */
    private boolean filterBySearchQuery(User user) {
        if (searchQuery == null || searchQuery.isEmpty()) {
            return true;
        }

        String lowercaseQuery = searchQuery.toLowerCase();
        return (user.getName() != null && user.getName().toLowerCase().contains(lowercaseQuery)) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowercaseQuery));
    }

    /**
     * Kiểm tra nếu user khớp với filter role
     */
    private boolean filterByRole(User user) {
        if (roleFilter == null) {
            return true;
        }
        return user.getRole() == roleFilter;
    }

    /**
     * Refresh tất cả dữ liệu
     */
    public void refreshAllData() {
        fetchAllUsers();
        fetchBannedUsers();
        fetchNormalUsers();
    }
}