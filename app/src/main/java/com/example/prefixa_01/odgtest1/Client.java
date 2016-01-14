package com.example.prefixa_01.odgtest1;

import java.util.UUID;

/**
 * Created by Prefixa_01 on 14/01/2016.
 */
public class Client {
    private String mName;
    private UUID mID;
    private String mClientID;

    public Client(){
        mID = UUID.randomUUID();
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmClientID() {
        return mClientID;
    }

    public void setmClientID(String mClientID) {
        this.mClientID = mClientID;
    }

    public UUID getmID() {
        return mID;
    }
}
