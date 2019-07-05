package com.pdftron.pdf.model;

/**
 * Gson conversion utility class.
 */
public class MeasureInfo {

    // IMPORTANT: this class is a Gson utility class, none of the field name should be changed

    private double factor;
    private String unit;
    private String decimalSymbol;
    private String thousandSymbol;
    private String display;
    private int precision;
    private String unitPrefix;
    private String unitSuffix;
    private String unitPosition;

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDecimalSymbol() {
        return decimalSymbol;
    }

    public void setDecimalSymbol(String decimalSymbol) {
        this.decimalSymbol = decimalSymbol;
    }

    public String getThousandSymbol() {
        return thousandSymbol;
    }

    public void setThousandSymbol(String thousandSymbol) {
        this.thousandSymbol = thousandSymbol;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public String getUnitPrefix() {
        return unitPrefix;
    }

    public void setUnitPrefix(String unitPrefix) {
        this.unitPrefix = unitPrefix;
    }

    public String getUnitSuffix() {
        return unitSuffix;
    }

    public void setUnitSuffix(String unitSuffix) {
        this.unitSuffix = unitSuffix;
    }

    public String getUnitPosition() {
        return unitPosition;
    }

    public void setUnitPosition(String unitPosition) {
        this.unitPosition = unitPosition;
    }
}
