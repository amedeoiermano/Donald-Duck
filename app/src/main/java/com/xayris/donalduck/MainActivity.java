package com.xayris.donalduck;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.xayris.donalduck.data.ComicsRepository;
import com.xayris.donalduck.data.entities.Comic;
import com.xayris.donalduck.databinding.ActivityMainBinding;
import com.xayris.donalduck.ui.detail.ComicDetailFragment;
import com.xayris.donalduck.utils.Utility;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener,  View.OnClickListener, MediaPlayer.OnCompletionListener, FragmentManager.OnBackStackChangedListener {
    MediaPlayer _bgMusicPlayer;
    int _bgMusicPlayerCurrentPos;
    ActivityMainBinding _binding;
    NavHostFragment _navHostFragment;
    NavController _navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                R.id.navigation_home, R.id.navigation_archive)
                .build();
        _navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        if(_navHostFragment != null) {
            _navController = _navHostFragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, _navController, appBarConfiguration);
            NavigationUI.setupWithNavController(_binding.navView, _navController);
        }
        _binding.navView.setOnItemSelectedListener(this);
        _bgMusicPlayer = MediaPlayer.create(this, R.raw.bg_music);
        _bgMusicPlayer.setOnCompletionListener(this);

        getSupportFragmentManager().addOnBackStackChangedListener(this);
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

    public void openComic(Comic comic) {
        _binding.navView.getLayoutParams().height = 0;
        getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.nav_host_fragment_activity_main, ComicDetailFragment.newInstance(comic.getIssue()), ComicDetailFragment.class.getName())
                .addToBackStack(null).commit();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        hideAnimation();
    }


    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            _binding.navView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        else
            _binding.navView.getLayoutParams().height = 0;
    }

    @Override
    public void onBackPressed() {
        if (!(getSupportFragmentManager().getFragments().get(0) instanceof NavHostFragment)) {
            getSupportFragmentManager().popBackStack();
        }
        else
            super.onBackPressed();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        _binding.toolbarShadow.setVisibility(item.getItemId() == R.id.navigation_archive ? View.GONE : View.VISIBLE);
        _navController.navigate(item.getItemId());
        return true;
    }
}