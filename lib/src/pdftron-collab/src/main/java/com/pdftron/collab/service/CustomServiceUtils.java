package com.pdftron.collab.service;

import com.pdftron.collab.db.CollabDatabase;
import com.pdftron.collab.db.entity.AnnotationEntity;
import com.pdftron.collab.db.entity.DocumentEntity;
import com.pdftron.collab.db.entity.LastAnnotationEntity;
import com.pdftron.collab.db.entity.ReplyEntity;
import com.pdftron.collab.db.entity.UserEntity;
import com.pdftron.collab.utils.JsonUtils;
import com.pdftron.collab.utils.Keys;
import com.pdftron.collab.utils.XfdfUtils;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CustomServiceUtils {

    /**
     * Set current user
     * Must run on background thread
     *
     * @param db       the {@link CollabDatabase}
     * @param userId   the unique identifier of the user
     * @param userName the name of the user
     */
    public static void addUser(CollabDatabase db, String userId, String userName) {
        UserEntity entity = new UserEntity(userId, userName, System.currentTimeMillis(), null);
        db.userDao().insert(entity);
    }

    /**
     * Set current document
     * Must run on background thread
     *
     * @param db         the {@link CollabDatabase}
     * @param documentId the unique identifier of the document
     */
    public static void addDocument(CollabDatabase db, String documentId) {
        DocumentEntity entity = new DocumentEntity(documentId, null,
                System.currentTimeMillis(), null);
        db.documentDao().insert(entity);
    }

    /**
     * Set current document
     * Must run on background thread
     *
     * @param db     the {@link CollabDatabase}
     * @param entity the {@link DocumentEntity}
     */
    public static void addDocument(CollabDatabase db, DocumentEntity entity) {
        db.documentDao().insert(entity);
    }

    /**
     * From remote: add annotations, commonly used for syncing initial annotations
     * Must run on background thread
     *
     * @param db          the {@link CollabDatabase}
     * @param annotations map of key=id, value={@link AnnotationEntity}
     */
    public static void addAnnotations(CollabDatabase db, HashMap<String, AnnotationEntity> annotations) {
        if (db != null && annotations != null) {
            ArrayList<ReplyEntity> replies = new ArrayList<>();
            HashMap<String, Date> lastReplyDateMap = new HashMap<>();
            StringBuilder builder = new StringBuilder();
            for (AnnotationEntity entity : annotations.values()) {
                builder.append(XfdfUtils.validateXfdf(entity.getXfdf()));
                ReplyEntity replyEntity = XfdfUtils.parseReplyEntity(entity);
                if (replyEntity != null) {
                    String parentId = replyEntity.getInReplyTo();
                    boolean canUpdate = false;
                    Date lastReplyDate = lastReplyDateMap.get(parentId);
                    if (null == lastReplyDate) {
                        lastReplyDate = replyEntity.getCreationDate();
                        canUpdate = true;
                        lastReplyDateMap.put(parentId, lastReplyDate);
                    } else if (replyEntity.getCreationDate().compareTo(lastReplyDate) > 0) {
                        lastReplyDate = replyEntity.getCreationDate();
                        canUpdate = true;
                        lastReplyDateMap.put(parentId, lastReplyDate);
                    }
                    // update the last reply for parent annotation
                    AnnotationEntity aEntity = annotations.get(parentId);
                    if (aEntity != null && canUpdate) {
                        String author = replyEntity.getAuthorName() != null ? replyEntity.getAuthorName() : replyEntity.getAuthorId();
                        aEntity.setLastReplyAuthor(author);
                        aEntity.setLastReplyContents(replyEntity.getContents());
                        aEntity.setLastReplyDate(replyEntity.getCreationDate());
                        annotations.put(parentId, aEntity);
                    }
                    replies.add(replyEntity);
                }
            }

            db.annotationDao().insertAnnotations(new ArrayList<>(annotations.values()));
            db.replyDao().insertReplies(replies);
            LastAnnotationEntity lastAnnotationEntity = new LastAnnotationEntity(Keys.ANNOT_XFDF, builder.toString());
            db.lastAnnotationDao().insert(lastAnnotationEntity);
        }
    }

    /**
     * From remote: add annotation
     * Must run on background thread
     *
     * @param db         the {@link CollabDatabase}
     * @param annotation the {@link AnnotationEntity}
     */
    public static void addAnnotation(CollabDatabase db, AnnotationEntity annotation) {
        if (db != null && annotation != null) {
            db.annotationDao().insert(annotation);

            ReplyEntity replyEntity = XfdfUtils.parseReplyEntity(annotation);
            if (replyEntity != null) {
                String parentId = replyEntity.getInReplyTo();
                try {
                    // update the last reply for parent annotation
                    String author = replyEntity.getAuthorName() != null ? replyEntity.getAuthorName() : replyEntity.getAuthorId();
                    db.annotationDao().updateReply(parentId, author, replyEntity.getContents(), replyEntity.getCreationDate());
                    String docId = annotation.getDocumentId();
                    DocumentEntity documentEntity = db.documentDao().getDocumentSync(docId);
                    AnnotationEntity annotationEntity = db.annotationDao().getAnnotationSync(parentId);
                    if (documentEntity != null && annotationEntity != null) {
                        UserEntity userEntity = db.userDao().getUserSync();
                        String activeAnnot = userEntity.getActiveAnnotation();
                        if (activeAnnot == null ||
                                !activeAnnot.equals(parentId)) {
                            // only update unreads if it is not active
                            String unreads = documentEntity.getUnreads();
                            String newUnreads = JsonUtils.addItemToArray(unreads, parentId);
                            db.documentDao().updateUnreads(docId, newUnreads);
                            unreads = annotationEntity.getUnreads();
                            JSONArray newUnreadsArr = JsonUtils.addItemToArrayImpl(unreads, replyEntity.getId());
                            db.annotationDao().updateUnreads(parentId, newUnreadsArr.toString(), newUnreadsArr.length());
                        }
                    }
                } catch (Exception ex) {
                    AnalyticsHandlerAdapter.getInstance().sendException(ex);
                }
                db.replyDao().insert(replyEntity);
            }

            LastAnnotationEntity lastAnnotationEntity = new LastAnnotationEntity(Keys.ANNOT_XFDF, annotation.getXfdf());
            db.lastAnnotationDao().insert(lastAnnotationEntity);
        }
    }

    /**
     * From remote: modify annotation
     * Must run on background thread
     *
     * @param db         the {@link CollabDatabase}
     * @param annotation the {@link AnnotationEntity}
     */
    public static void modifyAnnotation(CollabDatabase db, AnnotationEntity annotation) {
        if (null == db || null == annotation) {
            return;
        }
        db.annotationDao().update(annotation.getId(),
                annotation.getXfdf(),
                annotation.getAt(),
                annotation.getContents(),
                annotation.getYPos(),
                annotation.getColor(),
                annotation.getOpacity(),
                annotation.getDate());

        String contents = annotation.getContents();
        if (contents != null) {
            db.replyDao().update(annotation.getId(), contents, annotation.getPage(), annotation.getDate());

            ReplyEntity replyEntity = db.replyDao().getReplySync(annotation.getId());
            if (replyEntity != null) {
                ReplyEntity lastReply = db.replyDao().getLastReplySync(replyEntity.getInReplyTo());
                if (lastReply != null && replyEntity.getId().equals(lastReply.getId())) {
                    String author = replyEntity.getAuthorName() != null ? replyEntity.getAuthorName() : replyEntity.getAuthorId();
                    db.annotationDao().updateReply(replyEntity.getInReplyTo(), author, contents, replyEntity.getCreationDate());
                }
            }
        }

        LastAnnotationEntity lastAnnotationEntity = new LastAnnotationEntity(Keys.ANNOT_XFDF, annotation.getXfdf());
        db.lastAnnotationDao().insert(lastAnnotationEntity);
    }

    /**
     * From remote: delete annotation
     * Must run on background thread
     *
     * @param db      the {@link CollabDatabase}
     * @param annotId the unique identification of the annotation
     */
    public static void deleteAnnotation(CollabDatabase db, String annotId) {
        if (null == db || null == annotId) {
            return;
        }

        AnnotationEntity deletedAnnot = db.annotationDao().getAnnotationSync(annotId);
        db.annotationDao().delete(annotId);

        ReplyEntity replyEntity = db.replyDao().getReplySync(annotId);
        if (replyEntity != null) {
            // this is a reply
            // let's update the last reply
            ReplyEntity lastReply = db.replyDao().getLastReplySync(replyEntity.getInReplyTo());
            if (lastReply != null && replyEntity.getId().equals(lastReply.getId())) {
                try {
                    List<ReplyEntity> replies = db.replyDao().getSortedRepliesSync(replyEntity.getInReplyTo());
                    String author = null;
                    String contents = null;
                    Date replyDate = null;
                    if (replies != null && replies.size() > 1) {
                        // grab the reply before the one about to get deleted
                        ReplyEntity secondLastReply = replies.get(1);
                        author = secondLastReply.getAuthorName() != null ? secondLastReply.getAuthorName() : secondLastReply.getAuthorId();
                        contents = secondLastReply.getContents();
                        replyDate = secondLastReply.getCreationDate();
                    }
                    db.annotationDao().updateReply(replyEntity.getInReplyTo(), author, contents, replyDate);
                } catch (Exception ex) {
                    AnalyticsHandlerAdapter.getInstance().sendException(ex);
                }
            }
            if (deletedAnnot != null) {
                String docId = deletedAnnot.getDocumentId();
                DocumentEntity documentEntity = db.documentDao().getDocumentSync(docId);
                if (documentEntity != null) {
                    UserEntity userEntity = db.userDao().getUserSync();
                    String activeAnnot = userEntity.getActiveAnnotation();
                    if (activeAnnot == null ||
                            !activeAnnot.equals(replyEntity.getInReplyTo())) {
                        // only update unreads if it is not active
                        try {
                            String unreads = documentEntity.getUnreads();
                            if (unreads != null) {
                                String newUnreads = JsonUtils.removeItemFromArray(unreads, replyEntity.getInReplyTo());
                                db.documentDao().updateUnreads(docId, newUnreads);
                            }
                            AnnotationEntity parentAnnot = db.annotationDao().getAnnotationSync(replyEntity.getInReplyTo());
                            unreads = parentAnnot.getUnreads();
                            if (unreads != null) {
                                JSONArray newUnreadsArr = JsonUtils.removeItemFromArrayImpl(unreads, replyEntity.getId());
                                if (newUnreadsArr != null) {
                                    db.annotationDao().updateUnreads(replyEntity.getInReplyTo(), newUnreadsArr.toString(), newUnreadsArr.length());
                                }
                            }
                        } catch (Exception ex) {
                            AnalyticsHandlerAdapter.getInstance().sendException(ex);
                        }
                    }
                }
            }
        }

        db.replyDao().delete(annotId);

        String xfdf = XfdfUtils.wrapDeleteXfdf(annotId);
        LastAnnotationEntity lastAnnotationEntity = new LastAnnotationEntity(Keys.ANNOT_XFDF, xfdf);
        db.lastAnnotationDao().insert(lastAnnotationEntity);
    }

    /**
     * Cleanup all local cache
     * Must run on background thread
     */
    public static void cleanup(CollabDatabase db) {
        db.clearAllTables();
    }
}
