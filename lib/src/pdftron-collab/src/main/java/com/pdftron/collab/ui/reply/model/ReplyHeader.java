package com.pdftron.collab.ui.reply.model;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.Annot;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.AnnotUtils;
import com.pdftron.pdf.utils.Utils;

import java.util.Objects;

/**
 * View state model representing the content/information in the reply header.
 */
public class ReplyHeader {

    @NonNull
    private final String title;

    @NonNull
    private final String previewContent;
    @DrawableRes
    private final int previewIcon;
    @ColorInt
    private final int previewIconColor;
    private final float previewIconOpacity;

    private final boolean hasUnreadReplies;

    public ReplyHeader(@NonNull Context context, @NonNull Annot annot, boolean hasUnreadReplies) {
        this.title = Utils.capitalize(AnnotUtils.getAnnotTypeAsString(context, getType(annot)));
        this.previewContent = getContents(annot);
        this.previewIcon = AnnotUtils.getAnnotImageResId(getType(annot));
        this.previewIconColor = AnnotUtils.getAnnotColor(annot);
        this.previewIconOpacity = AnnotUtils.getAnnotOpacity(annot);
        this.hasUnreadReplies = hasUnreadReplies;
    }

    public ReplyHeader(@NonNull String title,
            @NonNull String previewContent,
            int previewIcon,
            int previewIconColor,
            float previewIconOpacity,
            boolean hasUnreadReplies) {
        this.title = title;
        this.previewContent = previewContent;
        this.previewIcon = previewIcon;
        this.previewIconColor = previewIconColor;
        this.previewIconOpacity = previewIconOpacity;
        this.hasUnreadReplies = hasUnreadReplies;
    }

    @NonNull
    private String getContents(@NonNull Annot annot) {
        try {
            return annot.getContents();
        } catch (PDFNetException e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
            return "";
        }
    }

    public boolean hasUnreadReplies() {
        return hasUnreadReplies;
    }

    private int getType(@NonNull Annot annot) {
        try {
            return annot.getType();
        } catch (PDFNetException e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
            return -1;
        }
    }

    @NonNull
    public String getPreviewContent() {
        return previewContent;
    }

    public int getPreviewIcon() {
        return previewIcon;
    }

    @ColorInt
    public int getPreviewIconColor() {
        return previewIconColor;
    }

    public float getPreviewIconOpacity() {
        return previewIconOpacity;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplyHeader that = (ReplyHeader) o;
        return previewIcon == that.previewIcon &&
                previewIconColor == that.previewIconColor &&
                Float.compare(that.previewIconOpacity, previewIconOpacity) == 0 &&
                hasUnreadReplies == that.hasUnreadReplies &&
                title.equals(that.title) &&
                previewContent.equals(that.previewContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, previewContent, previewIcon, previewIconColor, previewIconOpacity, hasUnreadReplies);
    }

    /**
     * Helper method to get a reply header title from a given {@link Annot}. It will use the
     * annotation content and if that is unavailable it will use the annotation type name.
     *
     * @param context used to get string resources
     * @param annot   used to determine the title
     * @return reply header title generated from the given {@link Annot}
     */
    @NonNull
    public static String getTitleFromAnnot(@NonNull Context context, @NonNull Annot annot) {
        String content;
        int type;
        try {
            content = annot.getContents();
            type = annot.getType();
        } catch (PDFNetException e) {
            e.printStackTrace();
            content = "";
            type = -1;
        }

        if (Utils.isNullOrEmpty(content)) {
            return Utils.capitalize(AnnotUtils.getAnnotTypeAsString(context, type));
        } else {
            return content;
        }
    }

    /**
     * Helper method to get a reply header title from a given annotation content and type. It will
     * use the annotation contents and if that is unavailable it will use the annotation type name.
     *
     * @param context used to get string resources
     * @param content content of the annotation
     * @param type    annotation type ID
     * @return reply header title generated from the given content and type
     */
    public static String getTitleFromContent(@NonNull Context context, @Nullable String content, int type) {
        if (Utils.isNullOrEmpty(content)) {
            return Utils.capitalize(AnnotUtils.getAnnotTypeAsString(context, type));
        } else {
            return content;
        }
    }
}
