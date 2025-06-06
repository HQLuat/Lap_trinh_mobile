package vn.edu.hcmuaf.fit.travelapp.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;

import vn.edu.hcmuaf.fit.travelapp.auth.data.repository.UserRepository;

public class SignupViewModel extends ViewModel {
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public void register(String fullName, String email, String password) {
        UserRepository userRepository = new UserRepository();
        userRepository.registerUser(fullName, email, password, new UserRepository.OnUserRegisterListener() {
            @Override
            public void onSuccess(FirebaseUser user) {
                userLiveData.postValue(user);
            }

            @Override
            public void onFailure(String errorMessage) {
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
}
