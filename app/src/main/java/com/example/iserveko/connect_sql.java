package com.example.iserveko; // MUST be at line 1

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;

public class connect_sql {

    Connection con;

    @SuppressLint("NewApi")
    public Connection conclass() {

        String ip = "192.168.1.7",
                port = "1433",
                db = "iserveko_data",
                username = "admin",
                password = "serverko_01";

        StrictMode.ThreadPolicy a = new StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .build();

        StrictMode.setThreadPolicy(a);

        String ConnectURL = null;

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");

            ConnectURL = "jdbc:jtds:sqlserver://" + ip + ":" + port
                    + "/" + db
                    + ";user=" + username
                    + ";password=" + password + ";";

            con = DriverManager.getConnection(ConnectURL);
        }
        catch (Exception e) {
            Log.e("Error is ", e.getMessage());
        }

        return con;
    }
}