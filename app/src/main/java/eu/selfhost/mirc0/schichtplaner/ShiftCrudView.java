package eu.selfhost.mirc0.schichtplaner;

import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TimePicker;

import java.sql.Time;
import java.util.Locale;

import eu.selfhost.mirc0.schichtplaner.database.Shift;

/**
 * TODO: document your custom view class.
 */
public class ShiftCrudView extends RelativeLayout {

    private Button _startButton;
    private Button _endButton;
    private View _colorBox;
    private int _selectedColor;
    private long _startTime;
    private long _endTime;
    private EditText _nameText;
    private EditText _shortNameText;

    public ShiftCrudView(Context context, Shift shift) {
        super(context);

        initControl(context, shift);
    }

    /**
     * Load component XML layout
     */
    private void initControl(Context context, Shift shift) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (inflater == null) {
            return;
        }
        inflater.inflate(R.layout.sample_shift_crudview, this);

        _nameText = findViewById(R.id.shift_crud_name);
        _nameText.setText(shift.getName());
        _shortNameText = findViewById(R.id.shift_crud_short_name);
        _shortNameText.setText(shift.getShortName());

        // layout is inflated, assign local variables to components
        // ========================================
        // Edit start time
        // ========================================
        _startButton = findViewById(R.id.shift_crud_start_time);
        Time startTime = shift.getStartTime();
        _startTime = startTime.getTime();
        long time = _startTime / 60000;
        int currentStartHour = (int) (time / 60);
        int currentStartMinute = (int) (time - currentStartHour * 60);

        String startTimeString = String.format(Locale.getDefault(),"%d : %02d", currentStartHour, currentStartMinute);
        _startButton.setText(startTimeString);
        _startButton.setOnClickListener(view -> {
            int minutes = (int) (_startTime / 60000);
            int hour = minutes / 60;
            int minute = minutes - hour*60;
            editTime(context, hour, minute, (timePicker, h, m) -> {
                _startTime = (h * 60 + m) * 60000;
                _startButton.setText(String.format(Locale.getDefault(),"%d : %02d", h, m));
            });
        });

        // ========================================
        // Edit end time
        // ========================================
        _endButton = findViewById(R.id.shift_crud_end_time);
        Time endTime = shift.getEndTime();
        _endTime = endTime.getTime();
        time = _endTime / 60000;
        int currentEndHour = (int) (time / 60);
        int currentEndMinute = (int) (time - currentEndHour * 60);

        String endTimeString = String.format(Locale.getDefault(),"%d : %02d", currentEndHour, currentEndMinute);
        _endButton.setText(endTimeString);
        _endButton.setOnClickListener(view -> {
            int minutes = (int) (_endTime / 60000);
            int hour = minutes / 60;
            int minute = minutes - hour*60;
            editTime(context, hour, minute, (timePicker, h, m) -> {
                _endTime = (h * 60 + m) * 60000;
                _endButton.setText(String.format(Locale.getDefault(), "%d : %02d", h, m));
            });
        });

        // ========================================
        // Edit color
        // ========================================

        int[] colors = {0xFF0000FF, 0xFF00FF00, 0xFF00FFFF, 0xFFFF0000, 0xFFFF00FF, 0xFFFFFF00};
////   int[] colors = {Color.BLUE, Color.GREEN, Color.CYAN, Color.RED, Color.MAGENTA, Color.YELLOW};
        ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                return new LinearGradient(0, 0, width, height,
                        colors,
                        null, Shader.TileMode.REPEAT);
            }
        };

        PaintDrawable p=new PaintDrawable();
        p.setShape(new RectShape());
        p.setShaderFactory(sf);
        View view = findViewById(R.id._shift_crud_current_color);
        view.setBackground(p);

        SeekBar colorBar = findViewById(R.id.shift_crud_color_seek_bar);
        int max = 256 * 5 - 1;
        colorBar.setMax(max);
        colorBar.setPadding(0,0,0,0);
        _colorBox = findViewById(R.id.shift_crud_color_box);
        colorBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                _selectedColor = progressToColor(progress);
                Drawable colorDrawable = new ColorDrawable(_selectedColor);
                _colorBox.setBackground(colorDrawable);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        int progress = colorToProgress(shift.getColor());
        colorBar.setProgress(progress);
    }

    private static int progressToColor(int progress) {
        int r = 0;
        int g = 0;
        int b = 0;

        if(progress == 0){
            b = 255;
        } else if(progress < 256) {
            g = progress%256;
            b = 256 - progress%256;
        } else if(progress < 256*2) {
            g = 255;
            b = progress%256;
        } else if(progress < 256*3) {
            r = progress%256;
            g = 256 - progress%256;
            b = 256 - progress%256;
        } else if(progress < 256*4) {
            r = 255;
            g = 0;
            b = progress%256;
        } else if(progress < 256*5) {
            r = 255;
            g = progress%256;
            b = 256 - progress%256;
        }

        return Color.argb(255, r, g, b);
    }

    private static int colorToProgress(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

//                             BLUE ->         BLUE <-     BLUE ->     BLUE <-     BLUE ->    BLUE <-      BLUE ->
//                                             GREEN ->                 GREEN <-              GREEN ->
//                                                                    RED ->
        //int[] colors = {0xFF0000FF, 0xFF00FF00, 0xFF00FFFF, 0xFFFF0000, 0xFFFF00FF, 0xFFFFFF00}
//                             0            256         512          768        1024     1280
        if (r == 0){ // < 512
            if (g == 255) // > 256
                return 256 + b;
            else // < 256
                return 255 - b;
        } else if (r < 255){
            return 512 + r;
        } else{ // > 768
            if (g == 0) // < 1024
                return 768 + b;
            else  // > 1024
                return 1024 + g;
        }
    }

    private void editTime(Context context, int hour, int minute, final TimePickerDialog.OnTimeSetListener onTimeSetListener) {

        final TimePicker timePicker = new TimePicker(context);
        timePicker.setHour(hour);
        timePicker.setMinute(minute);
        timePicker.setIs24HourView(true);
        new AlertDialog.Builder(context).setView(timePicker)
                .setTitle(R.string.create_shift)
                .setPositiveButton(R.string.ok,
                        (dialog, id) -> {
                            int h = timePicker.getHour();
                            int m = timePicker.getMinute();

                            onTimeSetListener.onTimeSet(timePicker, h, m);

                            dialog.cancel();
                        }).show();
    }

    public long getStartTime() {
        return _startTime;
    }

    public long getEndTime() {
        return _endTime;
    }

    public int getSelectedColor() {
        return _selectedColor;
    }

    public String getName() {
        return _nameText.getText().toString();
    }

    public String getShortName() {
        return _shortNameText.getText().toString();
    }
}
