package com.pdftron.collab.utils;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.collab.db.entity.AnnotationEntity;
import com.pdftron.collab.db.entity.ReplyEntity;
import com.pdftron.common.PDFNetException;
import com.pdftron.fdf.FDFDoc;
import com.pdftron.pdf.Annot;
import com.pdftron.pdf.Rect;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.AnnotUtils;
import com.pdftron.pdf.utils.Logger;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.sdf.Obj;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Utility class for XFDF
 */
public class XfdfUtils {

    private static final String TAG = XfdfUtils.class.getName();

    public static final String XFDF_START = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<xfdf xmlns=\"http://ns.adobe.com/xfdf/\" xml:space=\"preserve\">";
    public static final String XFDF_END = "</xfdf>";

    private static final String XML_ADD_START = "<add>";
    private static final String XML_ADD_END = "</add>";
    private static final String XML_MODIFY_START = "<modify>";
    private static final String XML_MODIFY_END = "</modify>";

    private static final String OP_ADD = "add";
    private static final String OP_MODIFY = "modify";
    private static final String OP_REMOVE = "remove";
    private static final String WS_DELETE = "delete";
    private static final String WS_CREATE = "create";

    /**
     * Utility method to return a valid XFDF command string
     */
    public static String validateXfdf(@NonNull String xfdf) {
        if (!xfdf.startsWith("<?xml")) {
            return XFDF_START + xfdf + XFDF_END;
        }
        return xfdf;
    }

    /**
     * Parses XFDF string into map of properties
     */
    public static HashMap<String, Object> parseXfdf(@NonNull String xfdf) {
        FDFDoc fdfDoc = null;
        try {
            xfdf = xfdf.replace("<add>", "");
            xfdf = xfdf.replace("</add>", "");
            xfdf = xfdf.replace("<modify>", "");
            xfdf = xfdf.replace("</modify>", "");
            if (!xfdf.startsWith("<?xml")) {
                xfdf = XFDF_START + xfdf + XFDF_END;
            }

            fdfDoc = FDFDoc.createFromXFDF(xfdf);
            Obj root = fdfDoc.getRoot();
            if (root != null) {
                Obj fdf = root.findObj(Keys.FDF_FDF);
                if (fdf != null) {
                    Obj annots = fdf.findObj(Keys.FDF_ANNOTS);
                    if (annots != null && annots.isArray()) {
                        HashMap<String, Object> map = new HashMap<>();
                        long size = annots.size();
                        for (int i = 0; i < size; i++) {
                            Obj annotObj = annots.getAt(i);
                            if (annotObj != null) {
                                Annot annot = new Annot(annotObj);

                                int type = annot.getType();
                                Rect rect = annot.getRect();
                                rect.normalize();
                                double yPos = rect.getY2();
                                int color = AnnotUtils.getAnnotColor(annot);
                                float opacity = AnnotUtils.getAnnotOpacity(annot);

                                String content = safeGetObjAsString(annotObj, Keys.FDF_CONTENTS);
                                String inReplyTo = safeGetObjAsString(annotObj, Keys.FDF_IN_REPLY_TO);
                                Date creationDate = deserializeDate(safeGetObjAsString(annotObj, Keys.FDF_CREATION_DATE));
                                Date date = deserializeDate(safeGetObjAsString(annotObj, Keys.FDF_DATE));
                                Integer page = safeGetObjAsInteger(annotObj, Keys.FDF_PAGE);

                                if (content != null) {
                                    map.put(Keys.FDF_CONTENTS, content);
                                }
                                if (inReplyTo != null) {
                                    map.put(Keys.FDF_IN_REPLY_TO, inReplyTo);
                                }
                                if (creationDate != null) {
                                    map.put(Keys.FDF_CREATION_DATE, creationDate);
                                }
                                if (date != null) {
                                    map.put(Keys.FDF_DATE, date);
                                }
                                if (page != null) {
                                    map.put(Keys.FDF_PAGE, String.valueOf(page + 1));
                                }
                                map.put(Keys.FDF_TYPE, String.valueOf(type));
                                map.put(Keys.FDF_YPOS, String.valueOf(yPos));
                                map.put(Keys.FDF_COLOR, String.valueOf(color));
                                map.put(Keys.FDF_OPACITY, String.valueOf(opacity));
                            }
                        }
                        return map;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.INSTANCE.LogE(TAG, "error parsing XML: " + xfdf);
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        } finally {
            if (fdfDoc != null) {
                Utils.closeQuietly(fdfDoc);
            }
        }
        return null;
    }

    private static String safeGetObjAsString(Obj obj, String key) throws PDFNetException {
        if (obj != null) {
            Obj result = obj.findObj(key);
            if (result != null && result.isString()) {
                return result.getAsPDFText();
            }
        }
        return null;
    }

    private static Integer safeGetObjAsInteger(Obj obj, String key) throws PDFNetException {
        if (obj != null) {
            Obj result = obj.findObj(key);
            if (result != null && result.isNumber()) {
                double number = result.getNumber();
                return (int) number;
            }
        }
        return null;
    }

    /**
     * Converts date string to {@link java.util.Date}
     */
    @Nullable
    public static Date deserializeDate(String dateStr) {
        if (dateStr != null) {
            try {
                int year = Integer.parseInt(dateStr.substring(2, 6), 10);
                int month = Integer.parseInt(dateStr.substring(6, 8), 10) - 1;
                int day = Integer.parseInt(dateStr.substring(8, 10), 10);
                int hour = Integer.parseInt(dateStr.substring(10, 12), 10);
                int minute = Integer.parseInt(dateStr.substring(12, 14), 10);
                int second = Integer.parseInt(dateStr.substring(14, 16), 10);

                TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
                Calendar calendar = Calendar.getInstance(utcTimeZone);
                calendar.set(year, month, day, hour, minute, second);

                String timezone = dateStr.substring(16);
                // e.g. -08'00' or Z00'00'
                if (timezone.length() == 7) {
                    String timezoneDirection = timezone.substring(0, 1);
                    if (!timezoneDirection.equals("Z")) {
                        // if the timezone is subtracting from UTC then we need to add back the hours
                        int timezoneMultiplier = timezoneDirection.equals("-") ? 1 : -1;
                        int timezoneHours = Integer.parseInt(timezone.substring(1, 3), 10);
                        int timezoneMinutes = Integer.parseInt(timezone.substring(4, 6), 10);
                        hour = hour + (timezoneMultiplier * timezoneHours);
                        minute = minute + (timezoneMultiplier * timezoneMinutes);
                        calendar.set(year, month, day, hour, minute, second);
                    }
                }

                int offset = utcTimeZone.getRawOffset() + utcTimeZone.getDSTSavings();
                long localTime = calendar.getTimeInMillis() + offset;
                return new Date(localTime);
            } catch (Exception ex) {
                AnalyticsHandlerAdapter.getInstance().sendException(ex);
            }
        }
        return null;
    }

    /**
     * Parse reply information from {@link AnnotationEntity}
     */
    @Nullable
    public static ReplyEntity parseReplyEntity(@NonNull AnnotationEntity annotation) {
        if (annotation.getId() != null &&
                annotation.getAuthorId() != null &&
                annotation.getInReplyTo() != null &&
                annotation.getContents() != null &&
                annotation.getCreationDate() != null &&
                annotation.getDate() != null) {
            ReplyEntity replyEntity = new ReplyEntity();
            replyEntity.setId(annotation.getId());
            replyEntity.setAuthorId(annotation.getAuthorId());
            replyEntity.setAuthorName(annotation.getAuthorName());
            replyEntity.setInReplyTo(annotation.getInReplyTo());
            replyEntity.setContents(annotation.getContents());
            replyEntity.setCreationDate(annotation.getCreationDate());
            replyEntity.setDate(annotation.getDate());
            replyEntity.setPage(annotation.getPage());
            return replyEntity;
        }
        return null;
    }

    /**
     * Parse XFDF information from JSON into {@link AnnotationEntity}
     */
    @Nullable
    public static AnnotationEntity parseAnnotationEntity(String documentId, JSONObject annotation) {
        AnnotationEntity entity = xfdfToAnnotationEntity(annotation);
        if (entity == null) {
            return null;
        }
        if (documentId != null) {
            entity.setDocumentId(documentId);
        }

        if (annotation.has(Keys.ANNOT_ID) &&
                annotation.has(Keys.ANNOT_XFDF)) {
            return entity;
        }
        return null;
    }

    /**
     * Parse required fields for {@link AnnotationEntity} from its XFDF information
     */
    public static void fillAnnotationEntity(AnnotationEntity input) {
        HashMap<String, Object> xfdfMap = parseXfdf(input.getXfdf());
        if (xfdfMap != null) {
            Date creationDate = (Date) xfdfMap.get(Keys.FDF_CREATION_DATE);
            if (creationDate != null) {
                input.setCreationDate(creationDate);
            }
            Date date = (Date) xfdfMap.get(Keys.FDF_DATE);
            if (date != null) {
                input.setDate(date);
            }
            String yPos = (String) xfdfMap.get(Keys.FDF_YPOS);
            if (yPos != null) {
                input.setYPos(Double.parseDouble(yPos));
            }
            String color = (String) xfdfMap.get(Keys.FDF_COLOR);
            if (color == null) {
                color = String.valueOf(Color.TRANSPARENT);
            }
            input.setColor(Integer.parseInt(color));
            String opacity = (String) xfdfMap.get(Keys.FDF_OPACITY);
            if (opacity == null) {
                opacity = String.valueOf(1.0f);
            }
            input.setOpacity(Float.parseFloat(opacity));
            String page = (String) xfdfMap.get(Keys.FDF_PAGE);
            if (page != null) {
                input.setPage(Integer.parseInt(page));
            }
            String type = (String) xfdfMap.get(Keys.FDF_TYPE);
            if (type != null) {
                input.setType(Integer.parseInt(type));
            }
            String content = (String) xfdfMap.get(Keys.FDF_CONTENTS);
            if (content != null) {
                input.setContents(content);
            }
            String inReplyTo = (String) xfdfMap.get(Keys.FDF_IN_REPLY_TO);
            if (inReplyTo != null) {
                input.setInReplyTo(inReplyTo);
            }
        }
    }

    /**
     * Creates {@link AnnotationEntity} from JSON that contains required information
     */
    @Nullable
    public static AnnotationEntity xfdfToAnnotationEntity(JSONObject annotation) {
        AnnotationEntity annotationEntity = new AnnotationEntity();
        try {
            if (annotation.has(Keys.ANNOT_ID)) {
                annotationEntity.setId(annotation.getString(Keys.ANNOT_ID));
            }
            if (annotation.has(Keys.ANNOT_DOCUMENT_ID)) {
                annotationEntity.setDocumentId(annotation.getString(Keys.ANNOT_DOCUMENT_ID));
            }
            if (annotation.has(Keys.ANNOT_AUTHOR_ID)) {
                annotationEntity.setAuthorId(annotation.getString(Keys.ANNOT_AUTHOR_ID));
            }
            if (annotation.has(Keys.ANNOT_AUTHOR_NAME)) {
                annotationEntity.setAuthorName(annotation.getString(Keys.ANNOT_AUTHOR_NAME));
            }
            if (annotation.has(Keys.ANNOT_PARENT)) {
                annotationEntity.setParent(annotation.getString(Keys.ANNOT_PARENT));
            }
            if (annotation.has(Keys.ANNOT_XFDF)) {
                String xfdf = annotation.getString(Keys.ANNOT_XFDF);
                annotationEntity.setXfdf(xfdf);
                fillAnnotationEntity(annotationEntity);
            }
            if (annotation.has(Keys.ANNOT_ACTION)) {
                annotationEntity.setAt(annotation.getString(Keys.ANNOT_ACTION));
            }
            annotationEntity.setUnreadCount(0);
            return annotationEntity;
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
        return null;
    }

    /**
     * Gets delete XFDF command
     */
    public static String wrapDeleteXfdf(String id) {
        return "<delete><id>" + id + "</id></delete>";
    }

    /**
     * Converts JSON with annotation properties to list of {@link AnnotationEntity}
     */
    @NonNull
    public static ArrayList<AnnotationEntity> convToAnnotations(@NonNull String xfdfJSON,
            @NonNull String documentId, @Nullable String userName) throws JSONException {
        ArrayList<AnnotationEntity> annotationList = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(xfdfJSON);
        if (jsonObject.has(Keys.CORE_DATA_ANNOTS)) {
            JSONArray annots = jsonObject.getJSONArray(Keys.CORE_DATA_ANNOTS);
            for (int i = 0; i < annots.length(); i++) {
                AnnotationEntity annotationEntity = new AnnotationEntity();
                annotationEntity.setDocumentId(documentId);
                boolean canAdd = true;
                JSONObject xfdfObj = annots.getJSONObject(i);
                String op = "";
                if (xfdfObj.has(Keys.CORE_DATA_OP)) {
                    op = xfdfObj.getString(Keys.CORE_DATA_OP);
                    annotationEntity.setAt(op);
                } else {
                    canAdd = false;
                }
                if (xfdfObj.has(Keys.CORE_DATA_ID)) {
                    annotationEntity.setId(xfdfObj.getString(Keys.CORE_DATA_ID));
                } else {
                    canAdd = false;
                }
                if (op.equals(OP_ADD)) {
                    if (xfdfObj.has(Keys.CORE_DATA_AUTHOR)) {
                        annotationEntity.setAuthorId(xfdfObj.getString(Keys.CORE_DATA_AUTHOR));
                    }
                    if (userName != null) {
                        annotationEntity.setAuthorName(userName);
                    }
                }
                if (xfdfObj.has(Keys.CORE_DATA_XFDF)) {
                    String xfdf = xfdfObj.getString(Keys.CORE_DATA_XFDF);
                    if (op.equals(OP_ADD)) {
                        annotationEntity.setXfdf(XML_ADD_START + xfdf + XML_ADD_END);
                    } else if (op.equals(OP_MODIFY)) {
                        annotationEntity.setXfdf(XML_MODIFY_START + xfdf + XML_MODIFY_END);
                    }
                }
                if (canAdd) {
                    annotationList.add(annotationEntity);
                }
            }
        }
        return annotationList;
    }

    @Nullable
    public static String prepareAnnotation(@NonNull String action,
            @NonNull ArrayList<AnnotationEntity> annotationEntities,
            @NonNull String documentId,
            @Nullable String userName) throws JSONException {
        JSONObject result = new JSONObject();
        JSONArray resultArray = new JSONArray();

        for (AnnotationEntity annotation : annotationEntities) {
            JSONObject resultObj = new JSONObject();
            String op = annotation.getAt();
            if (op != null) {
                switch (op) {
                    case OP_ADD:
                        resultObj.put(Keys.WS_DATA_AT, WS_CREATE);
                        break;
                    case OP_REMOVE:
                        resultObj.put(Keys.WS_DATA_AT, WS_DELETE);
                        break;
                    default:
                        resultObj.put(Keys.WS_DATA_AT, op);
                        break;
                }
                resultObj.put(Keys.WS_DATA_AID, annotation.getId());
                if (op.equals(OP_ADD)) {
                    resultObj.put(Keys.WS_DATA_AUTHOR, annotation.getAuthorId());
                    if (userName != null) {
                        resultObj.put(Keys.WS_DATA_ANAME, userName);
                    }
                }
                if (op.equals(OP_ADD) || op.equals(OP_MODIFY)) {
                    resultObj.put(Keys.WS_DATA_XFDF, annotation.getXfdf());
                }
                resultArray.put(resultObj);
            }
        }
        if (resultArray.length() > 0) {
            result.put(Keys.WS_DATA_ANNOTS, resultArray);
            result.put(Keys.WS_ACTION_KEY_T, "a_" + action);
            result.put(Keys.WS_DATA_DID, documentId);
            return result.toString();
        }
        return null;
    }
}
