package vn.edu.hcmuaf.fit.travelapp.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import vn.edu.hcmuaf.fit.travelapp.auth.data.repository.UserRepository;

public class LogoutViewModel extends ViewModel {
    private final MutableLiveData<Boolean> logoutSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private final UserRepository userRepository = new UserRepository();

    public void logout() {
        try {
            userRepository.logoutUser();
            logoutSuccessLiveData.setValue(true);
        } catch (Exception e) {
            errorLiveData.setValue("Logout failed: " + e.getMessage());
        }
    }

    public LiveData<Boolean> getLogoutSuccessLiveData() {
        return logoutSuccessLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
}
