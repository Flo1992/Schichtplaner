package eu.selfhost.mirc0.schichtplaner.database;

public abstract class OnShiftsChangedListener extends OnShiftChangedListener {
    public abstract void onShiftAdded(Shift shift);
    public abstract void onShiftDeleted(Shift shift);
}
