package dev.kyro.despair.controllers;

import com.google.cloud.firestore.annotation.Exclude;
import dev.kyro.despair.Despair;

import java.util.ArrayList;
import java.util.List;

public class Config {
    @Exclude public static Config INSTANCE;
    @Exclude public boolean onSaveCooldown = false;
    @Exclude public boolean saveQueued = false;

    public String PREFIX = ".";
    public long GUILD_ID;
    public long CATEGORY_ID;
    public long DISPLAY_CHANNEL_ID;
    public long NOTIFY_CHANNEL_ID;
    public long MEMBER_ROLE_ID;
    public long ADMIN_ROLE_ID;

    public Config() {
        INSTANCE = this;
    }

    public Config(boolean onSaveCooldown, boolean saveQueued, String PREFIX, long GUILD_ID, long CATEGORY_ID, long DISPLAY_CHANNEL_ID, long NOTIFY_CHANNEL_ID, long MEMBER_ROLE_ID, long ADMIN_ROLE_ID) {
        INSTANCE = this;
        this.onSaveCooldown = onSaveCooldown;
        this.saveQueued = saveQueued;
        this.PREFIX = PREFIX;
        this.GUILD_ID = GUILD_ID;
        this.CATEGORY_ID = CATEGORY_ID;
        this.DISPLAY_CHANNEL_ID = DISPLAY_CHANNEL_ID;
        this.NOTIFY_CHANNEL_ID = NOTIFY_CHANNEL_ID;
        this.MEMBER_ROLE_ID = MEMBER_ROLE_ID;
        this.ADMIN_ROLE_ID = ADMIN_ROLE_ID;
    }

    @Exclude
    public void save() {
        if(onSaveCooldown && !saveQueued) {
            saveQueued = true;
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                saveQueued = false;
                save();
            }).start();
        }
        if(!saveQueued && !onSaveCooldown){
            Despair.FIRESTORE.collection("despair").document("config").set(this);
            onSaveCooldown = true;
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                onSaveCooldown = false;
            }).start();
        }
    }
}
