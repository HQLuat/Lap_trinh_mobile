package vn.edu.hcmuaf.fit.travelapp.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import vn.edu.hcmuaf.fit.travelapp.auth.data.model.User;
import vn.edu.hcmuaf.fit.travelapp.auth.data.repository.UserRepository;

public class UserViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccessLiveData = new MutableLiveData<>();

    public UserViewModel() {
        userRepository = new UserRepository();
    }

    public void fetchCurrentUser() {
        userRepository.getCurrentUser(new UserRepository.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                userLiveData.setValue(user);
            }

            @Override
            public void onFailure(String errorMessage) {
                errorLiveData.setValue(errorMessage);
            }
        });
    }

    // Update user information
    public void saveUser(User user) {
        userRepository.createOrUpdateUser(user, new UserRepository.OnUserSaveListener() {
            @Override
            public void onSuccess() {
                saveSuccessLiveData.setValue(true);
            }

            @Override
            public void onFailure(String errorMessage) {
                errorLiveData.setValue(errorMessage);
                saveSuccessLiveData.setValue(false);
            }
        });
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getSaveSuccessLiveData() {
        return saveSuccessLiveData;
    }
}
