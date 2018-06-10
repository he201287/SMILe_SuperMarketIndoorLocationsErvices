package com.bonhomme.jb.smile;

public class UserProfileData {
    private String birthDate;
    private String firstName;
    private String lastName;
    private boolean isAdmin;
    private String userEmail;

    public UserProfileData() {}

    public String getBirthDate() {
        return birthDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public String getUserEmail() {
        return userEmail;
    }
}
