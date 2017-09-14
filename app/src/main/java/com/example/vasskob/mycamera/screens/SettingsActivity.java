package com.example.vasskob.mycamera.screens;

import android.annotation.TargetApi;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vasskob.mycamera.BuildConfig;
import com.example.vasskob.mycamera.R;
import com.example.vasskob.mycamera.utils.CameraUtils;
import com.example.vasskob.mycamera.utils.PictureSize;
import com.example.vasskob.mycamera.utils.PictureSizeLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.vasskob.mycamera.utils.Constants.BACK_CAMERA_2_QUALITY;
import static com.example.vasskob.mycamera.utils.Constants.BACK_CAMERA_QUALITY;
import static com.example.vasskob.mycamera.utils.Constants.BACK_VIDEO_QUALITY;
import static com.example.vasskob.mycamera.utils.Constants.CAMERA_CATEGORY;
import static com.example.vasskob.mycamera.utils.Constants.FRONT_CAMERA_QUALITY;
import static com.example.vasskob.mycamera.utils.Constants.FRONT_VIDEO_QUALITY;
import static com.example.vasskob.mycamera.utils.Constants.JPEG_COMPRESSION;
import static com.example.vasskob.mycamera.utils.Constants.UNKNOWN;


public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                if (index >= 0) {
                    String prefValue = listPreference.getEntries()[index].toString();
                    if (prefValue.contains("%")) {
                        prefValue = new StringBuilder(prefValue).insert(prefValue.indexOf("%"), "%").toString();
                    }
                    preference.setSummary(prefValue);
                } else preference.setSummary(null);
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }


    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onBackPressed() {
//        if (getFragmentManager().findFragmentById(com.android.internal.R.id.prefs) == null) {
//            super.onBackPressed();
//        } else finish();
        int count = getFragmentManager().getBackStackEntryCount();
        Log.d(TAG, "onBackPressed: count " + count);
        if (count == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || AdvancedFragment.class.getName().equals(fragmentName)
                || AboutFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        public static final int RESOLUTION_COUNT = 5;
        private static final int VIDEO_RES_COUNT = 10;
        private ListPreference backCamera2Pref;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
            initCameraResolutionList();

            bindPreferenceSummaryToValue(findPreference(BACK_CAMERA_QUALITY));
            bindPreferenceSummaryToValue(findPreference(FRONT_CAMERA_QUALITY));
            bindPreferenceSummaryToValue(findPreference(BACK_VIDEO_QUALITY));
            bindPreferenceSummaryToValue(findPreference(FRONT_VIDEO_QUALITY));
            bindPreferenceSummaryToValue(findPreference(JPEG_COMPRESSION));

        }

        private void initCameraResolutionList() {
            PictureSizeLoader.PictureSizes pictureSizes;
            pictureSizes = CameraUtils.loadPicSizesFromStorage(getActivity());
            if (pictureSizes == null) {
                Log.d(TAG, "initCameraResolutionList: ");
                pictureSizes = PictureSizeLoader.getPictureSizes();
                CameraUtils.savePicSizesToStorage(getActivity(), pictureSizes);
            }
            setBackCamera1Res(pictureSizes);
            setBackCamera2Res(pictureSizes);
            setFrontCameraRes(pictureSizes);
            setVideoFrontRes(pictureSizes);
            setVideoBackRes(pictureSizes);
        }

        private void setBackCamera2Res(PictureSizeLoader.PictureSizes pictureSizes) {
            List<PictureSize> camera2Sizes = pictureSizes.getBackCamera2Sizes();
            if (camera2Sizes != null) {
                backCamera2Pref = new ListPreference(getActivity());
                backCamera2Pref.setKey(BACK_CAMERA_2_QUALITY);
                backCamera2Pref.setTitle(getString(R.string.pref_title_back_camera2_photo_quality));
                setCameraRes(camera2Sizes, backCamera2Pref);
                PreferenceCategory rootScreen = (PreferenceCategory) findPreference(CAMERA_CATEGORY);
                rootScreen.addPreference(backCamera2Pref);
                bindPreferenceSummaryToValue(findPreference(BACK_CAMERA_2_QUALITY));
            }
        }

        private void setVideoBackRes(PictureSizeLoader.PictureSizes pictureSizes) {
            List<PictureSize> videoBackSizes = pictureSizes.getVideoQualitiesBack();
            ListPreference backVideoPref = (ListPreference) findPreference(BACK_VIDEO_QUALITY);
            // setCameraRes(videoBackSizes, backVideoPref);
            setVideoRes(videoBackSizes, backVideoPref);
        }

        private void setVideoRes(List<PictureSize> videoResSizes, ListPreference videoPref) {
            CharSequence[] entries;
            CharSequence[] entryValues;
            if (videoResSizes != null) {
                entries = new String[videoResSizes.size()];
                entryValues = new String[videoResSizes.size()];
                for (int i = 0; i < videoResSizes.size(); i++) {
                    String label = videoResSizes.get(i).getVideoLabel();
                    String stringRatio = CameraUtils.getStringRatio(videoResSizes.get(i).aspectRatio());
                    if (!label.equals(UNKNOWN)) {
                        entries[i] = stringRatio + " " + label + " " + videoResSizes.get(i).toString();
                        entryValues[i] = Integer.toString(videoResSizes.get(i).width()) + "x" + Integer.toString(videoResSizes.get(i).height());
                    }
                }
                videoPref.setEntries(removeNullValue(entries));
                videoPref.setEntryValues(removeNullValue(entryValues));
                setDefaultValues(videoPref, removeNullValue(entryValues)[0]);
            }
        }

        private CharSequence[] removeNullValue(CharSequence[] entries) {
            List<String> list = new ArrayList<>();
            for (CharSequence c : entries) {
                if (c != null && c.length() > 0) {
                    list.add(c.toString());
                }
            }
            return list.toArray(new CharSequence[list.size()]);
        }

        private void setVideoFrontRes(PictureSizeLoader.PictureSizes pictureSizes) {
            List<PictureSize> videoFrontSizes = pictureSizes.getVideoQualitiesFront();
            ListPreference frontVideoPref = (ListPreference) findPreference(FRONT_VIDEO_QUALITY);
            setVideoRes(videoFrontSizes, frontVideoPref);
        }

        private void setFrontCameraRes(PictureSizeLoader.PictureSizes pictureSizes) {
            List<PictureSize> cameraFrontSizes = pictureSizes.getFrontCameraSizes();
            ListPreference frontCameraPref = (ListPreference) findPreference(FRONT_CAMERA_QUALITY);
            setCameraRes(cameraFrontSizes, frontCameraPref);
        }

        private void setBackCamera1Res(PictureSizeLoader.PictureSizes pictureSizes) {
            List<PictureSize> camera1Sizes = pictureSizes.getBackCamera1Sizes();
            ListPreference backCamera1Pref = (ListPreference) findPreference(BACK_CAMERA_QUALITY);
            if (Camera.getNumberOfCameras() < 3) {
                backCamera1Pref.setTitle(R.string.pref_title_back_camera_photo_quality);
            } else {
                backCamera1Pref.setTitle(R.string.pref_title_back_camera1_photo_quality);
            }
            setCameraRes(camera1Sizes, backCamera1Pref);
        }

        private void setCameraRes(List<PictureSize> cameraSizes, ListPreference cameraPrefs) {
            CharSequence[] entries;
            CharSequence[] entryValues;
            if (cameraSizes != null) {
                entries = new String[RESOLUTION_COUNT];
                entryValues = new String[RESOLUTION_COUNT];
                for (int i = 0; i < Math.min(cameraSizes.size(), RESOLUTION_COUNT); i++) {
                    String stringRatio = CameraUtils.getStringRatio(cameraSizes.get(i).aspectRatio());
                    entries[i] = stringRatio + "  " + cameraSizes.get(i).toString();
                    entryValues[i] = Integer.toString(cameraSizes.get(i).width()) + "x" + Integer.toString(cameraSizes.get(i).height());
                }
                cameraPrefs.setEntries(entries);
                cameraPrefs.setEntryValues(entryValues);
                setDefaultValues(cameraPrefs, entryValues[0]);
            }
        }

        private void setDefaultValues(ListPreference cameraPrefs, CharSequence defaultValue) {
            if (noDefaultValue(cameraPrefs)) {
                Log.d(TAG, "setCameraRes: noDefaultValue");
                cameraPrefs.setDefaultValue(defaultValue.toString());
                cameraPrefs.setValue(defaultValue.toString());
                cameraPrefs.setSummary(defaultValue.toString());
            }
        }

        private boolean noDefaultValue(Preference prefs) {
            return PreferenceManager
                    .getDefaultSharedPreferences(prefs.getContext())
                    .getString(prefs.getKey(), "")
                    .isEmpty();
        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AdvancedFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_advanced);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AboutFragment extends PreferenceFragment {
        @BindView(R.id.textView3)
        TextView tvVersionName;
        private View rootView;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_about, container, false);
            ButterKnife.bind(this, rootView);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            tvVersionName.setText(String.format(getString(R.string.about_version), BuildConfig.VERSION_NAME));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            ButterKnife.bind(this, rootView).unbind();
        }
    }
}
