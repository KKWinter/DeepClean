package com.catchgift.trashclear.ads;

import com.cloudtech.ads.core.CTAdEventListener;
import com.cloudtech.ads.core.CTNative;


public class DCTAdEventListener implements CTAdEventListener {

    private void showMsg(String msg){
//        Toast.makeText(ContextHolder.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAdviewGotAdSucceed(CTNative result) {
        showMsg("onAdviewGotAdSucceed");
    }

    @Override
    public void onInterstitialLoadSucceed(CTNative result) {
        showMsg("onInterstitialLoadSucceed");
    }

    @Override
    public void onAdviewGotAdFail(CTNative result) {
        showMsg(result.getErrorsMsg());
    }

    @Override
    public void onAdviewIntoLandpage(CTNative result) {
        showMsg("onAdviewIntoLandpage");
    }

    @Override
    public void onStartLandingPageFail(CTNative result) {
        showMsg("onStartLandingPageFail");
    }

    @Override
    public void onAdviewDismissedLandpage(CTNative result) {
        showMsg("onAdviewDismissedLandpage");
    }

    @Override
    public void onAdviewClicked(CTNative result) {
        showMsg("onAdviewClicked");
    }

    @Override
    public void onAdviewClosed(CTNative result) {
        showMsg("onAdviewClosed");
    }

    @Override
    public void onAdviewDestroyed(CTNative result) {
        showMsg("onAdviewDestroyed");

    }

}
