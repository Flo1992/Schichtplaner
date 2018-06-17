package eu.selfhost.mirc0.schichtplaner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import eu.selfhost.mirc0.schichtplaner.controls.DateBackground;
import eu.selfhost.mirc0.schichtplaner.controls.ShiftButton;
import eu.selfhost.mirc0.schichtplaner.database.OnShiftsChangedListener;
import eu.selfhost.mirc0.schichtplaner.database.Shift;
import eu.selfhost.mirc0.schichtplaner.database.ShiftRepository;
import eu.selfhost.mirc0.schichtplaner.database.WorkingShift;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CalendarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CalendarFragment extends Fragment {

    private CaldroidFragment caldroidFragment;

    private final HashMap<ToggleButton, Shift> _shifts;
    private final HashMap<Long, DateBackground> _dateDrawables;
    private final List<WorkingShift> _workingShifts;
    private Shift _currentShift;

    private LinearLayout _buttonBar;
    private CompoundButton.OnCheckedChangeListener _onCheckedChangeListener;
    private ShiftRepository _repository;
    private ShiftEditController _editShiftListener;
    private Shift _menuDataContext;

    @SuppressLint("UseSparseArrays")
    public CalendarFragment() {
        // Required empty public constructor

        _shifts = new HashMap<>();
        _dateDrawables = new HashMap<>();
        _workingShifts = new ArrayList<>();
        _onCheckedChangeListener = (compoundButton, isChecked) -> {
            if (!isChecked) {
                _currentShift = null;
                return;
            }

            @SuppressWarnings("SuspiciousMethodCalls")
            Shift currentShift = _shifts.get(compoundButton);

            for (ToggleButton button : _shifts.keySet()) {
                if (button == compoundButton)
                    continue;

                button.setChecked(false);
            }
            _currentShift = currentShift;
        };
    }

    public void setRepository(ShiftRepository repository){
        _repository = repository;
        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());

        _repository.addOnShiftsChangedListener(new OnShiftsChangedListener() {
            @Override
            public void onShiftChanged(Shift shift) {

            }

            @Override
            public void onShiftAdded(Shift shift) {

                mainHandler.post(() -> addShiftButton(shift));
            }

            @Override
            public void onShiftDeleted(Shift shift) {

                mainHandler.post(() -> {
                    removeShiftButton(shift);
                    removeWorkingShifts(shift);
                });
            }
        });
    }

    private void removeShiftButton(Shift shift) {
        Map.Entry<ToggleButton, Shift> entry = findEntry(shift);
        if (entry == null)
            return;

        _buttonBar.removeView(entry.getKey());
        _shifts.remove(entry.getKey());
    }

    private Map.Entry<ToggleButton, Shift> findEntry(Shift shift){
        for (Map.Entry<ToggleButton, Shift> entry :
                _shifts.entrySet()) {
            if (entry.getValue().getId() == shift.getId())
                return entry;
        }

        return null;
    }

    private void setShiftButtons(List<Shift> shifts) {
        _shifts.clear();
        _buttonBar.removeAllViews();
        if (shifts == null)
            return;

        for (Shift shift : shifts) {
            addShiftButton(shift);
        }

        _buttonBar.invalidate();
    }

    private void addShiftButton(Shift shift) {
        Context context = getContext();
        if (context == null)
            return;

        ShiftButton button = new ShiftButton(context, shift);
        button.setOnCheckedChangeListener(_onCheckedChangeListener);
        registerForContextMenu(button);

        _shifts.put(button, shift);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(250, 200);
        params.setMargins(25, 25, 25, 25);

        _buttonBar.addView(button, params);
        _buttonBar.invalidate();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getClass() != ShiftButton.class)
            return;

        _menuDataContext = ((ShiftButton)v).getShift();
        menu.setHeaderTitle(_menuDataContext.getName());
        getActivity().getMenuInflater().inflate(R.menu.shift_button_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        if (_menuDataContext == null)
            return false;

        View view = getView();
        if (view == null) {
            return false;
        }
        View rootView = view.getRootView();
        Context context = rootView.getContext();

        switch (item.getItemId()) {
            case R.id.edit_shift_menu_item:
                _editShiftListener.editShift(context, _menuDataContext);
                break;
            case R.id.delete_shift_menu_item:
                new AlertDialog.Builder(context)
                        .setTitle(String.format(getString(R.string.delete_shift_question_title), _menuDataContext.getName()))
                        .setMessage(String.format(getString(R.string.delete_shift_question), _menuDataContext.getName()))
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> _repository.delete(_menuDataContext))
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {})
                        .create().show();

                break;
            default:
                return false;
        }
        return true;
    }

    private void setCustomResourceForDates() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup caldroid fragment
        caldroidFragment = new CaldroidFragment();

        // Setup arguments
        // If Activity is created after rotation
        if (savedInstanceState != null) {
            caldroidFragment.restoreStatesFromKey(savedInstanceState,
                    "CALDROID_SAVED_STATE");
        }
        // If activity is created from fresh
        else {
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
            args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);

            // Uncomment this to customize startDayOfWeek
            // args.putInt(CaldroidFragment.START_DAY_OF_WEEK,
            // CaldroidFragment.TUESDAY); // Tuesday

            // Uncomment this line to use Caldroid in compact mode
            // args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, false);

            // Uncomment this line to use dark theme
//            args.putInt(CaldroidFragment.THEME_RESOURCE, com.caldroid.R.style.CaldroidDefaultDark);

            caldroidFragment.setArguments(args);
        }

        setCustomResourceForDates();

        // Attach to the activity
        FragmentTransaction t = getChildFragmentManager().beginTransaction();
        t.replace(R.id.calendar, caldroidFragment);
        t.commit();

        setCalendarListeners();
    }

    private void setCalendarListeners() {
        caldroidFragment.setCaldroidListener(new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                if (_currentShift == null)
                    return;

                Optional<WorkingShift> selectedShift = _workingShifts.stream().filter(s -> isDateMatching(date, s)).findFirst();
                if (selectedShift.isPresent()){
                    removeWorkingShift(date, selectedShift.get());
                }
                else {
                    addWorkingNewShift(date);
                }
            }

            @Override
            public void onLongClickDate(Date date, View view) {
                super.onLongClickDate(date, view);
            }

            @Override
            public void onChangeMonth(int month, int year) {
                LoadShiftsForMonth(month, year);
            }
        });
    }

    private void LoadShiftsForMonth(int month, int year) {
        Calendar cal = Calendar.getInstance();

        cal.set(year, month-2, 1, 0, 0, 0);
        long start = cal.getTimeInMillis();

        cal.set(year, month+1, 1,0,0,0);
        long end = cal.getTimeInMillis();

        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            List<WorkingShift> workingShifts = _repository.getMonthShifts(start, end);
            for (Object shiftObject :
                    workingShifts.stream().filter(s1 -> _workingShifts.stream().allMatch(s2 -> s1.getId() != s2.getId())).toArray()) {

                WorkingShift workingShift = (WorkingShift) shiftObject;
                cal.setTimeInMillis(workingShift.getShift().getTime());

                Optional<Shift> shift = _shifts.values().stream().filter(s -> s.getId() == workingShift.getShiftId()).findAny();
                if (!shift.isPresent())
                    continue;

                Date date = cal.getTime();
                mainHandler.post(() -> addWorkingShift(date, workingShift, shift.get()));
            }

            mainHandler.post(() -> caldroidFragment.refreshView());
        }).start();
    }

    private void addWorkingNewShift(Date date) {
        WorkingShift workingShift = _repository.addWorkingShift(_currentShift, date);
        addWorkingShift(date, workingShift, _currentShift);
        caldroidFragment.refreshView();
    }

    private void addWorkingShift(Date date, WorkingShift workingShift, Shift shift) {
        _workingShifts.add(workingShift);

        long time = date.getTime();
        if (_dateDrawables.containsKey(time)){
            DateBackground dateDrawable = _dateDrawables.get(time);
            dateDrawable.addShift(shift);

            return;
        }

        DateBackground gradientDrawable = new DateBackground();
        gradientDrawable.addShift(shift);

        _dateDrawables.put(time, gradientDrawable);
        caldroidFragment.setBackgroundDrawableForDate(gradientDrawable, date);
        gradientDrawable.setOnChangedListener(new OnModifiedListener() {
            @Override
            public void onModified() {
                caldroidFragment.refreshView();
            }
        });
    }

    private void removeWorkingShift(Date date, WorkingShift selectedShift) {
        _workingShifts.remove(selectedShift);
        _repository.delete(selectedShift);

        long time = date.getTime();
        if (_dateDrawables.containsKey(time)){
            DateBackground dateDrawable = _dateDrawables.get(time);
            dateDrawable.removeShift(_currentShift);
        }
    }

    private void removeWorkingShifts(Shift shift) {
        for (WorkingShift workingShift :
                _workingShifts) {

            if (workingShift.getShiftId() != shift.getId())
                continue;

            for (DateBackground background :
                    _dateDrawables.values()) {
                background.removeShift(shift);
            }
        }
    }

    private boolean isDateMatching(Date date, WorkingShift s) {
        long time = date.getTime();
        long currentShift = s.getShift().getTime();
        return currentShift == time && s.getShiftId() == _currentShift.getId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        _buttonBar = view.findViewById(R.id.button_bar);
        setShiftButtons(_repository.getShifts());

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof OnFragmentInteractionListener)) {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setEditShiftListener(ShiftEditController editShiftListener) {
        _editShiftListener = editShiftListener;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
