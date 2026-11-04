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

    private static final String TAG = "MSSQL_CONN";
    private static final int TIMEOUT_SECONDS = 15;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public interface QueryCallback {
        void onSuccess(ResultSet resultSet);
        void onError(Exception e);
    }

    public static Connection getConnection() {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            DriverManager.setLoginTimeout(TIMEOUT_SECONDS);
        } catch (Exception e) {
            Log.e(TAG, "jTDS Driver missing or initialization error: " + e.getMessage(), e);
            return null;
        }

        String base = "jdbc:jtds:sqlserver://" + BuildConfig.MSSQL_SERVER + ":"
                + BuildConfig.MSSQL_PORT + "/" + BuildConfig.MSSQL_DATABASE
                + ";loginTimeout=" + TIMEOUT_SECONDS + ";socketTimeout=" + TIMEOUT_SECONDS;

        String[] urlsToTry = {
                base + ";ssl=off",
                base,
                base + ";ssl=request"
        };

        for (String url : urlsToTry) {
            try {
                Connection con = DriverManager.getConnection(
                        url, BuildConfig.MSSQL_USERNAME, BuildConfig.MSSQL_PASSWORD);
                if (con != null && !con.isClosed()) {
                    Log.d(TAG, "Connected successfully via: " + url);
                    return con;
                }
            } catch (Exception e) {
                Log.w(TAG, "Connection attempt failed via [" + url + "]: " + e.getMessage(), e);
            }
        }

        Log.e(TAG, "All MSSQL connection attempts to " + BuildConfig.MSSQL_SERVER + " failed! Check network, credentials, and server status.");
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
                    Exception connEx = new Exception(
                            "Could not connect to MSSQL Server (" + BuildConfig.MSSQL_SERVER + "). Check internet connection and Logcat (tag: " + TAG + ").");
                    Log.e(TAG, "getConnection() returned null for query: " + query);
                    handler.post(() -> callback.onError(connEx));
                    return;
                }

                statement = con.prepareStatement(query);
                for (int index = 0; index < parameters.length; index++) {
                    statement.setObject(index + 1, parameters[index]);
                }
                resultSet = statement.executeQuery();

                final Connection finalConnection = con;
                final PreparedStatement finalStatement = statement;
                final ResultSet finalResultSet = resultSet;

                handler.post(() -> {
                    try {
                        callback.onSuccess(finalResultSet);
                    } finally {
                        try { finalResultSet.close(); } catch (Exception ignored) { }
                        try { finalStatement.close(); } catch (Exception ignored) { }
                        try { finalConnection.close(); } catch (Exception ignored) { }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Query execution failed: " + e.getMessage(), e);
                try { if (resultSet != null) resultSet.close(); } catch (Exception ignored) { }
                try { if (statement != null) statement.close(); } catch (Exception ignored) { }
                try { if (con != null) con.close(); } catch (Exception ignored) { }
                handler.post(() -> callback.onError(e));
            }
        });
    }
}
