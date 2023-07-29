package com.okcaros.minusscreen;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MinusScreenViewRoot extends ConstraintLayout {
    public static final int TYPE_MAP = 1;
    public static final int TYPE_MUSIC = 2;
    public static final int TYPE_WEATHER = 3;
    float screenRatio;
    int activeAppPosition = 0;
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
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenW = dm.widthPixels;
        int screenH = dm.heightPixels;
        screenRatio = (float) screenW / screenH;

        if (isVerticalScreen()) {
            inflate(getContext(), R.layout.minus_screen_vertical, this);
        } else {
            inflate(getContext(), R.layout.minus_screen, this);
        }

        int dimension8 = (int) getResources().getDimensionPixelSize(R.dimen.dp_8);

        RecyclerView appMenuRcv = findViewById(R.id.app_menu_rcv);
        appMenuRcv.setPadding(dimension8, getStatusBarHeight(), dimension8, dimension8);
        findViewById(R.id.app_content).setPadding(0, getStatusBarHeight(), dimension8, dimension8);

        if (isVerticalScreen()) {
            findViewById(R.id.app_content).setPadding(dimension8, getStatusBarHeight(), dimension8, dimension8);
            appMenuRcv.setPadding(dimension8, 0, dimension8, dimension8);
        }

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

        findViewById(R.id.app_content).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback == null) {
                    return;
                }
                callback.showAppSelect(activeAppPosition);
            }
        });
    }

    public void setCallback(MinusScreenService.MinusScreenAgentCallback callback) {
        this.callback = callback;
    }

    private int getStatusBarHeight() {
        int height = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = (int) (getResources().getDimensionPixelSize(resourceId) + getResources().getDimensionPixelSize(R.dimen.dp_4));
        }
        return height;
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

                if (isVerticalScreen()) {
                    itemView.getLayoutParams().width = (int) ((parent.getMeasuredWidth() - 2 * getResources().getDimensionPixelSize(R.dimen.dp_8)) / 2);
                    return;
                }

                if (isWideScreen()) {
                    itemView.getLayoutParams().height = (int) ((parentHeight - 2 * getResources().getDimensionPixelSize(R.dimen.dp_8) - getStatusBarHeight()) / 2);
                    return;
                }

                // normal screen
                itemView.getLayoutParams().height = (int) ((parentHeight - 3 * getResources().getDimensionPixelSize(R.dimen.dp_8) - getStatusBarHeight()) / 3);
            }
        }

        public class MapViewHolder extends BaseViewHolder {
            ImageView mapHome;
            ImageView mapCompany;

            public MapViewHolder(@NonNull View itemView, @NonNull ViewGroup parent) {
                super(itemView, parent);

                mapHome = itemView.findViewById(R.id.map_home);
                mapCompany = itemView.findViewById(R.id.map_company);

                itemView.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Log.e("oklauncher", "什么情况？？？？？？");
                        return false;
                    }
                });

                mapHome.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Log.e("oklauncher", "dianjidianjidianjidianji");
                        return true;
                    }
                });

                mapCompany.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Log.e("oklauncher", "llllllllllllllllllllll");
                        return true;
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
}
