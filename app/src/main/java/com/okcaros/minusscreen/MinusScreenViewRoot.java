package com.okcaros.minusscreen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.okcaros.minusscreen.setting.SettingsActivity;
import com.okcaros.tool.ScreenTool;

import java.util.ArrayList;
import java.util.List;

public class MinusScreenViewRoot extends ConstraintLayout {
    private final static String Tag = "MinusScreenViewRoot";
    public static final int TYPE_MAP = 1;
    public static final int TYPE_MUSIC = 2;
    public static final int TYPE_WEATHER = 3;
    float screenRatio;
    int activeAppPosition = 0;
    private String activePackageName = null;
    private MinusScreenService.MinusScreenAgentCallback callback;

    public MinusScreenViewRoot(@NonNull Context context) {
        super(context);

        init();
    }

    public MinusScreenViewRoot(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MinusScreenViewRoot(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MinusScreenViewRoot(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        ScreenTool.ScreenInfo screenInfo = ScreenTool.getScreenInfo(getContext());
        int screenW = screenInfo.realWidth;
        int screenH = screenInfo.realHeight;
        screenRatio = (float) screenW / screenH;

        if (isVerticalScreen()) {
            inflate(getContext(), R.layout.minus_screen_vertical, this);
        } else {
            inflate(getContext(), R.layout.minus_screen, this);
        }

        int dimension8 = (int) getResources().getDimensionPixelSize(R.dimen.dp_8);

        View viewRoot = findViewById(R.id.minus_screen_container);
        if (isVerticalScreen()) {
            viewRoot.setPadding(0, 0, 0, screenInfo.navBarSize);
        } else {
            viewRoot.setPadding(screenInfo.navBarSize, 0, 0, 0);
        }
        RecyclerView appMenuRcv = findViewById(R.id.app_menu_rcv);

        View appContent = findViewById(R.id.app_content);
        ConstraintLayout.LayoutParams appContentLp = (ConstraintLayout.LayoutParams) appContent.getLayoutParams();

        if (isVerticalScreen()) {
            appContentLp.leftMargin = dimension8;
            appContentLp.topMargin = screenInfo.statusBarSize;
            appContentLp.rightMargin = dimension8;
            appContentLp.bottomMargin = dimension8;
            appMenuRcv.setPadding(dimension8, dimension8, dimension8, dimension8);
        } else {
            appContentLp.leftMargin = 0;
            appContentLp.topMargin = screenInfo.statusBarSize;
            appContentLp.rightMargin = dimension8;
            appContentLp.bottomMargin = dimension8;
            appMenuRcv.setPadding(dimension8, screenInfo.statusBarSize, dimension8, dimension8);
        }

        appContent.setLayoutParams(appContentLp);

        List<MenuAppEntity> dataList = new ArrayList<>();

        dataList.add(new MenuAppEntity(TYPE_MAP));
        dataList.add(new MenuAppEntity(TYPE_MUSIC));

        if (!isVerticalScreen()) {
            dataList.add(new MenuAppEntity(TYPE_WEATHER));
        }

        AppMenuAdapter adapter = new AppMenuAdapter(dataList);
        appMenuRcv.setAdapter(adapter);

        appMenuRcv.setLayoutManager(new LinearLayoutManager(getContext(), isVerticalScreen() ? LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL, false));

        appMenuRcv.setOnTouchListener((v, event) -> {
            if (callback == null) {
                return false;
            }
            return callback.onTouch(event);
        });


        findViewById(R.id.config_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback == null) {
                    return;
                }
                callback.configApp(activeAppPosition);
            }
        });
    }

    public void setCallback(MinusScreenService.MinusScreenAgentCallback callback) {
        this.callback = callback;
    }

    private boolean isVerticalScreen() {
        float verticalScreenRatio = 0.5f;
        return screenRatio <= verticalScreenRatio;
    }

    private boolean isWideScreen() {
        float wideScreenRatio = 2.5f;
        return screenRatio >= wideScreenRatio;
    }

    public class AppMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<MenuAppEntity> dataList;

        public AppMenuAdapter(List<MenuAppEntity> dataList) {
            this.dataList = dataList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            View view;
            RecyclerView.ViewHolder viewHolder = null;

            switch (viewType) {
                case TYPE_MAP: {
                    view = inflater.inflate(R.layout.app_map_item, parent, false);
                    viewHolder = new MapViewHolder(view, parent);
                    break;
                }
                case TYPE_MUSIC: {
                    view = inflater.inflate(R.layout.app_music_item, parent, false);
                    viewHolder = new MusicViewHolder(view, parent);
                    break;
                }
                case TYPE_WEATHER: {
                    view = inflater.inflate(R.layout.app_weather_item, parent, false);
                    viewHolder = new WeatherViewHolder(view, parent);
                    break;
                }
                default:
                    Log.e("oklauncher", "不会都是这里吧" + viewType);
                    view = inflater.inflate(R.layout.app_map_item, parent, false);
                    viewHolder = new MapViewHolder(view, parent);
                    break;
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MapViewHolder) {
                MapViewHolder mapViewHolder = (MapViewHolder) holder;

            } else if (holder instanceof MusicViewHolder) {
                MusicViewHolder musicViewHolder = (MusicViewHolder) holder;
                musicViewHolder.musicName.setText(getResources().getText(R.string.unknown_music));
                musicViewHolder.musicAuthor.setText(getResources().getText(R.string.unknown_singer));
            } else if (holder instanceof WeatherViewHolder) {
                WeatherViewHolder weatherViewHolder = (WeatherViewHolder) holder;

                weatherViewHolder.temperature.setText("30°");
                weatherViewHolder.weatherDetail.setText("多云");
                weatherViewHolder.location.setText("杭州市萧山区");
            }

            holder.itemView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // change active app
                    activeAppPosition = holder.getAdapterPosition();
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return dataList.get(position).getType();
        }

        public class BaseViewHolder extends RecyclerView.ViewHolder {
            public BaseViewHolder(@NonNull View itemView, @NonNull ViewGroup parent) {
                super(itemView);

                int parentHeight = parent.getMeasuredHeight();
                ScreenTool.ScreenInfo screenInfo = ScreenTool.getScreenInfo(getContext());
                if (isVerticalScreen()) {
                    itemView.getLayoutParams().width = (int) ((parent.getMeasuredWidth() - 2 * getResources().getDimensionPixelSize(R.dimen.dp_8)) / 2);
                    return;
                }

                if (isWideScreen()) {
                    itemView.getLayoutParams().height = (int) ((parentHeight - 2 * getResources().getDimensionPixelSize(R.dimen.dp_8) - screenInfo.statusBarSize) / 2);
                    return;
                }

                // normal screen
                itemView.getLayoutParams().height = (int) ((parentHeight - 3 * getResources().getDimensionPixelSize(R.dimen.dp_8) - screenInfo.statusBarSize) / 3);
            }
        }

        public class MapViewHolder extends BaseViewHolder {
            @SuppressLint("ClickableViewAccessibility")
            public MapViewHolder(@NonNull View itemView, @NonNull ViewGroup parent) {
                super(itemView, parent);
                itemView.findViewById(R.id.nav_icon).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appAndEnterFreeForm("com.autonavi.amapauto");
                    }
                });
            }
        }

        public class MusicViewHolder extends BaseViewHolder {
            TextView musicName;
            TextView musicAuthor;

            public MusicViewHolder(@NonNull View itemView, @NonNull ViewGroup parent) {
                super(itemView, parent);

                musicName = itemView.findViewById(R.id.music_name);
                musicAuthor = itemView.findViewById(R.id.music_author);

                itemView.findViewById(R.id.music_thumb_icon).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appAndEnterFreeForm("com.netease.cloudmusic");
                    }
                });
            }
        }

        public class WeatherViewHolder extends BaseViewHolder {
            ImageView weatherIcon;
            TextView temperature;
            TextView weatherDetail;
            TextView location;

            public WeatherViewHolder(@NonNull View itemView, @NonNull ViewGroup parent) {
                super(itemView, parent);

                weatherIcon = itemView.findViewById(R.id.weather_icon);
                temperature = itemView.findViewById(R.id.weather_temperature);
                weatherDetail = itemView.findViewById(R.id.weather_detail);
                location = itemView.findViewById(R.id.weather_location);

                weatherIcon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), SettingsActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        v.getContext().startActivity(i);
                    }
                });
            }
        }
    }

    public static class MenuAppEntity {
        public MenuAppEntity(int type) {
            this.type = type;
        }

        private int type;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

    private void appAndEnterFreeForm(String packageName) {
        activePackageName = packageName;

        int[] location = new int[2];
        View appContentView = findViewById(R.id.app_content);
        appContentView.getLocationInWindow(location);

        findViewById(R.id.empty_bg).setVisibility(INVISIBLE);

        int left = location[0];
        int top = location[1];
        int right = left + appContentView.getMeasuredWidth();
        int bottom = top + appContentView.getMeasuredHeight();

        // Log.d(Tag, String.format("left:%d top:%d right:%d bottom:%d", left, top, right, bottom));
        Intent intent = new Intent("pc.intent.action.FREEFORM_CONTROL");
        intent.putExtra("enter", true);
        intent.putExtra("packageName", packageName);
        intent.putExtra("left", left);
        intent.putExtra("top", top);
        intent.putExtra("right", right);
        intent.putExtra("bottom", bottom);

        getContext().sendBroadcast(intent);
    }


    public void onResume() {
        if (activePackageName != null && !activePackageName.equals("")) {
            appAndEnterFreeForm(activePackageName);
        }
    }
}
