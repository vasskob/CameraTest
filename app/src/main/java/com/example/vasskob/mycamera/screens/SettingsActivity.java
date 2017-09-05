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
import com.example.vasskob.mycamera.utils.PreferencesManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.vasskob.mycamera.utils.CameraUtils.BACK_CAMERA_2_QUALITY;
import static com.example.vasskob.mycamera.utils.CameraUtils.BACK_CAMERA_QUALITY;
import static com.example.vasskob.mycamera.utils.CameraUtils.BACK_VIDEO_QUALITY;
import static com.example.vasskob.mycamera.utils.CameraUtils.CAMERA_CATEGORY;
import static com.example.vasskob.mycamera.utils.CameraUtils.FRONT_CAMERA_QUALITY;
import static com.example.vasskob.mycamera.utils.CameraUtils.FRONT_VIDEO_QUALITY;
import static com.example.vasskob.mycamera.utils.CameraUtils.JPEG_COMPRESSION;


public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                if (index >= 0) {
                    String prefValue = listPreference.getEntries()[index].toString();
                    if (prefValue.contains("%")) {
                        prefValue = new StringBuilder(prefValue).insert(prefValue.indexOf("%"), "%").toString();
                    }
                    preference.setSummary(prefValue);
                } else preference.setSummary(null);
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    //    /**
//     * Binds a preference's summary to its value. More specifically, when the
//     * preference's value is changed, its summary (line of text below the
//     * preference title) is updated to reflect the value. The summary is also
//     * immediately updated upon calling this method. The exact display format is
//     * dependent on the type of preference.
//     *
//     * @see #sBindPreferenceSummaryToValueListener
//     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
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

//    @Override
//    public void onBackPressed() {
//        if (getFragmentManager().findFragmentById(com.android.internal.R.id.prefs) == null) {
//            super.onBackPressed();
//        } else finish();
//    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || AboutFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        public static final int RESOLUTION_COUNT = 5;
        private ListPreference backCamera2Pref;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
            //  if (fistLaunch()) {
            initCameraResolutionList();
//                PreferencesManager.setIsFirstLaunch(getActivity(), false);
//            }

            bindPreferenceSummaryToValue(findPreference(BACK_CAMERA_QUALITY));
            bindPreferenceSummaryToValue(findPreference(FRONT_CAMERA_QUALITY));
            bindPreferenceSummaryToValue(findPreference(BACK_VIDEO_QUALITY));
            bindPreferenceSummaryToValue(findPreference(FRONT_VIDEO_QUALITY));
            bindPreferenceSummaryToValue(findPreference(JPEG_COMPRESSION));

        }

        private boolean fistLaunch() {
            return PreferencesManager.isFirstLaunch(getActivity());
        }

        private void initCameraResolutionList() {
            PictureSizeLoader.PictureSizes pictureSizes = PictureSizeLoader.getPictureSizes();
            setBackCamera1Res(pictureSizes);
            setBackCamera2Res(pictureSizes);
            setFrontCameraRes(pictureSizes);
            setVideoFrontRes(pictureSizes);
            setVideoBackRes(pictureSizes);
            Log.d(TAG, "initCameraResolutionList: ");
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
            setCameraRes(videoBackSizes, backVideoPref);
        }

        private void setVideoFrontRes(PictureSizeLoader.PictureSizes pictureSizes) {
            List<PictureSize> videoFrontSizes = pictureSizes.getVideoQualitiesFront();
            ListPreference frontVideoPref = (ListPreference) findPreference(FRONT_VIDEO_QUALITY);
            setCameraRes(videoFrontSizes, frontVideoPref);
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
            Log.d(TAG, "setCameraRes: noDefaultValue");
            CharSequence[] entries;
            CharSequence[] entryValues;
            if (cameraSizes != null) {
                entries = new String[RESOLUTION_COUNT];
                entryValues = new String[RESOLUTION_COUNT];
                for (int i = 0; i < Math.min(cameraSizes.size(), RESOLUTION_COUNT); i++) {
                    String stringRatio = CameraUtils.getStringRatio(cameraSizes.get(i).aspectRatio());
                    entries[i] = stringRatio + "  " + cameraSizes.get(i).toString();
                    entryValues[i] = Integer.toString(cameraSizes.get(i).getWidth()) + "x" + Integer.toString(cameraSizes.get(i).getHeight());
                }
                cameraPrefs.setEntries(entries);
                cameraPrefs.setEntryValues(entryValues);
                if (noDefaultValue(cameraPrefs)) {
                    cameraPrefs.setDefaultValue(entryValues[0].toString());
                    cameraPrefs.setValue(entryValues[0].toString());
                    cameraPrefs.setSummary(entryValues[0].toString());
                }
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
