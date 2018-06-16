package eu.selfhost.mirc0.schichtplaner.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Shift {

    //region Fields

    @PrimaryKey(autoGenerate = true)
    private long Id;
    private int Color;
    private String Name;
    private String ShortName;
    private Time StartTime;
    private Time EndTime;

    @Ignore
    private List<OnShiftChangedListener> onChangeListeners;

    //endregion

    public Shift(){

        onChangeListeners = new ArrayList<>();
        StartTime = new Time(8*60*60000);
        EndTime = new Time(16*60*60000);
    }

    //region getter / setter

    public long getId() {
        return Id;
    }

    public void setId(long id) { Id = id; }

    public int getColor() {
        return Color;
    }

    public void setColor(int color) {

        Color = color;
        onChanged();
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
        onChanged();
    }

    public String getShortName() {
        return ShortName;
    }

    public void setShortName(String shortName) {
        ShortName = shortName;
        onChanged();
    }

    public Time getStartTime() {
        return StartTime;
    }

    public void setStartTime(Time startTime) {
        StartTime = startTime;
        onChanged();
    }

    public Time getEndTime() {
        return EndTime;
    }

    public void setEndTime(Time endTime) {
        EndTime = endTime;
        onChanged();
    }

    private void onChanged() {
        for (OnShiftChangedListener listener :
                onChangeListeners) {
            listener.onShiftChanged(this);
        }
    }

    //endregion

    public void registerOnChangeListener(OnShiftChangedListener listener){
        onChangeListeners.add(listener);
    }

    public void unregisterOnChangeListener(OnShiftChangedListener listener){
        onChangeListeners.remove(listener);
    }
}
