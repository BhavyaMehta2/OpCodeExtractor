package com.app.intentextractor;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Model {

    Context context1;

    public Model(Context context2) {
        context1 = context2;
    }

    public String main(List<String> intentList, String header) throws IOException {
        List<Integer> inputData = listToMap(intentList);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\n\t\"type\" : \"intent\",\n\t\"model\" : \"" + header + "\",\n\t\"attributes\" :" + inputData + "\n\n}", mediaType);
        Request request = new Request.Builder()
                .url("https://mr.eninehq.com:5000/predict")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("auth", "tha9ahqu5aivelahdahhaedeihahsimab7thoo6mi4shaeCeituN9AeB0AivieF0RieThiongiexueGohNgieK5shu9ahw8aiqui")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            JSONObject json = new JSONObject(response.body().string());

            Log.e("JSON", String.valueOf(json));

            return (json.getString("prediction").equalsIgnoreCase("0")) ? "Benign" : "Malicious";
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    List<Integer> listToMap(List<String> intentList)
    {
        LinkedHashMap<String, Integer> updatedMap = new LinkedHashMap<>();
        List<String> order = Arrays.asList("android.intent.action.AIRPLANE_MODE","android.intent.action.ALL_APPS","android.intent.action.ANSWER","android.intent.action.APP_ERROR","android.intent.action.APPLICATION_PREFERENCES","android.intent.action.APPLICATION_RESTRICTIONS_CHANGED","android.intent.action.ASSIST","android.intent.action.ATTACH_DATA","android.intent.action.BATTERY_CHANGED","android.intent.action.BATTERY_LOW","android.intent.action.BATTERY_OKAY","android.intent.action.BOOT_COMPLETED","android.intent.action.BUG_REPORT","android.intent.action.CALL","android.intent.action.CALL_BUTTON","android.intent.action.CAMERA_BUTTON","android.intent.action.CARRIER_SETUP","android.intent.action.CHOOSER","android.intent.action.CLOSE_SYSTEM_DIALOGS","android.intent.action.CONFIGURATION_CHANGED","android.intent.action.CREATE_DOCUMENT","android.intent.action.CREATE_SHORTCUT","android.intent.action.DATE_CHANGED","android.intent.action.VIEW","android.intent.action.DEFINE","android.intent.action.DELETE","android.intent.action.DEVICE_STORAGE_LOW","android.intent.action.DEVICE_STORAGE_OK","android.intent.action.DIAL","android.intent.action.DOCK_EVENT","android.intent.action.DREAMING_STARTED","android.intent.action.DREAMING_STOPPED","android.intent.action.EDIT","android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE","android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE","android.intent.action.FACTORY_TEST","android.intent.action.GET_CONTENT","android.intent.action.GET_RESTRICTION_ENTRIES","android.intent.action.GTALK_CONNECTED","android.intent.action.GTALK_DISCONNECTED","android.intent.action.HEADSET_PLUG","android.intent.action.INPUT_METHOD_CHANGED","android.intent.action.INSERT","android.intent.action.INSERT_OR_EDIT","android.intent.action.INSTALL_FAILURE","android.intent.action.INSTALL_PACKAGE","android.intent.action.LOCALE_CHANGED","android.intent.action.LOCKED_BOOT_COMPLETED","android.intent.action.MAIN","android.intent.action.MANAGE_NETWORK_USAGE","android.intent.action.MANAGE_PACKAGE_STORAGE","android.intent.action.MANAGED_PROFILE_ADDED","android.intent.action.MANAGED_PROFILE_AVAILABLE","android.intent.action.MANAGED_PROFILE_REMOVED","android.intent.action.MANAGED_PROFILE_UNAVAILABLE","android.intent.action.MANAGED_PROFILE_UNLOCKED","android.intent.action.MEDIA_BAD_REMOVAL","android.intent.action.MEDIA_BUTTON","android.intent.action.MEDIA_CHECKING","android.intent.action.MEDIA_EJECT","android.intent.action.MEDIA_MOUNTED","android.intent.action.MEDIA_NOFS","android.intent.action.MEDIA_REMOVED","android.intent.action.MEDIA_SCANNER_FINISHED","android.intent.action.MEDIA_SCANNER_SCAN_FILE","android.intent.action.MEDIA_SCANNER_STARTED","android.intent.action.MEDIA_SHARED","android.intent.action.MEDIA_UNMOUNTABLE","android.intent.action.MEDIA_UNMOUNTED","android.intent.action.MY_PACKAGE_REPLACED","android.intent.action.MY_PACKAGE_SUSPENDED","android.intent.action.MY_PACKAGE_UNSUSPENDED","android.intent.action.NEW_OUTGOING_CALL","android.intent.action.OPEN_DOCUMENT","android.intent.action.OPEN_DOCUMENT_TREE","android.intent.action.PACKAGE_ADDED","android.intent.action.PACKAGE_CHANGED","android.intent.action.PACKAGE_DATA_CLEARED","android.intent.action.PACKAGE_FIRST_LAUNCH","android.intent.action.PACKAGE_FULLY_REMOVED","android.intent.action.PACKAGE_INSTALL","android.intent.action.PACKAGE_NEEDS_VERIFICATION","android.intent.action.PACKAGE_REMOVED","android.intent.action.PACKAGE_REPLACED","android.intent.action.PACKAGE_RESTARTED","android.intent.action.PACKAGE_VERIFIED","android.intent.action.PACKAGES_SUSPENDED","android.intent.action.PACKAGES_UNSUSPENDED","android.intent.action.PASTE","android.intent.action.PICK","android.intent.action.PICK_ACTIVITY","android.intent.action.ACTION_POWER_CONNECTED","android.intent.action.ACTION_POWER_DISCONNECTED","android.intent.action.POWER_USAGE_SUMMARY","android.intent.action.PROCESS_TEXT","android.intent.action.PROVIDER_CHANGED","android.intent.action.QUICK_CLOCK","android.intent.action.QUICK_VIEW","android.intent.action.REBOOT","android.intent.action.RUN","android.intent.action.SCREEN_OFF","android.intent.action.SCREEN_ON","android.intent.action.SEARCH","android.intent.action.SEARCH_LONG_PRESS","android.intent.action.SEND","android.intent.action.SEND_MULTIPLE","android.intent.action.SENDTO","android.intent.action.SET_WALLPAPER","android.intent.action.SHOW_APP_INFO","android.intent.action.ACTION_SHUTDOWN","android.intent.action.SYNC","android.intent.action.SYSTEM_TUTORIAL","android.intent.action.TIME_SET","android.intent.action.TIME_TICK","android.intent.action.TIMEZONE_CHANGED","android.intent.action.TRANSLATE","android.intent.action.UID_REMOVED","android.intent.action.UMS_CONNECTED","android.intent.action.UMS_DISCONNECTED","android.intent.action.UNINSTALL_PACKAGE","android.intent.action.USER_BACKGROUND","android.intent.action.USER_FOREGROUND","android.intent.action.USER_INITIALIZE","android.intent.action.USER_PRESENT","android.intent.action.USER_UNLOCKED","android.intent.action.VIEW_LOCUS","android.intent.action.VIEW_PERMISSION_USAGE","android.intent.action.VOICE_COMMAND","android.intent.action.WALLPAPER_CHANGED","android.intent.action.WEB_SEARCH","android.intent.category.ALTERNATIVE","android.intent.category.APP_BROWSER","android.intent.category.APP_CALCULATOR","android.intent.category.APP_CALENDAR","android.intent.category.APP_CONTACTS","android.intent.category.APP_EMAIL","android.intent.category.APP_FILES","android.intent.category.APP_GALLERY","android.intent.category.APP_MAPS","android.intent.category.APP_MARKET","android.intent.category.APP_MESSAGING","android.intent.category.APP_MUSIC","android.intent.category.BROWSABLE","android.intent.category.CAR_DOCK","android.intent.category.CAR_MODE","android.intent.category.DEFAULT","android.intent.category.DESK_DOCK","android.intent.category.DEVELOPMENT_PREFERENCE","android.intent.category.EMBED","android.intent.category.FRAMEWORK_INSTRUMENTATION_TEST","CATEGORY_GADGET","android.intent.category.HE_DESK_DOCK","android.intent.category.HOME","android.intent.category.INFO","android.intent.category.LAUNCHER","android.intent.category.LE_DESK_DOCK","android.intent.category.LEANBACK_LAUNCHER","android.intent.category.MONKEY","android.intent.category.OPENABLE","android.intent.category.PREFERENCE","android.intent.category.SAMPLE_CODE","android.intent.category.SECONDARY_HOME","android.intent.category.SELECTED_ALTERNATIVE","android.intent.category.TAB","android.intent.category.TEST","android.intent.category.TYPED_OPENABLE","android.intent.category.UNIT_TEST","android.intent.category.VOICE","android.intent.category.VR_HOME","CREATOR","android.intent.extra.ALARM_COUNT","android.intent.extra.ALLOW_MULTIPLE","android.intent.extra.ALLOW_REPLACE","android.intent.extra.ALTERNATE_INTENTS","android.intent.extra.ASSIST_CONTEXT","android.intent.extra.ASSIST_INPUT_DEVICE_ID","android.intent.extra.ASSIST_INPUT_HINT_KEYBOARD","android.intent.extra.ASSIST_PACKAGE","android.intent.extra.ASSIST_UID","android.intent.extra.AUTO_LAUNCH_SINGLE_CHOICE","android.intent.extra.BCC","android.intent.extra.BUG_REPORT","android.intent.extra.CC","android.intent.extra.changed_component_name","android.intent.extra.changed_component_name_list","android.intent.extra.changed_package_list","android.intent.extra.changed_uid_list","android.intent.extra.CHOOSER_REFINEMENT_INTENT_SENDER","android.intent.extra.CHOOSER_TARGETS","android.intent.extra.CHOSEN_COMPONENT","android.intent.extra.CHOSEN_COMPONENT_INTENT_SENDER","android.intent.extra.COMPONENT_NAME","android.intent.extra.CONTENT_ANNOTATIONS","android.intent.extra.CONTENT_QUERY","android.intent.extra.DATA_REMOVED","android.intent.extra.DOCK_STATE","android.intent.extra.DONT_KILL_APP","android.intent.extra.DURATION_MILLIS","android.intent.extra.EMAIL","android.intent.extra.EXCLUDE_COMPONENTS","android.intent.extra.FROM_STORAGE","android.intent.extra.HTML_TEXT","android.intent.extra.INDEX","android.intent.extra.INITIAL_INTENTS","android.intent.extra.INSTALLER_PACKAGE_NAME","android.intent.extra.INTENT","android.intent.extra.KEY_EVENT","android.intent.extra.LOCAL_ONLY","android.intent.extra.LOCUS_ID","android.intent.extra.MIME_TYPES","android.intent.extra.NOT_UNKNOWN_SOURCE","android.intent.extra.ORIGINATING_URI","android.intent.extra.PACKAGE_NAME","android.intent.extra.PHONE_NUMBER","android.intent.extra.PROCESS_TEXT","android.intent.extra.PROCESS_TEXT_READONLY","android.intent.extra.QUICK_VIEW_FEATURES","android.intent.extra.QUIET_MODE","android.intent.extra.REFERRER","android.intent.extra.REFERRER_NAME","android.intent.extra.remote_intent_token","android.intent.extra.REPLACEMENT_EXTRAS","android.intent.extra.REPLACING","android.intent.extra.restrictions_bundle","android.intent.extra.restrictions_intent","android.intent.extra.restrictions_list","android.intent.extra.RESULT_RECEIVER","android.intent.extra.RETURN_RESULT","android.intent.extra.shortcut.ICON","android.intent.extra.shortcut.ICON_RESOURCE","android.intent.extra.shortcut.ID","android.intent.extra.shortcut.INTENT","android.intent.extra.shortcut.NAME","android.intent.extra.SHUTDOWN_USERSPACE_ONLY","android.intent.extra.SPLIT_NAME","android.intent.extra.STREAM","android.intent.extra.SUBJECT","android.intent.extra.SUSPENDED_PACKAGE_EXTRAS","android.intent.extra.TEMPLATE","android.intent.extra.TEXT","android.intent.extra.TITLE","android.intent.extra.UID","android.intent.extra.USER","android.dock_home");
        for(String s:order)
            updatedMap.put(s,0);

        for(String p: intentList)
        {
            if(updatedMap.containsKey(p))
            {
                updatedMap.put(p, 1);
            }
        }

        Log.d("TAG", String.valueOf(updatedMap));
        return new ArrayList<>(updatedMap.values());
    }
}