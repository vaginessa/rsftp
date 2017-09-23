package be.ppareit.swiftp;

/**
 * author: tuzhao
 * 2017-09-23 21:40
 */
public final class WifiStateUtil {

    private static boolean canRunStart;
    private static boolean canRunStop;

    static {
        canRunStart = true;
        canRunStop = true;
    }

    private WifiStateUtil() {
    }

    public static boolean isCanRunStart() {
        synchronized (WifiStateUtil.class) {
            return canRunStart;
        }
    }

    public static void setCanRunStart(boolean flag) {
        synchronized (WifiStateUtil.class) {
            canRunStart = flag;
        }
    }

    public static boolean isCanRunStop() {
        synchronized (WifiStateUtil.class) {
            return canRunStop;
        }
    }

    public static void setCanRunStop(boolean flag) {
        synchronized (WifiStateUtil.class) {
            canRunStop = flag;
        }
    }

}
