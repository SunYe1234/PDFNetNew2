package com.pdftron.pdf.model;

import com.pdftron.pdf.config.ToolConfig;
import com.pdftron.pdf.controls.AnnotationToolbar;
import com.pdftron.pdf.tools.ToolManager;

import java.util.ArrayList;

public class GroupedItem {
    private AnnotationToolbar annotationToolbar;
    private String mPrefKey;
    private int[] mAnnotTypes;

    public GroupedItem(AnnotationToolbar annotationToolbar, String prefKey, int[] annotTypes) {
        this.annotationToolbar = annotationToolbar;
        this.mPrefKey = prefKey;
        this.mAnnotTypes = annotTypes;
    }

    public ArrayList<Integer> getButtonIds() {
        ArrayList<Integer> buttonIds = new ArrayList<>();
        for (int annotType : mAnnotTypes) {
            buttonIds.add(annotationToolbar.getButtonIdFromAnnotType(annotType));
        }
        return buttonIds;
    }

    public String getPrefKey() {
        return mPrefKey;
    }

    public ArrayList<Integer> getAvailableAnnotTypes() {
        ArrayList<Integer> result = new ArrayList<>();
        for (int annotType : mAnnotTypes) {
            int buttonId = annotationToolbar.getButtonIdFromAnnotType(annotType);
            ToolManager.ToolMode toolMode = ToolConfig.getInstance().getToolModeByAnnotationToolbarItemId(buttonId);
            if (annotationToolbar.getToolManager() != null && !annotationToolbar.getToolManager().isToolModeDisabled(toolMode)) {
                result.add(annotType);
            }
        }
        return result;
    }

    public int getVisibleButtonId() {
        if (annotationToolbar.getVisibleAnnotTypeMap() != null && annotationToolbar.getVisibleAnnotTypeMap().containsKey(mPrefKey)) {
            int annotType = annotationToolbar.getVisibleAnnotTypeMap().get(mPrefKey);
            int buttonId = annotationToolbar.getButtonIdFromAnnotType(annotType);
            ToolManager.ToolMode toolMode = ToolConfig.getInstance().getToolModeByAnnotationToolbarItemId(buttonId);
            if (!annotationToolbar.getToolManager().isToolModeDisabled(toolMode)) {
                return buttonId;
            }
        }
        ArrayList<Integer> annotTypes = getAvailableAnnotTypes();
        if (annotTypes == null || annotTypes.isEmpty()) {
            return -1;
        }
        if (!annotationToolbar.hasAllTool() && annotationToolbar.getToolManager().hasAnnotToolbarPrecedence()) {
            return findPreferredButton();
        } else {
            int firstAnnotType = annotTypes.get(0);
            return annotationToolbar.getButtonIdFromAnnotType(firstAnnotType);
        }
    }

    private int findPreferredButton() {
        for (GroupedItem item : annotationToolbar.getGroupItems()) {
            if (item.getPrefKey().equals(mPrefKey)) {
                for (Integer aType : item.getAvailableAnnotTypes()) {
                    int bId = annotationToolbar.getButtonIdFromAnnotType(aType);
                    ToolManager.ToolMode tm = ToolConfig.getInstance().getToolModeByAnnotationToolbarItemId(bId);
                    if (annotationToolbar.getToolManager().getAnnotToolbarPrecedence().contains(tm)) {
                        return bId;
                    }
                }
            }
        }
        return -1;
    }

    public boolean contains(int annotType) {
        for (int type : mAnnotTypes) {
            if (type == annotType) {
                return true;
            }
        }
        return false;
    }


}
