package com.example.iserveko;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            DriverManager.setLoginTimeout(3);
        } catch (Exception e) {
            Log.e("MSSQL_DRIVER_ERROR", "jTDS Driver missing or error: " + e.getMessage());
            return null;
        }

        String base = "jdbc:jtds:sqlserver://" + SERVER + ":" + PORT + "/" + DB;

        String[] urlsToTry = {
                base,
                base + ";ssl=request"
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
        executeQueryAsync(query, new Object[0], callback);
    }

    public static void executeQueryAsync(String query, Object[] parameters, QueryCallback callback) {
        Handler handler = new Handler(Looper.getMainLooper());

        EXECUTOR.execute(() -> {
            Connection con = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                con = getConnection();
                if (con == null) {
                    handler.post(() -> callback.onError(new Exception(
                            "Could not connect to Somee MSSQL Server. Check internet connection and Logcat (MSSQL_TRY_FAILED).")));
                    return;
                }

                statement = con.prepareStatement(query);
                for (int index = 0; index < parameters.length; index++) {
                    statement.setObject(index + 1, parameters[index]);
                }
                resultSet = statement.executeQuery();

                Connection finalConnection = con;
                PreparedStatement finalStatement = statement;
                ResultSet finalResultSet = resultSet;
                handler.post(() -> {
                    try {
                        callback.onSuccess(finalResultSet);
                    } finally {
                        try { finalResultSet.close(); } catch (Exception ignored) { }
                        try { finalStatement.close(); } catch (Exception ignored) { }
                        try { finalConnection.close(); } catch (Exception ignored) { }
                    }
                });
                con = null;
                statement = null;
                resultSet = null;
            } catch (Exception e) {
                Log.e("MSSQL_QUERY_ERROR", "Query failed: " + e.getMessage());
                handler.post(() -> callback.onError(e));
            } finally {
                try { if (resultSet != null) resultSet.close(); } catch (Exception ignored) { }
                try { if (statement != null) statement.close(); } catch (Exception ignored) { }
                try { if (con != null) con.close(); } catch (Exception ignored) { }
            }
        });
    }
}