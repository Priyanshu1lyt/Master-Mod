package com.creatorshq.master.modules.easytp;

/** Status line + error window, extracted unchanged from the original EasyTpClient. */
final class EasyTpState {
    static final long ERROR_WINDOW_MS = 2000;
    private static String statusText = "";
    private static boolean statusError = false;
    private static long lastSendTime = 0;

    private EasyTpState() {}

    static void markCommandSent() { lastSendTime = System.currentTimeMillis(); }
    static long lastSendTime() { return lastSendTime; }
    static void setStatus(String text, boolean error) { statusText = text; statusError = error; }
    static String statusText() { return statusText; }
    static boolean isStatusError() { return statusError; }
    static void clearStatus() { statusText = ""; statusError = false; }
}
