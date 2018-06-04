package com.app.checkinmap.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ASUS-PC on 17/02/2018.
 */

public class UserProfileResponse {

    @SerializedName("totalSize")
    private int mTotalSize;

    @SerializedName("done")
    private boolean mIsDone;

    @SerializedName("records")
    private List<UserData> mUsers;

    public int getTotalSize() {
        return mTotalSize;
    }

    public void setTotalSize(int totalSize) {
        this.mTotalSize = totalSize;
    }

    public boolean isIsDone() {
        return mIsDone;
    }

    public void setIsDone(boolean isDone) {
        this.mIsDone = isDone;
    }

    public List<UserData> getUsers() {
        return mUsers;
    }

    public void setUsers(List<UserData> users) {
        this.mUsers = users;
    }

    /**
     * This class handle all the user information
     */
    public static class UserData{
        @SerializedName("Id")
        private String mId;

        @SerializedName("Email")
        private String mEmail;

        @SerializedName("FirstName")
        private String mFirstName;

        @SerializedName("LastName")
        private String mLastName;

        @SerializedName("Profile")
        private UserProfile mProfile;

        @SerializedName("Username")
        private String mUserName;

        @SerializedName("Country")
        private String mCountry;

        @SerializedName("IsActive")
        private String mIsActive;

        public String getId() {
            return mId;
        }

        public void setId(String id) {
            this.mId = id;
        }

        public String getEmail() {
            return mEmail;
        }

        public void setEmail(String email) {
            this.mEmail = email;
        }

        public String getFirstName() {
            return mFirstName;
        }

        public void setFirstName(String firstName) {
            this.mFirstName = firstName;
        }

        public String getLastName() {
            return mLastName;
        }

        public void setLastName(String lastName) {
            this.mLastName = lastName;
        }

        public UserProfile getProfile() {
            return mProfile;
        }

        public void setProfile(UserProfile profile) {
            this.mProfile = profile;
        }

        public String getUserName() {
            return mUserName;
        }

        public void setUserName(String userName) {
            this.mUserName = userName;
        }

        public String getCountry() {
            return mCountry;
        }

        public void setCountry(String country) {
            mCountry = country;
        }

        public String getIsActive() {
            return mIsActive;
        }

        public void setIsActive(String isActive) {
            this.mIsActive = isActive;
        }

        /**
         * This class handle all the user profile information
         */
        public static class UserProfile{
            @SerializedName("Id")
            private String mId;

            @SerializedName("Name")
            private String mName;

            public String getId() {
                return mId;
            }

            public void setId(String id) {
                this.mId = id;
            }

            public String getName() {
                return mName;
            }

            public void setName(String name) {
                this.mName = name;
            }
        }
    }
}
