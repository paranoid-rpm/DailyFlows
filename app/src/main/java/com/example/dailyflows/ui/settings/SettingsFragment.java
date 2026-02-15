package com.example.dailyflows.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textview.MaterialTextView;

import com.example.dailyflows.R;
import com.example.dailyflows.data.repo.BackupRepository;
import com.example.dailyflows.util.PrefsUtil;

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
        MaterialButton btnTheme = view.findViewById(R.id.btnTheme);
        MaterialButton btnStyle = view.findViewById(R.id.btnStyle);
        MaterialButton btnFontScale = view.findViewById(R.id.btnFontScale);

        btnExport.setOnClickListener(v -> createDocLauncher.launch("dailyflow-backup.json"));
        btnImport.setOnClickListener(v -> openDocLauncher.launch(new String[]{"application/json"}));

        btnTheme.setOnClickListener(v -> showThemeDialog());
        btnStyle.setOnClickListener(v -> showStyleDialog());
        btnFontScale.setOnClickListener(v -> showFontScaleDialog());
    }

    private void showThemeDialog() {
        String[] themes = {"Системная", "Светлая", "Тёмная"};
        int current = PrefsUtil.getTheme(requireContext());

        new AlertDialog.Builder(requireContext())
                .setTitle("Тема приложения")
                .setSingleChoiceItems(themes, current, (dialog, which) -> {
                    PrefsUtil.setTheme(requireContext(), which);
                    Toast.makeText(requireContext(), "Перезапустите приложение", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .show();
    }

    private void showStyleDialog() {
        String[] styles = {"Стандартный", "Apple Glass", "Pixel Minimalism"};
        int current = PrefsUtil.getStyle(requireContext());

        new AlertDialog.Builder(requireContext())
                .setTitle("Стиль интерфейса")
                .setSingleChoiceItems(styles, current, (dialog, which) -> {
                    PrefsUtil.setStyle(requireContext(), which);
                    Toast.makeText(requireContext(), "Перезапустите приложение", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .show();
    }

    private void showFontScaleDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_font_scale, null);
        Slider slider = dialogView.findViewById(R.id.sliderFontScale);
        MaterialTextView tvPreview = dialogView.findViewById(R.id.tvPreview);

        slider.setValue(PrefsUtil.getFontScale(requireContext()));
        slider.addOnChangeListener((sl, value, fromUser) -> {
            tvPreview.setTextSize(16 * value);
            tvPreview.setText("Масштаб: " + String.format("%.1f", value));
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("Размер шрифта")
                .setView(dialogView)
                .setPositiveButton("Применить", (dialog, which) -> {
                    PrefsUtil.setFontScale(requireContext(), slider.getValue());
                    Toast.makeText(requireContext(), "Перезапустите приложение", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
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
