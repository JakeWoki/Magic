package com.lin.magic.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.StringTokenizer;

public class TorServiceUtils {

    private final static String TAG = "TorUtils";
    public final static String SHELL_CMD_PIDOF = "pidof";
    public final static String SHELL_CMD_PS = "ps";

    public static int findProcessId(Context context) {
        String dataPath = context.getFilesDir().getParentFile().getParentFile().getAbsolutePath();
        String command = dataPath + "/" + OrbotHelper.ORBOT_PACKAGE_NAME + "/app_bin/tor";
        int procId = -1;

        try {
            procId = findProcessIdWithPidOf(command);

            if (procId == -1)
                procId = findProcessIdWithPS(command);
        } catch (Exception e) {
            try {
                procId = findProcessIdWithPS(command);
            } catch (Exception e2) {
                Log.e(TAG, "Unable to get proc id for command: " + URLEncoder.encode(command), e2);
            }
        }

        return procId;
    }

    // use 'pidof' command
    public static int findProcessIdWithPidOf(String command) throws Exception {

        int procId = -1;

        Runtime r = Runtime.getRuntime();

        Process procPs = null;

        String baseName = new File(command).getName();
        // fix contributed my mikos on 2010.12.10
        procPs = r.exec(new String[]{
                SHELL_CMD_PIDOF, baseName
        });
        // procPs = r.exec(SHELL_CMD_PIDOF);

        BufferedReader reader = new BufferedReader(new InputStreamReader(procPs.getInputStream()));
        String line = null;

        while ((line = reader.readLine()) != null) {

            try {
                // this line should just be the process id
                procId = Integer.parseInt(line.trim());
                break;
            } catch (NumberFormatException e) {
                Log.e("TorServiceUtils", "unable to parse process pid: " + line, e);
            }
        }

        return procId;

    }

    // use 'ps' command
    public static int findProcessIdWithPS(String command) throws Exception {

        int procId = -1;

        Runtime r = Runtime.getRuntime();

        Process procPs = null;

        procPs = r.exec(SHELL_CMD_PS);

        BufferedReader reader = new BufferedReader(new InputStreamReader(procPs.getInputStream()));
        String line = null;

        while ((line = reader.readLine()) != null) {
            if (line.indexOf(' ' + command) != -1) {

                StringTokenizer st = new StringTokenizer(line, " ");
                st.nextToken(); // proc owner

                procId = Integer.parseInt(st.nextToken().trim());

                break;
            }
        }

        return procId;

    }

}
