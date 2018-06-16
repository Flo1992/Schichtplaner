package eu.selfhost.mirc0.schichtplaner.database;

import android.graphics.Color;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShiftRepository {
    private final ShiftDatabase _shiftDatabase;
    private List<OnShiftsChangedListener> _onShiftsChangedListeners;
    private Thread _workingThread;

    private List<Shift> Shifts;

    public ShiftRepository(ShiftDatabase shiftDatabase){
        this._shiftDatabase = shiftDatabase;
        _onShiftsChangedListeners = new ArrayList<>();
        init();
    }

    public void addOnShiftsChangedListener(OnShiftsChangedListener listener){
        _onShiftsChangedListeners.add(listener);
    }

    public void removeOnShiftsChangedListener(OnShiftsChangedListener listener){
        _onShiftsChangedListeners.remove(listener);
    }

    private void init() {
        _workingThread = new Thread(() -> {
            DaoAccess dao = _shiftDatabase.daoAccess();
            Shifts = dao.getAllShifts();
            if (Shifts.size() > 0){
                return;
            }

            new Thread(() -> {
                Shift shift = new Shift();
                shift.setColor(Color.GREEN);
                shift.setName("Schicht 1");
                shift.setShortName("S1");
                shift.setStartTime(new Time((8*60*60000)));
                shift.setEndTime(new Time((16*60*60000)));
                createNew(shift);

                shift = new Shift();
                shift.setColor(Color.RED);
                shift.setName("Schicht 2");
                shift.setShortName("S2");
                shift.setStartTime(new Time((18*60*60000)));
                shift.setEndTime(new Time((22*60*60000)));
                createNew(shift);

                shift = new Shift();
                shift.setColor(Color.BLUE);
                shift.setName("Schicht 3");
                shift.setShortName("S3");
                shift.setStartTime(new Time((4*60*60000)));
                shift.setEndTime(new Time((10*60*60000)));
                createNew(shift);
            }).start();
        });
        _workingThread.start();
    }

    public void update(final Shift shift){
        final Thread prevThread = _workingThread;

        _workingThread = new Thread(() -> {
            try {
                prevThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            DaoAccess dao = _shiftDatabase.daoAccess();
            dao.update(shift);
            onShiftChanged(shift);
        });
        _workingThread.start();
    }

    public List<Shift> getShifts() {
        return Shifts;
    }

    public void createNew(final Shift shift) {
        final Thread prevThread = _workingThread;
        Shifts.add(shift);

        _workingThread = new Thread(() -> {
            try {
                prevThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            DaoAccess dao = _shiftDatabase.daoAccess();
            long id = dao.insert(shift);
            shift.setId(id);
            onShiftAdded(shift);
        });
        _workingThread.start();
    }

    public WorkingShift addWorkingShift(final Shift shift, final Date date) {
        final Thread prevThread = _workingThread;

        long time = date.getTime();
        Timestamp timestamp = new java.sql.Timestamp(time);

        final WorkingShift workingShift = new WorkingShift();
        workingShift.setShiftId(shift.getId());
        workingShift.setShift(timestamp);

        _workingThread = new Thread(() -> {
            try {
                prevThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            DaoAccess dao = _shiftDatabase.daoAccess();
            long id = dao.insert(workingShift);
            workingShift.setId(id);
        });
        _workingThread.start();

        return workingShift;
    }

    public void delete(WorkingShift workingShift){
        final Thread prevThread = _workingThread;

        _workingThread = new Thread(() -> {
            try {
                prevThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            DaoAccess dao = _shiftDatabase.daoAccess();
            dao.delete(workingShift);
        });
        _workingThread.start();
    }

    public void delete(Shift shift){
        final Thread prevThread = _workingThread;

        _workingThread = new Thread(() -> {
            try {
                prevThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            DaoAccess dao = _shiftDatabase.daoAccess();
            dao.delete(shift);
            onShiftDeleted(shift);
        });
        _workingThread.start();
    }

    private void onShiftAdded(Shift shift) {
        for (OnShiftsChangedListener listener :
                _onShiftsChangedListeners) {
            listener.onShiftAdded(shift);
        }
    }

    private void onShiftChanged(Shift shift) {
        for (OnShiftsChangedListener listener :
                _onShiftsChangedListeners) {
            listener.onShiftChanged(shift);
        }
    }

    private void onShiftDeleted(Shift shift) {
        for (OnShiftsChangedListener listener :
                _onShiftsChangedListeners) {
            listener.onShiftDeleted(shift);
        }
    }

    public List<WorkingShift> getMonthShifts(long start, long end) {
        return _shiftDatabase.daoAccess().getMonthShifts(start, end);
    }
}
