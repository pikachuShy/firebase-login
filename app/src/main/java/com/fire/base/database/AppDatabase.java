package com.fire.base.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Flag.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FlagDao flagDao();
}
