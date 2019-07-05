package com.pdftron.collab.ui.annotlist.component.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pdftron.collab.R;
import com.pdftron.collab.ui.annotlist.model.list.AnnotationList;
import com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListContent;
import com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListHeader;
import com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListItem;
import com.pdftron.collab.ui.base.component.BaseUIView;
import com.pdftron.collab.ui.reply.model.ReplyHeader;
import com.pdftron.collab.ui.view.UnreadNotificationView;
import com.pdftron.collab.utils.date.ReplyDateFormat;
import com.pdftron.pdf.utils.AnnotUtils;
import com.pdftron.pdf.utils.Utils;

import java.text.DateFormat;
import java.util.Date;

/**
 * A {@link BaseUIView} for the annotation list. Responsible for inflating the view, connecting
 * view listeners, and providing an interface to update the views.
 * {@hide}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AnnotationListUIView extends BaseUIView<AnnotationListEvent> {

    private final AnnotListAdapter mAdapter;

    public AnnotationListUIView(@NonNull ViewGroup parent) {
        super(parent);
        Context context = parent.getContext();
        View container = LayoutInflater.from(context)
                .inflate(R.layout.content_annot_list, parent, true);

        RecyclerView annotRecyclerView = container.findViewById(R.id.annot_list);
        annotRecyclerView.setLayoutManager(getLayoutManager(context));
        mAdapter = new AnnotListAdapter(ReplyDateFormat.getSimpleDateFormat(context));

        annotRecyclerView.setAdapter(mAdapter);
    }

    private RecyclerView.LayoutManager getLayoutManager(Context context) {
        return new LinearLayoutManager(context);
    }

    private void onAnnotClicked(@Nullable AnnotationListContent annot) {
        if (mEventObservable != null) {
            mEventObservable.onNext(
                    new AnnotationListEvent(
                            AnnotationListEvent.Type.ANNOTATION_ITEM_CLICKED,
                            annot
                    ));
        }
    }

    public void setAnnotList(@NonNull AnnotationList annotList) {
        mAdapter.setAnnotList(annotList);
    }

    /**
     * RecyclerView adapter for displaying annotation list items, which can be either headers
     * or annotation content.
     */
    private class AnnotListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Nullable
        private AnnotationList mAnnots;
        private final DateFormat mDateFormat;

        private AnnotListAdapter(@NonNull DateFormat dateFormat) {
            mDateFormat = dateFormat;
        }

        @Override
        public int getItemViewType(int position) {
            if (mAnnots != null) {
                if (mAnnots.get(position).isHeader()) {
                    return AnnotationListItem.LAYOUT_HEADER;
                } else {
                    return AnnotationListItem.LAYOUT_CONTENT;
                }
            } else {
                return 0;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            RecyclerView.ViewHolder holder;
            if (viewType == AnnotationListItem.LAYOUT_HEADER) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_annot_list_header, viewGroup, false);
                holder = new AnnotItemHeader(view);
                return holder;
            } else {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_annot_list_content, viewGroup, false);
                holder = new AnnotItemContent(v);
                return holder;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder annotItemViewholder, int position) {
            if (mAnnots != null) {
                AnnotationListItem annotItem = mAnnots.get(position);

                switch (annotItemViewholder.getItemViewType()) {
                    case AnnotationListItem.LAYOUT_HEADER:
                        initItemHeader((AnnotationListHeader) annotItem, (AnnotItemHeader) annotItemViewholder);
                        break;
                    case AnnotationListItem.LAYOUT_CONTENT:
                        initItemContent((AnnotationListContent) annotItem, (AnnotItemContent) annotItemViewholder);
                        break;
                }
            }
        }

        private void initItemHeader(@NonNull AnnotationListHeader annotItem,
                @NonNull AnnotItemHeader annotItemViewholder) {

            String headerTitle = annotItem.getHeaderString();
            annotItemViewholder.mPageNumber.setText(headerTitle);
        }

        private void initItemContent(@NonNull AnnotationListContent annotItem,
                @NonNull AnnotItemContent annotItemViewholder) {

            // Get all the view data
            String reply = getReplyString(annotItem);
            String title = getTitleString(annotItemViewholder.itemView.getContext(), annotItem);
            int iconImg = AnnotUtils.getAnnotImageResId(annotItem.getType());
            Date lastReplyDate = annotItem.getLastReplyDate();
            Date creationDate = annotItem.getCreationDate();
            int color = annotItem.getColor() == -1 ? Color.BLACK : annotItem.getColor();
            float opacity = annotItem.getOpacity();
            int unreadCount = annotItem.getUnreadCount();

            // Set the view data
            annotItemViewholder.mIcon.setImageResource(iconImg);
            annotItemViewholder.mIcon.setColorFilter(color);
            annotItemViewholder.mIcon.setAlpha(opacity);
            annotItemViewholder.mDate.setText(lastReplyDate == null ?
                    mDateFormat.format(creationDate) :
                    mDateFormat.format(lastReplyDate));

            if (Utils.isNullOrEmpty(title)) {
                annotItemViewholder.mTitle.setVisibility(View.GONE);
            } else {
                annotItemViewholder.mTitle.setText(title);
                annotItemViewholder.mTitle.setVisibility(View.VISIBLE);
            }

            if (Utils.isNullOrEmpty(reply)) {
                annotItemViewholder.mLastComment.setVisibility(View.GONE);
            } else {
                annotItemViewholder.mLastComment.setText(reply);
                annotItemViewholder.mLastComment.setVisibility(View.VISIBLE);
            }

            annotItemViewholder.mNotificationIcon.setVisibility(unreadCount > 0 ? View.VISIBLE: View.GONE );
            annotItemViewholder.mNotificationIcon.setText(Integer.toString(unreadCount));

            annotItemViewholder.itemView.setOnClickListener(
                    v -> {
                        if (mAnnots != null) {
                            AnnotationListItem content = mAnnots.get(annotItemViewholder.getAdapterPosition());
                            if (!content.isHeader()) {
                                onAnnotClicked((AnnotationListContent) content);
                            }
                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            return mAnnots == null ? 0 : mAnnots.size();
        }

        void setAnnotList(@NonNull AnnotationList annotItems) {
            mAnnots = annotItems;
            notifyDataSetChanged(); // todo diff utils
        }

        @Nullable
        private String getTitleString(@NonNull Context context,
                @NonNull AnnotationListContent annotItem) {
            String content = annotItem.getContent();
            int type = annotItem.getType();
            return ReplyHeader.getTitleFromContent(context, content, type);
        }

        @Nullable
        private String getReplyString(AnnotationListContent annotItem) {
            String lastReplyAuthor = annotItem.getLastReplyAuthor();
            String lastReplyComment = annotItem.getLastReplyComment();
            if (Utils.isNullOrEmpty(lastReplyAuthor) || Utils.isNullOrEmpty(lastReplyComment)) {
                return null;
            } else {
                return String.format("%s: %s", lastReplyAuthor, lastReplyComment);
            }
        }
    }

    private static class AnnotItemContent extends RecyclerView.ViewHolder {

        final ImageView mIcon;
        final TextView mTitle;
        final TextView mDate;
        final TextView mLastComment;
        final UnreadNotificationView mNotificationIcon;

        AnnotItemContent(@NonNull View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.image);
            mTitle = itemView.findViewById(R.id.title);
            mDate = itemView.findViewById(R.id.date);
            mLastComment = itemView.findViewById(R.id.last_comment);
            mNotificationIcon = itemView.findViewById(R.id.notification_icon);
        }
    }

    private static class AnnotItemHeader extends RecyclerView.ViewHolder {

        final TextView mPageNumber;

        AnnotItemHeader(@NonNull View itemView) {
            super(itemView);
            mPageNumber = itemView.findViewById(R.id.page_number);
        }
    }
}
