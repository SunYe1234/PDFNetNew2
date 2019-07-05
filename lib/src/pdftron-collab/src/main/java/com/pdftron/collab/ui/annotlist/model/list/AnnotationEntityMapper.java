package com.pdftron.collab.ui.annotlist.model.list;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import com.pdftron.collab.db.entity.AnnotationEntity;
import com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListContent;
import com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListHeader;
import com.pdftron.pdf.Annot;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.utils.ViewerUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Adapter that converts {@link AnnotationEntity} objects to
 * {@link com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListItem} objects.
 * {@hide}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AnnotationEntityMapper<K extends AnnotationListHeader.HeaderData> {

    private final List<AnnotationEntity> entities;

    public AnnotationEntityMapper(List<AnnotationEntity> entities) {
        this.entities = entities;
    }

    public AnnotationListHeader<K> getHeader(@NonNull K key) {
        return new AnnotationListHeader<>(key);
    }

    public List<AnnotationListContent> fromEntities(
            @NonNull PDFViewCtrl pdfViewCtrl) {
        List<AnnotationListContent> messages = new ArrayList<>();

        if (entities != null) {
            for (AnnotationEntity entity : entities) {
                if (entity.getInReplyTo() == null) {
                    messages.add(fromEntityImpl(entity, pdfViewCtrl));
                }
            }
        }
        return messages;
    }

    private AnnotationListContent fromEntityImpl(@NonNull AnnotationEntity annotEntity,
            @NonNull PDFViewCtrl pdfViewCtrl) {
        // Get all required parameters
        int type = annotEntity.getType();
        int pageNum = annotEntity.getPage();
        String content = annotEntity.getContents();
        String author = annotEntity.getAuthorName();
        Date creationDate = annotEntity.getCreationDate();
        Annot annot = ViewerUtils.getAnnotById(pdfViewCtrl, annotEntity.getId(), pageNum);
        double yPos = annotEntity.getYPos();
        Date lastReplyDate = annotEntity.getLastReplyDate();
        int color = annotEntity.getColor();
        float opacity = annotEntity.getOpacity();
        String lastReplyAuthor = annotEntity.getLastReplyAuthor();
        String lastReplyComment = annotEntity.getLastReplyContents();
        int unreadCount = annotEntity.getUnreadCount();

        return new AnnotationListContent(type,
                pageNum,
                content,
                author,
                creationDate,
                annot,
                yPos,
                lastReplyComment,
                lastReplyDate,
                lastReplyAuthor,
                color,
                opacity,
                unreadCount);
    }
}
