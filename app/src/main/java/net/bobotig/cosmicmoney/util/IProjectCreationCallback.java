package net.bobotig.cosmicmoney.util;

/**
 * Callback
 */
public interface IProjectCreationCallback {
    void onFinish();

    void onFinish(String result, String message, boolean usePrivateApi);

    void onScheduled();
}
