package com.example.iserveko;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class connect_sql {

    // Somee Cloud MSSQL Database Credentials
    private static final String SERVER = "iserveko.mssql.somee.com";
    private static final String PORT = "1433";
    private static final String DB = "iserveko";
    private static final String USERNAME = "XAVRYN_SQLLogin_1";
    private static final String PASSWORD = "raijin28";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public interface QueryCallback {
        void onSuccess(ResultSet resultSet);
        void onError(Exception e);
    }

    public static Connection getConnection() {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            DriverManager.setLoginTimeout(15);
        } catch (Exception e) {
            Log.e("MSSQL_DRIVER_ERROR", "jTDS Driver missing or error: " + e.getMessage());
            return null;
        }

        String base = "jdbc:jtds:sqlserver://" + SERVER + ":" + PORT + "/" + DB;

        // Same server, different SSL modes. The password is not put in the URL, so it is not logged.
        String[] urlsToTry = {
                base,
                base + ";ssl=request",
                base + ";ssl=require",
                base + ";useNTLMv2=true;ssl=request"
        };

        for (String url : urlsToTry) {
            try {
                Connection con = DriverManager.getConnection(url, USERNAME, PASSWORD);
                if (con != null && !con.isClosed()) {
                    Log.d("MSSQL_SUCCESS", "Connected via: " + url);
                    return con;
                }
            } catch (Exception e) {
                Log.w("MSSQL_TRY_FAILED", "Failed via [" + url + "]: " + e.getMessage());
            }
        }

        Log.e("MSSQL_ERROR", "All Somee MSSQL connection attempts failed!");
        return null;
    }

    public static void executeQueryAsync(String query, QueryCallback callback) {
        Handler handler = new Handler(Looper.getMainLooper());

        EXECUTOR.execute(() -> {
            try {
                Connection con = getConnection();
                if (con != null) {
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    handler.post(() -> callback.onSuccess(rs));
                } else {
                    handler.post(() -> callback.onError(new Exception(
                            "Could not connect to Somee MSSQL Server. Check internet connection and Logcat (MSSQL_TRY_FAILED).")));
                }
            } catch (Exception e) {
                Log.e("MSSQL_QUERY_ERROR", "Query failed: " + e.getMessage());
                handler.post(() -> callback.onError(e));
            }
        });
    }
}