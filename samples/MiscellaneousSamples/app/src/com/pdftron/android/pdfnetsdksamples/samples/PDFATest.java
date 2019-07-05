//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.android.pdfnetsdksamples.samples;

import com.pdftron.android.pdfnetsdksamples.OutputListener;
import com.pdftron.android.pdfnetsdksamples.PDFNetSample;
import com.pdftron.android.pdfnetsdksamples.R;
import com.pdftron.android.pdfnetsdksamples.util.Utils;
import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.PDFNet;
import com.pdftron.pdf.pdfa.PDFACompliance;

import java.io.File;
import java.util.ArrayList;

public class PDFATest extends PDFNetSample {

	private static OutputListener mOutputListener;

	private static ArrayList<String> mFileList = new ArrayList<>();

    public PDFATest() {
        setTitle(R.string.sample_pdfa_title);
        setDescription(R.string.sample_pdfa_description);

        // The standard library does not include PDF/A validation/conversion,
        // thus this sample will fail. Please, comment out this call
        // if using the full libraries.
        // DisableRun();
    }

	@Override
	public void run(OutputListener outputListener) {
		super.run(outputListener);
		mOutputListener = outputListener;
		mFileList.clear();
		printHeader(outputListener);
        try {
            PDFNet.setColorManagement(PDFNet.e_lcms); // Required for proper PDF/A validation and conversion.

            //-----------------------------------------------------------
            // Example 1: PDF/A Validation
            //-----------------------------------------------------------
            String filename = "newsletter.pdf";
            PDFACompliance pdf_a = new PDFACompliance(false, Utils.getAssetTempFile(INPUT_PATH + filename).getAbsolutePath(), null, PDFACompliance.e_Level1B, null, 10);
            printResults(pdf_a, filename);
            pdf_a.destroy();

            //-----------------------------------------------------------
            // Example 2: PDF/A Conversion
            //-----------------------------------------------------------
            filename = "fish.pdf";
            pdf_a = new PDFACompliance(true, Utils.getAssetTempFile(INPUT_PATH + filename).getAbsolutePath(), null, PDFACompliance.e_Level1B, null, 10);
            filename = Utils.createExternalFile("pdfa.pdf").getAbsolutePath();
            pdf_a.saveAs(filename, true);
            pdf_a.destroy();
            mFileList.add("pdf_a.pdf");

            // Re-validate the document after the conversion...
            pdf_a = new PDFACompliance(false, filename, null, PDFACompliance.e_Level1B, null, 10);
            printResults(pdf_a, filename);
            pdf_a.destroy();

        } catch (PDFNetException e) {
            mOutputListener.println(e.getMessage());
        }

        mOutputListener.println("Done.");

		for (String file : mFileList) {
			addToFileList(file);
		}
		printFooter(outputListener);
	}


    static void printResults(PDFACompliance pdf_a, String filename) {
        try {
            int err_cnt = pdf_a.getErrorCount();
            mOutputListener.print(filename);
            if (err_cnt == 0) {
                mOutputListener.print(": OK.\n");
            } else {
                mOutputListener.println(" is NOT a valid PDF/A file.");
                for (int i = 0; i < err_cnt; ++i) {
                    int c = pdf_a.getError(i);
                    mOutputListener.println(" - e_PDFA" + c + ": " + PDFACompliance.getPDFAErrorMessage(c) + ".");
                    if (true) {
                        int num_refs = pdf_a.getRefObjCount(c);
                        if (num_refs > 0) {
                            mOutputListener.print("   Objects:");
                            for (int j = 0; j < num_refs; ) {
                                mOutputListener.print(String.valueOf(pdf_a.getRefObj(c, j)));
                                if (++j != num_refs) mOutputListener.print(", ");
                            }
                            mOutputListener.println();
                        }
                    }
                }
                mOutputListener.println();
            }
        } catch (PDFNetException e) {
            mOutputListener.println(e.getMessage());
        }
    }

}
