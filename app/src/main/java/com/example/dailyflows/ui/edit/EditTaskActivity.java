package com.example.dailyflows.ui.edit;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Vibrator;
import android.content.Context;

import androidx.annotation.Nullable;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.Calendar;
import java.util.UUID;

import com.example.dailyflows.R;
import com.example.dailyflows.data.local.AppDatabase;
import com.example.dailyflows.data.local.entities.TaskEntity;
import com.example.dailyflows.data.repo.TaskRepository;
import com.example.dailyflows.util.DateTimeUtil;
import com.example.dailyflows.ui.BaseActivity;

public class EditTaskActivity extends BaseActivity {

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_PREFILL_DAY = "prefill_day";

    private TaskRepository repo;
    private TaskEntity task;
    private Vibrator vibrator;

    private TextInputEditText etTitle;
    private TextInputEditText etNote;
    private MaterialTextView tvWhen;
    private MaterialTextView tvPriority;
    private Slider sliderPriority;

    private long pickedDayMillis = 0;
    private int pickedHour = 9;
    private int pickedMinute = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        repo = new TaskRepository(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        etTitle = findViewById(R.id.etTitle);
        etNote = findViewById(R.id.etNote);
        tvWhen = findViewById(R.id.tvWhen);
        tvPriority = findViewById(R.id.tvPriority);
        sliderPriority = findViewById(R.id.sliderPriority);

        sliderPriority.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && vibrator != null) vibrator.vibrate(10);
            tvPriority.setText(getPriorityEmoji((int) value) + " Приоритет: " + (int) value);
        });

        findViewById(R.id.btnDateTime).setOnClickListener(v -> {
            haptic();
            pickDateTime();
        });
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            haptic();
            save();
        });

        String id = getIntent().getStringExtra(EXTRA_TASK_ID);
        long prefillDay = getIntent().getLongExtra(EXTRA_PREFILL_DAY, 0);

        if (id != null) {
            new Thread(() -> {
                TaskEntity t = AppDatabase.get(this).taskDao().getById(id);
                runOnUiThread(() -> bind(t));
            }).start();
        } else {
            TaskEntity t = new TaskEntity();
            t.id = UUID.randomUUID().toString();
            t.title = "";
            t.note = "";
            t.priority = 0;
            t.done = false;
            t.dueAtMillis = prefillDay > 0 ? (prefillDay + 9 * 60 * 60 * 1000L) : 0;
            bind(t);
        }
    }

    private void bind(TaskEntity t) {
        if (t == null) {
            finish();
            return;
        }

        task = t;

        etTitle.setText(t.title);
        etNote.setText(t.note != null ? t.note : "");
        sliderPriority.setValue(t.priority);
        tvPriority.setText(getPriorityEmoji(t.priority) + " Приоритет: " + t.priority);

        if (t.dueAtMillis > 0) {
            tvWhen.setText(DateTimeUtil.formatDate(t.dueAtMillis) + " " + DateTimeUtil.formatTime(t.dueAtMillis));
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(t.dueAtMillis);
            pickedDayMillis = DateTimeUtil.atStartOfDay(t.dueAtMillis);
            pickedHour = c.get(Calendar.HOUR_OF_DAY);
            pickedMinute = c.get(Calendar.MINUTE);
        } else {
            tvWhen.setText("Не задано");
        }
    }

    private void pickDateTime() {
        long initial = pickedDayMillis > 0 ? pickedDayMillis : System.currentTimeMillis();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Дата")
                .setSelection(initial)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) return;
            pickedDayMillis = DateTimeUtil.atStartOfDay(selection);

            TimePickerDialog dlg = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        pickedHour = hourOfDay;
                        pickedMinute = minute;

                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(pickedDayMillis);
                        c.set(Calendar.HOUR_OF_DAY, pickedHour);
                        c.set(Calendar.MINUTE, pickedMinute);
                        c.set(Calendar.SECOND, 0);
                        c.set(Calendar.MILLISECOND, 0);

                        task.dueAtMillis = c.getTimeInMillis();
                        tvWhen.setText(DateTimeUtil.formatDate(task.dueAtMillis) + " " + DateTimeUtil.formatTime(task.dueAtMillis));
                        haptic();
                    },
                    pickedHour,
                    pickedMinute,
                    true
            );
            dlg.show();
        });

        picker.show(getSupportFragmentManager(), "date_picker_edit");
    }

    private void save() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            etTitle.setError("Введите название");
            return;
        }

        task.title = title;
        task.note = etNote.getText() != null ? etNote.getText().toString() : "";
        task.priority = (int) sliderPriority.getValue();

        repo.upsert(task, this, () -> runOnUiThread(this::finish));
    }

    private void haptic() {
        if (vibrator != null) vibrator.vibrate(20);
    }

    private String getPriorityEmoji(int priority) {
        if (priority == 0) return "⚪";
        if (priority <= 2) return "🟢";
        if (priority <= 5) return "🟡";
        if (priority <= 8) return "🟠";
        return "🔴";
    }
}
