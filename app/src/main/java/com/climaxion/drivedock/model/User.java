package com.climaxion.drivedock.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String createdAt;

    public String getFullName() {
        if (firstName == null && lastName == null) return "";
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }
}
