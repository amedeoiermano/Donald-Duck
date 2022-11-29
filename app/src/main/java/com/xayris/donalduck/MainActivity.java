package com.xayris.donalduck;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.xayris.donalduck.data.ComicsRepository;
import com.xayris.donalduck.data.entities.Comic;
import com.xayris.donalduck.databinding.ActivityMainBinding;
import com.xayris.donalduck.ui.detail.ComicDetailFragment;
import com.xayris.donalduck.utils.Utility;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {
    MediaPlayer _bgMusicPlayer;
    int _bgMusicPlayerCurrentPos;
    ActivityMainBinding _binding;
    NavHostFragment _navHostFragment;
    NavController _navController;
    ActivityResultLauncher<Intent> _restoreDataLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _restoreDataLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        ComicsRepository.getInstance().restoreData(MainActivity.this, result.getData());
                    }
                });
        ComicsRepository.getInstance().createDatabase(this);
        ComicsRepository.getInstance().loadComics();
        _binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(_binding.getRoot());

        findViewById(R.id.logo).setClickable(true);
        findViewById(R.id.logo).setFocusable(true);
        findViewById(R.id.logo).setOnClickListener(this);

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_archive, R.id.navigation_detail)
                .build();
        _navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        if(_navHostFragment != null) {
            _navController = _navHostFragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, _navController, appBarConfiguration);
            NavigationUI.setupWithNavController(_binding.navView, _navController);
        }
        _bgMusicPlayer = MediaPlayer.create(this, R.raw.bg_music);
        _bgMusicPlayer.setOnCompletionListener(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(_navController.getCurrentDestination().getId() != R.id.navigation_archive)
            return false;
        menu.clear();
        new MenuInflater(this).inflate(R.menu.toolbar_menu, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    private void startBgMusic() {
        showAnimation();
        _bgMusicPlayer.start();
    }

    private void stopBgMusic() {
        hideAnimation();
        _bgMusicPlayer.stop();
        _bgMusicPlayer.prepareAsync();
    }

    private void resumeBgMusic() {
        if(_bgMusicPlayerCurrentPos > 0) {
            _bgMusicPlayer.seekTo(_bgMusicPlayerCurrentPos);
            _bgMusicPlayer.start();
        }
    }

    private void pauseBgMusic() {
        if(_bgMusicPlayer.isPlaying()) {
            _bgMusicPlayerCurrentPos = _bgMusicPlayer.getCurrentPosition();
            _bgMusicPlayer.pause();
        }
    }

    @SuppressLint("DiscouragedApi")
    private void showAnimation() {
        int randomNum = ThreadLocalRandom.current().nextInt(1, 3 + 1);
        String gifResKey = getString(R.string.animated_gif_key, String.valueOf(randomNum));
        Glide.with(getApplicationContext()).load(getResources().getIdentifier(gifResKey, "drawable", getPackageName())).into(new DrawableImageViewTarget(_binding.animatedImg));
        ValueAnimator animator = ValueAnimator.ofInt(0,255);
        animator.setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(valueAnimator -> _binding.animatedImg.setImageAlpha((Integer) valueAnimator.getAnimatedValue()));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                _binding.animatedImg.setVisibility(View.VISIBLE);
            }
        });
        animator.start();
    }

    private void hideAnimation() {
        ValueAnimator animator = ValueAnimator.ofInt(255,0);
        animator.setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(valueAnimator -> _binding.animatedImg.setImageAlpha((Integer) valueAnimator.getAnimatedValue()));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                _binding.animatedImg.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeBgMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseBgMusic();
    }

    @Override
    protected void onDestroy() {
        stopBgMusic();
        _bgMusicPlayer.release();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.backupData) {
            ComicsRepository.getInstance().backupData(this);
        }
        if(item.getItemId() == R.id.restoreData) {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, "Choose a file");
            _restoreDataLauncher.launch(chooseFile);
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.logo)
        {
            Utility.performHapticFeedback(getApplicationContext());
            if(_bgMusicPlayer.isPlaying())
                stopBgMusic();
            else
                startBgMusic();
        }
    }

    public void openComic(Comic comic, Category category) {
        hideNavBar();
        Bundle args = new Bundle();
        args.putString(ComicDetailFragment.ARG_ISSUE, comic.getIssue());
        args.putString(ComicDetailFragment.ARG_CATEGORY, category.toString());
        _navController.navigate(R.id.action_detail, args);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        hideAnimation();
    }


    public void addComic() {
        _navController.navigate(R.id.action_detail);
    }

    public void showNavBar() {
        invalidateOptionsMenu();
        _binding.navView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    public void hideNavBar() {
        invalidateOptionsMenu();
        _binding.navView.getLayoutParams().height = 0;

    }
}