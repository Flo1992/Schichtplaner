package eu.selfhost.mirc0.schichtplaner.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.sql.Timestamp;

@Entity
public class WorkingShift {

    @PrimaryKey(autoGenerate = true)
    private long Id;

    public WorkingShift() {
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    private long ShiftId;

    public long getShiftId() {
        return ShiftId;
    }

    public void setShiftId(long shiftId) {
        ShiftId = shiftId;
    }

    private Timestamp Shift;

    public Timestamp getShift() {
        return Shift;
    }

    public void setShift(Timestamp shift) {
        Shift = shift;
    }
}
