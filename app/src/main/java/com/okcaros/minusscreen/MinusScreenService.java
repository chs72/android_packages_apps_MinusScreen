package com.okcaros.minusscreen;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.libraries.launcherclient.ILauncherOverlay;
import com.google.android.libraries.launcherclient.ILauncherOverlayCallback;

public class MinusScreenService extends Service {
    // 负一屏
    WindowManager.LayoutParams mLayoutParams;
    WindowManager mWindowManager;
    View layoutView;
    Handler messageHandler = new Handler();
    ILauncherOverlayCallback callback;
    int screenW;
    float viewOffsetProgress;

    private float startLayoutX;
    private float startX;
    private float currentX;
    private float offset;
    private float minSwipeRatio = 0.4F;
    private float minSwipeSpeed = 0.5F;

    @Override
    public void onCreate() {
        super.onCreate();

        layoutView = View.inflate(getBaseContext(), R.layout.main_activity, null);
        layoutView.setOnTouchListener(new View.OnTouchListener() {
            private long startTime;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 用户按下屏幕的动作
                        startTime = System.currentTimeMillis();
                        startX = event.getRawX();
                        startLayoutX = mLayoutParams.x;
                        Log.e("oklauncher", "start x" + startX);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 用户在屏幕上移动的动作
                        Log.e("oklauncher", event.getRawX() + "current x");
                        currentX = event.getRawX();

                        offset = currentX - startX;
                        Log.e("oklauncher", offset + "offset");

                        if (offset < 0) {
                            mLayoutParams.x = (int) (startLayoutX + offset);
                            viewOffsetProgress = 1 - offset / screenW;

                            mWindowManager.updateViewLayout(layoutView, mLayoutParams);

                            try {
                                callback.overlayScrollChanged(viewOffsetProgress);
                            } catch (RemoteException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        float speed = offset / (System.currentTimeMillis() - startTime);
                        Log.e("oklauncher", "end scroll" + speed);
                        float x = mLayoutParams.x;
                        if (speed < -minSwipeSpeed) {
                            mLayoutParams.x = -screenW;
                            viewOffsetProgress = 0;
                        } else {
                            // finish move
                            if (offset  < -screenW * minSwipeRatio) {
                                mLayoutParams.x = -screenW;
                                viewOffsetProgress = 0;
                            } else {
                                mLayoutParams.x = 0;
                                viewOffsetProgress = 1;
                            }
                        }

                        Animation animation = new TranslateAnimation(x, mLayoutParams.x, 0, 0);
                        animation.setDuration(5000);
                        layoutView.startAnimation(animation);
                        mWindowManager.updateViewLayout(layoutView, mLayoutParams);
                        try {
                            callback.overlayScrollChanged(viewOffsetProgress);
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                }

                return true; // 返回 true 表示已处理该触摸事件，false 表示未处理
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("oklauncher", "service call");

        init();

        return stub;
    }

    private void init() {
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }

    ILauncherOverlay.Stub stub = new ILauncherOverlay.Stub() {
        private long startTime;

        @Override
        public void startScroll() throws RemoteException {
            Log.e("oklauncher", "startScroll");
            startTime = System.currentTimeMillis();
        }

        @Override
        public void onScroll(float progress) throws RemoteException {
            messageHandler.post(new Runnable() {
                @Override
                public void run() {
                    viewOffsetProgress = progress;
                    mLayoutParams.x = (int) (-screenW * (1 - progress));
                    mWindowManager.updateViewLayout(layoutView, mLayoutParams);

                    try {
                        callback.overlayScrollChanged(progress);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        @Override
        public void endScroll() throws RemoteException {
            Log.e("oklauncher", "end scroll");
            messageHandler.post(new Runnable() {
                @Override
                public void run() {
                    long currentTime = System.currentTimeMillis();
                    float speed = screenW * viewOffsetProgress / (currentTime - startTime);

                    Log.e("oklauncher", speed + "速度" + currentTime + "currentTime" + startTime + "startTime" + viewOffsetProgress + "progress");
                    if (speed > minSwipeSpeed) {
                        mLayoutParams.x = 0;
                        viewOffsetProgress = 1;
                    } else {
                        if (viewOffsetProgress > minSwipeRatio) {
                            mLayoutParams.x = 0;
                            viewOffsetProgress = 1;
                        } else {
                            mLayoutParams.x = -screenW;
                            viewOffsetProgress = 0;
                        }
                    }

                    mWindowManager.updateViewLayout(layoutView, mLayoutParams);
                    try {
                        callback.overlayScrollChanged(viewOffsetProgress);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    Log.e("oklauncher end scroll", mLayoutParams.x + "---------" + viewOffsetProgress);
                }
            });

        }

        @Override
        public void windowAttached(WindowManager.LayoutParams lp, ILauncherOverlayCallback cb, int flags) throws RemoteException {
            Log.e("oklauncher", lp.packageName);
        }

        @Override
        public void windowDetached(boolean isChangingConfigurations) throws RemoteException {

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
            Log.e("oklauncher", "open overlay");
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
            Log.e("oklauncher", "windowAttached2");

            DisplayMetrics dm = getResources().getDisplayMetrics();
            screenW = dm.widthPixels;

            WindowManager.LayoutParams lp = bundle.getParcelable("layout_params");

            mLayoutParams = new WindowManager.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
            mLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            mLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mLayoutParams.gravity = Gravity.START;
            // 负一屏的 Window 层级比 Launcher 的大就可以
            mLayoutParams.type = lp.type + 1;

            Log.e("oklauncher", lp.type + "-----" + lp.token);
            mLayoutParams.token = lp.token;
            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS |
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            mLayoutParams.x = -screenW;
            mLayoutParams.format = PixelFormat.TRANSLUCENT;

            messageHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWindowManager.addView(layoutView, mLayoutParams);
                }
            });


            if (cb != null) {
                callback = cb;
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
