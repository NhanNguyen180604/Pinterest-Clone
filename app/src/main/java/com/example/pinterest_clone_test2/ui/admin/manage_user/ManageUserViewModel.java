package com.example.pinterest_clone_test2.ui.admin.manage_user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import com.example.pinterest_clone_test2.models.User;

public class ManageUserViewModel extends ViewModel {

    public enum TabType { NORMAL, BLOCKED }

    private final MutableLiveData<List<User>> users = new MutableLiveData<>(new ArrayList<>());
    private final List<User> allUsers = new ArrayList<>();

    private TabType currentTab = TabType.NORMAL;

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public void addUser(User user) {
        allUsers.add(user);
        applyFilter();
    }

    public void deleteUser(User user) {
        allUsers.remove(user);
        applyFilter();
    }

    public void blockUser(User user) {
        user.setBlocked(true);
        applyFilter();
    }

    public void unblockUser(User user) {
        user.setBlocked(false);
        applyFilter();
    }

    public void setTab(TabType tab) {
        currentTab = tab;
        applyFilter();
    }

    public void applyFilter() {
        List<User> filtered = new ArrayList<>();
        for (User u : allUsers) {
            if ((currentTab == TabType.NORMAL && !u.isBlocked()) ||
                    (currentTab == TabType.BLOCKED && u.isBlocked())) {
                filtered.add(u);
            }
        }

        // Sắp xếp theo tên
        filtered.sort((a, b) -> a.getFirstName().compareToIgnoreCase(b.getFirstName()));
        users.setValue(filtered);
    }
}
