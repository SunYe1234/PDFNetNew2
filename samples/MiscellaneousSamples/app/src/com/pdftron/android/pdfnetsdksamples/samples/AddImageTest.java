//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.android.pdfnetsdksamples.samples;

import com.pdftron.common.Matrix2D;
import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.Element;
import com.pdftron.pdf.ElementBuilder;
import com.pdftron.pdf.ElementWriter;
import com.pdftron.pdf.Font;
import com.pdftron.pdf.Image;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.Page;
import com.pdftron.pdf.Rect;
import com.pdftron.sdf.Obj;
import com.pdftron.sdf.ObjSet;
import com.pdftron.sdf.SDFDoc;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.pdftron.android.pdfnetsdksamples.OutputListener;
import com.pdftron.android.pdfnetsdksamples.PDFNetSample;
import com.pdftron.android.pdfnetsdksamples.R;
import com.pdftron.android.pdfnetsdksamples.util.Utils;

import java.io.File;
import java.util.ArrayList;

public class AddImageTest extends PDFNetSample {

	private static OutputListener mOutputListener;

	private static ArrayList<String> mFileList = new ArrayList<>();

    public AddImageTest() {
        setTitle(R.string.sample_addimage_title);
        setDescription(R.string.sample_addimage_description);
    }

	@Override
	public void run(OutputListener outputListener) {
		super.run(outputListener);
		mOutputListener = outputListener;
		mFileList.clear();
		printHeader(outputListener);

        try {
            mOutputListener.println("-------------------------------------------------");

			PDFDoc doc = new PDFDoc();

			ElementBuilder f = new ElementBuilder(); // Used to build new Element objects
			ElementWriter writer = new ElementWriter(); // Used to write Elements to the page

			Page page = doc.pageCreate(); // Start a new page
			writer.begin(page); // Begin writing to this page

			// ----------------------------------------------------------
			// Add JPEG image to the output file
			Image img = Image.create(doc.getSDFDoc(), Utils.getAssetTempFile(INPUT_PATH + "peppers.jpg").getAbsolutePath());
			Element element = f.createImage(img, 50, 500, img.getImageWidth()/2, img.getImageHeight()/2);
			writer.writePlacedElement(element);

			// ----------------------------------------------------------
			// Add a PNG image to the output file
			img = Image.create(doc.getSDFDoc(), Utils.getAssetTempFile(INPUT_PATH + "butterfly.png").getAbsolutePath());
			element = f.createImage(img, new Matrix2D(100, 0, 0, 100, 300, 500));
			writer.writePlacedElement(element);

			// ----------------------------------------------------------
			// Add a GIF image to the output file
			img = Image.create(doc.getSDFDoc(), Utils.getAssetTempFile(INPUT_PATH + "pdfnet.gif").getAbsolutePath());
			element = f.createImage(img, new Matrix2D(img.getImageWidth(), 0, 0, img.getImageHeight(), 50, 350));
			writer.writePlacedElement(element);

			// ----------------------------------------------------------
			// Add a TIFF image to the output file
			img = Image.create(doc.getSDFDoc(), Utils.getAssetTempFile(INPUT_PATH + "grayscale.tif").getAbsolutePath());
			element = f.createImage(img, new Matrix2D(img.getImageWidth(), 0, 0, img.getImageHeight(), 10, 50));
			writer.writePlacedElement(element);

			writer.end();           // Save the page
			doc.pagePushBack(page); // Add the page to the document page sequence

			// ----------------------------------------------------------
			// Embed a monochrome TIFF. Compress the image using lossy JBIG2 filter.

			page = doc.pageCreate(new Rect(0, 0, 612, 794));
			writer.begin(page); // begin writing to this page

			// Note: encoder hints can be used to select between different compression methods.
			// For example to instruct PDFNet to compress a monochrome image using JBIG2 compression.
			ObjSet hint_set = new ObjSet();
			Obj enc = hint_set.createArray();  // Initilaize encoder 'hint' parameter
			enc.pushBackName("JBIG2");
			enc.pushBackName("Lossy");

			img = Image.create(doc.getSDFDoc(), Utils.getAssetTempFile(INPUT_PATH + "multipage.tif").getAbsolutePath());
			element = f.createImage(img, new Matrix2D(612, 0, 0, 794, 0, 0));
			writer.writePlacedElement(element);

			writer.end();           // Save the page
			doc.pagePushBack(page); // Add the page to the document page sequence

			// ----------------------------------------------------------
			// Add a JPEG2000 (JP2) image to the output file

			// Create a new page
			page = doc.pageCreate();
			writer.begin(page); // Begin writing to the page

			// Embed the image.
			img = Image.create(doc.getSDFDoc(), Utils.getAssetTempFile(INPUT_PATH + "palm.jp2").getAbsolutePath());

			// Position the image on the page.
			element = f.createImage(img, new Matrix2D(img.getImageWidth(), 0, 0, img.getImageHeight(), 96, 80));
			writer.writePlacedElement(element);

			// Write 'JPEG2000 Sample' text string under the image.
			writer.writeElement(f.createTextBegin(Font.create(doc.getSDFDoc(), Font.e_times_roman), 32));
			element = f.createTextRun("JPEG2000 Sample");
			element.setTextMatrix(1, 0, 0, 1, 190, 30);
			writer.writeElement(element);
			writer.writeElement(f.createTextEnd());

			writer.end(); // Finish writing to the page
			doc.pagePushBack(page);

			// ----------------------------------------------------------
			// doc.Save((Utils.createExternalFile("addimage.pdf").getAbsolutePath()).c_str(), Doc.e_remove_unused, 0);
			doc.save((Utils.createExternalFile("addimage.pdf").getAbsolutePath()), SDFDoc.SaveMode.LINEARIZED, null);
            mFileList.add(new File(doc.getFileName()).getName());
			doc.close();
			mOutputListener.println("Done. Result saved in addimage.pdf...");
		}
		catch (PDFNetException e)
		{
			e.printStackTrace();
			mOutputListener.println(e.getStackTrace());
		}

		for (String file : mFileList) {
			addToFileList(file);
		}
		printFooter(outputListener);
	}

}
