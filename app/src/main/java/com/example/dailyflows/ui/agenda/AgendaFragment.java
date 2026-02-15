package com.example.dailyflows.ui.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
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

    private TaskRepository repo;
    private long selectedDayMillis;

    private MaterialTextView tvDate;
    private MaterialTextView tvOnline;
    private TaskListAdapter adapter;
    private RecyclerView rv;
    private FloatingActionButton fab;

    public AgendaFragment() {
        super(R.layout.fragment_agenda);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repo = new TaskRepository(requireContext());

        tvDate = view.findViewById(R.id.tvDate);
        tvOnline = view.findViewById(R.id.tvOnline);
        fab = view.findViewById(R.id.fabAdd);

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

        MaterialButton btnPickDate = view.findViewById(R.id.btnPickDate);

        selectedDayMillis = DateTimeUtil.atStartOfDay(System.currentTimeMillis());
        renderHeader();

        btnPickDate.setOnClickListener(v -> openDatePicker());
        fab.setOnClickListener(v -> {
            fab.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                fab.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            }).start();

            Intent i = new Intent(requireContext(), EditTaskActivity.class);
            i.putExtra(EditTaskActivity.EXTRA_PREFILL_DAY, selectedDayMillis);
            startActivity(i);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        repo.observeAll().observe(getViewLifecycleOwner(), tasks -> {
            long start = DateTimeUtil.atStartOfDay(selectedDayMillis);
            long end = DateTimeUtil.atEndOfDay(selectedDayMillis);

            List<TaskEntity> out = new ArrayList<>();
            for (TaskEntity t : tasks) {
                if (t.dueAtMillis == 0) continue;
                if (t.dueAtMillis >= start && t.dueAtMillis <= end) out.add(t);
            }
            adapter.submitList(out);
            rv.scheduleLayoutAnimation();
        });
    }

    private void deleteTask(TaskEntity task, View view) {
        new Thread(() -> {
            AppDatabase.get(requireContext()).taskDao().delete(task);
        }).start();
        Snackbar.make(view, "Удалено: " + task.title, Snackbar.LENGTH_LONG)
                .setAction("Отменить", v -> {
                    new Thread(() -> AppDatabase.get(requireContext()).taskDao().upsert(task)).start();
                })
                .show();
    }

    private void renderHeader() {
        tvDate.setText("День: " + DateTimeUtil.formatDate(selectedDayMillis));

        String online = WeatherRepository.getText(requireContext());
        if (online == null) online = "Онлайн: нет (подождите фонового обновления)";
        tvOnline.setText(online);
    }

    private void openDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите дату")
                .setSelection(selectedDayMillis)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                selectedDayMillis = DateTimeUtil.atStartOfDay(selection);
                renderHeader();
            }
        });

        picker.show(getParentFragmentManager(), "date_picker");
    }
}
