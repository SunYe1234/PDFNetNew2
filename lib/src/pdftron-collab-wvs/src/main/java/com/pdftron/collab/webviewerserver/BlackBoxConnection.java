package com.pdftron.collab.webviewerserver;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.pdftron.collab.db.CollabDatabase;
import com.pdftron.collab.db.entity.AnnotationEntity;
import com.pdftron.collab.db.entity.DocumentEntity;
import com.pdftron.collab.service.CustomService;
import com.pdftron.collab.utils.JsonUtils;
import com.pdftron.collab.utils.Keys;
import com.pdftron.collab.utils.XfdfUtils;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.Logger;
import com.pdftron.pdf.utils.Utils;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Handling web socket connection from client to WebViewer Server.
 */
public class BlackBoxConnection extends WebSocketListener implements CustomService {

    private static final String TAG = BlackBoxConnection.class.getName();

    private static final int NORMAL_CLOSURE_STATUS = 1000;

    private OkHttpClient client;

    private String mServerRoot;

    private final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private CompositeDisposable mDisposables;
    private WebSocket mWebSocket;

    private final CollabDatabase mCollabDatabase;

    private String mDocId;

    private String mUserId;
    private String mUsername;

    /**
     * Service for WebViewer Server
     */
    public BlackBoxConnection(@NonNull final Context applicationContext) {
        mCollabDatabase = CollabDatabase.getInstance(applicationContext);
    }

    /**
     * Starts a collaboration session, a new random user will be generated and used for the session
     *
     * @param wvsRoot    the root address of your WebViewer Server instance
     * @param wvsFileUrl the Url to the file to be collaborated on
     * @param wvsShareId the unique identifier for an existing collaboration session, pass null to generate a new session
     */
    public void start(@NonNull String wvsRoot, @NonNull String wvsFileUrl, @Nullable String wvsShareId) {
        start(wvsRoot, wvsFileUrl, wvsShareId, null, null);
    }

    /**
     * Starts a collaboration session
     *
     * @param wvsRoot    the root address of your WebViewer Server instance
     * @param wvsFileUrl the Uri to the file to be collaborated on
     * @param wvsShareId the unique identifier for an existing collaboration session, pass null to generate a new session
     * @param userId     the unique identifier of the user
     * @param userName   the name of the user
     */
    public void start(@NonNull String wvsRoot, @NonNull String wvsFileUrl, @Nullable String wvsShareId, @Nullable String userId, @Nullable String userName) {
        // user
        if (!Utils.isNullOrEmpty(userId)) {
            mUserId = userId;
            mUsername = userName;
        }

        // process root
        String blackbox = "blackbox";
        if (wvsRoot.contains(blackbox)) {
            int index = wvsRoot.indexOf(blackbox);
            wvsRoot = wvsRoot.substring(0, index);
        }
        if (!wvsRoot.endsWith("/")) {
            wvsRoot = wvsRoot + "/";
        }
        mServerRoot = wvsRoot;
        client = new OkHttpClient.Builder()
                .build();
        mDisposables = new CompositeDisposable();

        debugOutput("start connection for [" + mServerRoot + "] [" + wvsShareId + "] [" + wvsFileUrl + "]");

        if (null == wvsShareId) {
            // no share id, let's try fetching one
            mDisposables.add(getShareId(wvsFileUrl)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(responsePair -> {
                        if (responsePair.successful) {
                            JSONObject documentObj = new JSONObject(responsePair.body);
                            if (documentObj.has(Keys.DOCUMENT_SHARE_ID)) {
                                String shareId = documentObj.getString(Keys.DOCUMENT_SHARE_ID);
                                debugOutput("getShareId: " + shareId);
                                startImpl(wvsFileUrl, shareId);
                            }
                        }
                    }, throwable -> {
                        debugOutput("getShareId error");
                        if (throwable != null) {
                            throwable.printStackTrace();
                        }
                    })
            );
        } else {
            startImpl(wvsFileUrl, wvsShareId);
        }
    }

    /**
     * Stops the current collaboration session
     * All resources are cleaned up
     */
    public void stop() {
        client = null;
        if (mWebSocket != null) {
            mWebSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye!");
        }
        mWebSocket = null;
        if (mDisposables != null) {
            mDisposables.add(
                    nukeDB()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::cleanup,
                                    throwable -> errorOutput("nukeDB failed")));
        }
    }

    private void startImpl(@NonNull String wvsFileUrl, @NonNull String wvsShareId) {
        mDisposables.add(Single.zip(createFromUri(wvsFileUrl, wvsShareId), getSessionInfo(), Pair::new)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(p -> {
                    if (p.first != null && p.second != null) {
                        if (p.first.successful && p.second.successful) {

                            JSONObject documentObj = new JSONObject(p.first.body);
                            String docId = documentObj.getString(Keys.DOCUMENT_ID);

                            if (getAnnotApiRoot() == null) {
                                errorOutput("server root is not supplied");
                            } else {
                                if (docId != null) {
                                    initiateCollaboration(docId, getAnnotApiRoot());
                                }
                            }
                        }
                    }
                }, throwable -> {
                    debugOutput("start error");
                    if (throwable != null) {
                        throwable.printStackTrace();
                    }
                })
        );
    }

    private void cleanup() {
        debugOutput("cleanup...");
        if (mDisposables != null) {
            mDisposables.clear();
        }
        mDisposables = null;
    }

    private void initiateCollaboration(@NonNull String docId, @NonNull String serverRoot) {
        mDocId = docId;
        Request request = new Request.Builder().url(serverRoot).build();
        mWebSocket = client.newWebSocket(request, this);

        client.dispatcher().executorService().shutdown();
    }

    private Completable nukeDB() {
        return Completable.fromAction(() -> {
            cleanup(mCollabDatabase);
        });
    }

    private Single<ResponsePair> getSessionInfo() {
        return Single.fromCallable(this::getSessionInfoImpl);
    }

    private ResponsePair getSessionInfoImpl() {
        try {
            if (mUserId != null) {
                safeInsertUser(mUserId, mUsername);
                JSONObject user = new JSONObject();
                user.put(Keys.USER_ID, mUserId);
                user.put(Keys.USER_NAME, mUsername);
                return new ResponsePair(HttpURLConnection.HTTP_OK, true, user.toString());
            } else {
                String url = mServerRoot + "demo/SessionInfo.jsp";
                ResponsePair response = get(url, null);
                if (response != null && response.successful) {
                    JSONObject sessionInfo = new JSONObject(response.body);
                    String userId = sessionInfo.getString(Keys.USER_ID);
                    String userName = sessionInfo.getString(Keys.USER_NAME);

                    if (userId != null) {
                        safeInsertUser(userId, userName);
                    }
                }
                return response;
            }
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
        return null;
    }

    private Single<ResponsePair> createFromUri(String uri, String shareId) {
        return Single.fromCallable(() -> createFromUriImpl(uri, shareId));
    }

    private ResponsePair createFromUriImpl(String uri, String shareId) {
        try {
            if (getApiRoot() == null) {
                errorOutput("server root is not supplied");
                return null;
            }
            JSONObject json = documentJson(uri, shareId);
            String url = getApiRoot() + "AddDocToSession";
            ResponsePair response = get(url, json);

            if (response != null && response.successful) {
                JSONObject documentObj = new JSONObject(response.body);
                String docId = documentObj.getString(Keys.DOCUMENT_ID);
                String newShareId = documentObj.getString(Keys.DOCUMENT_SHARE_ID);
                if (docId != null) {
                    safeInsertDocument(docId, newShareId);
                }
            }

            return response;
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
        return null;
    }

    private Single<ResponsePair> getShareId(String fileUrl) {
        return Single.fromCallable(() -> getShareIdImpl(fileUrl));
    }

    private ResponsePair getShareIdImpl(String fileUrl) {
        try {
            if (getBlackboxRoot() == null) {
                errorOutput("server root is not supplied");
            }
            String type = "url";

            String ext = FilenameUtils.getExtension(fileUrl);

            String url = Uri.parse(getBlackboxRoot() + "AuxUpload")
                    .buildUpon()
                    .appendQueryParameter("ext", ext)
                    .appendQueryParameter("type", type)
                    .appendQueryParameter("uri", fileUrl)
                    .build().toString();

            debugOutput("getShareIdImpl: " + url);

            ResponsePair pair = post(url, "");
            if (pair != null && pair.successful) {
                debugOutput("getShareIdImpl: " + pair.body);
                ResponsePair p2 = createFromUriImpl(fileUrl, null);
                if (p2 != null) {
                    debugOutput("getShareIdImpl p2: " + p2.body);
                    return p2;
                }
            }
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
        return null;
    }

    private JSONObject documentJson(@NonNull String uri, @Nullable String shareId) throws JSONException {
        JSONObject docJson = new JSONObject();
        docJson.put(Keys.DOCUMENT_URI, uri);
        if (shareId != null) {
            docJson.put(Keys.DOCUMENT_SHARE, shareId);
        }
        String ext = FilenameUtils.getExtension(uri);
        docJson.put(Keys.DOCUMENT_EXT, "." + ext);

        String str = docJson.toString();
        debugOutput("documentJson: " + str);

        return docJson;
    }

    private ResponsePair post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return new ResponsePair(response.code(), response.isSuccessful(), response.body().string());
            } else {
                return null;
            }
        }
    }

    private ResponsePair get(String url, JSONObject params) throws IOException, JSONException, NullPointerException {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            debugOutput("unable to parse url: " + url);
            return null;
        }
        HttpUrl.Builder httpBuilder = httpUrl.newBuilder();
        if (params != null) {
            httpBuilder.addQueryParameter(Keys.DOCUMENT_URI, params.getString(Keys.DOCUMENT_URI));
            if (params.has(Keys.DOCUMENT_SHARE)) {
                httpBuilder.addQueryParameter(Keys.DOCUMENT_SHARE, params.getString(Keys.DOCUMENT_SHARE));
            }
            httpBuilder.addQueryParameter(Keys.DOCUMENT_EXT, params.getString(Keys.DOCUMENT_EXT));
        }

        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return new ResponsePair(response.code(), response.isSuccessful(), response.body().string());
            } else {
                return null;
            }
        }
    }

    private void safeInsertUser(String userId, String userName) {
        try {
            debugOutput("id: " + userId);
            debugOutput("user_name: " + userName);
            addUser(mCollabDatabase, userId, userName);
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
    }

    private void safeInsertDocument(String docId, String shareId) {
        try {
            debugOutput("doc_id: " + docId);
            DocumentEntity documentEntity = new DocumentEntity(docId, shareId,
                    System.currentTimeMillis(), null);
            addDocument(mCollabDatabase, documentEntity);
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
    }

    private void safeInsertAnnotation(JSONObject annotation) {
        debugOutput("insert: " + annotation.toString());
        if (!JsonUtils.isValidInsertEntity(annotation)) {
            return;
        }
        AnnotationEntity annotationEntity = JsonUtils.parseRetrieveMessage(annotation, mDocId);
        addAnnotation(mCollabDatabase, annotationEntity);
    }

    private void safeModifyAnnotation(JSONObject annotation) {
        debugOutput("modify: " + annotation.toString());
        if (!JsonUtils.isValidUpdateEntity(annotation)) {
            return;
        }
        AnnotationEntity entity = XfdfUtils.xfdfToAnnotationEntity(annotation);
        modifyAnnotation(mCollabDatabase, entity);
    }

    private void safeDeleteAnnotation(JSONObject annotation) {
        debugOutput("delete: " + annotation.toString());
        if (!JsonUtils.isValidDeleteEntity(annotation)) {
            return;
        }
        try {
            String id = annotation.getString(Keys.ANNOT_ID);
            deleteAnnotation(mCollabDatabase, id);
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
    }

    private String getApiRoot() {
        if (mServerRoot == null) {
            return null;
        }
        return mServerRoot + "demo/";
    }

    private String getAnnotApiRoot() {
        if (mServerRoot == null) {
            return null;
        }
        return mServerRoot + "blackbox/annot";
    }

    private String getBlackboxRoot() {
        if (mServerRoot == null) {
            return null;
        }
        return mServerRoot + "blackbox/";
    }

    // CustomService start

    @Override
    public void sendAnnotation(String action, ArrayList<AnnotationEntity> annotations, String documentId, @Nullable String userName) {
        if (Utils.isNullOrEmpty(action) || Utils.isNullOrEmpty(documentId)) {
            return;
        }

        try {
            String result = XfdfUtils.prepareAnnotation(action, annotations, documentId, userName);
            debugOutput("sendAnnotation result: " + result);
            if (mWebSocket != null && result != null) {
                mWebSocket.send(result);
            }
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
    }

    // CustomService end

    // WebSocketListener start

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Keys.WS_ACTION_KEY_T, Keys.WS_ACTION_RETRIEVE);
            jsonObject.put(Keys.ANNOT_DOCUMENT_ID, mDocId);

            webSocket.send(jsonObject.toString());
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        debugOutput("Receiving : ");

        if (text.contains("hb")) {
            debugOutput("msg: " + text);
        }

        try {
            JSONObject jsonObject = new JSONObject(text);

            if (jsonObject.has(Keys.WS_ACTION_KEY_HB)) {
                JSONObject outObject = new JSONObject();
                outObject.put(Keys.WS_ACTION_KEY_HB, true);
                webSocket.send(outObject.toString());
                return;
            }
            if (jsonObject.has(Keys.WS_ACTION_KEY_END)) {
                webSocket.close(NORMAL_CLOSURE_STATUS, "Server requested close. Goodbye!");
                return;
            }

            if (jsonObject.has(Keys.WS_ACTION_KEY_T) &&
                    jsonObject.has(Keys.WS_ACTION_KEY_DATA)) {
                String action = jsonObject.getString(Keys.WS_ACTION_KEY_T);
                Object data = jsonObject.get(Keys.WS_ACTION_KEY_DATA);
                HashMap<String, AnnotationEntity> initialAnnots = new HashMap<>();

                if (action.equals(Keys.WS_ACTION_RETRIEVE)) {
                    debugOutput("a_retrieve START");
                }

                if (data instanceof JSONArray) {
                    JSONArray dataArr = jsonObject.getJSONArray(Keys.WS_ACTION_KEY_DATA);
                    for (int i = 0; i < dataArr.length(); i++) {
                        Object value = dataArr.get(i);
                        if (value instanceof JSONObject) {
                            JSONObject annotJson = dataArr.getJSONObject(i);
                            if (action.equals(Keys.WS_ACTION_RETRIEVE)) {
                                AnnotationEntity entity = JsonUtils.parseRetrieveMessage(annotJson, mDocId);
                                if (entity != null) {
                                    initialAnnots.put(entity.getId(), entity);
                                }
                            } else if (action.equals(Keys.WS_ACTION_CREATE)) {
                                safeInsertAnnotation(annotJson);
                            } else if (action.equals(Keys.WS_ACTION_MODIFY)) {
                                safeModifyAnnotation(annotJson);
                            } else if (action.equals(Keys.WS_ACTION_DELETE)) {
                                safeDeleteAnnotation(annotJson);
                            }
                        }
                    }
                }
                if (action.equals(Keys.WS_ACTION_RETRIEVE)) {
                    addAnnotations(mCollabDatabase, initialAnnots);
                    debugOutput("a_retrieve END");
                }
            }
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        debugOutput("Closing : " + code + " / " + reason);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        debugOutput("Closed : " + code + " / " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        errorOutput("Error : " + t.getMessage() + " | " + response);
    }

    // WebSocketListener end

    private void debugOutput(String msg) {
        Logger.INSTANCE.LogD(TAG, msg);
    }

    private void errorOutput(String msg) {
        Logger.INSTANCE.LogE(TAG, msg);
    }

    static class ResponsePair {
        int code;
        boolean successful;
        String body;

        ResponsePair(int code, boolean successful, String body) {
            this.code = code;
            this.successful = successful;
            this.body = body;
        }
    }
}
