package com.resumebuilder.resumebuilderapi.util;

/*
This class AppConstants contains the routes that are used in controller
This class provides abstraction or security, directly not to expose the routes or endpoints to users.
 */
public class AppConstants {
    public static final String AUTH_CONTROLLER = "/api/auth";
    public static final String REGISTER = "/register";
    public static final String VERIFY_EMAIL = "/verify-email";
    public static final String UPLOAD_IMAGE = "/upload-image";
    public static final String LOGIN = "/login";
    public static final String RESEND_VERIFICATION = "/resend-verification";
}