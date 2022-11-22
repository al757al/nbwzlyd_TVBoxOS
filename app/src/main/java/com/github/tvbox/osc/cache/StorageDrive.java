package com.github.tvbox.osc.cache;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "storageDriver")
public class StorageDrive implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "type")
    public int type;
    @ColumnInfo(name = "configJson")
    public String configJson;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
