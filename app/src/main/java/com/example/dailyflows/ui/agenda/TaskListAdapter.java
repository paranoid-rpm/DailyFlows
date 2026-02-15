package com.example.dailyflows.ui.agenda;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import com.example.dailyflows.R;
import com.example.dailyflows.data.local.entities.TaskEntity;
import com.example.dailyflows.util.DateTimeUtil;

public class TaskListAdapter extends ListAdapter<TaskEntity, TaskListAdapter.VH> {

    public interface Listener {
        void onToggleDone(TaskEntity task, boolean done);
        void onClick(TaskEntity task);
        void onDelete(TaskEntity task);
    }

    private final Listener listener;

    public TaskListAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<TaskEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull TaskEntity a, @NonNull TaskEntity b) {
                return a.id.equals(b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull TaskEntity a, @NonNull TaskEntity b) {
                return a.title.equals(b.title) && a.done == b.done && a.priority == b.priority;
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH vh, int position) {
        TaskEntity task = getItem(position);

        vh.tvTitle.setText(getPriorityEmoji(task.priority) + " " + task.title);
        vh.tvTime.setText(DateTimeUtil.formatTime(task.dueAtMillis));
        vh.cbDone.setChecked(task.done);

        // Show note preview
        if (task.note != null && !task.note.isEmpty()) {
            vh.tvNote.setVisibility(View.VISIBLE);
            String preview = task.note.length() > 50 ? task.note.substring(0, 50) + "..." : task.note;
            vh.tvNote.setText(preview.replace("\n", " "));
        } else {
            vh.tvNote.setVisibility(View.GONE);
        }

        // Priority color bar
        vh.priorityBar.setBackgroundColor(getPriorityColor(task.priority));

        vh.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onToggleDone(task, isChecked);
        });

        vh.card.setOnClickListener(v -> {
            if (listener != null) listener.onClick(task);
        });

        vh.card.setOnLongClickListener(v -> {
            if (listener != null) listener.onClick(task);
            return true;
        });
    }

    private String getPriorityEmoji(int priority) {
        if (priority == 0) return "⚪";
        if (priority <= 2) return "🟢";
        if (priority <= 5) return "🟡";
        if (priority <= 8) return "🟠";
        return "🔴";
    }

    private int getPriorityColor(int priority) {
        if (priority == 0) return Color.parseColor("#BDBDBD");
        if (priority <= 2) return Color.parseColor("#4CAF50");
        if (priority <= 5) return Color.parseColor("#FFEB3B");
        if (priority <= 8) return Color.parseColor("#FF9800");
        return Color.parseColor("#F44336");
    }

    static class VH extends RecyclerView.ViewHolder {
        MaterialCardView card;
        View priorityBar;
        MaterialTextView tvTitle;
        MaterialTextView tvNote;
        MaterialTextView tvTime;
        CheckBox cbDone;

        public VH(@NonNull View v) {
            super(v);
            card = (MaterialCardView) v;
            priorityBar = v.findViewById(R.id.priorityBar);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvNote = v.findViewById(R.id.tvNote);
            tvTime = v.findViewById(R.id.tvTime);
            cbDone = v.findViewById(R.id.cbDone);
        }
    }

    public static class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private final Listener listener;
        private final TaskListAdapter adapter;

        public SwipeToDeleteCallback(Listener listener, TaskListAdapter adapter) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.listener = listener;
            this.adapter = adapter;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            TaskEntity task = adapter.getItem(pos);
            if (listener != null) listener.onDelete(task);
        }

        @Override
        public void onChildDraw(@NonNull android.graphics.Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            View itemView = viewHolder.itemView;
            float alpha = 1.0f - Math.abs(dX) / (float) itemView.getWidth();
            itemView.setAlpha(alpha);
        }
    }
}
