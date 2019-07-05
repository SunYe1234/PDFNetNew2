package com.pdftron.pdf.dialog.pagelabel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

abstract class PageLabelView {
    PageLabelView(ViewGroup parent, PageLabelSettingChangeListener listener) {
    }

    abstract void updatePreview(String preview);

    abstract void initViewStates(@Nullable PageLabelSetting initState);

    abstract void invalidFromPage(boolean isValid);

    abstract void invalidToPage(boolean isValid);

    abstract void invalidStartNumber(boolean isValid);

    interface PageLabelSettingChangeListener {
        void setAll(boolean isAll);

        void setSelectedPage(boolean isSelected);

        void setPageRange(@NonNull String fromStr, @NonNull String toStr);

        void setStartNumber(@NonNull String startStr);

        void setStyle(@NonNull PageLabelSetting.PageLabelStyle style);

        void setPrefix(@NonNull String prefix);

        void completeSettings();
    }
}
