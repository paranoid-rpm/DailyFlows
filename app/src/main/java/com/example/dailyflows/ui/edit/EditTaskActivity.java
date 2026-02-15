package com.example.dailyflows.ui.edit;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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

    private static final String TAG = "EditTaskActivity";

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_PREFILL_DAY = "prefill_day";

    private TaskRepository repo;
    private TaskEntity task;
    private Vibrator vibrator;

    private Toolbar toolbar;
    private TextInputEditText etTitle;
    private TextInputEditText etNote;
    private ExtendedFloatingActionButton fabSave;

    private long pickedDayMillis = 0;
    private int pickedHour = 9;
    private int pickedMinute = 0;
    private int currentPriority = 0;
    private String selectedTag = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        repo = new TaskRepository(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etTitle = findViewById(R.id.etTitle);
        etNote = findViewById(R.id.etNote);
        fabSave = findViewById(R.id.fabSave);

        findViewById(R.id.btnBold).setOnClickListener(v -> applyStyle(Typeface.BOLD));
        findViewById(R.id.btnItalic).setOnClickListener(v -> applyStyle(Typeface.ITALIC));
        findViewById(R.id.btnUnderline).setOnClickListener(v -> applyUnderline());
        findViewById(R.id.btnBullet).setOnClickListener(v -> insertBullet());
        findViewById(R.id.btnHeading).setOnClickListener(v -> applyHeading());
        findViewById(R.id.btnColor).setOnClickListener(v -> showColorPicker());

        fabSave.setOnClickListener(v -> {
            haptic();
            fabSave.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                fabSave.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            }).start();
            showSaveDialog();
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
        currentPriority = t.priority;

        if (t.dueAtMillis > 0) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(t.dueAtMillis);
            pickedDayMillis = DateTimeUtil.atStartOfDay(t.dueAtMillis);
            pickedHour = c.get(Calendar.HOUR_OF_DAY);
            pickedMinute = c.get(Calendar.MINUTE);
        }
    }

    private void showSaveDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_note, null);

        MaterialTextView tvDateTime = dialogView.findViewById(R.id.tvSelectedDateTime);
        MaterialTextView tvPriority = dialogView.findViewById(R.id.tvPriorityLabel);
        Slider slider = dialogView.findViewById(R.id.sliderPriority);
        Chip chipWork = dialogView.findViewById(R.id.chipWork);
        Chip chipPersonal = dialogView.findViewById(R.id.chipPersonal);
        Chip chipIdeas = dialogView.findViewById(R.id.chipIdeas);

        slider.setValue(currentPriority);
        tvPriority.setText(getPriorityEmoji(currentPriority) + " Приоритет: " + currentPriority);

        if (pickedDayMillis > 0) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(pickedDayMillis);
            c.set(Calendar.HOUR_OF_DAY, pickedHour);
            c.set(Calendar.MINUTE, pickedMinute);
            tvDateTime.setText(DateTimeUtil.formatDate(c.getTimeInMillis()) + " " + DateTimeUtil.formatTime(c.getTimeInMillis()));
        }

        slider.addOnChangeListener((sl, value, fromUser) -> {
            if (fromUser) haptic();
            tvPriority.setText(getPriorityEmoji((int) value) + " Приоритет: " + (int) value);
        });

        dialogView.findViewById(R.id.btnPickDateTime).setOnClickListener(v -> pickDateTime(tvDateTime));

        chipWork.setOnCheckedChangeListener((button, checked) -> { if (checked) selectedTag = "Работа"; });
        chipPersonal.setOnCheckedChangeListener((button, checked) -> { if (checked) selectedTag = "Личное"; });
        chipIdeas.setOnCheckedChangeListener((button, checked) -> { if (checked) selectedTag = "Идеи"; });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSaveDialog).setOnClickListener(v -> {
            currentPriority = (int) slider.getValue();
            save();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void pickDateTime(MaterialTextView tvDateTime) {
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
                        tvDateTime.setText(DateTimeUtil.formatDate(task.dueAtMillis) + " " + DateTimeUtil.formatTime(task.dueAtMillis));
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
            etTitle.setError("Введите заголовок");
            return;
        }

        task.title = title;
        task.note = etNote.getText() != null ? etNote.getText().toString() : "";
        task.priority = currentPriority;

        if (selectedTag != null && !task.note.contains("#")) {
            task.note = "#" + selectedTag + "\n" + task.note;
        }

        Log.d(TAG, "Saving task: " + task.title + ", id: " + task.id);

        repo.upsert(task, this, () -> {
            Log.d(TAG, "Task saved, finishing activity");
            finish();
        });
    }

    private void applyStyle(int style) {
        int start = etNote.getSelectionStart();
        int end = etNote.getSelectionEnd();
        if (start >= end) return;

        SpannableString spannable = new SpannableString(etNote.getText());
        spannable.setSpan(new StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        etNote.setText(spannable);
        etNote.setSelection(end);
        haptic();
    }

    private void applyUnderline() {
        int start = etNote.getSelectionStart();
        int end = etNote.getSelectionEnd();
        if (start >= end) return;

        SpannableString spannable = new SpannableString(etNote.getText());
        spannable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        etNote.setText(spannable);
        etNote.setSelection(end);
        haptic();
    }

    private void insertBullet() {
        int cursor = etNote.getSelectionStart();
        etNote.getText().insert(cursor, "• ");
        haptic();
    }

    private void applyHeading() {
        int start = etNote.getSelectionStart();
        int end = etNote.getSelectionEnd();
        if (start >= end) return;

        SpannableString spannable = new SpannableString(etNote.getText());
        spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        etNote.setText(spannable);
        etNote.setSelection(end);
        haptic();
    }

    private void showColorPicker() {
        int start = etNote.getSelectionStart();
        int end = etNote.getSelectionEnd();
        if (start >= end) return;

        String[] colors = {"Красный", "Синий", "Зелёный", "Оранжевый", "Фиолетовый"};
        int[] colorValues = {Color.RED, Color.BLUE, Color.GREEN, Color.rgb(255, 140, 0), Color.MAGENTA};

        new AlertDialog.Builder(this)
                .setTitle("Цвет текста")
                .setItems(colors, (dialog, which) -> {
                    SpannableString spannable = new SpannableString(etNote.getText());
                    spannable.setSpan(new ForegroundColorSpan(colorValues[which]), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    etNote.setText(spannable);
                    etNote.setSelection(end);
                    haptic();
                })
                .show();
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
