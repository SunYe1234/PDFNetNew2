//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.android.pdfnetsdksamples;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;

import com.pdftron.android.pdfnetsdksamples.samples.AddImageTest;
import com.pdftron.android.pdfnetsdksamples.samples.AnnotationTest;
import com.pdftron.android.pdfnetsdksamples.samples.BookmarkTest;
import com.pdftron.android.pdfnetsdksamples.samples.ContentReplacerTest;
import com.pdftron.android.pdfnetsdksamples.samples.DigitalSignaturesTest;
import com.pdftron.android.pdfnetsdksamples.samples.ElementBuilderTest;
import com.pdftron.android.pdfnetsdksamples.samples.ElementEditTest;
import com.pdftron.android.pdfnetsdksamples.samples.ElementReaderAdvTest;
import com.pdftron.android.pdfnetsdksamples.samples.ElementReaderTest;
import com.pdftron.android.pdfnetsdksamples.samples.EncTest;
import com.pdftron.android.pdfnetsdksamples.samples.FDFTest;
import com.pdftron.android.pdfnetsdksamples.samples.ImageExtractTest;
import com.pdftron.android.pdfnetsdksamples.samples.ImpositionTest;
import com.pdftron.android.pdfnetsdksamples.samples.InteractiveFormsTest;
import com.pdftron.android.pdfnetsdksamples.samples.JBIG2Test;
import com.pdftron.android.pdfnetsdksamples.samples.LogicalStructureTest;
import com.pdftron.android.pdfnetsdksamples.samples.OptimizerTest;
import com.pdftron.android.pdfnetsdksamples.samples.PDFATest;
import com.pdftron.android.pdfnetsdksamples.samples.PDFDocMemoryTest;
import com.pdftron.android.pdfnetsdksamples.samples.PDFDrawTest;
import com.pdftron.android.pdfnetsdksamples.samples.PDFLayersTest;
import com.pdftron.android.pdfnetsdksamples.samples.PDFPackageTest;
import com.pdftron.android.pdfnetsdksamples.samples.PDFPageTest;
import com.pdftron.android.pdfnetsdksamples.samples.PDFRedactTest;
import com.pdftron.android.pdfnetsdksamples.samples.PageLabelsTest;
//import com.pdftron.android.pdfnetsdksamples.samples.DocxConvertTest;
import com.pdftron.android.pdfnetsdksamples.samples.RectTest;
import com.pdftron.android.pdfnetsdksamples.samples.SDFTest;
import com.pdftron.android.pdfnetsdksamples.samples.StamperTest;
import com.pdftron.android.pdfnetsdksamples.samples.TextExtractTest;
import com.pdftron.android.pdfnetsdksamples.samples.TextSearchTest;
import com.pdftron.android.pdfnetsdksamples.samples.U3DTest;
import com.pdftron.android.pdfnetsdksamples.samples.UnicodeWriteTest;
import com.pdftron.android.pdfnetsdksamples.samples.PatternTest;
import com.pdftron.android.pdfnetsdksamples.samples.OfficeToPDFTest;

public class MiscellaneousSamplesApplication extends Application {

    private List<PDFNetSample> mListSamples = new ArrayList<PDFNetSample>();
    private static MiscellaneousSamplesApplication singleton;
    private Context m_contex;
    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
//        mListSamples.add(new DocxConvertTest());
        mListSamples.add(new AddImageTest());
        mListSamples.add(new AnnotationTest());
        mListSamples.add(new BookmarkTest());
        mListSamples.add(new ContentReplacerTest());
        mListSamples.add(new DigitalSignaturesTest());
        mListSamples.add(new ElementBuilderTest());
        mListSamples.add(new ElementEditTest());
        mListSamples.add(new ElementReaderTest());
        mListSamples.add(new ElementReaderAdvTest());
        mListSamples.add(new EncTest());
        mListSamples.add(new FDFTest());
        mListSamples.add(new ImageExtractTest());
        mListSamples.add(new ImpositionTest());
        mListSamples.add(new InteractiveFormsTest());
        mListSamples.add(new JBIG2Test());
        mListSamples.add(new LogicalStructureTest());
        mListSamples.add(new OptimizerTest());
        mListSamples.add(new PageLabelsTest());
        mListSamples.add(new PatternTest());
        mListSamples.add(new PDFATest());
        mListSamples.add(new PDFDocMemoryTest());
        mListSamples.add(new PDFDrawTest());
        mListSamples.add(new PDFLayersTest());
        mListSamples.add(new PDFPackageTest());
        mListSamples.add(new PDFPageTest());
        mListSamples.add(new PDFRedactTest());
        mListSamples.add(new RectTest());
        mListSamples.add(new SDFTest());
        mListSamples.add(new StamperTest());
        mListSamples.add(new TextExtractTest());
        mListSamples.add(new TextSearchTest());
        mListSamples.add(new U3DTest());
        mListSamples.add(new UnicodeWriteTest());
        mListSamples.add(new OfficeToPDFTest(getApplicationContext()));

        m_contex = getApplicationContext();
    }

    public List<PDFNetSample> getContent() {
        return this.mListSamples;
    }

    public static MiscellaneousSamplesApplication getInstance() {
        return singleton;
    }

    public Context getContext() {return m_contex;}
}
