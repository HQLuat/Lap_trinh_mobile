package vn.edu.hcmuaf.fit.travelapp.admin.userManagement.data.repository;

import vn.edu.hcmuaf.fit.travelapp.auth.data.model.User;

public interface OnUserActionListener {
    void onDeleteUser(User user);
    void onUpdateUser(User user);
}
