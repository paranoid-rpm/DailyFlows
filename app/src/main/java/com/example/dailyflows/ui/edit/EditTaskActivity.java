package com.example.dailyflows.ui.edit;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

        findViewById(R.id.btnBold).setOnClickListener(v -> {
            haptic();
            applyBold();
        });
        findViewById(R.id.btnItalic).setOnClickListener(v -> {
            haptic();
            applyItalic();
        });
        findViewById(R.id.btnUnderline).setOnClickListener(v -> {
            haptic();
            applyUnderline();
        });
        findViewById(R.id.btnBullet).setOnClickListener(v -> {
            haptic();
            insertBullet();
        });
        findViewById(R.id.btnHeading).setOnClickListener(v -> {
            haptic();
            applyHeading();
        });
        findViewById(R.id.btnColor).setOnClickListener(v -> {
            haptic();
            showColorPicker();
        });

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
        
        // Load HTML formatted text
        if (t.note != null && !t.note.isEmpty()) {
            Spanned spanned = Html.fromHtml(t.note, Html.FROM_HTML_MODE_COMPACT);
            etNote.setText(spanned);
        } else {
            etNote.setText("");
        }
        
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
        
        // Save as HTML to preserve formatting
        Editable editable = etNote.getText();
        if (editable != null) {
            String html = Html.toHtml((Spanned) editable, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
            task.note = html;
        } else {
            task.note = "";
        }

        if (selectedTag != null && !task.note.contains("#")) {
            task.note = "<p><b>#" + selectedTag + "</b></p>" + task.note;
        }

        Log.d(TAG, "Saving task: " + task.title + ", id: " + task.id + ", note length: " + task.note.length());

        repo.upsert(task, this, () -> {
            Log.d(TAG, "Task saved, finishing activity");
            finish();
        });
    }

    private void applyBold() {
        int start = etNote.getSelectionStart();
        int end = etNote.getSelectionEnd();
        
        Log.d(TAG, "applyBold: start=" + start + ", end=" + end);
        
        if (start < 0 || end < 0) {
            Toast.makeText(this, "Выделите текст для форматирования", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (start >= end) {
            Toast.makeText(this, "Выделите текст для форматирования", Toast.LENGTH_SHORT).show();
            return;
        }

        Editable editable = etNote.getText();
        if (editable == null || editable.length() == 0) {
            Toast.makeText(this, "Введите текст", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            editable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Toast.makeText(this, "Жирный текст применён", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error applying bold", e);
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void applyItalic() {
        int start = etNote.getSelectionStart();
        int end = etNote.getSelectionEnd();
        
        Log.d(TAG, "applyItalic: start=" + start + ", end=" + end);
        
        if (start < 0 || end < 0 || start >= end) {
            Toast.makeText(this, "Выделите текст для форматирования", Toast.LENGTH_SHORT).show();
            return;
        }

        Editable editable = etNote.getText();
        if (editable == null || editable.length() == 0) {
            Toast.makeText(this, "Введите текст", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            editable.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Toast.makeText(this, "Курсив применён", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error applying italic", e);
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void applyUnderline() {
        int start = etNote.getSelectionStart();
        int end = etNote.getSelectionEnd();
        
        Log.d(TAG, "applyUnderline: start=" + start + ", end=" + end);
        
        if (start < 0 || end < 0 || start >= end) {
            Toast.makeText(this, "Выделите текст для форматирования", Toast.LENGTH_SHORT).show();
            return;
        }

        Editable editable = etNote.getText();
        if (editable == null || editable.length() == 0) {
            Toast.makeText(this, "Введите текст", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            editable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Toast.makeText(this, "Подчёркивание применено", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error applying underline", e);
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void insertBullet() {
        int cursor = etNote.getSelectionStart();
        Log.d(TAG, "insertBullet: cursor=" + cursor);
        
        if (cursor < 0) {
            Toast.makeText(this, "Поставьте курсор", Toast.LENGTH_SHORT).show();
            return;
        }

        Editable editable = etNote.getText();
        if (editable == null) {
            Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            editable.insert(cursor, "• ");
            etNote.setSelection(cursor + 2);
            Toast.makeText(this, "Маркер добавлен", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error inserting bullet", e);
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void applyHeading() {
        int start = etNote.getSelectionStart();
        int end = etNote.getSelectionEnd();
        
        Log.d(TAG, "applyHeading: start=" + start + ", end=" + end);
        
        if (start < 0 || end < 0 || start >= end) {
            Toast.makeText(this, "Выделите текст для заголовка", Toast.LENGTH_SHORT).show();
            return;
        }

        Editable editable = etNote.getText();
        if (editable == null || editable.length() == 0) {
            Toast.makeText(this, "Введите текст", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            editable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Toast.makeText(this, "Заголовок применён", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error applying heading", e);
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showColorPicker() {
        int start = etNote.getSelectionStart();
        int end = etNote.getSelectionEnd();
        
        Log.d(TAG, "showColorPicker: start=" + start + ", end=" + end);
        
        if (start < 0 || end < 0 || start >= end) {
            Toast.makeText(this, "Выделите текст для окраски", Toast.LENGTH_SHORT).show();
            return;
        }

        Editable editable = etNote.getText();
        if (editable == null || editable.length() == 0) {
            Toast.makeText(this, "Введите текст", Toast.LENGTH_SHORT).show();
            return;
        }

        View colorView = getLayoutInflater().inflate(R.layout.dialog_color_picker, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(colorView)
                .create();

        int[] colorIds = {R.id.colorRed, R.id.colorPink, R.id.colorPurple, R.id.colorDeepPurple,
                R.id.colorIndigo, R.id.colorBlue, R.id.colorCyan, R.id.colorTeal,
                R.id.colorGreen, R.id.colorLightGreen, R.id.colorYellow, R.id.colorOrange,
                R.id.colorDeepOrange, R.id.colorBrown, R.id.colorGrey};
        
        int[] colors = {Color.parseColor("#F44336"), Color.parseColor("#E91E63"), 
                Color.parseColor("#9C27B0"), Color.parseColor("#673AB7"),
                Color.parseColor("#3F51B5"), Color.parseColor("#2196F3"),
                Color.parseColor("#00BCD4"), Color.parseColor("#009688"),
                Color.parseColor("#4CAF50"), Color.parseColor("#8BC34A"),
                Color.parseColor("#FFEB3B"), Color.parseColor("#FF9800"),
                Color.parseColor("#FF5722"), Color.parseColor("#795548"),
                Color.parseColor("#9E9E9E")};

        for (int i = 0; i < colorIds.length; i++) {
            final int color = colors[i];
            colorView.findViewById(colorIds[i]).setOnClickListener(v -> {
                try {
                    editable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    Toast.makeText(this, "Цвет применён", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e(TAG, "Error applying color", e);
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        dialog.show();
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
