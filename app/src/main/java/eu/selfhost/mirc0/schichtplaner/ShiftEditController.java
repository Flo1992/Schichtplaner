package eu.selfhost.mirc0.schichtplaner;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import eu.selfhost.mirc0.schichtplaner.database.Shift;
import eu.selfhost.mirc0.schichtplaner.database.ShiftRepository;

public class ShiftEditController {

    private ShiftRepository repository;

    ShiftEditController(ShiftRepository repository){

        this.repository = repository;
    }

    public void createNewShift(final Context context) {

        final Shift shift = new Shift();
        editShift(context, shift, R.string.create_shift, () -> {

            Toast.makeText(context, R.string.shift_created_toast, Toast.LENGTH_LONG).show();
            repository.createNew(shift);
        });
    }

    public void editShift(Context context, Shift shift) {

        editShift(context, shift, R.string.edit_shift, () -> {

            Toast.makeText(context, R.string.shift_updated_toast, Toast.LENGTH_LONG).show();
            repository.update(shift);
        });
    }

    private void editShift(Context context, Shift shift, int title, Runnable confirmAction) {

        ShiftCrudView crudView = new ShiftCrudView(context, shift);

        new AlertDialog.Builder(context)
                .setView(crudView)
                .setTitle(title)
                .setPositiveButton(R.string.ok,
                        (dialog, id) -> {
                            applyShiftProperties(crudView, shift);
                            confirmAction.run();
                            dialog.cancel();
                        })
                .setNegativeButton(R.string.cancel,
                        (dialog, id) -> {
                            Toast.makeText(context, R.string.shift_dismissed_toast, Toast.LENGTH_LONG).show();
                            dialog.cancel();
                        }).show();
    }

    private void applyShiftProperties(ShiftCrudView view, Shift shift) {

        shift.setName(view.getName());
        shift.setShortName(view.getShortName());

        int color = view.getSelectedColor();
        shift.setColor(color);

        long startTime = view.getStartTime();
        shift.getStartTime().setTime(startTime);

        long endTime = view.getEndTime();
        shift.getEndTime().setTime(endTime);
    }
}
