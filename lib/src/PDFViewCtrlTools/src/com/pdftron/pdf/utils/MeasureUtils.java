package com.pdftron.pdf.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.Annot;
import com.pdftron.pdf.Point;
import com.pdftron.pdf.annots.Line;
import com.pdftron.pdf.model.AnnotStyle;
import com.pdftron.pdf.model.MeasureInfo;
import com.pdftron.pdf.model.RulerItem;
import com.pdftron.pdf.tools.RulerCreate;
import com.pdftron.sdf.Obj;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MeasureUtils {

    public static final String TAG = MeasureUtils.class.getName();

    // supported units
    public static final String U_PT = "pt";
    public static final String U_IN = "in";
    public static final String U_MM = "mm";
    public static final String U_CM = "cm";
    public static final String U_M = "m";
    public static final String U_KM = "km";
    public static final String U_FT = "ft";
    public static final String U_YD = "yd";
    public static final String U_MI = "mi";

    // Keys
    public static String K_Measure = "Measure";
    public static String K_IT = "IT";

    public static String K_LineDimension = "LineDimension";
    public static String K_PolyLineDimension = "PolyLineDimension";
    public static String K_PolygonDimension = "PolygonDimension";

    public static String K_scale = "scale";
    public static String K_axis = "axis";
    public static String K_distance = "distance";
    public static String K_area = "area";

    public static int PRECISION_DEFAULT = 100;

    public static String getDefaultMeasureInfo() {
        try {
            //ref: WV/HTML5/CoreControls/Tools.js
            Gson gson = new Gson();

            // axis
            MeasureInfo axis = new MeasureInfo();
            axis.setFactor(0.0138889);
            axis.setUnit(U_IN);
            axis.setDecimalSymbol(".");
            axis.setThousandSymbol(",");
            axis.setDisplay("D");
            axis.setPrecision(PRECISION_DEFAULT);
            axis.setUnitPrefix("");
            axis.setUnitSuffix("");
            axis.setUnitPosition("S");

            String axisJson = gson.toJson(axis);

            // distance
            MeasureInfo distance = new MeasureInfo();
            distance.setFactor(1);
            distance.setUnit(U_IN);
            distance.setDecimalSymbol(".");
            distance.setThousandSymbol(",");
            distance.setDisplay("D");
            distance.setPrecision(PRECISION_DEFAULT);
            distance.setUnitPrefix("");
            distance.setUnitSuffix("");
            distance.setUnitPosition("S");

            String distanceJson = gson.toJson(distance);

            // area
            MeasureInfo area = new MeasureInfo();
            area.setFactor(1);
            area.setUnit("sq in");
            area.setDecimalSymbol(".");
            area.setThousandSymbol(",");
            area.setDisplay("D");
            area.setPrecision(PRECISION_DEFAULT);
            area.setUnitPrefix("");
            area.setUnitSuffix("");
            area.setUnitPosition("S");

            String areaJson = gson.toJson(area);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(K_scale, "1 in = 1 in");
            jsonObject.put(K_axis, new JSONObject(axisJson));
            jsonObject.put(K_distance, new JSONObject(distanceJson));
            jsonObject.put(K_area, new JSONObject(areaJson));

            String measureInfo = jsonObject.toString();
            Log.d(TAG, "getDefaultMeasureInfo: " + measureInfo);

            return jsonObject.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    public static String getAnnotMeasureInfo(Annot annot) throws Exception {
        if (annot == null || !annot.isValid()) {
            return null;
        }
        int annotType = AnnotUtils.getAnnotType(annot);
        if (annotType != AnnotStyle.CUSTOM_ANNOT_TYPE_RULER &&
            annotType != AnnotStyle.CUSTOM_ANNOT_TYPE_PERIMETER_MEASURE &&
            annotType != AnnotStyle.CUSTOM_ANNOT_TYPE_AREA_MEASURE) {
            return null;
        }

        // load default
        String defaultMeasureInfo = getDefaultMeasureInfo();
        JSONObject measureJson = new JSONObject(defaultMeasureInfo);
        JSONObject axisJson = measureJson.getJSONObject(K_axis);
        JSONObject distanceJson = measureJson.getJSONObject(K_distance);
        JSONObject areaJson = measureJson.getJSONObject(K_area);

        MeasureInfo axisInfo = getFromJSON(axisJson.toString());
        MeasureInfo distanceInfo = getFromJSON(distanceJson.toString());
        MeasureInfo areaInfo = getFromJSON(areaJson.toString());

        if (axisInfo == null || distanceInfo == null || areaInfo == null) {
            return null;
        }

        // load from annot
        Obj obj = annot.getSDFObj();

        Obj measureObj = obj.findObj(K_Measure);
        if (measureObj != null) {
            Obj scaleObj = measureObj.findObj(getScaleKey());
            if (scaleObj != null && scaleObj.isString()) {
                measureJson.put(K_scale, scaleObj.getAsPDFText());
            }
            // axis
            Obj axisArray = measureObj.findObj(getAxisKey());
            if (axisArray != null && axisArray.isArray() && axisArray.size() > 0) {
                Obj axis = axisArray.getAt(0);
                if (axis != null && axis.isDict()) {
                    Obj factor = axis.findObj(getFactorKey());
                    if (factor != null && factor.isNumber()) {
                        axisInfo.setFactor(factor.getNumber());
                    }
                    Obj precision = axis.findObj(getPrecisionKey());
                    if (precision != null && precision.isNumber()) {
                        axisInfo.setPrecision((int) precision.getNumber());
                    }
                    Obj display = axis.findObj(getDistanceKey());
                    if (display != null && display.isName()) {
                        axisInfo.setDisplay(display.getName());
                    }
                    Obj decimalSymbol = axis.findObj(getDecimalSymbolKey());
                    if (decimalSymbol != null && decimalSymbol.isString()) {
                        axisInfo.setDecimalSymbol(decimalSymbol.getAsPDFText());
                    }
                    Obj thousandSymbol = axis.findObj(getThousandSymbolKey());
                    if (thousandSymbol != null && thousandSymbol.isString()) {
                        axisInfo.setThousandSymbol(thousandSymbol.getAsPDFText());
                    }
                    Obj unitSuffix = axis.findObj(getUnitSuffixKey());
                    if (unitSuffix != null && unitSuffix.isString()) {
                        axisInfo.setUnitSuffix(unitSuffix.getAsPDFText());
                    }
                    Obj unit = axis.findObj(getUnitKey());
                    if (unit != null && unit.isString()) {
                        axisInfo.setUnit(unit.getAsPDFText());
                    }
                    Obj unitPrefix = axis.findObj(getUnitPrefixKey());
                    if (unitPrefix != null && unitPrefix.isString()) {
                        axisInfo.setUnitPrefix(unitPrefix.getAsPDFText());
                    }
                    Obj unitPosition = axis.findObj(getUnitPositionKey());
                    if (unitPosition != null && unitPosition.isName()) {
                        axisInfo.setUnitPosition(unitPosition.getName());
                    }
                }
            }

            // distance
            Obj distanceArray = measureObj.findObj(getDistanceKey());
            if (distanceArray != null && distanceArray.isArray() && distanceArray.size() > 0) {
                Obj distance = distanceArray.getAt(0);
                if (distance != null && distance.isDict()) {
                    Obj factor = distance.findObj(getFactorKey());
                    if (factor != null && factor.isNumber()) {
                        distanceInfo.setFactor(factor.getNumber());
                    }
                    Obj precision = distance.findObj(getPrecisionKey());
                    if (precision != null && precision.isNumber()) {
                        distanceInfo.setPrecision((int) precision.getNumber());
                    }
                    Obj display = distance.findObj(getDistanceKey());
                    if (display != null && display.isName()) {
                        distanceInfo.setDisplay(display.getName());
                    }
                    Obj decimalSymbol = distance.findObj(getDecimalSymbolKey());
                    if (decimalSymbol != null && decimalSymbol.isString()) {
                        distanceInfo.setDecimalSymbol(decimalSymbol.getAsPDFText());
                    }
                    Obj thousandSymbol = distance.findObj(getThousandSymbolKey());
                    if (thousandSymbol != null && thousandSymbol.isString()) {
                        distanceInfo.setThousandSymbol(thousandSymbol.getAsPDFText());
                    }
                    Obj unitSuffix = distance.findObj(getUnitSuffixKey());
                    if (unitSuffix != null && unitSuffix.isString()) {
                        distanceInfo.setUnitSuffix(unitSuffix.getAsPDFText());
                    }
                    Obj unit = distance.findObj(getUnitKey());
                    if (unit != null && unit.isString()) {
                        distanceInfo.setUnit(unit.getAsPDFText());
                    }
                    Obj unitPrefix = distance.findObj(getUnitPrefixKey());
                    if (unitPrefix != null && unitPrefix.isString()) {
                        distanceInfo.setUnitPrefix(unitPrefix.getAsPDFText());
                    }
                    Obj unitPosition = distance.findObj(getUnitPositionKey());
                    if (unitPosition != null && unitPosition.isName()) {
                        distanceInfo.setUnitPosition(unitPosition.getName());
                    }
                }
            }

            // area
            Obj areaArray = measureObj.findObj(getAreaKey());
            if (areaArray != null && areaArray.isArray() && areaArray.size() > 0) {
                Obj area = areaArray.getAt(0);
                if (area != null && area.isDict()) {
                    Obj factor = area.findObj(getFactorKey());
                    if (factor != null && factor.isNumber()) {
                        areaInfo.setFactor(factor.getNumber());
                    }
                    Obj precision = area.findObj(getPrecisionKey());
                    if (precision != null && precision.isNumber()) {
                        areaInfo.setPrecision((int) precision.getNumber());
                    }
                    Obj display = area.findObj(getDistanceKey());
                    if (display != null && display.isName()) {
                        areaInfo.setDisplay(display.getName());
                    }
                    Obj decimalSymbol = area.findObj(getDecimalSymbolKey());
                    if (decimalSymbol != null && decimalSymbol.isString()) {
                        areaInfo.setDecimalSymbol(decimalSymbol.getAsPDFText());
                    }
                    Obj thousandSymbol = area.findObj(getThousandSymbolKey());
                    if (thousandSymbol != null && thousandSymbol.isString()) {
                        areaInfo.setThousandSymbol(thousandSymbol.getAsPDFText());
                    }
                    Obj unitSuffix = area.findObj(getUnitSuffixKey());
                    if (unitSuffix != null && unitSuffix.isString()) {
                        areaInfo.setUnitSuffix(unitSuffix.getAsPDFText());
                    }
                    Obj unit = area.findObj(getUnitKey());
                    if (unit != null && unit.isString()) {
                        areaInfo.setUnit(unit.getAsPDFText());
                    }
                    Obj unitPrefix = area.findObj(getUnitPrefixKey());
                    if (unitPrefix != null && unitPrefix.isString()) {
                        areaInfo.setUnitPrefix(unitPrefix.getAsPDFText());
                    }
                    Obj unitPosition = area.findObj(getUnitPositionKey());
                    if (unitPosition != null && unitPosition.isName()) {
                        areaInfo.setUnitPosition(unitPosition.getName());
                    }
                }
            }

            updateMeasureInfo(measureJson, K_axis, axisInfo);
            updateMeasureInfo(measureJson, K_distance, distanceInfo);
            updateMeasureInfo(measureJson, K_area, areaInfo);

            String measureInfo = measureJson.toString();
            Log.d(TAG, "getAnnotMeasureInfo: " + measureInfo);

            return measureJson.toString();
        }

        return null;
    }

    public static MeasureInfo getFromJSON(String json) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(json, MeasureInfo.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String getIT(Annot annot) throws PDFNetException {
        if (annot == null || !annot.isValid()) {
            return null;
        }
        Obj obj = annot.getSDFObj();
        Obj itObj = obj.findObj(K_IT);
        if (itObj != null && itObj.isName()) {
            return itObj.getName();
        }
        return null;
    }

    public static void putMeasurementInfo(Annot annot, String json) throws Exception {
        if (annot == null || !annot.isValid()) {
            return;
        }
        int annotType = annot.getType();
        if (annotType != Annot.e_Line &&
            annotType != Annot.e_Polyline &&
            annotType != Annot.e_Polygon) {
            return;
        }
        JSONObject measureJson = new JSONObject(json);
        String scale = measureJson.getString(K_scale);
        JSONObject axisJson = measureJson.getJSONObject(K_axis);
        JSONObject distanceJson = measureJson.getJSONObject(K_distance);
        JSONObject areaJson = measureJson.getJSONObject(K_area);

        MeasureInfo axisInfo = getFromJSON(axisJson.toString());
        MeasureInfo distanceInfo = getFromJSON(distanceJson.toString());
        MeasureInfo areaInfo = getFromJSON(areaJson.toString());

        if (axisInfo == null || distanceInfo == null || areaInfo == null) {
            return;
        }

        Obj obj = annot.getSDFObj();

        // IT
        String it = K_LineDimension;
        if (annotType == Annot.e_Polyline) {
            it = K_PolyLineDimension;
        } else if (annotType == Annot.e_Polygon) {
            it = K_PolygonDimension;
        }
        obj.putName(K_IT, it);

        // Measure
        Obj measureObj = obj.putDict(K_Measure);
        measureObj.putName(getTypeKey(), K_Measure);
        measureObj.putString(getScaleKey(), scale);

        Obj axisArray = measureObj.putArray(getAxisKey());
        Obj distanceArray = measureObj.putArray(getDistanceKey());
        Obj areaArray = measureObj.putArray(getAreaKey());

        Obj axis = axisArray.pushBackDict();
        axis.putNumber(getFactorKey(), axisInfo.getFactor());
        axis.putNumber(getPrecisionKey(), axisInfo.getPrecision());
        axis.putName(getDisplayKey(), axisInfo.getDisplay());
        axis.putString(getDecimalSymbolKey(), axisInfo.getDecimalSymbol());
        axis.putString(getThousandSymbolKey(), axisInfo.getThousandSymbol());
        axis.putString(getUnitSuffixKey(), axisInfo.getUnitSuffix());
        axis.putString(getUnitKey(), axisInfo.getUnit());
        axis.putString(getUnitPrefixKey(), axisInfo.getUnitPrefix());
        axis.putName(getUnitPositionKey(), axisInfo.getUnitPosition());

        Obj distance = distanceArray.pushBackDict();
        distance.putNumber(getFactorKey(), distanceInfo.getFactor());
        distance.putNumber(getPrecisionKey(), distanceInfo.getPrecision());
        distance.putName(getDisplayKey(), distanceInfo.getDisplay());
        distance.putString(getDecimalSymbolKey(), distanceInfo.getDecimalSymbol());
        distance.putString(getThousandSymbolKey(), distanceInfo.getThousandSymbol());
        distance.putString(getUnitSuffixKey(), distanceInfo.getUnitSuffix());
        distance.putString(getUnitKey(), distanceInfo.getUnit());
        distance.putString(getUnitPrefixKey(), distanceInfo.getUnitPrefix());
        distance.putName(getUnitPositionKey(), distanceInfo.getUnitPosition());

        Obj area = areaArray.pushBackDict();
        area.putNumber(getFactorKey(), areaInfo.getFactor());
        area.putNumber(getPrecisionKey(), areaInfo.getPrecision());
        area.putName(getDisplayKey(), areaInfo.getDisplay());
        area.putString(getDecimalSymbolKey(), areaInfo.getDecimalSymbol());
        area.putString(getThousandSymbolKey(), areaInfo.getThousandSymbol());
        area.putString(getUnitSuffixKey(), areaInfo.getUnitSuffix());
        area.putString(getUnitKey(), areaInfo.getUnit());
        area.putString(getUnitPrefixKey(), areaInfo.getUnitPrefix());
        area.putName(getUnitPositionKey(), areaInfo.getUnitPosition());
    }


    public static String getTypeKey() {
        return "Type";
    }

    public static String getScaleKey() {
        return "R";
    }

    public static String getAxisKey() {
        return "X";
    }

    public static String getDistanceKey() {
        return "D";
    }

    public static String getAreaKey() {
        return "A";
    }

    public static String getUnitKey() {
        return "U";
    }

    public static String getFactorKey() {
        return "C";
    }

    public static String getDecimalSymbolKey() {
        return "RD";
    }

    public static String getThousandSymbolKey() {
        return "RT";
    }

    public static String getPrecisionKey() {
        return "D";
    }

    public static String getDisplayKey() {
        return "F";
    }

    public static String getUnitPrefixKey() {
        return "PS";
    }

    public static String getUnitSuffixKey() {
        return "SS";
    }

    public static String getUnitPositionKey() {
        return "O";
    }

    public static String getMeasurementText(double value, MeasureInfo numberFormat) {
        String unit = numberFormat.getUnit();

        return modifyLastUnitValue(value, numberFormat) + " " + unit;
    }

    public static String modifyLastUnitValue(double value, MeasureInfo numberFormat) {
        String integerPart = "", fractionalPart = "";
        int precision;
        String display = numberFormat.getDisplay();

        if (display.equals("D")) { // decimal
            precision = numberFormat.getPrecision();
            if (precision % 10 != 0) {
                Log.w(TAG, "precision for decimal display must be a multiple of 10");
            }

            int scale = String.valueOf((precision / 10)).length();

            double result = BigDecimal.valueOf(value)
                .setScale(scale, BigDecimal.ROUND_HALF_UP)
                .doubleValue();

            String[] temp = String.valueOf(result).split("\\.");
            integerPart = temp[0];
            fractionalPart = precision == 1 ? "" : numberFormat.getDecimalSymbol() + temp[1];
        } else if (display.equals("F")) { // fractional
            integerPart = trunc(value);

            precision = numberFormat.getPrecision();
            fractionalPart = " " + Math.round((value % 1) * precision) + "/" + precision;
        } else if (display.equals("R")) { // round
            integerPart = String.valueOf(Math.round(value));
            fractionalPart = "";
        } else if (display.equals("T")) { // truncate
            integerPart = trunc(value);
            fractionalPart = "";
        }

        return addThousandsSymbol(integerPart, numberFormat.getThousandSymbol()) + fractionalPart;
    }

    private static String trunc(double value) {
        String[] temp = String.valueOf(value).split("\\.");
        return temp[0];
    }

    public static String addThousandsSymbol(String value, String thousandsSymbol) {
        for (int i = value.length() - 3; i > 0; i -= 3) {
            value = value.substring(0, i) + thousandsSymbol + value.substring(i);
        }

        return value;
    }

    public static HashMap<String, Integer> getPrecisions() {
        HashMap<String, Integer> precisions = new HashMap<>(5);
        precisions.put("1", 1);
        precisions.put("0.1", 10);
        precisions.put("0.01", 100);
        precisions.put("0.001", 1000);
        precisions.put("0.0001", 10000);
        return precisions;
    }

    public static int getPrecision(String which) {
        HashMap<String, Integer> precisions = getPrecisions();
        if (precisions.get(which) != null) {
            return precisions.get(which);
        }
        return PRECISION_DEFAULT;
    }

    public static HashMap<String, Double> getUnitConversion() {
        // the base unit is cm
        HashMap<String, Double> unitConversion = new HashMap<>(9);
        unitConversion.put(U_MM, 0.1);
        unitConversion.put(U_CM, 1.0);
        unitConversion.put(U_M, 100.0);
        unitConversion.put(U_KM, 100000.0);
        unitConversion.put(U_MI, 160394.0);
        unitConversion.put(U_YD, 91.44);
        unitConversion.put(U_FT, 30.48);
        unitConversion.put(U_IN, 2.54);
        unitConversion.put(U_PT, 0.0352778);
        return unitConversion;
    }

    public static double getUnitConversion(String unit) {
        HashMap<String, Double> conversion = getUnitConversion();
        if (conversion.get(unit) != null) {
            return conversion.get(unit);
        }
        return 1.0;
    }

    public static void updateMeasureInfo(@NonNull JSONObject jsonObject, String key, MeasureInfo info) throws Exception {
        if (key == null || info == null) {
            return;
        }
        Gson gson = new Gson();
        String jsonStr = gson.toJson(info);
        jsonObject.put(key, new JSONObject(jsonStr));
    }

    public static String setScaleAndPrecision(int annotType, @NonNull JSONObject jsonObject,
                                  @NonNull RulerItem rulerItem) {
        try {
            // scale
            StringBuilder builder = new StringBuilder();
            builder.append(rulerItem.mRulerBase);
            builder.append(" ");
            builder.append(rulerItem.mRulerBaseUnit);
            builder.append(" = ");
            builder.append(rulerItem.mRulerTranslate);
            builder.append(" ");
            builder.append(rulerItem.mRulerTranslateUnit);
            Log.d(TAG, "setScale: " + builder.toString());
            jsonObject.put(K_scale, builder.toString());
            // unit
            MeasureInfo info = getMeasureInfo(annotType, jsonObject);
            if (info != null) {
                String unit = rulerItem.mRulerTranslateUnit;
                if (annotType == AnnotStyle.CUSTOM_ANNOT_TYPE_AREA_MEASURE) {
                    unit = "sq " + rulerItem.mRulerTranslateUnit;
                }
                info.setUnit(unit);
                info.setPrecision(rulerItem.mPrecision);
                String key = getMeasureKey(annotType);
                updateMeasureInfo(jsonObject, key, info);
            }
            // factor
            MeasureInfo axisInfo = getAxisInfo(jsonObject);
            if (axisInfo != null) {
                axisInfo.setFactor((rulerItem.mRulerTranslate / rulerItem.mRulerBase) * (getUnitConversion(U_PT) / getUnitConversion(rulerItem.mRulerBaseUnit)));
                updateMeasureInfo(jsonObject, K_axis, axisInfo);
            }

            Log.d(TAG, "setScale final: " + jsonObject.toString());
            return jsonObject.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static RulerItem calibrate(Annot annot, RulerItem rulerItem, float userInput) throws Exception {
        if (annot == null || !annot.isValid() || annot.getType() != Annot.e_Line) {
            return null;
        }
        Line line = new Line(annot);
        Point pt1 = line.getStartPoint();
        Point pt2 = line.getEndPoint();
        double lineLength = getLineLength(pt1.x, pt1.y, pt2.x, pt2.y);
        MeasureImpl measure = new MeasureImpl(AnnotUtils.getAnnotType(annot));
        if (measure.getMeasure() != null) {
            double axisFactor = userInput / lineLength / measure.getMeasure().getFactor();
            double rulerTranslate = axisFactor / (getUnitConversion(U_PT) / getUnitConversion(rulerItem.mRulerBaseUnit)) * rulerItem.mRulerBase;
            rulerItem.mRulerTranslate = (float) rulerTranslate;

            // update annot
            RulerCreate.adjustContents(line, rulerItem, line.getStartPoint().x, line.getStartPoint().y,
                    line.getEndPoint().x, line.getEndPoint().y);
            return rulerItem;
        }
        return null;
    }

    public static double getLineLength(double pt1x, double pt1y, double pt2x, double pt2y) {
        return Math.sqrt(Math.pow((pt2x - pt1x), 2) + Math.pow((pt2y - pt1y), 2));
    }

    public static RulerItem getRulerItemFromAnnot(Annot annot) {
        try {
            String measureInfo = getAnnotMeasureInfo(annot);
            if (measureInfo != null) {
                JSONObject jsonObject = new JSONObject(measureInfo);
                RulerItem item = getScale(jsonObject);
                if (item != null) {
                    item.mPrecision = getPrecision(AnnotUtils.getAnnotType(annot), jsonObject);
                    Log.d(TAG, "getRulerItemFromAnnot: " + item.toString());
                    return item;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Converts FDF to RulerItem
     */
    @Nullable
    public static RulerItem getScale(@NonNull JSONObject jsonObject) {
        String scale = safeGetString(jsonObject, K_scale);
        if (scale != null) {
            Pattern p = Pattern.compile("(\\d*(?:.\\d+)?\\s\\w+)\\s=\\s(\\d*(?:.\\d+)?\\s\\w+)");
            Matcher m = p.matcher(scale);
            while (m.find()) {
                // i.e. 1.5 cm = 2 m
                RulerItem item = new RulerItem();
                String found = m.group();
                // since android regex doesn't seem to match universal regex, here we will manually split
                String[] parts = found.split("=");
                if (parts.length == 2) {
                    String[] g1 = parts[0].trim().split(" ");
                    String[] g2 = parts[1].trim().split(" ");
                    item.mRulerBase = Float.parseFloat(g1[0]);
                    item.mRulerBaseUnit = g1[1];
                    item.mRulerTranslate = Float.parseFloat(g2[0]);
                    item.mRulerTranslateUnit = g2[1];
                    Log.d(TAG, "getScale:" + item.toString());
                    return item;
                }
            }
        }
        return null;
    }

    public static int getPrecision(int annotType, @NonNull JSONObject jsonObject) {
        MeasureInfo measureInfo = getMeasureInfo(annotType, jsonObject);
        if (measureInfo != null) {
            return measureInfo.getPrecision();
        }
        return PRECISION_DEFAULT;
    }

    public static MeasureInfo getAxisInfo(@NonNull JSONObject jsonObject) {
        JSONObject result = safeGetJSON(jsonObject, K_axis);
        if (result != null) {
            return getFromJSON(result.toString());
        }
        return null;
    }

    public static String getMeasureKey(int annotType) {
        if (annotType == AnnotStyle.CUSTOM_ANNOT_TYPE_RULER ||
            annotType == AnnotStyle.CUSTOM_ANNOT_TYPE_PERIMETER_MEASURE) {
            return K_distance;
        } else if (annotType == AnnotStyle.CUSTOM_ANNOT_TYPE_AREA_MEASURE) {
            return K_area;
        }
        return null;
    }

    public static MeasureInfo getMeasureInfo(int annotType, @NonNull JSONObject jsonObject) {
        JSONObject result = getMeasureJSON(annotType, jsonObject);
        if (result != null) {
            return getFromJSON(result.toString());
        }
        return null;
    }

    @Nullable
    public static JSONObject getMeasureJSON(int annotType, @NonNull JSONObject jsonObject) {
        if (annotType == AnnotStyle.CUSTOM_ANNOT_TYPE_RULER ||
            annotType == AnnotStyle.CUSTOM_ANNOT_TYPE_PERIMETER_MEASURE) {
            return safeGetJSON(jsonObject, K_distance);
        } else if (annotType == AnnotStyle.CUSTOM_ANNOT_TYPE_AREA_MEASURE) {
            return safeGetJSON(jsonObject, K_area);
        }
        return null;
    }

    @Nullable
    public static String safeGetString(JSONObject json, String key) {
        try {
            if (json.has(key)) {
                return json.getString(key);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static int safeGetInt(JSONObject json, String key, int defaultValue) {
        try {
            if (json.has(key)) {
                return json.getInt(key);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return defaultValue;
    }

    @Nullable
    public static JSONObject safeGetJSON(JSONObject json, String key) {
        try {
            if (json.has(key)) {
                return json.getJSONObject(key);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
