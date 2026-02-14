package com.example.dailyflows.ui.settings;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import com.example.dailyflows.R;
import com.example.dailyflows.data.repo.BackupRepository;

public class SettingsFragment extends Fragment {

    private BackupRepository backupRepository;
    private MaterialTextView tvStatus;

    private ActivityResultLauncher<String> createDocLauncher;
    private ActivityResultLauncher<String[]> openDocLauncher;

    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backupRepository = new BackupRepository(requireContext());

        createDocLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/json"),
                this::onCreateDocResult
        );

        openDocLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::onOpenDocResult
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvStatus = view.findViewById(R.id.tvStatus);

        MaterialButton btnExport = view.findViewById(R.id.btnExport);
        MaterialButton btnImport = view.findViewById(R.id.btnImport);

        btnExport.setOnClickListener(v -> createDocLauncher.launch("dailyflow-backup.json"));
        btnImport.setOnClickListener(v -> openDocLauncher.launch(new String[]{"application/json"}));
    }

    private void onCreateDocResult(Uri uri) {
        if (uri == null) return;
        tvStatus.setText("Экспорт: выполняется...");

        backupRepository.exportTo(
                requireContext(),
                uri,
                () -> requireActivity().runOnUiThread(() -> {
                    tvStatus.setText("Экспорт: готово");
                    Toast.makeText(requireContext(), "Экспорт готов", Toast.LENGTH_SHORT).show();
                }),
                () -> requireActivity().runOnUiThread(() -> {
                    tvStatus.setText("Экспорт: ошибка");
                    Toast.makeText(requireContext(), "Ошибка экспорта", Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void onOpenDocResult(Uri uri) {
        if (uri == null) return;
        tvStatus.setText("Импорт: выполняется...");

        backupRepository.importFrom(
                requireContext(),
                uri,
                () -> requireActivity().runOnUiThread(() -> {
                    tvStatus.setText("Импорт: готово");
                    Toast.makeText(requireContext(), "Импорт готов", Toast.LENGTH_SHORT).show();
                }),
                () -> requireActivity().runOnUiThread(() -> {
                    tvStatus.setText("Импорт: ошибка");
                    Toast.makeText(requireContext(), "Ошибка импорта", Toast.LENGTH_SHORT).show();
                })
        );
    }
}
