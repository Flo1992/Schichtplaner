package eu.selfhost.mirc0.schichtplaner.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(entities = {Shift.class, WorkingShift.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverters.class})
public abstract class ShiftDatabase extends RoomDatabase {
    public abstract DaoAccess daoAccess();
}
