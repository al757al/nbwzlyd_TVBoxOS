package com.github.tvbox.osc.cache;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * @author pj567
 * @date :2021/1/7
 * @description:
 */
@Dao
public interface StorageDriveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(StorageDrive drive);

    @Query("select * from storageDriver order by id")
    List<StorageDrive> getAll();

    @Query("delete from storageDriver where `id`=:id")
    void delete(int id);
}