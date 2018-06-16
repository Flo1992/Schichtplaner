package eu.selfhost.mirc0.schichtplaner.controls;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Stream;

import eu.selfhost.mirc0.schichtplaner.OnModifiedListener;
import eu.selfhost.mirc0.schichtplaner.database.OnShiftChangedListener;
import eu.selfhost.mirc0.schichtplaner.database.Shift;

public class DateBackground extends GradientDrawable {
    private HashSet<Shift> _shifts;
    private OnModifiedListener _listener;

    public DateBackground() {
        super(GradientDrawable.Orientation.TOP_BOTTOM, new int[0]);

        _shifts = new HashSet<>();
    }

    public void addShift(Shift shift){

        _shifts.add(shift);
        shift.registerOnChangeListener(new OnShiftChangedListener() {
            @Override
            public void onShiftChanged(Shift shift) {
                updateColors();
            }
        });

        updateColors();
    }

    public void removeShift(Shift shift){

        if(!_shifts.contains(shift))
            return;

        _shifts.remove(shift);
        updateColors();
    }

    private void updateColors() {

        Stream<Integer> colors = _shifts.stream().sorted(Comparator.comparing(Shift::getStartTime)).map(Shift::getColor);

        int count = _shifts.size();
        int[] gradientColors;

        if (count == 0){
            gradientColors = new int[]{Color.TRANSPARENT, Color.TRANSPARENT};
        }
        else {
            gradientColors = new int[count * 2];
            final int[] i = {0};
            colors.forEach(c -> {
                gradientColors[i[0]++] = c;
                gradientColors[i[0]++] = c;
            });
        }

        setColors(gradientColors);
        invalidateSelf();

        onModified();
    }

    private void onModified() {
        if (_listener != null)
            _listener.onModified();
    }

    public void setOnChangedListener(OnModifiedListener listener){
        _listener = listener;
    }
}
