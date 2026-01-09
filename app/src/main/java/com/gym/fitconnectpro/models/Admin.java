package com.gym.fitconnectpro.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "Admins",
    indices = {@Index(value = "username", unique = true), @Index(value = "email", unique = true)}
)
public class Admin {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "admin_id")
    public int adminId;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "password_hash")
    public String passwordHash;

    @ColumnInfo(name = "full_name")
    public String fullName;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "phone")
    public String phone;

    @ColumnInfo(name = "role")
    public String role;

    @ColumnInfo(name = "account_status", defaultValue = "ACTIVE")
    public String accountStatus;

    @ColumnInfo(name = "created_date", defaultValue = "CURRENT_TIMESTAMP")
    public String createdDate;

    @ColumnInfo(name = "last_login")
    public String lastLogin;

    // Default constructor
    public Admin() {}

    // Constructor for creating new admin
    public Admin(String username, String passwordHash, String fullName, String email, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.accountStatus = "ACTIVE";
    }
}
