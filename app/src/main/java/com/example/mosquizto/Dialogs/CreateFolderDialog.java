package com.example.mosquizto.Dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mosquizto.Dto.request.CreateFolderRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.FolderResponse;
import com.example.mosquizto.Network.itf.FolderApi;
import com.example.mosquizto.R;
import com.example.mosquizto.Util.ApiErrorHelper;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class CreateFolderDialog extends DialogFragment {

    private static final String TAG = "CreateFolderDialog";

    private EditText etFolderName;
    private EditText etFolderDesc;
    private ImageButton btnSave;
    private Call<ApiResponse<FolderResponse>> createFolderCall;

    @Inject
    FolderApi folderApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_folder, container, false);

        etFolderName = view.findViewById(R.id.et_folder_name);
        etFolderDesc = view.findViewById(R.id.et_folder_desc);
        TextView btnCancel = view.findViewById(R.id.tv_cancel);
        btnSave = view.findViewById(R.id.btn_save_folder);

        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etFolderName.getText().toString().trim();
            String description = etFolderDesc.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), R.string.ntEnterFolderName, Toast.LENGTH_SHORT).show();
            } else if (description.isEmpty()) {
                Toast.makeText(requireContext(), R.string.ntEnterFolderDescription, Toast.LENGTH_SHORT).show();
                etFolderDesc.requestFocus();
            } else {
                createFolder(name, description);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        if (createFolderCall != null) {
            createFolderCall.cancel();
        }
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F6F7FB")));
            }
        }
    }

    private void createFolder(String name, String description) {
        if (folderApi == null) {
            Log.e(TAG, "createFolder: folderApi is null");
            Toast.makeText(requireContext(), R.string.msg_failed_create_folder, Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        CreateFolderRequest request = new CreateFolderRequest(name, description);
        Log.d(TAG, "createFolder request name=" + request.getName()
                + ", descriptionLength=" + request.getDescription().length());

        createFolderCall = folderApi.createFolder(request);
        createFolderCall.enqueue(new Callback<ApiResponse<FolderResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FolderResponse>> call, Response<ApiResponse<FolderResponse>> response) {
                if (!isAdded()) return;

                btnSave.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    FolderResponse folder = response.body().getData();
                    if (folder != null && folder.getId() != null) {
                        Bundle result = new Bundle();
                        result.putLong("FOLDER_ID", folder.getId());
                        result.putString("FOLDER_NAME", folder.getName());
                        getParentFragmentManager().setFragmentResult("folder_created", result);

                        Toast.makeText(requireContext(),
                                getString(R.string.msg_folder_created, folder.getName()),
                                Toast.LENGTH_SHORT).show();
                        dismiss();
                        return;
                    }
                    Log.w(TAG, "createFolder success but data is null, message="
                            + response.body().getMessage());
                }

                String error = ApiErrorHelper.extractMessage(response);
                Log.e(TAG, "createFolder failed: " + error);
                Toast.makeText(requireContext(),
                        getString(R.string.msg_failed_create_folder) + ": " + error,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ApiResponse<FolderResponse>> call, Throwable t) {
                if (call.isCanceled() || !isAdded()) return;

                btnSave.setEnabled(true);
                Log.e(TAG, "createFolder onFailure", t);
                Toast.makeText(requireContext(), ApiErrorHelper.networkError(requireContext()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
