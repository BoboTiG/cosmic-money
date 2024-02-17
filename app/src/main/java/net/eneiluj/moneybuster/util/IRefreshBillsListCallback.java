package net.eneiluj.moneybuster.util;

/**
 * Call back into the BillsListActivity and ask it to refresh the list in the UI
 */
public interface IRefreshBillsListCallback {
    void refreshLists(boolean scrollToTop);
}
