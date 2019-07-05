package com.pdftron.collab.utils;

import com.pdftron.collab.db.entity.AnnotationEntity;
import com.pdftron.collab.db.entity.ReplyEntity;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;
import java.util.Date;

public class XfdfUtilsTest {

    private static String sDocumentId = "Test123";
    private static String sAnnotId = "ASDFG-QWERT";
    private static String sParentAnnotId = "GHJKL-YUIOP";
    private static String sUserId = "Bob123";
    private static String sUserName = "Bob";
    private static String sAction = "add";
    private static String sFakeXfdf = "some-xfdf";
    private static String sFakeContent = "some-content";
    private static int sPage = 1;

    @Test
    public void parseReplyEntity() {
        AnnotationEntity annotationEntity = new AnnotationEntity();
        annotationEntity.setId(sAnnotId);
        annotationEntity.setInReplyTo(sParentAnnotId);
        annotationEntity.setDocumentId(sDocumentId);
        annotationEntity.setAuthorId(sUserId);
        annotationEntity.setAuthorName(sUserName);
        annotationEntity.setXfdf(sFakeXfdf);
        annotationEntity.setContents(sFakeContent);
        annotationEntity.setPage(sPage);
        Date now = new Date();
        annotationEntity.setCreationDate(now);
        annotationEntity.setDate(now);

        ReplyEntity replyEntity = XfdfUtils.parseReplyEntity(annotationEntity);
        Assert.assertNotNull(replyEntity);
        Assert.assertEquals(sAnnotId, replyEntity.getId());
        Assert.assertEquals(sParentAnnotId, replyEntity.getInReplyTo());
        Assert.assertEquals(sUserId, replyEntity.getAuthorId());
        Assert.assertEquals(sUserName, replyEntity.getAuthorName());
        Assert.assertEquals(sFakeContent, replyEntity.getContents());
        Assert.assertEquals(sPage, replyEntity.getPage());
        Assert.assertEquals(now, replyEntity.getDate());
        Assert.assertEquals(now, replyEntity.getCreationDate());
    }

    @Test
    public void wrapDeleteXfdf() {
        String id = sAnnotId;
        String result = XfdfUtils.wrapDeleteXfdf(id);
        Assert.assertEquals("<delete><id>" + sAnnotId + "</id></delete>", result);
    }

    @Test
    public void convToAnnotations() throws JSONException {
        String xfdfJson = "{\n" +
                "        \"annots\": [\n" +
                "            {\n" +
                "                \"op\": \"add\",\n" +
                "                \"id\": \"2f0b8e56-c624-4e3a-a3a6-f072311694ad\",\n" +
                "                \"parent_id\": \"\",\n" +
                "                \"author\": \"832D8592F7141CB7A7674261C181A3DC\",\n" +
                "                \"xfdf\": \"<square style=\\\"solid\\\" width=\\\"5\\\" color=\\\"#E44234\\\" opacity=\\\"1\\\" creationdate=\\\"D:20190326220941Z\\\" flags=\\\"print\\\" date=\\\"D:20190326220941Z\\\" name=\\\"2f0b8e56-c624-4e3a-a3a6-f072311694ad\\\" page=\\\"0\\\" rect=\\\"68.1528,33.8239,173.984,120.879\\\" title=\\\"832D8592F7141CB7A7674261C181A3DC\\\" />\"\n" +
                "            }]\n" +
                "    }";
        String documentId = sDocumentId;
        String userName = sUserName;
        ArrayList<AnnotationEntity> annotationEntities = XfdfUtils.convToAnnotations(xfdfJson, documentId, userName);
        Assert.assertEquals(1, annotationEntities.size());
        AnnotationEntity entity = annotationEntities.get(0);
        Assert.assertEquals(sAction, entity.getAt());
        Assert.assertEquals("2f0b8e56-c624-4e3a-a3a6-f072311694ad", entity.getId());
        Assert.assertEquals("832D8592F7141CB7A7674261C181A3DC", entity.getAuthorId());
        Assert.assertEquals(sUserName, entity.getAuthorName());
        Assert.assertEquals(sDocumentId, entity.getDocumentId());
    }

    @Test
    public void prepareAnnotation() throws JSONException {
        String xfdfJson = "{\n" +
                "        \"annots\": [\n" +
                "            {\n" +
                "                \"op\": \"add\",\n" +
                "                \"id\": \"2f0b8e56-c624-4e3a-a3a6-f072311694ad\",\n" +
                "                \"parent_id\": \"\",\n" +
                "                \"author\": \"832D8592F7141CB7A7674261C181A3DC\",\n" +
                "                \"xfdf\": \"<square style=\\\"solid\\\" width=\\\"5\\\" color=\\\"#E44234\\\" opacity=\\\"1\\\" creationdate=\\\"D:20190326220941Z\\\" flags=\\\"print\\\" date=\\\"D:20190326220941Z\\\" name=\\\"2f0b8e56-c624-4e3a-a3a6-f072311694ad\\\" page=\\\"0\\\" rect=\\\"68.1528,33.8239,173.984,120.879\\\" title=\\\"832D8592F7141CB7A7674261C181A3DC\\\" />\"\n" +
                "            }]\n" +
                "    }";
        String documentId = sDocumentId;
        String userName = sUserName;
        ArrayList<AnnotationEntity> annotationEntities = XfdfUtils.convToAnnotations(xfdfJson, documentId, userName);
        String result = XfdfUtils.prepareAnnotation(sAction, annotationEntities, documentId, userName);
        String expected = "{\"annots\":[{\"at\":\"create\",\"aId\":\"2f0b8e56-c624-4e3a-a3a6-f072311694ad\",\"author\":\"832D8592F7141CB7A7674261C181A3DC\",\"aName\":\"Bob\",\"xfdf\":\"<add><square style=\\\"solid\\\" width=\\\"5\\\" color=\\\"#E44234\\\" opacity=\\\"1\\\" creationdate=\\\"D:20190326220941Z\\\" flags=\\\"print\\\" date=\\\"D:20190326220941Z\\\" name=\\\"2f0b8e56-c624-4e3a-a3a6-f072311694ad\\\" page=\\\"0\\\" rect=\\\"68.1528,33.8239,173.984,120.879\\\" title=\\\"832D8592F7141CB7A7674261C181A3DC\\\" \\/><\\/add>\"}],\"t\":\"a_add\",\"dId\":\"Test123\"}";
        JSONAssert.assertEquals(new JSONObject(expected), new JSONObject(result), false);
    }
}
