package eu.selfhost.mirc0.schichtplaner.database;

import android.arch.persistence.room.TypeConverter;

import java.sql.Time;
import java.sql.Timestamp;

public class DateConverters {
    @TypeConverter
    public static Timestamp fromTimestamp(Long value) {
        return value == null ? null : new Timestamp(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Timestamp time) {
        return time == null ? null : time.getTime();
    }

    @TypeConverter
    public static Time fromTime(Long value) {
        return value == null ? null : new Time(value);
    }

    @TypeConverter
    public static Long dateToTime(Time time) {
        return time == null ? null : time.getTime();
    }
}