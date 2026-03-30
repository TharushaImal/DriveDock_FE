package com.climaxion.drivedock.util;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0]{1}[7]{1}[01245678]{1}[0-9]{7}$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2;
    }
}
