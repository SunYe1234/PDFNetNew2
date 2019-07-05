package com.pdftron.collab.service;

import com.pdftron.collab.db.entity.AnnotationEntity;
import com.pdftron.collab.db.entity.DocumentEntity;
import com.pdftron.collab.db.entity.LastAnnotationEntity;
import com.pdftron.collab.db.entity.ReplyEntity;
import com.pdftron.collab.db.entity.UserEntity;
import com.pdftron.collab.utils.XfdfUtils;

import java.util.Date;

public class DBUtil {

    public static String sUserId = "ZXCVB-MNBVC";
    public static String sUserName = "Bob";
    public static String sDocumentId = "Test123";
    public static String sShareId = "998877";
    public static String sActiveAnnotId = "QWERT-POIUY";
    public static String sFakeXfdf = "some-xfdf";
    public static String sFakeUnreadStr = "some-unread";

    // annot
    public static String sAnnotId1 = "e091eba1-57e3-250e-681b-3df1bc72294c";
    public static String sAnnotXfdf1 = "<add><polyline xmlns=\\\"http:\\/\\/ns.adobe.com\\/xfdf\\/\\\" title=\\\"3D2BFF52D10618CCBC0C446345014CF9\\\" color=\\\"#000000\\\" creationdate=\\\"D:20190320123510-07'00'\\\" date=\\\"D:20190320123512-07'00'\\\" subject=\\\"Polyline\\\" name=\\\"e091eba1-57e3-250e-681b-3df1bc72294c\\\" flags=\\\"print\\\" rect=\\\"385.507894,234.732853,747.632997,468.901417\\\" page=\\\"0\\\" head=\\\"None\\\" tail=\\\"None\\\"><vertices>386.51,379.52;633.19,235.73;746.63,431.63;594.93,467.9;594.27,467.9<\\/vertices><\\/polyline><\\/add>";
    public static String sAuthor1 = "3D2BFF52D10618CCBC0C446345014CF9";
    private static String sCreateDate1 = "D:20190320123510-07'00'";
    private static String sDate1 = "D:20190320123512-07'00'";

    // annot
    public static String sAnnotId2 = "f4c10b2a-f0c3-44dd-bcb6-b4b30ad39dc9";
    public static String sAnnotXfdf2 = "<add><square style=\\\"solid\\\" width=\\\"5\\\" color=\\\"#E44234\\\" opacity=\\\"1\\\" creationdate=\\\"D:20190326220941Z\\\" flags=\\\"print\\\" date=\\\"D:20190326220941Z\\\" name=\\\"f4c10b2a-f0c3-44dd-bcb6-b4b30ad39dc9\\\" page=\\\"0\\\" rect=\\\"68.1528,33.8239,173.984,120.879\\\" title=\\\"832D8592F7141CB7A7674261C181A3DC\\\" \\/><\\/add>";
    public static String sAuthor2 = "832D8592F7141CB7A7674261C181A3DC";
    private static String sCreateDate2 = "D:20190326220941Z";
    private static String sDate2 = "D:20190326220941Z";

    // reply: parent is annot2, newer than reply5
    public static String sAnnotId3 = "7270f150-5db2-a9ec-da9a-52dfb27ea60b";
    public static String sAnnotXfdf3 = "<add><text xmlns=\\\"http:\\/\\/ns.adobe.com\\/xfdf\\/\\\" page=\\\"0\\\" rect=\\\"287.018,112.801,307.018,132.801\\\" color=\\\"#FFFF00\\\" flags=\\\"print,nozoom,norotate\\\" name=\\\"7270f150-5db2-a9ec-da9a-52dfb27ea60b\\\" title=\\\"515CDE282977B94B8DAD3D24858581E0\\\" subject=\\\"Comment\\\" date=\\\"D:20190321172311-07'00'\\\" creationdate=\\\"D:20190321172311-07'00'\\\" icon=\\\"Comment\\\" inreplyto=\\\"f4c10b2a-f0c3-44dd-bcb6-b4b30ad39dc9\\\"><contents>888<\\/contents><\\/text><\\/add>";
    public static String sAuthor3 = "515CDE282977B94B8DAD3D24858581E0";
    public static String sContent3 = "888";
    private static String sCreateDate3 = "D:20190321172311-07'00'";
    private static String sDate3 = "D:20190321172311-07'00'";

    // reply: parent is annot1
    public static String sAnnotId4 = "-LaRaUAqfVWfJ7N5WQP1";
    public static String sAnnotXfdf4 = "<add><text creationdate=\\\"D:20190320193528Z\\\" flags=\\\"print,nozoom,norotate\\\" inreplyto=\\\"e091eba1-57e3-250e-681b-3df1bc72294c\\\" date=\\\"D:20190320193528Z\\\" name=\\\"-LaRaUAqfVWfJ7N5WQP1\\\" page=\\\"0\\\" rect=\\\"385.508,468.901,405.508,488.901\\\" title=\\\"7F4E7B5703A15E32BFC48C0E5ED5469D\\\">\\n\\t\\t\\t<popup date=\\\"D:20190320193528Z\\\" name=\\\"-LaRaUAqfVWfJ7N5WQP2\\\" page=\\\"0\\\" rect=\\\"405.508,488.901,475.508,558.901\\\" \\/>\\n\\t\\t\\t<contents>324234234234<\\/contents>\\n\\t\\t<\\/text><\\/add>";
    public static String sAuthor4 = "7F4E7B5703A15E32BFC48C0E5ED5469D";
    public static String sContent4 = "324234234234";
    private static String sCreateDate4 = "D:20190320193528Z";
    private static String sDate4 = "D:20190320193528Z";

    // reply: parent is annot2, older than reply3
    public static String sAnnotId5 = "cdcf7061-5392-8793-92fa-cef8ad3b08bd";
    public static String sAnnotXfdf5 = "<add><text xmlns=\\\"http:\\/\\/ns.adobe.com\\/xfdf\\/\\\" page=\\\"0\\\" rect=\\\"287.018,112.801,307.018,132.801\\\" color=\\\"#FFFF00\\\" flags=\\\"print,nozoom,norotate\\\" name=\\\"cdcf7061-5392-8793-92fa-cef8ad3b08bd\\\" title=\\\"515CDE282977B94B8DAD3D24858581E0\\\" subject=\\\"Comment\\\" date=\\\"D:20190321160638-07'00'\\\" creationdate=\\\"D:20190321160638-07'00'\\\" icon=\\\"Comment\\\" inreplyto=\\\"f4c10b2a-f0c3-44dd-bcb6-b4b30ad39dc9\\\"><contents>werwer<\\/contents><\\/text><\\/add>";
    public static String sAuthor5 = "515CDE282977B94B8DAD3D24858581E0";
    public static String sContent5 = "werwer";
    private static String sCreateDate5 = "D:20190321160638-07'00'";
    private static String sDate5 = "D:20190321160638-07'00'";

    public static String sModifiedContent5 = "something else";
    public static String sAnnotModifiedXfdf5 = "<add><text xmlns=\\\"http:\\/\\/ns.adobe.com\\/xfdf\\/\\\" page=\\\"0\\\" rect=\\\"287.018,112.801,307.018,132.801\\\" color=\\\"#FFFF00\\\" flags=\\\"print,nozoom,norotate\\\" name=\\\"cdcf7061-5392-8793-92fa-cef8ad3b08bd\\\" title=\\\"515CDE282977B94B8DAD3D24858581E0\\\" subject=\\\"Comment\\\" date=\\\"D:20190321160638-07'00'\\\" creationdate=\\\"D:20190321160638-07'00'\\\" icon=\\\"Comment\\\" inreplyto=\\\"f4c10b2a-f0c3-44dd-bcb6-b4b30ad39dc9\\\"><contents>something else<\\/contents><\\/text><\\/add>";


    public static String sModifiedContent3 = "works on my machine";
    public static String sAnnotModifiedXfdf3 = "<add><text xmlns=\\\"http:\\/\\/ns.adobe.com\\/xfdf\\/\\\" page=\\\"0\\\" rect=\\\"287.018,112.801,307.018,132.801\\\" color=\\\"#FFFF00\\\" flags=\\\"print,nozoom,norotate\\\" name=\\\"7270f150-5db2-a9ec-da9a-52dfb27ea60b\\\" title=\\\"515CDE282977B94B8DAD3D24858581E0\\\" subject=\\\"Comment\\\" date=\\\"D:20190321172311-07'00'\\\" creationdate=\\\"D:20190321172311-07'00'\\\" icon=\\\"Comment\\\" inreplyto=\\\"f4c10b2a-f0c3-44dd-bcb6-b4b30ad39dc9\\\"><contents>works on my machine<\\/contents><\\/text><\\/add>";

    public static UserEntity createUser() {
        return new UserEntity(sUserId, sUserName, System.currentTimeMillis(), null);
    }

    public static LastAnnotationEntity createLastAnnotation() {
        return new LastAnnotationEntity(sActiveAnnotId, sFakeXfdf);
    }

    public static DocumentEntity createDocument() {
        return new DocumentEntity(sDocumentId, sShareId, System.currentTimeMillis(), null);
    }

    public static AnnotationEntity createAnnot1() {
        return createAnnotImpl(sAnnotId1, sAuthor1, sAnnotXfdf1, sCreateDate1, sDate1);
    }

    public static AnnotationEntity createAnnot2() {
        return createAnnotImpl(sAnnotId2, sAuthor2, sAnnotXfdf2, sCreateDate2, sDate2);
    }

    public static AnnotationEntity createAnnot3() {
        return createAnnotImpl(sAnnotId3, sAuthor3, sAnnotXfdf3, sCreateDate3, sDate3);
    }

    public static AnnotationEntity createModifiedAnnot3() {
        return createAnnotImpl(sAnnotId3, sAuthor3, sAnnotModifiedXfdf3, sCreateDate3, sDate3);
    }

    public static AnnotationEntity createAnnot4() {
        return createAnnotImpl(sAnnotId4, sAuthor4, sAnnotXfdf4, sCreateDate4, sDate4);
    }

    public static AnnotationEntity createAnnot5() {
        return createAnnotImpl(sAnnotId5, sAuthor5, sAnnotXfdf5, sCreateDate5, sDate5);
    }

    public static AnnotationEntity createModifiedAnnot5() {
        return createAnnotImpl(sAnnotId5, sAuthor5, sAnnotModifiedXfdf5, sCreateDate5, sDate5);
    }

    public static ReplyEntity createReply1() {
        return createReplyImpl(sAnnotId3, sAuthor3, sAnnotId2, sContent3, sCreateDate3, sDate3);
    }

    public static ReplyEntity createReply2() {
        return createReplyImpl(sAnnotId4, sAuthor4, sAnnotId1, sContent4, sCreateDate4, sDate4);
    }

    public static ReplyEntity createReply3() {
        return createReplyImpl(sAnnotId5, sAuthor5, sAnnotId2, sContent5, sCreateDate5, sDate5);
    }

    private static AnnotationEntity createAnnotImpl(String id, String authorId,
            String xfdf, String createDateStr, String dateStr) {
        AnnotationEntity entity = new AnnotationEntity();
        entity.setId(id);
        entity.setDocumentId(sDocumentId);
        entity.setAuthorId(authorId);
        // remove escape characters
        xfdf = xfdf.replace("\\", "");
        entity.setXfdf(xfdf);
        XfdfUtils.fillAnnotationEntity(entity);
        Date createDate = XfdfUtils.deserializeDate(createDateStr);
        if (createDate != null) {
            entity.setCreationDate(createDate);
        }
        Date date = XfdfUtils.deserializeDate(dateStr);
        if (date != null) {
            entity.setDate(date);
        }
        entity.setUnreadCount(0);
        return entity;
    }

    private static ReplyEntity createReplyImpl(String id, String authorId,
            String parentId, String content, String createDateStr, String dateStr) {
        ReplyEntity entity = new ReplyEntity();
        entity.setId(id);
        entity.setAuthorId(authorId);
        entity.setInReplyTo(parentId);
        entity.setContents(content);
        Date createDate = XfdfUtils.deserializeDate(createDateStr);
        if (createDate != null) {
            entity.setCreationDate(createDate);
        }
        Date date = XfdfUtils.deserializeDate(dateStr);
        if (date != null) {
            entity.setDate(date);
        }
        return entity;
    }
}
