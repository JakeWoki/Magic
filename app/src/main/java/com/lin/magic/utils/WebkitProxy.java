package com.lin.magic.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.Log;
import android.webkit.WebView;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WebkitProxy {

    public static boolean setProxy(String appClass, Context ctx, WebView wView, String host, int port) throws Exception {

        setSystemProperties(host, port);

        boolean worked = setWebkitProxyLollipop(ctx, host, port);

        return worked;
    }

    public static boolean resetProxy(String appClass, Context ctx) throws Exception {

        resetSystemProperties();

        return resetLollipopProxy(appClass, ctx);
    }

    @TargetApi(21)
    public static boolean resetLollipopProxy(String appClass, Context appContext) {

        return setWebkitProxyLollipop(appContext, null, 0);
    }

    // http://stackanswers.com/questions/25272393/android-webview-set-proxy-programmatically-on-android-l
    @TargetApi(21) // for android.util.ArrayMap methods
    @SuppressWarnings("rawtypes")
    private static boolean setWebkitProxyLollipop(Context appContext, String host, int port) {

        try {
            Class applictionClass = Class.forName("android.app.Application");
            Field mLoadedApkField = applictionClass.getDeclaredField("mLoadedApk");
            mLoadedApkField.setAccessible(true);
            Object mloadedApk = mLoadedApkField.get(appContext);
            Class loadedApkClass = Class.forName("android.app.LoadedApk");
            Field mReceiversField = loadedApkClass.getDeclaredField("mReceivers");
            mReceiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) mReceiversField.get(mloadedApk);
            for (Object receiverMap : receivers.values()) {
                for (Object receiver : ((ArrayMap) receiverMap).keySet()) {
                    Class clazz = receiver.getClass();
                    if (clazz.getName().contains("ProxyChangeListener")) {
                        Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);
                        Object proxyInfo = null;
                        if (host != null) {
                            final String CLASS_NAME = "android.net.ProxyInfo";
                            Class cls = Class.forName(CLASS_NAME);
                            Method buildDirectProxyMethod = cls.getMethod("buildDirectProxy", String.class, Integer.TYPE);
                            proxyInfo = buildDirectProxyMethod.invoke(cls, host, port);
                        }
                        intent.putExtra("proxy", (Parcelable) proxyInfo);
                        onReceiveMethod.invoke(receiver, appContext, intent);
                    }
                }
            }
            return true;
        } catch (ClassNotFoundException e) {
            Log.d("ProxySettings", "Exception setting WebKit proxy on Lollipop through ProxyChangeListener: " + e.toString());
        } catch (NoSuchFieldException e) {
            Log.d("ProxySettings", "Exception setting WebKit proxy on Lollipop through ProxyChangeListener: " + e.toString());
        } catch (IllegalAccessException e) {
            Log.d("ProxySettings", "Exception setting WebKit proxy on Lollipop through ProxyChangeListener: " + e.toString());
        } catch (NoSuchMethodException e) {
            Log.d("ProxySettings", "Exception setting WebKit proxy on Lollipop through ProxyChangeListener: " + e.toString());
        } catch (InvocationTargetException e) {
            Log.d("ProxySettings", "Exception setting WebKit proxy on Lollipop through ProxyChangeListener: " + e.toString());
        }
        return false;
    }

    private static void setSystemProperties(String host, int port) {

        System.setProperty("proxyHost", host);
        System.setProperty("proxyPort", Integer.toString(port));

        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", Integer.toString(port));

        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", Integer.toString(port));

        System.setProperty("socks.proxyHost", host);
        System.setProperty("socks.proxyPort", Integer.toString(OrbotHelper.DEFAULT_PROXY_SOCKS_PORT));

        System.setProperty("socksProxyHost", host);
        System.setProperty("socksProxyPort", Integer.toString(OrbotHelper.DEFAULT_PROXY_SOCKS_PORT));
    }

    private static void resetSystemProperties() {

        System.setProperty("proxyHost", "");
        System.setProperty("proxyPort", "");

        System.setProperty("http.proxyHost", "");
        System.setProperty("http.proxyPort", "");

        System.setProperty("https.proxyHost", "");
        System.setProperty("https.proxyPort", "");

        System.setProperty("socks.proxyHost", "");
        System.setProperty("socks.proxyPort", Integer.toString(OrbotHelper.DEFAULT_PROXY_SOCKS_PORT));

        System.setProperty("socksProxyHost", "");
        System.setProperty("socksProxyPort", Integer.toString(OrbotHelper.DEFAULT_PROXY_SOCKS_PORT));
    }

}
