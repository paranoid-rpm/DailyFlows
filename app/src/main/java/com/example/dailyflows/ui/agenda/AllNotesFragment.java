package com.example.dailyflows.ui.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import com.example.dailyflows.R;
import com.example.dailyflows.data.local.entities.TaskEntity;
import com.example.dailyflows.data.repo.TaskRepository;
import com.example.dailyflows.ui.edit.EditTaskActivity;

public class AllNotesFragment extends Fragment {

    private TaskRepository repo;
    private TaskListAdapter adapter;

    public AllNotesFragment() {
        super(R.layout.fragment_all_notes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repo = new TaskRepository(requireContext());

        MaterialToolbar toolbar = view.findViewById(R.id.toolbarAll);
        toolbar.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        RecyclerView rv = view.findViewById(R.id.rvAll);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

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
                // In all-notes screen we still allow delete via swipe on the list itself
            }
        });
        rv.setAdapter(adapter);

        repo.observeAll().observe(getViewLifecycleOwner(), tasks -> {
            // Show all tasks: done, overdue, future, everything.
            adapter.submitList(tasks);
        });
    }
}
