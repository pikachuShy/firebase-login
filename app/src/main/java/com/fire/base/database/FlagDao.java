package com.fire.base.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

@Dao
public interface FlagDao {
    @Query("SELECT * FROM flag")
    List<Flag> getAll();

    @Query("SELECT * FROM flag WHERE code LIKE :code LIMIT 1")
    Flag findByCode(String code);

    @Query("SELECT * FROM flag WHERE dialCode LIKE :dialCode LIMIT 1")
    Flag findByDialCode(String dialCode);

    @Insert
    void insertAll(Flag... flags);
}


