package com.pdftron.demo.app;

class CheckDifferentFileTypeResult {

    private final int fileType;
    private final boolean openDocument;
    private final String password;

    CheckDifferentFileTypeResult(int fileType, boolean openDocument, String password) {
        this.fileType = fileType;
        this.openDocument = openDocument;
        this.password = password;
    }

    int getFileType() {
        return this.fileType;
    }

    boolean getOpenDocument() {
        return this.openDocument;
    }

    String getPassword() {
        return this.password;
    }
}
