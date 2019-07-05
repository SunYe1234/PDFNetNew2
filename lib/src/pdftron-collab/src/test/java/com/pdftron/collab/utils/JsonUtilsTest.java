package com.pdftron.collab.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class JsonUtilsTest {

    @Test
    public void addItemToArray() throws JSONException {
        // case 1: not empty
        // expected
        JSONArray array = new JSONArray();
        array.put("item1");
        array.put("item2");

        // actual
        JSONArray array2 = new JSONArray();
        array2.put("item1");
        String result = JsonUtils.addItemToArray(array2.toString(), "item2");
        JSONArray resultArray = new JSONArray(result);

        JSONAssert.assertEquals(array, resultArray, false);

        // case 2: empty
        array = new JSONArray();
        array.put("item1");

        // actual
        array2 = new JSONArray();
        result = JsonUtils.addItemToArray(array2.toString(), "item1");
        resultArray = new JSONArray(result);

        JSONAssert.assertEquals(array, resultArray, false);
    }

    @Test
    public void removeItemFromArray() throws JSONException {
        // expected
        JSONArray array = new JSONArray();
        array.put("item1");
        array.put("item2");
        array.put("item2");
        array.put("item3");
        array.put("item3");

        // actual
        JSONArray array2 = new JSONArray();
        array2.put("item1");
        array2.put("item2");
        array2.put("item2");
        array2.put("item2");
        array2.put("item3");
        array2.put("item3");

        String result = JsonUtils.removeItemFromArray(array2.toString(), "item2");
        JSONArray resultArray = new JSONArray(result);

        JSONAssert.assertEquals(array, resultArray, false);
    }

    @Test
    public void removeAllItemFromArray() throws JSONException {
        // expected
        JSONArray array = new JSONArray();
        array.put("item1");
        array.put("item3");
        array.put("item3");

        // actual
        JSONArray array2 = new JSONArray();
        array2.put("item1");
        array2.put("item2");
        array2.put("item2");
        array2.put("item2");
        array2.put("item3");
        array2.put("item3");

        String result = JsonUtils.removeAllItemFromArray(array2.toString(), "item2");
        JSONArray resultArray = new JSONArray(result);

        JSONAssert.assertEquals(array, resultArray, false);
    }
}
