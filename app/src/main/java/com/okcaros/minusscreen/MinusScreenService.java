package com.okcaros.minusscreen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.libraries.launcherclient.ILauncherOverlay;
import com.google.android.libraries.launcherclient.ILauncherOverlayCallback;

import java.util.concurrent.atomic.AtomicBoolean;

public class MinusScreenService extends Service {
    private final static String Tag = "MinusScreenService";
    private final static float MinSwipeRatio = 0.1F;
    private final static int MsgOnScroll = 1;
    private final static int MsgOnScrollEnd = 2;
    private final static int MsgOnWindowsAttached = 3;

    private final static int MsgOnWindowsDetach = 4;
    WindowManager mWindowManager;
    private WindowManager.LayoutParams mRootContainerLp;
    private MinusScreenViewRoot minusScreenViewRoot;
    private final Handler messageHandler;
    ILauncherOverlayCallback launcherOverlayCallback;
    private float motionEventDownRawX;
    private int screenW;
    private float overlayScrollValue;
    private final AtomicBoolean animating = new AtomicBoolean(false);

    @SuppressLint("HandlerLeak")
    public MinusScreenService() {
        messageHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case MsgOnScroll: {
                        float progress = (float) msg.obj;
                        mRootContainerLp.x = (int) (-screenW * (1 - progress));
                        mWindowManager.updateViewLayout(minusScreenViewRoot, mRootContainerLp);
                        overlayScrollChanged(progress);
                        break;
                    }
                    case MsgOnScrollEnd: {
                        float startX = mRootContainerLp.x;
                        float endX = 0;
                        float endProgress;

                        if (overlayScrollValue <= MinSwipeRatio) {
                            endX = -screenW;
                            endProgress = 0;
                        } else {
                            endX = 0;
                            endProgress = 1;
                        }

                        updateViewWithAnim(startX, endX, overlayScrollValue, endProgress);
                        break;
                    }

                    case MsgOnWindowsAttached: {
                        Bundle bundle = (Bundle) msg.obj;
                        if (minusScreenViewRoot != null && minusScreenViewRoot.getParent() != null) {
                            break;
                        }
                        WindowManager.LayoutParams lp = bundle.getParcelable("layout_params");
                        if (lp == null) {
                            break;
                        }

                        DisplayMetrics dm = getResources().getDisplayMetrics();
                        screenW = dm.widthPixels;

                        mRootContainerLp = new WindowManager.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
                        mRootContainerLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        mRootContainerLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        mRootContainerLp.gravity = Gravity.START;

                        // ensure minus screen's index large than Launcher
                        mRootContainerLp.type = lp.type + 1;
                        mRootContainerLp.token = lp.token;
                        mRootContainerLp.flags = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS |
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
                                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
                        mRootContainerLp.x = -screenW;
                        mRootContainerLp.format = PixelFormat.TRANSLUCENT;
                        mWindowManager.addView(minusScreenViewRoot, mRootContainerLp);
                        break;
                    }

                    case MsgOnWindowsDetach: {
                        if (minusScreenViewRoot != null && minusScreenViewRoot.getParent() != null) {
                            mWindowManager.removeViewImmediate(minusScreenViewRoot);
                            minusScreenViewRoot = null;
                            mRootContainerLp = null;
                        }
                        break;
                    }
                }
            }
        };
    }

    public interface MinusScreenAgentCallback {
        boolean onTouch(MotionEvent event);

        void showAppSelect(int appType);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initEvent();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        return stub;
    }

    public boolean onTouch(MotionEvent event) {
        float offset;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                motionEventDownRawX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                offset = event.getRawX() - motionEventDownRawX;

                if (offset < 0) {
                    mRootContainerLp.x = (int) offset;
                    mWindowManager.updateViewLayout(minusScreenViewRoot, mRootContainerLp);
                    overlayScrollChanged(1 + offset / screenW);
                }
                break;
            case MotionEvent.ACTION_UP:
                offset = event.getRawX() - motionEventDownRawX;
                float endX = -screenW;
                float endProgress = 0;

                if (offset >= -screenW * MinSwipeRatio) {
                    endX = 0;
                    endProgress = 1;
                }

                updateViewWithAnim(mRootContainerLp.x, endX, overlayScrollValue, endProgress);
                break;
        }

        return false;
    }

    public void updateViewWithAnim(float startX, float endX, float startProgress, float endProgress) {
        if (!animating.compareAndSet(false, true)) {
            return;
        }
        ValueAnimator animMinusScreen = ValueAnimator.ofFloat(0, 1);
        animMinusScreen.setDuration(200);
        animMinusScreen.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                mRootContainerLp.x = (int) ((endX - startX) * v + startX);
                mWindowManager.updateViewLayout(minusScreenViewRoot, mRootContainerLp);

                overlayScrollChanged((endProgress - startProgress) * v + startProgress);
            }
        });
        animMinusScreen.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animating.set(false);
            }
        });
        animMinusScreen.start();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initEvent() {
        minusScreenViewRoot = new MinusScreenViewRoot(this);
        minusScreenViewRoot.setCallback(new MinusScreenAgentCallback() {
            @Override
            public boolean onTouch(MotionEvent event) {
                return MinusScreenService.this.onTouch(event);
            }

            @Override
            public void showAppSelect(int appType) {
                // ToDo 实现APP选择
//                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//
//                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
//                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
//                lp.gravity = Gravity.CENTER;
//
//                lp.type = mLayoutParams.type + 2;
//                lp.token = mLayoutParams.token;
//
//                lp.flags = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS |
//                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
//                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
//                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
//                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
//                lp.format = PixelFormat.TRANSLUCENT;
//
//                AppSelectController appSelectController = new AppSelectController(MinusScreenService.this, appType);
//                mWindowManager.addView(appSelectController, lp);
            }
        });
        minusScreenViewRoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return MinusScreenService.this.onTouch(event);
            }
        });
    }

    private void overlayScrollChanged(float value) {
        try {
            launcherOverlayCallback.overlayScrollChanged(value);
            overlayScrollValue = value;
        } catch (RemoteException e) {
            Log.d(Tag, "overlayScrollChanged failed");
        }
    }

    ILauncherOverlay.Stub stub = new ILauncherOverlay.Stub() {
        @Override
        public void startScroll() {

        }

        @Override
        public void onScroll(float progress) {
            Message msg = new Message();
            msg.what = MsgOnScroll;
            msg.obj = progress;

            messageHandler.sendMessage(msg);
        }

        @Override
        public void endScroll() {
            Message msg = new Message();
            msg.what = MsgOnScrollEnd;

            messageHandler.sendMessage(msg);
        }

        @Override
        public void windowAttached(WindowManager.LayoutParams lp, ILauncherOverlayCallback cb, int flags) throws RemoteException {

        }

        @Override
        public void windowDetached(boolean isChangingConfigurations) {
            Message message = new Message();
            message.what = MsgOnWindowsDetach;
            messageHandler.sendMessage(message);
        }

        @Override
        public void closeOverlay(int flags) throws RemoteException {

        }

        @Override
        public void onPause() throws RemoteException {

        }

        @Override
        public void onResume() throws RemoteException {

        }

        @Override
        public void openOverlay(int flags) throws RemoteException {

        }

        @Override
        public void requestVoiceDetection(boolean start) throws RemoteException {

        }

        @Override
        public String getVoiceSearchLanguage() throws RemoteException {
            return null;
        }

        @Override
        public boolean isVoiceDetectionRunning() throws RemoteException {
            return false;
        }

        @Override
        public boolean hasOverlayContent() throws RemoteException {
            return false;
        }

        @Override
        public void windowAttached2(Bundle bundle, ILauncherOverlayCallback cb) throws RemoteException {
            Message message = new Message();
            message.what = MsgOnWindowsAttached;
            message.obj = bundle;
            messageHandler.sendMessage(message);

            if (cb != null) {
                launcherOverlayCallback = cb;
                cb.overlayStatusChanged(1);
            }
        }

        @Override
        public void unusedMethod() throws RemoteException {

        }

        @Override
        public void setActivityState(int flags) throws RemoteException {

        }

        @Override
        public boolean startSearch(byte[] data, Bundle bundle) throws RemoteException {
            return false;
        }
    };
}
