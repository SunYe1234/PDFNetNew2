//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.android.pdfnetsdksamples.samples;

import com.pdftron.android.pdfnetsdksamples.OutputListener;
import com.pdftron.android.pdfnetsdksamples.PDFNetSample;
import com.pdftron.android.pdfnetsdksamples.R;
import com.pdftron.android.pdfnetsdksamples.util.Utils;
import com.pdftron.pdf.Element;
import com.pdftron.pdf.ElementBuilder;
import com.pdftron.pdf.ElementWriter;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.Page;
import com.pdftron.pdf.PageIterator;
import com.pdftron.pdf.Rect;
import com.pdftron.sdf.SDFDoc;

import java.io.File;
import java.util.ArrayList;

//-----------------------------------------------------------------------------------
//The sample illustrates how multiple pages can be combined/imposed
//using PDFNet. Page imposition can be used to arrange/order pages
//prior to printing or to assemble a 'master' page from several 'source'
//pages. Using PDFNet API it is possible to write applications that can
//re-order the pages such that they will display in the correct order
//when the hard copy pages are compiled and folded correctly.
//-----------------------------------------------------------------------------------

public class ImpositionTest extends PDFNetSample {

	private static OutputListener mOutputListener;

	private static ArrayList<String> mFileList = new ArrayList<>();

    public ImpositionTest() {
        setTitle(R.string.sample_imposition_title);
        setDescription(R.string.sample_imposition_description);
    }

	@Override
	public void run(OutputListener outputListener) {
		super.run(outputListener);
		mOutputListener = outputListener;
		mFileList.clear();
		printHeader(outputListener);

        try {
            mOutputListener.println("-------------------------------------------------");
            mOutputListener.println("Opening the input pdf...");

            String filein = Utils.getAssetTempFile(INPUT_PATH + "newsletter.pdf").getAbsolutePath();
            String fileout = Utils.createExternalFile("newsletter_booklet.pdf").getAbsolutePath();

            PDFDoc in_doc = new PDFDoc(filein);
            in_doc.initSecurityHandler();

            // Create a list of pages to import from one PDF document to another.
            Page[] copy_pages = new Page[in_doc.getPageCount()];
            int j = 0;
            for (PageIterator itr = in_doc.getPageIterator(); itr.hasNext(); j++) {
                copy_pages[j] = itr.next();
            }

            PDFDoc new_doc = new PDFDoc();
            Page[] imported_pages = new_doc.importPages(copy_pages);

            // Paper dimension for A3 format in points. Because one inch has
            // 72 points, 11.69 inch 72 = 841.69 points
            Rect media_box = new Rect(0, 0, 1190.88, 841.69);
            double mid_point = media_box.getWidth() / 2;

            ElementBuilder builder = new ElementBuilder();
            ElementWriter writer = new ElementWriter();

            for (int i = 0; i < imported_pages.length; ++i) {
                // Create a blank new A3 page and place on it two pages from the input document.
                Page new_page = new_doc.pageCreate(media_box);
                writer.begin(new_page);

                // Place the first page
                Page src_page = imported_pages[i];
                Element element = builder.createForm(src_page);

                double sc_x = mid_point / src_page.getPageWidth();
                double sc_y = media_box.getHeight() / src_page.getPageHeight();
                double scale = sc_x < sc_y ? sc_x : sc_y; // min(sc_x, sc_y)
                element.getGState().setTransform(scale, 0, 0, scale, 0, 0);
                writer.writePlacedElement(element);

                // Place the second page
                ++i;
                if (i < imported_pages.length) {
                    src_page = imported_pages[i];
                    element = builder.createForm(src_page);
                    sc_x = mid_point / src_page.getPageWidth();
                    sc_y = media_box.getHeight() / src_page.getPageHeight();
                    scale = sc_x < sc_y ? sc_x : sc_y; // min(sc_x, sc_y)
                    element.getGState().setTransform(scale, 0, 0, scale, mid_point, 0);
                    writer.writePlacedElement(element);
                }

                writer.end();
                new_doc.pagePushBack(new_page);
            }

            new_doc.save(fileout, SDFDoc.SaveMode.LINEARIZED, null);
            mFileList.add(new File(new_doc.getFileName()).getName());
            new_doc.close();
            in_doc.close();
            mOutputListener.println("Done. Result saved in newsletter_booklet.pdf...");
        } catch (Exception e) {
            e.printStackTrace();
        }

		for (String file : mFileList) {
			addToFileList(file);
		}
		printFooter(outputListener);
	}

}
