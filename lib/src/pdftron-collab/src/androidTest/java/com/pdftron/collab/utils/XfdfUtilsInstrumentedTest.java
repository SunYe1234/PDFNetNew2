package com.pdftron.collab.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.pdftron.collab.R;
import com.pdftron.collab.db.entity.AnnotationEntity;
import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.PDFNet;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

@RunWith(AndroidJUnit4.class)
public class XfdfUtilsInstrumentedTest {

    private static String sJSONStr = "{\"dId\":\"UmLSYDcq-BbB2JJCln0XyXumZejHA6VqaYw2DUsmZLk=\",\"aId\":\"2f0b8e56-c624-4e3a-a3a6-f072311694ad\",\"author\":\"832D8592F7141CB7A7674261C181A3DC\",\"aName\":\"Cobra\",\"xfdf\":\"<add><square style=\\\"solid\\\" width=\\\"5\\\" color=\\\"#E44234\\\" opacity=\\\"1\\\" creationdate=\\\"D:20190326220941Z\\\" flags=\\\"print\\\" date=\\\"D:20190326220941Z\\\" name=\\\"2f0b8e56-c624-4e3a-a3a6-f072311694ad\\\" page=\\\"0\\\" rect=\\\"68.1528,33.8239,173.984,120.879\\\" title=\\\"832D8592F7141CB7A7674261C181A3DC\\\" \\/><\\/add>\",\"at\":\"create\"}";
    private static String sRealDocId = "REAL_DOC_ID";

    @Before
    public void initPDFNet() throws PDFNetException {
        Context appContext = InstrumentationRegistry.getTargetContext();

        PDFNet.initialize(appContext, R.raw.pdfnet, "test");
    }

    @Test
    public void parseAnnotationEntity() throws JSONException {
        JSONObject jsonObject = new JSONObject(sJSONStr);
        AnnotationEntity annotationEntity = JsonUtils.parseRetrieveMessage(jsonObject, sRealDocId);
        Assert.assertNotNull(annotationEntity);
        Assert.assertEquals(sRealDocId, annotationEntity.getDocumentId());
    }

    @Test
    public void xfdfToAnnotationEntity() throws JSONException {
        JSONObject jsonObject = new JSONObject(sJSONStr);
        AnnotationEntity annotationEntity = XfdfUtils.xfdfToAnnotationEntity(jsonObject);
        Assert.assertNotNull(annotationEntity);
        Assert.assertEquals("UmLSYDcq-BbB2JJCln0XyXumZejHA6VqaYw2DUsmZLk=", annotationEntity.getDocumentId());
        Assert.assertEquals("2f0b8e56-c624-4e3a-a3a6-f072311694ad", annotationEntity.getId());
        Assert.assertEquals("832D8592F7141CB7A7674261C181A3DC", annotationEntity.getAuthorId());
        Assert.assertEquals("Cobra", annotationEntity.getAuthorName());
        Assert.assertEquals(Color.parseColor("#E44234"), annotationEntity.getColor());
        Date expectedDate = XfdfUtils.deserializeDate("D:20190326220941Z");
        Assert.assertNotNull(expectedDate);
        Assert.assertTrue(Math.abs(expectedDate.getTime() - annotationEntity.getCreationDate().getTime()) <= 2f);
        Assert.assertEquals(1, annotationEntity.getPage());
    }
}
