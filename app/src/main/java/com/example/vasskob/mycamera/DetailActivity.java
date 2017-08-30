package com.example.vasskob.mycamera;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.vasskob.mycamera.utils.CameraUtils.PHOTO_PATH;


public class DetailActivity extends Activity {

    @BindView(R.id.iv_photo)
    ImageView ivPhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        hideStatusBar();

        loadPhoto();
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void loadPhoto() {
        String photoPath;
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            photoPath = null;
        } else {
            photoPath = extras.getString(PHOTO_PATH);
        }
        Glide.with(this)
                .load(photoPath)
                .into(ivPhoto);
    }

    @OnClick(R.id.btn_back)
    protected void onBackBtnClick() {
        onBackPressed();
    }
}
