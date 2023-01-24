package dev.kyro.despair.controllers;

import com.google.cloud.firestore.annotation.Exclude;
import dev.kyro.despair.Despair;
import dev.kyro.despair.misc.Variables;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KOS {
    @Exclude
    public static KOS INSTANCE;
    public List<KOSPlayer> kosList = new ArrayList<>();
    @Exclude
    public boolean onSaveCooldown = false;
    @Exclude
    public boolean saveQueued = false;

    public KOS() {
        INSTANCE = this;
    }

    public KOS(List<KOSPlayer> kos) {
        INSTANCE = this;
        setKosList(kos);
    }

    public void setKosList(List<KOSPlayer> kosList) {
        for(KOSPlayer player : kosList) {
            addPlayer(player, false);
        }
    }

    public void addPlayer(KOSPlayer kosPlayer, boolean save) {
        kosList.add(kosPlayer);
        if(save) save();
    }

    public void removePlayer(KOSPlayer kosPlayer, boolean save) {
        kosList.remove(kosPlayer);
        if(save) save();
    }

    public boolean containsPlayer(UUID uuid) {
        for(KOSPlayer kosPlayer : kosList) if(kosPlayer.uuid.equals(uuid.toString())) return true;
        return false;
    }

    public boolean containsPlayer(String name) {
        for(KOSPlayer kosPlayer : kosList) if(kosPlayer.name.equals(name.toLowerCase())) return true;
        return false;
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
        if(!saveQueued && !onSaveCooldown) {
            Despair.FIRESTORE.collection(Variables.COLLECTION).document("kos").set(this);
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

    public static class KOSPlayer {

        public String name;
        public String uuid;
        public List<String> tags = new ArrayList<>();

        @Exclude
        public HypixelPlayer hypixelPlayer;

        public KOSPlayer() { }

        public void setUuid(String uuid) {
            this.uuid = uuid;
            hypixelPlayer = new HypixelPlayer(UUID.fromString(uuid));
        }

        public KOSPlayer(String name, String uuid, List<String> tags) {
            this.name = name;
            setUuid(uuid);
            this.tags = tags;
        }

        @Exclude
        public String getTagsAsString() {
            if(tags.isEmpty()) return "";
            String tagString = " [";
            for(int i = 0; i < tags.size(); i++) {
                if(i != 0) tagString += ", ";
                tagString += "`" + tags.get(i) + "`";
            }
            return tagString + "]";
        }
    }
}
