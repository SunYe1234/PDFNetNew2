//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.android.pdfnetsdksamples.samples;

import com.pdftron.android.pdfnetsdksamples.OutputListener;
import com.pdftron.android.pdfnetsdksamples.PDFNetSample;
import com.pdftron.android.pdfnetsdksamples.R;
import com.pdftron.android.pdfnetsdksamples.util.Utils;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PageIterator;
import com.pdftron.pdf.Rect;
import com.pdftron.sdf.SDFDoc;

import java.io.File;
import java.util.ArrayList;

public class RectTest extends PDFNetSample {

	private static OutputListener mOutputListener;

	private static ArrayList<String> mFileList = new ArrayList<>();

    public RectTest() {
        setTitle(R.string.sample_rect_title);
        setDescription(R.string.sample_rect_description);
    }

	@Override
	public void run(OutputListener outputListener) {
		super.run(outputListener);
		mOutputListener = outputListener;
		mFileList.clear();
		printHeader(outputListener);

        try // Test  - Adjust the position of content within the page.
        {
            mOutputListener.println("_______________________________________________");
            mOutputListener.println("Opening the input pdf...");

            PDFDoc input_doc = new PDFDoc((Utils.getAssetTempFile(INPUT_PATH + "tiger.pdf").getAbsolutePath()));
            input_doc.initSecurityHandler();

            PageIterator pg_itr1 = input_doc.getPageIterator();

            Rect media_box = pg_itr1.next().getMediaBox();

            media_box.setX1(media_box.getX1() - 200);    // translate the page 200 units (1 uint = 1/72 inch)
            media_box.setX2(media_box.getX2() - 200);

            media_box.update();

            input_doc.save(Utils.createExternalFile("tiger_shift.pdf").getAbsolutePath(), SDFDoc.SaveMode.NO_FLAGS, null);
            mFileList.add(new File(input_doc.getFileName()).getName());
            input_doc.close();
            mOutputListener.println("Done. Result saved in tiger_shift...");
        } catch (Exception e) {
            mOutputListener.println(e.getStackTrace());
        }

		for (String file : mFileList) {
			addToFileList(file);
		}
		printFooter(outputListener);
	}

}
