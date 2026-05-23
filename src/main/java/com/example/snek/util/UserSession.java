package com.example.snek.util;

public class UserSession {
    private static int loggedInUserId = -1;
    private static String loggedInUsername = null;

    public static void login(int id, String username) {
        loggedInUserId = id;
        loggedInUsername = username;
    }

    public static void logout() {
        loggedInUserId = -1;
        loggedInUsername = null;
    }

    public static int getUserId() { return loggedInUserId; }
    public static String getUsername() { return loggedInUsername; }
    public static boolean isLoggedIn() { return loggedInUserId != -1; }
}