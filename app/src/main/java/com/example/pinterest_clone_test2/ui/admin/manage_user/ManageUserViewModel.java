package com.example.pinterest_clone_test2.ui.admin.manage_user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.pinterest_clone_test2.models.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageUserViewModel extends ViewModel {
    private final MutableLiveData<List<User>> userListLiveData = new MutableLiveData<>();

    public ManageUserViewModel() {
        // Dữ liệu mẫu
        List<User> mockUsers = new ArrayList<>(Arrays.asList(
                new User("password1", "user1@example.com", "Tân", "01/01/1990", User.Gender.Nam),
                new User("password2", "user2@example.com", "Tandy", "02/02/1992", User.Gender.Nữ),
                new User("password3", "user3@example.com", "Võ", "03/03/1995", User.Gender.Khác),
                new User("password4", "user4@example.com", "Minh", "04/04/1998", User.Gender.Nam),
                new User("password5", "user5@example.com", "Lan", "05/05/1999", User.Gender.Nữ),
                new User("password6", "user6@example.com", "Bảo", "06/06/2000", User.Gender.Nam),
                new User("password7", "user7@example.com", "Hằng", "07/07/1997", User.Gender.Nữ),
                new User("password8", "user8@example.com", "Dũng", "08/08/1996", User.Gender.Nam),
                new User("password9", "user9@example.com", "Vy", "09/09/2001", User.Gender.Nữ),
                new User("password10", "user10@example.com", "Quân", "10/10/1994", User.Gender.Nam),
                new User("password11", "user11@example.com", "Linh", "11/11/1993", User.Gender.Nữ),
                new User("password12", "user12@example.com", "Sơn", "12/12/1995", User.Gender.Nam),
                new User("password13", "user13@example.com", "Trúc", "13/03/1992", User.Gender.Nữ),
                new User("password14", "user14@example.com", "Thành", "14/04/1991", User.Gender.Nam),
                new User("password15", "user15@example.com", "Hải", "15/05/1990", User.Gender.Nam),
                new User("password16", "user16@example.com", "An", "16/06/1999", User.Gender.Nữ),
                new User("password17", "user17@example.com", "Khang", "17/07/2002", User.Gender.Nam),
                new User("password18", "user18@example.com", "Diệp", "18/08/1998", User.Gender.Nữ),
                new User("password19", "user19@example.com", "Phong", "19/09/2000", User.Gender.Nam),
                new User("password20", "user20@example.com", "Trang", "20/10/1997", User.Gender.Nữ)
        ));

        userListLiveData.setValue(mockUsers);
    }

    public LiveData<List<User>> getUserList() {
        return userListLiveData;
    }
}