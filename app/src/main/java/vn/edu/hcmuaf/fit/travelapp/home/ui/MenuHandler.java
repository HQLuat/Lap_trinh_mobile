package vn.edu.hcmuaf.fit.travelapp.home.ui;

import android.app.Activity;
import android.content.Intent;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.auth.ui.ProfileActivity;
import vn.edu.hcmuaf.fit.travelapp.order.TransactionActivity;

public class MenuHandler {
    private Activity activity;
    private ChipNavigationBar bottomNavigation;
    private int currentMenuId;

    public MenuHandler(Activity activity, ChipNavigationBar bottomNavigation, int currentMenuId) {
        this.activity = activity;
        this.bottomNavigation = bottomNavigation;
        this.currentMenuId = currentMenuId;
    }

    public void setupMenu() {
        bottomNavigation.setItemSelected(currentMenuId, false);

        bottomNavigation.setOnItemSelectedListener(id -> {
            if (id == currentMenuId) {
                return;
            }

            if (id == R.id.home) {
                Intent intent = new Intent(activity, MainActivity.class);
                activity.startActivity(intent);
            } else if (id == R.id.favorites) {
            } else if (id == R.id.transaction) {
                Intent intent = new Intent(activity, TransactionActivity.class);
                activity.startActivity(intent);
            } else if (id == R.id.profile) {
                Intent intent = new Intent(activity, ProfileActivity.class);
                activity.startActivity(intent);
            }
        });
    }
}
