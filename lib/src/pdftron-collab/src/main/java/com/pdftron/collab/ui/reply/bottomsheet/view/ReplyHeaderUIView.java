package com.pdftron.collab.ui.reply.bottomsheet.view;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pdftron.collab.R;
import com.pdftron.collab.ui.reply.bottomsheet.BottomSheetReplyFragment;
import com.pdftron.collab.ui.reply.component.header.BaseHeaderUIView;
import com.pdftron.collab.ui.view.NotificationImageButton;

/**
 * Base class for {@link BaseHeaderUIView} that represents a reply header in the
 * {@link BottomSheetReplyFragment}
 * {@hide}
 */
public class ReplyHeaderUIView extends BaseHeaderUIView {

    private final TextView mHeaderTitle;
    private final ImageView mAnnotIcon;
    private final TextView mAnnotText;
    private final ConstraintLayout mPreviewContainer;
    private final NotificationImageButton mHeaderList;

    public ReplyHeaderUIView(@NonNull ViewGroup parent) {
        super(parent);

        Context context = parent.getContext();
        View container = LayoutInflater.from(context)
                .inflate(R.layout.content_reply_header, parent, true);

        LayoutInflater.from(context)
                .inflate(R.layout.view_horizontal_divider, parent, true);

        mPreviewContainer = LayoutInflater.from(context)
                .inflate(R.layout.content_reply_header_preview, parent, true)
                .findViewById(R.id.reply_header_preview_container);

        // Inflate views
        mHeaderTitle = container.findViewById(R.id.header_title);
        ImageView close = container.findViewById(R.id.header_close);
        mHeaderList = container.findViewById(R.id.header_list);
        mAnnotIcon = container.findViewById(R.id.annot_icon);
        mAnnotText = container.findViewById(R.id.annot_text);

        close.setOnClickListener(v -> this.onCloseClicked(null));
        mHeaderList.setOnClickListener(v -> this.onListClicked(null));
    }

    @Override
    public void showPreviewHeader() {
        mPreviewContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void hidePreviewHeader() {
        mPreviewContainer.setVisibility(View.GONE);
    }

    @Override
    public void setHeaderTitle(@NonNull String title) {
        mHeaderTitle.setText(title);
    }

    @Override
    public void setNotificationIcon(boolean hasUnreadReplies) {
        mHeaderList.setNotificationVisibility(hasUnreadReplies);
    }

    @Override
    public void setAnnotationPreviewIcon(@DrawableRes int iconRed, @ColorInt int color, float opacity) {
        mAnnotIcon.setImageResource(iconRed);
        mAnnotIcon.setColorFilter(color);
        mAnnotIcon.setAlpha(opacity);
    }

    @Override
    public void setAnnotationPreviewText(@NonNull String text) {
        mAnnotText.setText(text);
    }
}
