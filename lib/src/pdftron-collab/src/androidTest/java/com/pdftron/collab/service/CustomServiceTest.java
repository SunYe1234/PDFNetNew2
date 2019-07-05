package com.pdftron.collab.service;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.pdftron.collab.R;
import com.pdftron.collab.db.CollabDatabase;
import com.pdftron.collab.db.dao.AnnotationDao;
import com.pdftron.collab.db.dao.DocumentDao;
import com.pdftron.collab.db.dao.LastAnnotationDao;
import com.pdftron.collab.db.dao.ReplyDao;
import com.pdftron.collab.db.dao.UserDao;
import com.pdftron.collab.db.entity.AnnotationEntity;
import com.pdftron.collab.db.entity.DocumentEntity;
import com.pdftron.collab.db.entity.LastAnnotationEntity;
import com.pdftron.collab.db.entity.UserEntity;
import com.pdftron.collab.utils.JsonUtils;
import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.PDFNet;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class CustomServiceTest {

    private AnnotationDao annotationDao;
    private DocumentDao documentDao;
    private LastAnnotationDao lastAnnotationDao;
    private ReplyDao replyDao;
    private UserDao userDao;

    private CollabDatabase db;
    private MyService service;

    @Before
    public void createDb() throws PDFNetException {
        Context appContext = InstrumentationRegistry.getTargetContext();

        PDFNet.initialize(appContext, R.raw.pdfnet, "test");

        db = Room.inMemoryDatabaseBuilder(appContext, CollabDatabase.class).build();
        annotationDao = db.annotationDao();
        documentDao = db.documentDao();
        lastAnnotationDao = db.lastAnnotationDao();
        replyDao = db.replyDao();
        userDao = db.userDao();

        service = new MyService();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void readWriteUser() {
        userDao.insert(DBUtil.createUser());

        UserEntity user2 = userDao.getUserSync();

        Assert.assertNull(user2.getActiveAnnotation());

        userDao.update(DBUtil.sUserId, DBUtil.sActiveAnnotId);

        user2 = userDao.getUserSync();
        Assert.assertEquals(DBUtil.sActiveAnnotId, user2.getActiveAnnotation());
    }

    @Test
    public void readWriteLastAnnotation() {
        lastAnnotationDao.insert(DBUtil.createLastAnnotation());

        List<LastAnnotationEntity> list = lastAnnotationDao.getLastAnnotationsSync();
        Assert.assertEquals(DBUtil.sActiveAnnotId, list.get(0).getId());
        Assert.assertEquals(DBUtil.sFakeXfdf, list.get(0).getXfdf());
    }

    @Test
    public void readWriteDocument() {
        documentDao.insert(DBUtil.createDocument());

        DocumentEntity result = documentDao.getDocumentSync(DBUtil.sDocumentId);
        Assert.assertEquals(DBUtil.sDocumentId, result.getId());
        Assert.assertEquals(DBUtil.sShareId, result.getShareId());
        Assert.assertNull(result.getUnreads());

        documentDao.updateUnreads(DBUtil.sDocumentId, DBUtil.sFakeUnreadStr);

        result = documentDao.getDocumentSync(DBUtil.sDocumentId);

        Assert.assertEquals(DBUtil.sFakeUnreadStr, result.getUnreads());
    }

    @Test
    public void readWriteAnnotation() {
        List<AnnotationEntity> annots = new ArrayList<>(2);
        annots.add(DBUtil.createAnnot1());
        annots.add(DBUtil.createAnnot2());
        annotationDao.insertAnnotations(annots);

        Assert.assertNull(annotationDao.getAnnotationSync(DBUtil.sAnnotId3));

        annotationDao.insert(DBUtil.createAnnot3());

        Assert.assertEquals(DBUtil.sAnnotId3, annotationDao.getAnnotationSync(DBUtil.sAnnotId3).getId());
    }

    @Test
    public void readWriteReply() {
        replyDao.insert(DBUtil.createReply1());

        Assert.assertEquals(DBUtil.sAnnotId3, replyDao.getReplySync(DBUtil.sAnnotId3).getId());

        replyDao.insert(DBUtil.createReply3());

        Assert.assertEquals(DBUtil.sAnnotId3, replyDao.getLastReplySync(DBUtil.sAnnotId2).getId());
    }

    @Test
    public void serviceAddUser() {
        service.addUser(db, DBUtil.sUserId, DBUtil.sUserName);

        Assert.assertEquals(DBUtil.sUserId, userDao.getUserSync().getId());
        Assert.assertEquals(DBUtil.sUserName, userDao.getUserSync().getName());
        Assert.assertNull(userDao.getUserSync().getActiveAnnotation());
    }

    @Test
    public void serviceAddDocument() {
        service.addDocument(db, DBUtil.createDocument());

        List<DocumentEntity> list = documentDao.getDocumentsSync();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(DBUtil.sDocumentId, list.get(0).getId());
        Assert.assertEquals(DBUtil.sShareId, list.get(0).getShareId());
        Assert.assertNull(list.get(0).getUnreads());
    }

    @Test
    public void serviceAddAnnotations() {
        HashMap<String, AnnotationEntity> annotations = new HashMap<>();
        annotations.put(DBUtil.sAnnotId1, DBUtil.createAnnot1());
        annotations.put(DBUtil.sAnnotId2, DBUtil.createAnnot2());
        annotations.put(DBUtil.sAnnotId3, DBUtil.createAnnot3());
        annotations.put(DBUtil.sAnnotId4, DBUtil.createAnnot4());
        annotations.put(DBUtil.sAnnotId5, DBUtil.createAnnot5());

        service.addAnnotations(db, annotations);

        // annot
        Assert.assertEquals(5, annotationDao.getAnnotationsSync(DBUtil.sDocumentId).size());

        AnnotationEntity annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId1);
        Assert.assertEquals(DBUtil.sAuthor4, annot.getLastReplyAuthor());
        Assert.assertEquals(DBUtil.sContent4, annot.getLastReplyContents());

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(DBUtil.sAuthor3, annot.getLastReplyAuthor());
        Assert.assertEquals(DBUtil.sContent3, annot.getLastReplyContents());

        // reply
        Assert.assertEquals(1, replyDao.getSortedRepliesSync(DBUtil.sAnnotId1).size());
        Assert.assertEquals(2, replyDao.getSortedRepliesSync(DBUtil.sAnnotId2).size());
    }

    @Test
    public void serviceAddAnnotation() throws JSONException {
        service.addUser(db, DBUtil.sUserId, DBUtil.sUserName);
        service.addDocument(db, DBUtil.createDocument());

        HashMap<String, AnnotationEntity> anntations = new HashMap<>();
        anntations.put(DBUtil.sAnnotId1, DBUtil.createAnnot1());
        anntations.put(DBUtil.sAnnotId2, DBUtil.createAnnot2());

        service.addAnnotations(db, anntations);

        Assert.assertEquals(2, annotationDao.getAnnotationsSync(DBUtil.sDocumentId).size());
        Assert.assertEquals(0, replyDao.getSortedRepliesSync(DBUtil.sAnnotId2).size());

        Assert.assertNull(db.documentDao().getDocumentSync(DBUtil.sDocumentId).getUnreads());

        service.addAnnotation(db, DBUtil.createAnnot5());

        AnnotationEntity annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(DBUtil.sAuthor5, annot.getLastReplyAuthor());
        Assert.assertEquals(DBUtil.sContent5, annot.getLastReplyContents());
        Assert.assertEquals(1, annot.getUnreadCount());

        DocumentEntity document = db.documentDao().getDocumentSync(DBUtil.sDocumentId);
        Assert.assertNotNull(document.getUnreads());
        JSONArray unreads = new JSONArray(document.getUnreads());
        JSONArray expected = new JSONArray();
        expected.put(DBUtil.sAnnotId2);
        JSONAssert.assertEquals(expected, unreads, false);

        // pretend user reading the annot
        userDao.update(DBUtil.sUserId, DBUtil.sAnnotId2);
        documentDao.updateUnreads(DBUtil.sDocumentId, JsonUtils.removeAllItemFromArray(document.getUnreads(), DBUtil.sAnnotId2));
        annotationDao.resetUnreadCount(DBUtil.sAnnotId2);

        service.addAnnotation(db, DBUtil.createAnnot3());

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(DBUtil.sAuthor3, annot.getLastReplyAuthor());
        Assert.assertEquals(DBUtil.sContent3, annot.getLastReplyContents());
        Assert.assertEquals(0, annot.getUnreadCount());

        document = db.documentDao().getDocumentSync(DBUtil.sDocumentId);
        Assert.assertNotNull(document.getUnreads());
        unreads = new JSONArray(document.getUnreads());
        expected = new JSONArray();
        JSONAssert.assertEquals(expected, unreads, false);

        List<LastAnnotationEntity> list = lastAnnotationDao.getLastAnnotationsSync();
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.get(0).getXfdf().contains(DBUtil.sAnnotId3));

        // now let's add reply to different annotation
        service.addAnnotation(db, DBUtil.createAnnot4());

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId1);
        Assert.assertEquals(DBUtil.sAuthor4, annot.getLastReplyAuthor());
        Assert.assertEquals(DBUtil.sContent4, annot.getLastReplyContents());

        document = db.documentDao().getDocumentSync(DBUtil.sDocumentId);
        Assert.assertNotNull(document.getUnreads());
        unreads = new JSONArray(document.getUnreads());
        expected = new JSONArray();
        expected.put(DBUtil.sAnnotId1);
        JSONAssert.assertEquals(expected, unreads, false);

        Assert.assertEquals(1, annot.getUnreadCount());
    }

    @Test
    public void serviceModifyAnnotation() {
        service.addUser(db, DBUtil.sUserId, DBUtil.sUserName);
        service.addDocument(db, DBUtil.createDocument());

        HashMap<String, AnnotationEntity> annotations = new HashMap<>();
        annotations.put(DBUtil.sAnnotId1, DBUtil.createAnnot1());
        annotations.put(DBUtil.sAnnotId2, DBUtil.createAnnot2());
        annotations.put(DBUtil.sAnnotId3, DBUtil.createAnnot3());
        annotations.put(DBUtil.sAnnotId4, DBUtil.createAnnot4());
        annotations.put(DBUtil.sAnnotId5, DBUtil.createAnnot5());

        service.addAnnotations(db, annotations);

        service.modifyAnnotation(db, DBUtil.createModifiedAnnot5());

        Assert.assertEquals(DBUtil.sModifiedContent5, replyDao.getReplySync(DBUtil.sAnnotId5).getContents());
        Assert.assertEquals(DBUtil.sModifiedContent5, annotationDao.getAnnotationSync(DBUtil.sAnnotId5).getContents());

        AnnotationEntity annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(DBUtil.sAuthor3, annot.getLastReplyAuthor());
        Assert.assertEquals(DBUtil.sContent3, annot.getLastReplyContents());

        service.modifyAnnotation(db, DBUtil.createModifiedAnnot3());

        Assert.assertEquals(DBUtil.sModifiedContent3, replyDao.getReplySync(DBUtil.sAnnotId3).getContents());
        Assert.assertEquals(DBUtil.sModifiedContent3, annotationDao.getAnnotationSync(DBUtil.sAnnotId3).getContents());

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(DBUtil.sAuthor3, annot.getLastReplyAuthor());
        Assert.assertEquals(DBUtil.sModifiedContent3, annot.getLastReplyContents());
    }

    @Test
    public void serviceDeleteAnnotation1() {
        service.addUser(db, DBUtil.sUserId, DBUtil.sUserName);
        service.addDocument(db, DBUtil.createDocument());

        HashMap<String, AnnotationEntity> annotations = new HashMap<>();
        annotations.put(DBUtil.sAnnotId1, DBUtil.createAnnot1());
        annotations.put(DBUtil.sAnnotId2, DBUtil.createAnnot2());
        annotations.put(DBUtil.sAnnotId3, DBUtil.createAnnot3());
        annotations.put(DBUtil.sAnnotId4, DBUtil.createAnnot4());
        annotations.put(DBUtil.sAnnotId5, DBUtil.createAnnot5());

        service.addAnnotations(db, annotations);

        service.deleteAnnotation(db, DBUtil.sAnnotId5);

        Assert.assertNull(annotationDao.getAnnotationSync(DBUtil.sAnnotId5));
        Assert.assertNull(replyDao.getReplySync(DBUtil.sAnnotId5));

        AnnotationEntity annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(DBUtil.sAuthor3, annot.getLastReplyAuthor());
        Assert.assertEquals(DBUtil.sContent3, annot.getLastReplyContents());
    }

    @Test
    public void serviceDeleteAnnotation2() throws JSONException {
        service.addUser(db, DBUtil.sUserId, DBUtil.sUserName);
        service.addDocument(db, DBUtil.createDocument());

        HashMap<String, AnnotationEntity> annotations = new HashMap<>();
        annotations.put(DBUtil.sAnnotId1, DBUtil.createAnnot1());
        annotations.put(DBUtil.sAnnotId2, DBUtil.createAnnot2());
        annotations.put(DBUtil.sAnnotId4, DBUtil.createAnnot4());
        annotations.put(DBUtil.sAnnotId5, DBUtil.createAnnot5());

        service.addAnnotations(db, annotations);

        service.addAnnotation(db, DBUtil.createAnnot3());

        DocumentEntity document = db.documentDao().getDocumentSync(DBUtil.sDocumentId);
        Assert.assertNotNull(document.getUnreads());
        JSONArray unreads = new JSONArray(document.getUnreads());
        JSONArray expected = new JSONArray();
        expected.put(DBUtil.sAnnotId2);
        JSONAssert.assertEquals(expected, unreads, false);

        AnnotationEntity annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(1, annot.getUnreadCount());

        service.deleteAnnotation(db, DBUtil.sAnnotId3);

        Assert.assertNull(annotationDao.getAnnotationSync(DBUtil.sAnnotId3));
        Assert.assertNull(replyDao.getReplySync(DBUtil.sAnnotId3));

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(DBUtil.sAuthor5, annot.getLastReplyAuthor());
        Assert.assertEquals(DBUtil.sContent5, annot.getLastReplyContents());
        Assert.assertEquals(0, annot.getUnreadCount());

        document = db.documentDao().getDocumentSync(DBUtil.sDocumentId);
        Assert.assertNotNull(document.getUnreads());
        unreads = new JSONArray(document.getUnreads());
        expected = new JSONArray();
        JSONAssert.assertEquals(expected, unreads, false);
    }

    @Test
    public void serviceDeleteAnnotation3() {
        service.addUser(db, DBUtil.sUserId, DBUtil.sUserName);
        service.addDocument(db, DBUtil.createDocument());

        HashMap<String, AnnotationEntity> annotations = new HashMap<>();
        annotations.put(DBUtil.sAnnotId1, DBUtil.createAnnot1());
        annotations.put(DBUtil.sAnnotId2, DBUtil.createAnnot2());

        service.addAnnotations(db, annotations);

        service.addAnnotation(db, DBUtil.createAnnot5());

        AnnotationEntity annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(1, annot.getUnreadCount());

        service.addAnnotation(db, DBUtil.createAnnot3());

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(2, annot.getUnreadCount());

        service.deleteAnnotation(db, DBUtil.sAnnotId3);

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(1, annot.getUnreadCount());

        service.deleteAnnotation(db, DBUtil.sAnnotId5);

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(0, annot.getUnreadCount());

        service.addAnnotation(db, DBUtil.createAnnot5());
        service.addAnnotation(db, DBUtil.createAnnot3());

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(2, annot.getUnreadCount());

        service.deleteAnnotation(db, DBUtil.sAnnotId5);

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(1, annot.getUnreadCount());

        service.deleteAnnotation(db, DBUtil.sAnnotId3);

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(0, annot.getUnreadCount());
    }

    @Test
    public void serviceDeleteAnnotation4() {
        service.addUser(db, DBUtil.sUserId, DBUtil.sUserName);
        service.addDocument(db, DBUtil.createDocument());

        HashMap<String, AnnotationEntity> annotations = new HashMap<>();
        annotations.put(DBUtil.sAnnotId1, DBUtil.createAnnot1());
        annotations.put(DBUtil.sAnnotId2, DBUtil.createAnnot2());
        annotations.put(DBUtil.sAnnotId5, DBUtil.createAnnot5());

        service.addAnnotations(db, annotations);

        AnnotationEntity annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(0, annot.getUnreadCount());

        service.addAnnotation(db, DBUtil.createAnnot3());

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(1, annot.getUnreadCount());

        service.deleteAnnotation(db, DBUtil.sAnnotId3);

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(0, annot.getUnreadCount());

        service.deleteAnnotation(db, DBUtil.sAnnotId5);

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(0, annot.getUnreadCount());

        service.addAnnotation(db, DBUtil.createAnnot5());

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(1, annot.getUnreadCount());

        annotationDao.resetUnreadCount(DBUtil.sAnnotId2);

        annot = annotationDao.getAnnotationSync(DBUtil.sAnnotId2);
        Assert.assertEquals(0, annot.getUnreadCount());
        Assert.assertNull(annot.getUnreads());
    }
}
