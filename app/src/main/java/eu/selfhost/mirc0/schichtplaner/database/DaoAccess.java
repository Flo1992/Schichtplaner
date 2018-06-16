package eu.selfhost.mirc0.schichtplaner.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface DaoAccess {

    @Insert
    long insert(Shift Shift);

    @Query("SELECT * FROM Shift WHERE Id = :id")
    Shift getShift (int id);

    @Query("SELECT * FROM Shift")
    List<Shift> getAllShifts();

    @Query("DELETE FROM Shift")
    void clearShifts();

    @Update
    void update (Shift shift);

    @Delete
    void delete (Shift shifts);

    @Insert
    long insert(WorkingShift Shift);

    @Query("SELECT * FROM WorkingShift WHERE Id = :id")
    WorkingShift getWorkingShift (int id);

    @Query("SELECT * FROM WorkingShift WHERE Id = :shift")
    List<WorkingShift> getAllWorkingShifts(int shift);

    @Update
    void update (WorkingShift shift);

    @Update
    void update (List<WorkingShift> shift);

    @Delete
    void delete (WorkingShift shifts);

    @Query("DELETE FROM WorkingShift")
    void clearWorkingShifts();

    @Query("SELECT * FROM WorkingShift WHERE Shift > :start AND Shift < :end")
    List<WorkingShift> getMonthShifts(long start, long end);
}
