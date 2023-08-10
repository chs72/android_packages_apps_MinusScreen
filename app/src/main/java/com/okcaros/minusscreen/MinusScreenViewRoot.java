package com.okcaros.minusscreen;

import static android.view.KeyEvent.KEYCODE_MEDIA_NEXT;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.okcaros.tool.AndroidTool;
import com.okcaros.tool.PcConst;
import com.okcaros.tool.ScreenTool;

import java.util.ArrayList;
import java.util.List;

public class MinusScreenViewRoot extends ConstraintLayout {
    private final static String Tag = "MinusScreenViewRoot";
    public static final int TYPE_MAP = 1;
    public static final int TYPE_MUSIC = 2;
    public static final int TYPE_WEATHER = 3;
    // autonavi broadcast intent
    public static final String AUTONAVI_STANDARD_BROADCAST_SEND = "AUTONAVI_STANDARD_BROADCAST_SEND";
    public static final String AUTONAVI_STANDARD_BROADCAST_RECV = "AUTONAVI_STANDARD_BROADCAST_RECV";

    // autonavi package name
    public static final String AUTONAVI_PACKAGE_NAME = "com.autonavi.amapauto";

    private SpecialAddress specialAddress = null;

    private MapListener mapListener;

    // Used to identify whether the widget has been automatically opened When MinusScreen first show.
    boolean initActiveCalled = false;
    private int activeWidgetType = TYPE_MAP;
    private MinusScreenService.MinusScreenAgentCallback callback;
    private final List<MenuAppEntity> dataList = new ArrayList<>();
    private final AppMenuAdapter appMenuAdapter;

    public MinusScreenViewRoot(@NonNull Context context) {
        super(context);
        appMenuAdapter = new AppMenuAdapter();

        ScreenTool.ScreenInfo screenInfo = ScreenTool.getScreenInfo(getContext());

        if (isVerticalScreen()) {
            inflate(getContext(), R.layout.minus_screen_vertical, this);
        } else {
            inflate(getContext(), R.layout.minus_screen, this);
        }

        int dimension8 = getResources().getDimensionPixelSize(R.dimen.dp_8);

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

        dataList.add(new MenuAppEntity(TYPE_MAP));
        dataList.add(new MenuAppEntity(TYPE_MUSIC));

        if (!isVerticalScreen()) {
            dataList.add(new MenuAppEntity(TYPE_WEATHER));
        }

        appMenuRcv.setAdapter(appMenuAdapter);

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
                callback.configApp();
            }
        });
    }

    public MinusScreenViewRoot(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        appMenuAdapter = null;
        Log.e(Tag, "unSupport MinusScreenViewRoot constructor");
    }

    public MinusScreenViewRoot(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        appMenuAdapter = null;
        Log.e(Tag, "unSupport MinusScreenViewRoot constructor");
    }

    public MinusScreenViewRoot(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        appMenuAdapter = null;
        Log.e(Tag, "unSupport MinusScreenViewRoot constructor");
    }

    public void setCallback(MinusScreenService.MinusScreenAgentCallback callback) {
        this.callback = callback;
    }

    private boolean isVerticalScreen() {
        float verticalScreenRatio = 0.5f;
        ScreenTool.ScreenInfo screenInfo = ScreenTool.getScreenInfo(getContext());
        return (float) screenInfo.realWidth / screenInfo.realHeight <= verticalScreenRatio;
    }

    private boolean isWideScreen() {
        float wideScreenRatio = 2.5f;
        ScreenTool.ScreenInfo screenInfo = ScreenTool.getScreenInfo(getContext());
        return (float) screenInfo.realWidth / screenInfo.realHeight >= wideScreenRatio;
    }

    public class AppMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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
                Object data = dataList.get(position).getData();
                if (data instanceof MenuAppEntity.MediaMenuEntityData) {
                    MenuAppEntity.MediaMenuEntityData mediaData = (MenuAppEntity.MediaMenuEntityData) data;
                    musicViewHolder.musicName.setText(mediaData.getTitle());
                    musicViewHolder.musicAuthor.setText(mediaData.getArtist());

                    musicViewHolder.itemView.findViewById(R.id.music_next).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(PcConst.NORMAL_PC_MEDIA_KEY);
                            intent.putExtra("action", KEYCODE_MEDIA_NEXT);
                            v.getContext().sendBroadcast(intent);
                        }
                    });
                    musicViewHolder.itemView.findViewById(R.id.music_previous).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(PcConst.NORMAL_PC_MEDIA_KEY);
                            intent.putExtra("action", KEYCODE_MEDIA_PREVIOUS);
                            v.getContext().sendBroadcast(intent);
                        }
                    });
                    musicViewHolder.itemView.findViewById(R.id.music_play).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(PcConst.NORMAL_PC_MEDIA_KEY);
                            intent.putExtra("action", KEYCODE_MEDIA_PLAY_PAUSE);
                            v.getContext().sendBroadcast(intent);
                        }
                    });
                }
            } else if (holder instanceof WeatherViewHolder) {
                WeatherViewHolder weatherViewHolder = (WeatherViewHolder) holder;

                weatherViewHolder.temperature.setText("30°");
                weatherViewHolder.weatherDetail.setText("多云");
                weatherViewHolder.location.setText("杭州市萧山区");
            }
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
                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openFreeformApp(TYPE_MAP);
                    }
                });

                itemView.findViewById(R.id.map_search).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openFreeformApp(TYPE_MAP);
                        navTo();
                    }
                });

                itemView.findViewById(R.id.map_home).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openFreeformApp(TYPE_MAP);
                        navToSpecialAddress(1);
                    }
                });

                itemView.findViewById(R.id.map_company).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openFreeformApp(TYPE_MAP);
                        navToSpecialAddress(2);
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

                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openFreeformApp(TYPE_MUSIC);
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

                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openFreeformApp(TYPE_WEATHER);
                    }
                });
            }
        }
    }

    public static class MenuAppEntity {
        private int type;
        private Object data;

        public MenuAppEntity(int type) {
            this.type = type;

            switch (type) {
                case TYPE_WEATHER:
                case TYPE_MAP: {
                    break;
                }
                case TYPE_MUSIC: {
                    this.data = new MediaMenuEntityData();
                    break;
                }
            }
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public Object getData() {
            return data;
        }

        public static class MediaMenuEntityData {
            private String title = "";
            private String albumTitle = "";
            private String artist = "";

            private int duration = 0;

            public MediaMenuEntityData() {

            }

            public MediaMenuEntityData(
                    String title,
                    String albumTitle,
                    String artist,
                    int duration
            ) {
                this.artist = artist;
                this.albumTitle = albumTitle;
                this.title = title;
                this.duration = duration;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getAlbumTitle() {
                return albumTitle;
            }

            public void setAlbumTitle(String albumTitle) {
                this.albumTitle = albumTitle;
            }

            public String getArtist() {
                return artist;
            }

            public void setArtist(String artist) {
                this.artist = artist;
            }

            public int getDuration() {
                return duration;
            }

            public void setDuration(int duration) {
                this.duration = duration;
            }
        }
    }

    private void openFreeformApp(int wType) {
        activeWidgetType = wType;

        SharedPreferences sharedConfig = PreferenceManager.getDefaultSharedPreferences(getContext());
        String pgName = "";
        switch (wType) {
            case TYPE_MAP: {
                pgName = sharedConfig.getString(getResources().getString(R.string.preference_key_map), "");
                break;
            }
            case TYPE_MUSIC: {
                pgName = sharedConfig.getString(getResources().getString(R.string.preference_key_music), "");
                break;
            }
            case TYPE_WEATHER: {

            }
        }

        if (!pgName.equals("") && isAppInstalled(pgName)) {
            findViewById(R.id.empty_bg).setVisibility(INVISIBLE);
            appAndEnterFreeForm(pgName);
            return;
        }
        // 隐藏所有FreeForm窗口
        AndroidTool.backHome(getContext());
        findViewById(R.id.empty_bg).setVisibility(VISIBLE);
    }

    private void appAndEnterFreeForm(String packageName) {
        int[] location = new int[2];
        View appContentView = findViewById(R.id.app_content);
        appContentView.getLocationInWindow(location);

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

    private boolean isAppInstalled(String packageName){
        PackageManager pm = getContext().getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e){
            installed = false;
        }
        return installed;
    }

    public void onMediaInfoChange(String title,
                                  String albumTitle,
                                  String artist,
                                  int duration) {

        int musicPosition = -1;
        for (MenuAppEntity entity : dataList) {
            musicPosition++;
            if (entity.getType() != TYPE_MUSIC) {
                continue;
            }
            MenuAppEntity.MediaMenuEntityData data = (MenuAppEntity.MediaMenuEntityData) entity.getData();
            data.setArtist(artist);
            data.setAlbumTitle(albumTitle);
            data.setTitle(title);
            data.setDuration(duration);
            break;
        }
        if (appMenuAdapter != null) {
            appMenuAdapter.notifyItemChanged(musicPosition);
        }
    }

    public class MapListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int KEY_TYPE = intent.getIntExtra("KEY_TYPE", 0);
            switch (KEY_TYPE) {
                case 10046: {
                    double lon = intent.getDoubleExtra("LON", 0);
                    double lat = intent.getDoubleExtra("LAT", 0);
                    if (Math.abs(lon) + Math.abs(lat) > 1) {
                        specialAddress = new SpecialAddress(
                                intent.getStringExtra("POINAME"),
                                lon,
                                lat
                        );
                    }
                    break;
                }
            }
        }
    }

    public static class SpecialAddress {
        public String poiName;
        public double lon;
        public double lat;

        public SpecialAddress(String poiName, double lon, double lat) {
            this.poiName = poiName;
            this.lon = lon;
            this.lat = lat;
        }
    }

    /**
     * @param aim 1: home 2: company only for autonavi map
     */
    private void navToSpecialAddress(int aim) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                registerMapListener();
                Intent getAddressIntent = new Intent(AUTONAVI_STANDARD_BROADCAST_RECV);
                getAddressIntent.putExtra("KEY_TYPE", 10045);
                getAddressIntent.putExtra("EXTRA_TYPE", aim);
                getAddressIntent.setPackage(AUTONAVI_PACKAGE_NAME);
                getContext().sendBroadcast(getAddressIntent);
                specialAddress = null;
                int i = 0;
                while (i <= 5) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {

                    }
                    if (specialAddress != null) {
                        break;
                    }
                    i++;
                }

                if (specialAddress == null) {
                    unRegisterMapListener();
                    return;
                }

                mapNaviTo(specialAddress.poiName, specialAddress.lon, specialAddress.lat);
                unRegisterMapListener();
            }
        }).start();
    }

    public void registerMapListener() {
        unRegisterMapListener();
        mapListener = new MapListener();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AUTONAVI_STANDARD_BROADCAST_SEND);
        getContext().registerReceiver(mapListener, intentFilter);
    }

    public void unRegisterMapListener() {
        try {
            if (mapListener != null) {
                getContext().unregisterReceiver(mapListener);
                mapListener = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navTo() {
        Intent intent = new Intent();
        intent.setAction(AUTONAVI_STANDARD_BROADCAST_RECV);
        intent.putExtra("KEY_TYPE", 10036);
        intent.putExtra("KEYWORDS", "");
        intent.putExtra("SOURCE_APP", "VC_CAR");
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setPackage(AUTONAVI_PACKAGE_NAME);
        getContext().sendBroadcast(intent);
    }

    private void mapNaviTo(String dName, double lon, double lat) {
        Intent intent = new Intent();
        intent.setAction(AUTONAVI_STANDARD_BROADCAST_RECV);
        intent.putExtra("KEY_TYPE", 10007);
        intent.putExtra("EXTRA_DNAM", dName);
        intent.putExtra("EXTRA_DLON", lon);
        intent.putExtra("EXTRA_DLAT", lat);
        intent.putExtra("EXTRA_DEV", 0);
        intent.putExtra("EXTRA_M", -1);
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setPackage(AUTONAVI_PACKAGE_NAME);
        getContext().sendBroadcast(intent);
    }

    public void onStateChange(MinusScreenService.MinusScreenState state) {
        if (state == MinusScreenService.MinusScreenState.SHOW && (callback.needResumeFreeformWindow() || !initActiveCalled)) {
            openFreeformApp(activeWidgetType);
            initActiveCalled = true;
        }
    }
}
