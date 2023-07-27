package com.okcaros.minusscreen;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.libraries.launcherclient.ILauncherOverlay;
import com.google.android.libraries.launcherclient.ILauncherOverlayCallback;

public class MinusScreenService extends Service {
    static WindowManager.LayoutParams mLayoutParams;
    static WindowManager mWindowManager;
    static MinusScreenController minusScreenController;
    Handler messageHandler = new Handler();
    static ILauncherOverlayCallback callback;
    private static long startTime;
    private static float startLayoutX;
    private static float startX;
    private static float currentX;
    private static float offset;
    static int screenW;
    static float viewOffsetProgress;
    private static float minSwipeRatio = 0.4F;
    private static float minSwipeSpeed = 0.5F;

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

    public void initView(Bundle bundle) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenW = dm.widthPixels;

        WindowManager.LayoutParams lp = bundle.getParcelable("layout_params");

        mLayoutParams = new WindowManager.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);

        mLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mLayoutParams.gravity = Gravity.START;

        // ensure minus screen's index large than Launcher
        mLayoutParams.type = lp.type + 1;

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
                mWindowManager.addView(minusScreenController, mLayoutParams);

                if (minusScreenController != null) {
                    minusScreenController.setLayoutParams(mLayoutParams);
                }
            }
        });
    }

    public static boolean  onTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e("oklauncher", "??????");
                startTime = System.currentTimeMillis();
                startX = event.getRawX();
                startLayoutX = mLayoutParams.x;
                break;
            case MotionEvent.ACTION_MOVE:
                currentX = event.getRawX();

                offset = currentX - startX;

                if (offset < 0) {
                    mLayoutParams.x = (int) (startLayoutX + offset);
                    viewOffsetProgress = 1 + offset / screenW;

                    mWindowManager.updateViewLayout(minusScreenController, mLayoutParams);

                    try {
                        callback.overlayScrollChanged(viewOffsetProgress);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                offset = event.getRawX() - startX;
                float speed = offset / (System.currentTimeMillis() - startTime);

                float startX = mLayoutParams.x;
                float endX = -screenW;
                float startProgress = viewOffsetProgress;
                float endProgress = 0;

                if (speed >= -minSwipeSpeed && offset >= -screenW * minSwipeRatio) {
                    endX = 0;
                    endProgress = 1;
                }

                updateViewWithAnim(startX, endX, startProgress, endProgress);
                break;
        }

        return false;
    };

    public static void updateViewWithAnim(float startX, float endX, float startProgress, float endProgress) {
        ValueAnimator animMinusScreen = ValueAnimator.ofFloat(startX, endX);
        animMinusScreen.setDuration(200);
        animMinusScreen.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                float moveX = (float) animation.getAnimatedValue();
                mLayoutParams.x = (int) moveX;
                mWindowManager.updateViewLayout(minusScreenController, mLayoutParams);
            }
        });
        animMinusScreen.start();
        
        ValueAnimator animLauncher = ValueAnimator.ofFloat(startProgress, endProgress);
        animLauncher.setDuration(200);
        animLauncher.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                float moveProgress = (float) animation.getAnimatedValue();
                viewOffsetProgress = moveProgress;
                try {
                    callback.overlayScrollChanged(viewOffsetProgress);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        animLauncher.start();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initEvent() {
        minusScreenController = new MinusScreenController(this);
        minusScreenController.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return MinusScreenService.this.onTouch(event);
            }
        });
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
                    mWindowManager.updateViewLayout(minusScreenController, mLayoutParams);
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

                    float startX = mLayoutParams.x;
                    float endX = 0;
                    float startProgress = viewOffsetProgress;
                    float endProgress;

                    if (speed <= minSwipeSpeed && viewOffsetProgress <= minSwipeRatio) {
                        endX = -screenW;
                        endProgress = 0;
                    } else {
                        endProgress = 1;
                    }

                    updateViewWithAnim(startX, endX, startProgress, endProgress);
                }
            });

        }

        @Override
        public void windowAttached(WindowManager.LayoutParams lp, ILauncherOverlayCallback cb, int flags) throws RemoteException {

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
            initView(bundle);

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
