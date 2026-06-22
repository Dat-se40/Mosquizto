package com.example.mosquizto.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Activities.FolderDetailActivity;
import com.example.mosquizto.Adapters.FolderAdapter;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.FolderSummaryResponse;
import com.example.mosquizto.Network.itf.FolderApi;
import com.example.mosquizto.R;
import com.example.mosquizto.Util.ApiErrorHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

@AndroidEntryPoint
public class FoldersFragment extends Fragment {
    private static final String TAG = "FoldersFragment";

    private RecyclerView rvFolders;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FolderAdapter folderAdapter;
    private Call<ApiResponse<List<FolderSummaryResponse>>> foldersCall;
    private List<FolderSummaryResponse> originalList = new ArrayList<>();

    @Inject
    FolderApi folderApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folders, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listenForFolderCreated();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (folderApi != null) {
            // Reload mỗi lần quay lại tab để thấy folder mới tạo/xóa.
            loadFolders();
        }
    }

    @Override
    public void onDestroyView() {
        if (foldersCall != null) {
            // Hủy request nếu Fragment bị đóng để tránh callback vào UI cũ.
            foldersCall.cancel();
        }
        super.onDestroyView();
    }

    private void initViews(View view) {
        rvFolders = view.findViewById(R.id.rvFolders);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        folderAdapter = new FolderAdapter(new ArrayList<>(), folder -> {
            Intent intent = new Intent(requireContext(), FolderDetailActivity.class);
            intent.putExtra("FOLDER_ID", folder.getId());
            intent.putExtra("FOLDER_TITLE", folder.getName());
            startActivity(intent);
        });
        folderAdapter.setOnFolderOptionsListener(this::showDeleteFolderDialog);

        rvFolders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFolders.setAdapter(folderAdapter);
    }

    private void listenForFolderCreated() {
        // Khi dialog tạo folder báo thành công, reload lại danh sách.
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
                "folder_created",
                getViewLifecycleOwner(),
                (requestKey, result) -> loadFolders()
        );
    }

    private void loadFolders() {
        if (foldersCall != null) {
            // Chỉ giữ request mới nhất khi user đổi tab nhanh.
            foldersCall.cancel();
        }
        showLoading(true);
        foldersCall = folderApi.getAllOwnFolders();
        foldersCall.enqueue(new Callback<ApiResponse<List<FolderSummaryResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FolderSummaryResponse>>> call, Response<ApiResponse<List<FolderSummaryResponse>>> response) {
                if (!isAdded()) return;

                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<FolderSummaryResponse> folders = response.body().getData();

                    originalList = folders != null ? folders : new ArrayList<>();
                    showFolders(folders != null ? folders : new ArrayList<>());
                } else {
                    showFolders(new ArrayList<>());
                    Toast.makeText(requireContext(), "Không thể tải danh sách thư mục", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FolderSummaryResponse>>> call, Throwable t) {
                if (call.isCanceled() || !isAdded()) return;

                showLoading(false);
                showFolders(new ArrayList<>());
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean loading) {
        // Khi đang tải thì ẩn list và empty text.
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        rvFolders.setVisibility(loading ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    private void showFolders(List<FolderSummaryResponse> folders) {
        folderAdapter.setFolders(folders);
        // Nếu list rỗng, hiện thông báo thay vì RecyclerView trống.
        boolean isEmpty = folders.isEmpty();
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvFolders.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // Hàm nhận từ khóa tìm kiếm từ LibraryFragment truyền xuống cho tab Folder
    public void filterData(String query) {
        if (originalList == null || folderAdapter == null) return;

        String cleanQuery = query.toLowerCase().trim();

        // Nếu ô tìm kiếm trống, hiển thị lại toàn bộ danh sách thư mục gốc
        if (cleanQuery.isEmpty()) {
            showFolders(originalList);
            return;
        }

        List<FolderSummaryResponse> filteredList = new ArrayList<>();
        for (FolderSummaryResponse folder : originalList) {
            // Lọc theo tên thư mục (Không phân biệt chữ hoa thường)
            if (folder != null && folder.getName() != null && folder.getName().toLowerCase().contains(cleanQuery)) {
                filteredList.add(folder);
            }
        }

        // Gọi hàm hiển thị (Hàm này tự lo việc ẩn hiện màn hình trống tvEmpty của bạn)
        showFolders(filteredList);
    }

    private void showDeleteFolderDialog(FolderSummaryResponse folder, int position) {
        if (folder == null || folder.getId() == null || !isAdded()) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.deleteFolder)
                .setMessage(R.string.ntRemoveStudySets)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) ->
                        deleteFolder(folder.getId(), position))
                .show();
    }

    private void deleteFolder(Long folderId, int position) {
        folderApi.deleteFolder(folderId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    if (originalList != null) {
                        originalList.removeIf(f -> f.getId() != null && f.getId().equals(folderId));
                    }
                    folderAdapter.removeItem(position);
                    if (folderAdapter.getItemCount() == 0) {
                        showFolders(new ArrayList<>());
                    }
                    Toast.makeText(requireContext(), R.string.ntFolderDeleted, Toast.LENGTH_SHORT).show();
                } else {
                    String message = ApiErrorHelper.extractMessage(response);
                    Log.e(TAG, "deleteFolder failed: " + message);
                    Toast.makeText(requireContext(), getString(R.string.ntDeleteFailed) + ": " + message,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "deleteFolder onFailure", t);
                Toast.makeText(requireContext(), R.string.ntConnectionError, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
