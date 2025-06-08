package vn.edu.hcmuaf.fit.travelapp.auth.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.auth.data.model.User;
import vn.edu.hcmuaf.fit.travelapp.auth.data.repository.UserRepository;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.datasource.DeleteCallback;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.datasource.UploadCallback;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteResult = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository();
    }

    public void fetchCurrentUser() {
        Log.d("UserViewModel", "Đang gọi API lấy user");
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

    public void fetchUsers() {
        userRepository.getAllUsers(new UserRepository.OnUsersFetchListener() {
            @Override
            public void onSuccess(List<User> users) {
                usersLiveData.setValue(users);
            }

            @Override
            public void onFailure(String errorMessage) {
                errorLiveData.setValue(errorMessage);
            }
        });
    }

    public void fetchUser(String userId) {
        userRepository.findUserById(userId, new UserRepository.OnUserFetchListener() {
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


    public void updateUser(User user, Uri imageUri) {
        Log.d("update user", "Update button clicked");
        if (imageUri != null) {
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                String oldImageUrl = user.getProfileImageUrl();
                String publicId = extractPublicIdFromUrl(oldImageUrl);

                userRepository.deleteUserImageFromCloudinary(getApplication(), publicId, new DeleteCallback() {
                    @Override
                    public void onSuccess() {
                        uploadNewImage(user, imageUri);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        errorLiveData.setValue("Failed to delete old image: " + error.getMessage());
                        saveSuccessLiveData.setValue(false);
                    }
                });
            } else {
                uploadNewImage(user, imageUri);
            }
        } else {
            Log.d("update user", "prepare save");
            // save user information without image
            saveUser(user);
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            Uri uri = Uri.parse(imageUrl);
            String path = uri.getPath();
            if (path != null) {
                int index = path.indexOf("/users/");
                if (index != -1) {
                    return path.substring(index + 1, path.lastIndexOf("."));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void uploadNewImage(User user, Uri imageUri) {
        userRepository.uploadProfileImageToCloudinary(getApplication(), imageUri, new UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                user.setProfileImageUrl(imageUrl);
                saveUser(user);
            }

            @Override
            public void onFailure(Throwable error) {
                errorLiveData.setValue("Upload failed: " + error.getMessage());
                saveSuccessLiveData.setValue(false);
            }
        });
    }

    private void saveUser(User user) {
        userRepository.updateUser(user, new UserRepository.OnUserSaveListener() {
            @Override
            public void onSuccess() {
                Log.d("save success", "saved");
                saveSuccessLiveData.setValue(true);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("save", "failure");
                errorLiveData.setValue(errorMessage);
                saveSuccessLiveData.setValue(false);
            }
        });
    }

    public void deleteUser(String userId) {
        userRepository.deleteUserById(userId, new UserRepository.OnUserDeleteListener() {
            @Override
            public void onSuccess() {
                deleteResult.postValue(true);
            }

            @Override
            public void onFailure(String errorMessage) {
                deleteResult.postValue(false);
                Log.e("UserViewModel", "Delete failed: " + errorMessage);
            }
        });
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public MutableLiveData<List<User>> getUsersLiveData() {
        return usersLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getSaveSuccessLiveData() {
        return saveSuccessLiveData;
    }

    public LiveData<Boolean> getDeleteResult() {
        return deleteResult;
    }
}
