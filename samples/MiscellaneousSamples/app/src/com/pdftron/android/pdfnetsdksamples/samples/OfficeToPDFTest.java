//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.android.pdfnetsdksamples.samples;

import android.content.Context;

import com.pdftron.android.pdfnetsdksamples.OutputListener;
import com.pdftron.android.pdfnetsdksamples.PDFNetSample;
import com.pdftron.android.pdfnetsdksamples.R;
import com.pdftron.android.pdfnetsdksamples.util.Utils;
import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.Convert;
import com.pdftron.pdf.DocumentConversion;
import com.pdftron.pdf.OfficeToPDFOptions;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFNet;
import com.pdftron.sdf.SDFDoc;

import java.io.File;
import java.util.ArrayList;

/**
 * The following sample illustrates how to use the PDF.Convert utility class to convert
 * .docx files to PDF
 * <p>
 * This conversion is performed entirely within the PDFNet and has *no* external or
 * system dependencies dependencies -- Conversion results will be the sam whether
 * on Windows, Linux or Android.
 * <p>
 * Please contact us if you have any questions.
 */
public class OfficeToPDFTest extends PDFNetSample {

	private static OutputListener mOutputListener;

	private static ArrayList<String> mFileList = new ArrayList<>();

    public OfficeToPDFTest(Context context) {
        try {
            String layoutPluginPath = Utils.copyResourceToTempFolder(context, R.raw.pdftron_layout_resources, false, "pdftron_layout_resources.plugin");
            PDFNet.addResourceSearchPath(layoutPluginPath);
            String layoutSmartPluginPath = Utils.copyResourceToTempFolder(context, R.raw.pdftron_smart_substitution, false, "pdftron_smart_substitution.plugin");
            PDFNet.addResourceSearchPath(layoutSmartPluginPath);
        } catch (Exception e) {
            mOutputListener.println(e.getStackTrace());
        }

        setTitle(R.string.sample_officetopdf_title);
        setDescription(R.string.sample_officetopdf_description);
    }

	@Override
	public void run(OutputListener outputListener) {
		super.run(outputListener);
		mOutputListener = outputListener;
		mFileList.clear();
		printHeader(outputListener);
        File resFile = Utils.getAssetTempFile(INPUT_PATH + "pdftron_smart_substitution.plugin");
		PDFNet.setResourcesPath(resFile.getParent());

        // first the one-line conversion interface
        simpleDocxConvert("simple-word_2007.docx", "simple-word_2007_a.pdf");

        // then the more flexible line-by-line interface
        flexibleDocxConvert("the_rime_of_the_ancient_mariner.docx", "the_rime_of_the_ancient_mariner.pdf");


		for (String file : mFileList) {
			addToFileList(file);
		}
		printFooter(outputListener);
	}


    public static void simpleDocxConvert(String inputFilename, String outputFilename) {
        try {

            // perform the conversion with no optional parameters
            PDFDoc pdfdoc = new PDFDoc();
            Convert.officeToPdf(pdfdoc, Utils.getAssetTempFile(INPUT_PATH + inputFilename).getAbsolutePath(), null);

            // save the result
            pdfdoc.save(Utils.createExternalFile(outputFilename).getAbsolutePath(), SDFDoc.SaveMode.INCREMENTAL, null);
            mFileList.add(new File(pdfdoc.getFileName()).getName());

            // And we're done!
            mOutputListener.println("Done conversion " + Utils.createExternalFile(outputFilename).getAbsolutePath());
        } catch (PDFNetException e) {
            mOutputListener.println("Unable to convert MS Office document, error:");
            e.printStackTrace();
            mOutputListener.println(e.getStackTrace());
        }
    }

    public static void flexibleDocxConvert(String inputFilename, String outputFilename) {
        try {
            OfficeToPDFOptions options = new OfficeToPDFOptions();
            options.setSmartSubstitutionPluginPath(INPUT_PATH);

            // create a conversion object -- this sets things up but does not yet
            // perform any conversion logic.
            // in a multithreaded environment, this object can be used to monitor
            // the conversion progress and potentially cancel it as well
            DocumentConversion conversion = Convert.streamingPdfConversion(
                    Utils.getAssetTempFile(INPUT_PATH + inputFilename).getAbsolutePath(), options);

            mOutputListener.println(inputFilename + ": " + Math.round(conversion.getProgress() * 100.0)
                    + "% " + conversion.getProgressLabel());

            // actually perform the conversion
            while (conversion.getConversionStatus() == DocumentConversion.e_incomplete) {
                conversion.convertNextPage();
                mOutputListener.println(inputFilename + ": " + Math.round(conversion.getProgress() * 100.0)
                        + "% " + conversion.getProgressLabel());
            }

            if (conversion.tryConvert() == DocumentConversion.e_success) {
                int num_warnings = conversion.getNumWarnings();

                // print information about the conversion
                for (int i = 0; i < num_warnings; ++i) {
                    mOutputListener.println("Warning: " + conversion.getWarningString(i));
                }

                // save the result
                PDFDoc doc = conversion.getDoc();
                doc.save(Utils.createExternalFile(outputFilename).getAbsolutePath(), SDFDoc.SaveMode.INCREMENTAL, null);
                mFileList.add(new File(doc.getFileName()).getName());

                // done
                mOutputListener.println("Done conversion " + Utils.createExternalFile(outputFilename).getAbsolutePath());
            } else {
                mOutputListener.println("Encountered an error during conversion: " + conversion.getErrorString());
            }
        } catch (PDFNetException e) {
            mOutputListener.println("Unable to convert MS Office document, error:");
            e.printStackTrace();
            mOutputListener.println(e.getStackTrace());
        }
    }

}
