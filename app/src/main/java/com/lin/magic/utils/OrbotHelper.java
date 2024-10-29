package com.lin.magic.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;

public class OrbotHelper {

    public final static String ORBOT_PACKAGE_NAME = "org.torproject.android";
    public final static String TOR_SERVICES_PACKAGE_NAME = "org.torproject.torservices";
    /**
     * A request to Orbot to transparently start Tor services
     */
    public final static String ACTION_START = "org.torproject.android.intent.action.START";
    /**
     * A {@link String} {@code packageName} for Orbot to direct its status reply
     * to, used in {@link #ACTION_START} {@link Intent}s sent to Orbot
     */
    public final static String EXTRA_PACKAGE_NAME = "org.torproject.android.intent.extra.PACKAGE_NAME";
    public final static int DEFAULT_PROXY_SOCKS_PORT = 9050;

    public static boolean isOrbotInstalled(Context context) {
        return isAppInstalled(context, ORBOT_PACKAGE_NAME);
    }

    public static boolean isTorServicesInstalled(Context context) {
        return isAppInstalled(context, TOR_SERVICES_PACKAGE_NAME);
    }

    private static boolean isAppInstalled(Context context, String uri) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Check if the tor process is running.  This method is very
     * brittle, and is therefore deprecated in favor of using the
     * {@link #ACTION_STATUS} {@code Intent} along with the
     * {@link #requestStartTor(Context)} method.
     */
    @Deprecated
    public static boolean isOrbotRunning(Context context) {
        int procId = TorServiceUtils.findProcessId(context);

        return (procId != -1);
    }

    /**
     * First, checks whether Orbot is installed. If Orbot is installed, then a
     * broadcast {@link Intent} is sent to request Orbot to start
     * transparently in the background. When Orbot receives this {@code
     * Intent}, it will immediately reply to the app that called this method
     * with an {@link #ACTION_STATUS} {@code Intent} that is broadcast to the
     * {@code packageName} of the provided {@link Context} (i.e.  {@link
     * Context#getPackageName()}.
     * <p>
     * That reply {@link #ACTION_STATUS} {@code Intent} could say that the user
     * has disabled background starts with the status
     * {@link #STATUS_STARTS_DISABLED}. That means that Orbot ignored this
     * request.  To directly prompt the user to start Tor, use
     * {@link #requestShowOrbotStart(Activity)}, which will bring up
     * Orbot itself for the user to manually start Tor.  Orbot always broadcasts
     * it's status, so your app will receive those no matter how Tor gets
     * started.
     *
     * @param context the app {@link Context} will receive the reply
     * @return whether the start request was sent to TorServices or Orbot
     * @see #requestShowOrbotStart(Activity activity)
     * @see <a href="https://developer.android.com/about/versions/oreo/background#services">Android 26 "O" Background Service Limitations</a>
     */
    public static boolean requestStartTor(final Context context) {
        Intent intent = getTorStartIntent(context);
        String packageName = intent.getPackage();
        if (packageName == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 26) {
            context.sendBroadcast(intent);
        } else if (TOR_SERVICES_PACKAGE_NAME.equals(packageName)) {
            intent.setComponent(
                    new ComponentName(TOR_SERVICES_PACKAGE_NAME,
                            TOR_SERVICES_PACKAGE_NAME + ".WakeUpService"));
            ServiceConnection unbind = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    context.sendBroadcast(getTorStartIntent(context));
                    context.unbindService(this);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    // now that TorServices has awoken, request status event
                    context.sendBroadcast(getTorStartIntent(context));
                }
            };
            context.bindService(intent, unbind, Context.BIND_AUTO_CREATE);
        } else if (ORBOT_PACKAGE_NAME.equals(packageName)) {
            context.sendBroadcast(intent);
        } else {
            return false; // this should never happen, but just in case
        }
        return true;
    }

    /**
     * @return an {@link Intent} targeted at the installed Tor provider app,
     * e.g. Orbot or TorServices.  If neither is installed, then this will
     * return an implicit {@code Intent} with no {@code packageName}.
     */
    public static Intent getTorStartIntent(Context context) {
        Intent intent = new Intent(ACTION_START);
        intent.putExtra(EXTRA_PACKAGE_NAME, context.getPackageName());
        if (isTorServicesInstalled(context)) {
            intent.setPackage(TOR_SERVICES_PACKAGE_NAME);
        } else if (isOrbotInstalled(context)) {
            intent.setPackage(ORBOT_PACKAGE_NAME);
        }
        return intent;
    }
}
