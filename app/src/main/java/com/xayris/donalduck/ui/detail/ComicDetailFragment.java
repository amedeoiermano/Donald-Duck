package com.xayris.donalduck.ui.detail;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.xayris.donalduck.MainActivity;
import com.xayris.donalduck.R;
import com.xayris.donalduck.adapters.StoriesAdapter;
import com.xayris.donalduck.data.ComicsExplorer;
import com.xayris.donalduck.data.ComicsRepository;
import com.xayris.donalduck.data.entities.Comic;
import com.xayris.donalduck.databinding.FragmentComicDetailBinding;
import com.xayris.donalduck.utils.Utility;

import java.util.Objects;


public class ComicDetailFragment extends Fragment implements ComicsExplorer.OnComicDownloadedListener, View.OnClickListener {

    Comic _comic;
    public static final String ARG_ISSUE = "issue";
    FragmentComicDetailBinding _binding;

    public ComicDetailFragment() {
    }

    public static ComicDetailFragment newInstance(String issue) {
        ComicDetailFragment fragment = new ComicDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ISSUE, issue);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _binding = FragmentComicDetailBinding.inflate(inflater, container, false);
        _binding.storiesList.setLayoutManager(new LinearLayoutManager(getContext()));
        _binding.deleteComicBtn.setOnClickListener(this);
        _binding.coverImg.setOnClickListener(this);
        return _binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity)requireActivity()).hideMenu();
        if (getArguments() != null) {
            String issue = getArguments().getString(ARG_ISSUE);
            if(issue == null)
                promptNewComic();
            else
                loadComic(issue);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

    private void loadComic(String issue) {
        // gets existing comic
        _comic= ComicsRepository.getInstance().getComic(issue);
        if(_comic != null)
            showComic();
    }

    private void promptNewComic() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),R.style.Theme_DonaldDuck_Dialog);
        builder.setTitle(getString(R.string.prompt_issue_number));
        final AppCompatEditText input = new AppCompatEditText(requireContext());
        input.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        input.setTextColor(Color.WHITE);
        input.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.harvats));
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.confirm), (d, which) -> {
            Editable issue = input.getText();
            if(issue == null || issue.length() == 0)
            {
                Utility.showToast(getContext(), R.string.comic_issue_number_not_provided, Toast.LENGTH_SHORT);
                promptNewComic();
                return;

            }
            Utility.showToast(getContext(), R.string.downloading_comic, Toast.LENGTH_LONG);
            // gets existing comic
            _comic= ComicsRepository.getInstance().getComic(issue.toString());
            if(_comic != null)
                showComic();
            else
                ComicsExplorer.downloadComic(issue.toString(),ComicDetailFragment.this);
            input.clearFocus();
            Utility.hideKeyboard(requireActivity());
        });
        builder.setCancelable(false);
        builder.setNegativeButton(getString(R.string.cancel), (d, which) -> { d.cancel();
            input.clearFocus();
            Utility.hideKeyboard(requireActivity());
            requireActivity().onBackPressed();
        });
        builder.show();
        Utility.showKeyboard(input, requireActivity());
    }

    @Override
    public void onComicDownloaded(ComicsExplorer.DownloadComicResult result) {
        requireActivity().runOnUiThread(() -> {
            if (result.getStatus() == ComicsExplorer.DownloadComicResult.DownloadComicStatus.Success) {
                _comic = result.getComic();
                showComic();
                // stores the comic
                ComicsRepository.getInstance().saveComic(_comic);
            } else {
                int msg = result.getStatus() == ComicsExplorer.DownloadComicResult.DownloadComicStatus.IssueNotFound ? R.string.issue_not_found : R.string.error_downloading_comic;
                Utility.showToast(getContext(), msg, Toast.LENGTH_SHORT);
                promptNewComic();
            }
        });
    }

    private void showComic() {
        Utility.hideToast();
        _binding.issueTxt.setText(getString(R.string.issue_number, _comic.getIssue()));
        _binding.issueDateTxt.setText(_comic.getIssueDateFormatted());
        Glide.with(requireContext().getApplicationContext()).load(_comic.getCoverUrl()).into(_binding.coverImg);
        _binding.storiesList.setAdapter(new StoriesAdapter(_comic, ComicsRepository.getInstance()::setStoryRead));
        _binding.deleteComicBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.deleteComicBtn)
            showDeleteComicDialog();
        if(v.getId() == R.id.coverImg)
            promptNewComicCover();
    }

    private void promptNewComicCover() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),R.style.Theme_DonaldDuck_Dialog);
        builder.setTitle(getString(R.string.prompt_cover_image));
        final AppCompatEditText input = new AppCompatEditText(requireContext());
        input.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        input.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        input.setTextColor(Color.WHITE);
        input.setText(_comic.getCoverUrl());
        if(_comic.getCoverUrl() != null)
            input.setSelection(0, Objects.requireNonNull(input.getText()).length());
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
            Editable newCoverUrl = input.getText();
            if(newCoverUrl == null || newCoverUrl.length() == 0) {
                Utility.showToast(requireContext(), R.string.comic_cover_url_not_provided, Toast.LENGTH_SHORT);
                promptNewComicCover();
                return;
            }
            ComicsRepository.getInstance().updateComicCoverUrl(_comic, newCoverUrl.toString());
            Glide.with(requireContext().getApplicationContext()).load(_comic.getCoverUrl()).into(_binding.coverImg);
        });
        builder.setCancelable(false);
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> { dialog.cancel();
            Utility.hideKeyboard(requireActivity());
        });
        builder.show();
        Utility.showKeyboard(input, requireActivity());
    }

    private void showDeleteComicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),R.style.Theme_DonaldDuck_Dialog);
        builder.setTitle(R.string.delete_comic_confirm);
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> deleteComic());
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void deleteComic() {
        ComicsRepository.getInstance().deleteComic(_comic);
        requireActivity().onBackPressed();
        Toast.makeText(requireContext(), R.string.comic_deleted, Toast.LENGTH_SHORT).show();
    }
}