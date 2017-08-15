package org.tuzhao.ftp.util;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.WeakHashMap;

/**
 * zhaotu
 * 17-8-15
 */
public final class ActivityStack {

    private static final String TAG = "ActivityStack";

    private static final WeakHashMap<Activity, String> stack = new WeakHashMap<>();

    private static final ActivityStack instance = new ActivityStack();

    private ActivityStack() {
        g("you create a new activity stack!");
    }

    public static ActivityStack getInstance() {
        return instance;
    }

    public void add(Activity key) {
        if (key == null)
            return;
        String v = key.getClass().getName();
        g("add activity is " + v);
        stack.put(key, v);
    }

    public String remove(Activity key) {
        if (key == null)
            return "";
        return stack.remove(key);
    }

    public void close(@Nullable Activity key) {
        if (key == null)
            return;
        key.finish();
        remove(key);
    }

    public boolean containKey(Activity key) {
        return stack.containsKey(key);
    }

    public boolean containValue(String value) {
        return stack.containsValue(value);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        int size = stack.size();
        g("size is " + size);
        return size;
    }

    public void clear() {
        for (Activity activity : stack.keySet()) {
            if (activity != null)
                activity.finish();
        }
        stack.clear();
    }

    private static void g(Object msg) {
        Log.d(TAG, String.valueOf(msg));
    }

}
