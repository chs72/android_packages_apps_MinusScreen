package com.okcaros.minusscreen;

public class EventBusEvent {
    public static class MediaInfo {
        public MinusScreenViewRoot.MenuAppEntity.MediaMenuEntityData data;

        public MediaInfo(MinusScreenViewRoot.MenuAppEntity.MediaMenuEntityData data) {
            this.data = data;
        }
    }
}
