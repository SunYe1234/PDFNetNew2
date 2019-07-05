package com.pdftron.pdf.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.pdftron.pdf.Annot;
import com.pdftron.pdf.config.ToolStyleConfig;
import com.pdftron.pdf.model.AnnotStyle;
import com.pdftron.pdf.model.MeasureInfo;
import com.pdftron.pdf.model.RulerItem;
import com.pdftron.pdf.tools.Tool;

import org.json.JSONObject;

public class MeasureImpl {

    private JSONObject mMeasureFDF;

    private int mAnnotType;

    public MeasureImpl(int annotType) {
        mAnnotType = annotType;
        String json = MeasureUtils.getDefaultMeasureInfo();
        initMeasureFDF(json);
    }

    private void initMeasureFDF(String json) {
        if (json != null) {
            try {
                mMeasureFDF = new JSONObject(json);
            } catch (Exception ex) {
                AnalyticsHandlerAdapter.getInstance().sendException(ex);
            }
        }
    }

    public void updateRulerItem(RulerItem rulerItem) {
        if (mMeasureFDF != null) {
            String result = MeasureUtils.setScaleAndPrecision(mAnnotType, mMeasureFDF, rulerItem);
            initMeasureFDF(result);
        }
    }

    public void setupAnnotProperty(Context context, AnnotStyle annotStyle) {
        RulerItem rulerItem = new RulerItem(
                annotStyle.getRulerBaseValue(),
                annotStyle.getRulerBaseUnit(),
                annotStyle.getRulerTranslateValue(),
                annotStyle.getRulerTranslateUnit(),
                annotStyle.getPrecision());

        updateRulerItem(rulerItem);

        SharedPreferences settings = Tool.getToolPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(ToolStyleConfig.getInstance().getRulerBaseUnitKey(mAnnotType, ""), rulerItem.mRulerBaseUnit);
        editor.putString(ToolStyleConfig.getInstance().getRulerTranslateUnitKey(mAnnotType, ""), rulerItem.mRulerTranslateUnit);
        editor.putFloat(ToolStyleConfig.getInstance().getRulerBaseValueKey(mAnnotType, ""), rulerItem.mRulerBase);
        editor.putFloat(ToolStyleConfig.getInstance().getRulerTranslateValueKey(mAnnotType, ""), rulerItem.mRulerTranslate);
        editor.putInt(ToolStyleConfig.getInstance().getRulerPrecisionKey(mAnnotType, ""), rulerItem.mPrecision);
        editor.apply();
    }

    public void handleDown(Context context) {
        SharedPreferences settings = Tool.getToolPreferences(context);

        RulerItem rulerItem = new RulerItem();
        rulerItem.mRulerBaseUnit = settings.getString(ToolStyleConfig.getInstance().getRulerBaseUnitKey(mAnnotType, ""),
            ToolStyleConfig.getInstance().getDefaultRulerBaseUnit(context, mAnnotType));
        rulerItem.mRulerBase = settings.getFloat(ToolStyleConfig.getInstance().getRulerBaseValueKey(mAnnotType, ""),
            ToolStyleConfig.getInstance().getDefaultRulerBaseValue(context, mAnnotType));
        rulerItem.mRulerTranslateUnit = settings.getString(ToolStyleConfig.getInstance().getRulerTranslateUnitKey(mAnnotType, ""),
            ToolStyleConfig.getInstance().getDefaultRulerTranslateUnit(context, mAnnotType));
        rulerItem.mRulerTranslate = settings.getFloat(ToolStyleConfig.getInstance().getRulerTranslateValueKey(mAnnotType, ""),
            ToolStyleConfig.getInstance().getDefaultRulerTranslateValue(context, mAnnotType));
        rulerItem.mPrecision = settings.getInt(ToolStyleConfig.getInstance().getRulerPrecisionKey(mAnnotType, ""),
            ToolStyleConfig.getInstance().getDefaultRulerPrecision(context, mAnnotType));

        updateRulerItem(rulerItem);
    }

    @Nullable
    public MeasureInfo getAxis() {
        if (mMeasureFDF != null) {
            return MeasureUtils.getAxisInfo(mMeasureFDF);
        }
        return null;
    }

    @Nullable
    public MeasureInfo getMeasure() {
        if (mMeasureFDF != null) {
            return MeasureUtils.getMeasureInfo(mAnnotType, mMeasureFDF);
        }
        return null;
    }

    public String getMeasurementText(double value, MeasureInfo measureInfo) {
        return MeasureUtils.getMeasurementText(value, measureInfo);
    }

    public void commit(Annot annot) {
        if (mMeasureFDF != null) {
            try {
                MeasureUtils.putMeasurementInfo(annot, mMeasureFDF.toString());
            } catch (Exception ex) {
                AnalyticsHandlerAdapter.getInstance().sendException(ex);
            }
        }
    }

}
