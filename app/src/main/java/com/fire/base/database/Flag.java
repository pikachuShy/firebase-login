package com.fire.base.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Flag {
    @PrimaryKey(autoGenerate = true) public int id;
    @ColumnInfo public String code;
    @ColumnInfo public String unicode;
    @ColumnInfo public String name;
    @ColumnInfo public String title;
    @ColumnInfo public String dialCode;
}
