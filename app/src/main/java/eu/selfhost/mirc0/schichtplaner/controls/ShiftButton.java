package eu.selfhost.mirc0.schichtplaner.controls;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import eu.selfhost.mirc0.schichtplaner.database.OnShiftChangedListener;
import eu.selfhost.mirc0.schichtplaner.database.Shift;

public class ShiftButton extends ToggleButton {

    private Shift _shift;

    public ShiftButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ShiftButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShiftButton(Context context, Shift shift) {

        super(context);
        Handler mainHandler = new Handler(Looper.getMainLooper());
        setTextSize(20);
        _shift = shift;
        setButtonContent();
        shift.registerOnChangeListener(new OnShiftChangedListener() {
            @Override
            public void onShiftChanged(Shift shift) {

                mainHandler.post(() -> setButtonContent());
            }
        });
    }

    private void setButtonContent() {

        String shortName = _shift.getShortName();
        if (shortName == null)
            shortName = "-";

        setText(shortName);
        setTextOn(shortName);
        setTextOff(shortName);

        StateListDrawable res = new StateListDrawable();

        GradientDrawable shapeChecked = new GradientDrawable();
        shapeChecked.setShape(GradientDrawable.RECTANGLE);
        shapeChecked.setColor(_shift.getColor());
        shapeChecked.setStroke(20, Color.BLACK);

        GradientDrawable shapeUnchecked = new GradientDrawable();
        shapeUnchecked.setShape(GradientDrawable.RECTANGLE);
        shapeUnchecked.setColor(_shift.getColor());
        shapeUnchecked.setStroke(10, Color.BLACK);

        res.addState(new int[]{ android.R.attr.state_checked}, shapeChecked);
        res.addState(new int[]{-android.R.attr.state_checked}, shapeUnchecked);

        setBackgroundDrawable(res);

        setGravity(25);
        invalidate();
    }

    public Shift getShift() {
        return _shift;
    }
}
