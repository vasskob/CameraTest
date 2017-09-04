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
import com.example.vasskob.mycamera.utils.PictureSizeLoader;
import com.example.vasskob.mycamera.utils.PreferencesManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


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
        CameraUtils.loadAvailableResolutions();
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

            bindPreferenceSummaryToValue(findPreference("back_camera1_quality"));
            bindPreferenceSummaryToValue(findPreference("front_camera_quality"));
            bindPreferenceSummaryToValue(findPreference("back_video_quality"));
            bindPreferenceSummaryToValue(findPreference("front_video_quality"));
            bindPreferenceSummaryToValue(findPreference("jpeg_compression"));

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
            List<Camera.Size> camera2Sizes = pictureSizes.getBackCamera2Sizes();
            if (camera2Sizes != null) {
                backCamera2Pref = new ListPreference(getActivity());
                backCamera2Pref.setKey("back_camera2_quality");
                backCamera2Pref.setTitle(getString(R.string.pref_title_back_camera2_photo_quality));
                backCamera2Pref.setDefaultValue("0");
                setCameraRes(camera2Sizes, backCamera2Pref);
                PreferenceCategory rootScreen = (PreferenceCategory) findPreference("camera_category");
                rootScreen.addPreference(backCamera2Pref);
                bindPreferenceSummaryToValue(findPreference("back_camera2_quality"));
            }
        }

        private void setVideoBackRes(PictureSizeLoader.PictureSizes pictureSizes) {
            List<Camera.Size> videoBackSizes = pictureSizes.getVideoQualitiesBack();
            ListPreference backVideoPref = (ListPreference) findPreference("back_video_quality");
            setCameraRes(videoBackSizes, backVideoPref);
        }

        private void setVideoFrontRes(PictureSizeLoader.PictureSizes pictureSizes) {
            List<Camera.Size> videoFrontSizes = pictureSizes.getVideoQualitiesFront();
            ListPreference frontVideoPref = (ListPreference) findPreference("front_video_quality");
            setCameraRes(videoFrontSizes, frontVideoPref);
        }

        private void setFrontCameraRes(PictureSizeLoader.PictureSizes pictureSizes) {
            List<Camera.Size> cameraFrontSizes = pictureSizes.getFrontCameraSizes();
            ListPreference frontCameraPref = (ListPreference) findPreference("front_camera_quality");
            setCameraRes(cameraFrontSizes, frontCameraPref);
        }

        private void setBackCamera1Res(PictureSizeLoader.PictureSizes pictureSizes) {
            List<Camera.Size> camera1Sizes = pictureSizes.getBackCamera1Sizes();
            ListPreference backCamera1Pref = (ListPreference) findPreference("back_camera1_quality");
            setCameraRes(camera1Sizes, backCamera1Pref);
        }

        private void setCameraRes(List<Camera.Size> cameraSizes, ListPreference cameraPrefs) {
            CharSequence[] entries;
            CharSequence[] entryValues;
            if (cameraSizes != null) {
                entries = new String[cameraSizes.size()];
                entryValues = new String[cameraSizes.size()];
                for (int i = 0; i < cameraSizes.size(); i++) {
                    entries[i] = cameraSizes.get(i).width + "x" + cameraSizes.get(i).height;
                    entryValues[i] = Integer.toString(cameraSizes.get(i).width + cameraSizes.get(i).height);
                }
                cameraPrefs.setEntries(entries);
                cameraPrefs.setEntryValues(entryValues);
            }
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
