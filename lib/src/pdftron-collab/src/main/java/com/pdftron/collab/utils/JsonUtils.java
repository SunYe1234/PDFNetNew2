package com.pdftron.collab.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.collab.db.entity.AnnotationEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class JsonUtils {

    @Nullable
    public static AnnotationEntity parseRetrieveMessage(
            @NonNull JSONObject annotJson,
            @NonNull String docId) {
        if (JsonUtils.isValidInsertEntity(annotJson)) {
            AnnotationEntity entity = XfdfUtils.parseAnnotationEntity(docId, annotJson);
            if (entity != null) {
                if (JsonUtils.isValidInsertEntity(entity)) {
                    return entity;
                }
            }
        }
        return null;
    }

    public static boolean isValidInsertEntity(@NonNull AnnotationEntity entity) {
        return entity.getId() != null &&
                entity.getDocumentId() != null &&
                entity.getAuthorId() != null &&
                entity.getXfdf() != null &&
                entity.getAt() != null &&
                entity.getCreationDate() != null &&
                entity.getDate() != null;
    }

    /**
     * Check against whether the json passed in is a valid annotation
     */
    public static boolean isValidInsertEntity(@NonNull JSONObject annotJson) {
        Iterator<String> iterator = annotJson.keys();
        ArrayList<String> keys = new ArrayList<>();
        while (iterator.hasNext()) {
            String key = iterator.next();
            keys.add(key);
        }
        return keys.contains(Keys.ANNOT_ID) &&
                keys.contains(Keys.ANNOT_AUTHOR_ID) &&
                keys.contains(Keys.ANNOT_XFDF) &&
                keys.contains(Keys.ANNOT_ACTION);
    }

    public static boolean isValidUpdateEntity(@NonNull JSONObject annotJson) {
        Iterator<String> iterator = annotJson.keys();
        ArrayList<String> keys = new ArrayList<>();
        while (iterator.hasNext()) {
            String key = iterator.next();
            keys.add(key);
        }
        return keys.contains(Keys.ANNOT_ID) &&
                keys.contains(Keys.ANNOT_XFDF) &&
                keys.contains(Keys.ANNOT_ACTION);
    }

    public static boolean isValidDeleteEntity(@NonNull JSONObject annotJson) {
        Iterator<String> iterator = annotJson.keys();
        ArrayList<String> keys = new ArrayList<>();
        while (iterator.hasNext()) {
            String key = iterator.next();
            keys.add(key);
        }
        return keys.contains(Keys.ANNOT_ID);
    }

    @NonNull
    public static JSONArray addItemToArrayImpl(String jsonString, String item) throws JSONException {
        JSONArray json;
        if (null == jsonString) {
            json = new JSONArray();
        } else {
            json = new JSONArray(jsonString);
        }
        json.put(item);
        return json;
    }

    @NonNull
    public static String addItemToArray(String jsonString, String item) throws JSONException {
        return addItemToArrayImpl(jsonString, item).toString();
    }

    @Nullable
    public static JSONArray removeItemFromArrayImpl(@NonNull String jsonString, @NonNull String item) throws JSONException {
        JSONArray json = new JSONArray(jsonString);
        return safeRemoveFirstByValue(json, item);
    }

    @Nullable
    public static String removeItemFromArray(@NonNull String jsonString, @NonNull String item) throws JSONException {
        JSONArray out = removeItemFromArrayImpl(jsonString, item);
        return out != null ? out.toString() : null;
    }

    @Nullable
    public static String removeAllItemFromArray(@NonNull String jsonString, @NonNull String item) throws JSONException {
        JSONArray json = new JSONArray(jsonString);
        JSONArray out = safeRemoveByValue(json, item);
        return out != null ? out.toString() : null;
    }

    @Nullable
    public static JSONArray safeRemoveByValue(@NonNull JSONArray jsonArray, @NonNull String item) {
        try {
            JSONArray output = new JSONArray();
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                String it = jsonArray.getString(i);
                // excluding all the item
                if (!it.equals(item)) {
                    output.put(it);
                }
            }
            return output;
        } catch (Exception ex) {
            return null;
        }
    }

    @Nullable
    public static JSONArray safeRemoveFirstByValue(@NonNull JSONArray jsonArray, @NonNull String item) {
        try {
            boolean foundFirst = false;
            JSONArray output = new JSONArray();
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                // excluding only the first instance
                String it = jsonArray.getString(i);
                if (!it.equals(item) || foundFirst) {
                    output.put(it);
                } else if (it.equals(item)) {
                    foundFirst = true;
                }
            }
            return output;
        } catch (Exception ex) {
            return null;
        }
    }

    public static int safeGetJsonArrayLength(@NonNull String jsonString) {
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            return jsonArray.length();
        } catch (Exception ex) {
            return 0;
        }
    }
}
