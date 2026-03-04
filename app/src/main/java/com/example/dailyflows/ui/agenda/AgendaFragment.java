package com.example.dailyflows.ui.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import com.example.dailyflows.R;
import com.example.dailyflows.data.local.AppDatabase;
import com.example.dailyflows.data.local.entities.TaskEntity;
import com.example.dailyflows.data.repo.TaskRepository;
import com.example.dailyflows.data.repo.WeatherRepository;
import com.example.dailyflows.ui.edit.EditTaskActivity;
import com.example.dailyflows.util.DateTimeUtil;

public class AgendaFragment extends Fragment {

    private static final String TAG = "AgendaFragment";
    private static final String DELETED_MARK = "__deleted__";

    private TaskRepository repo;
    private long selectedDayMillis;

    private MaterialTextView tvDate;
    private MaterialTextView tvOnline;
    private TaskListAdapter adapter;
    private RecyclerView rv;
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabAll;
    private FloatingActionButton fabPickDate;

    public AgendaFragment() {
        super(R.layout.fragment_agenda);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repo = new TaskRepository(requireContext());

        tvDate = view.findViewById(R.id.tvDate);
        tvOnline = view.findViewById(R.id.tvOnline);
        fabAdd = view.findViewById(R.id.fabAdd);
        fabAll = view.findViewById(R.id.fabAllNotes);
        fabPickDate = view.findViewById(R.id.fabPickDate);

        rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down);
        rv.setLayoutAnimation(animation);

        adapter = new TaskListAdapter(new TaskListAdapter.Listener() {
            @Override
            public void onToggleDone(TaskEntity task, boolean done) {
                repo.setDone(task.id, done, requireContext());
            }

            @Override
            public void onClick(TaskEntity task) {
                Intent i = new Intent(requireContext(), EditTaskActivity.class);
                i.putExtra(EditTaskActivity.EXTRA_TASK_ID, task.id);
                startActivity(i);
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onDelete(TaskEntity task) {
                deleteTask(task, view);
            }
        });
        rv.setAdapter(adapter);

        ItemTouchHelper touchHelper = new ItemTouchHelper(new TaskListAdapter.SwipeToDeleteCallback(new TaskListAdapter.Listener() {
            @Override
            public void onToggleDone(TaskEntity task, boolean done) {}

            @Override
            public void onClick(TaskEntity task) {}

            @Override
            public void onDelete(TaskEntity task) {
                deleteTask(task, view);
            }
        }, adapter));
        touchHelper.attachToRecyclerView(rv);

        selectedDayMillis = DateTimeUtil.atStartOfDay(System.currentTimeMillis());
        renderHeader();

        fabPickDate.setOnClickListener(v -> openDatePicker());

        fabAll.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                    )
                    .replace(R.id.container, new AllNotesFragment())
                    .addToBackStack("all_notes")
                    .commit();
        });

        MaterialCardView headerCard = view.findViewById(R.id.dayHeaderCard);
        GestureDetector detector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 120;
            private static final int SWIPE_VELOCITY_THRESHOLD = 120;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY)
                        && Math.abs(diffX) > SWIPE_THRESHOLD
                        && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        selectedDayMillis = DateTimeUtil.atStartOfDay(selectedDayMillis - 24L * 60L * 60L * 1000L);
                    } else {
                        selectedDayMillis = DateTimeUtil.atStartOfDay(selectedDayMillis + 24L * 60L * 60L * 1000L);
                    }
                    renderHeader();
                    updateListFromCache();
                    return true;
                }
                return false;
            }
        });
        headerCard.setOnTouchListener((v, event) -> detector.onTouchEvent(event));

        fabAdd.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), EditTaskActivity.class);
            i.putExtra(EditTaskActivity.EXTRA_PREFILL_DAY, selectedDayMillis);
            Log.d(TAG, "Starting EditTaskActivity with prefill day: " + selectedDayMillis);
            startActivity(i);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        repo.observeAll().observe(getViewLifecycleOwner(), tasks -> {
            cachedTasks = tasks != null ? tasks : new ArrayList<>();
            updateListFromCache();
            rv.scheduleLayoutAnimation();
        });
    }

    private List<TaskEntity> cachedTasks = new ArrayList<>();

    private void updateListFromCache() {
        long start = DateTimeUtil.atStartOfDay(selectedDayMillis);
        long end = DateTimeUtil.atEndOfDay(selectedDayMillis);

        List<TaskEntity> out = new ArrayList<>();
        for (TaskEntity t : cachedTasks) {
            if (t == null) continue;
            if (DELETED_MARK.equals(t.projectId)) continue;
            if (t.dueAtMillis == 0) continue;
            if (t.dueAtMillis >= start && t.dueAtMillis <= end) out.add(t);
        }
        adapter.submitList(out);
        Log.d(TAG, "Filter day=" + selectedDayMillis + " start=" + start + " end=" + end + " total=" + cachedTasks.size() + " shown=" + out.size());
    }

    private void deleteTask(TaskEntity task, View view) {
        final String oldProjectId = task.projectId;
        task.projectId = DELETED_MARK;
        task.updatedAtMillis = System.currentTimeMillis();

        new Thread(() -> {
            AppDatabase.get(requireContext()).taskDao().upsert(task);
        }).start();

        Snackbar.make(view, getString(R.string.all_notes) + ": " + task.title, Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    task.projectId = oldProjectId;
                    task.updatedAtMillis = System.currentTimeMillis();
                    new Thread(() -> AppDatabase.get(requireContext()).taskDao().upsert(task)).start();
                })
                .show();
    }

    private void renderHeader() {
        tvDate.setText(DateTimeUtil.formatWeekday(selectedDayMillis));

        String online = WeatherRepository.getText(requireContext());
        if (online == null) online = getString(R.string.online_unavailable);
        tvOnline.setText(online);
    }

    private void openDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.pick_date))
                .setSelection(selectedDayMillis)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                selectedDayMillis = DateTimeUtil.atStartOfDay(selection);
                renderHeader();
                updateListFromCache();
            }
        });

        picker.show(getParentFragmentManager(), "date_picker");
    }
}
