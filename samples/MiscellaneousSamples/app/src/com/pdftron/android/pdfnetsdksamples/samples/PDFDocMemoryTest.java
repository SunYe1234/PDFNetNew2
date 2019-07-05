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
import com.pdftron.filters.FilterReader;
import com.pdftron.filters.MappedFile;
import com.pdftron.pdf.Element;
import com.pdftron.pdf.ElementReader;
import com.pdftron.pdf.ElementWriter;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.Page;
import com.pdftron.pdf.PageIterator;
import com.pdftron.sdf.SDFDoc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PDFDocMemoryTest extends PDFNetSample {

	private static OutputListener mOutputListener;

	private static ArrayList<String> mFileList = new ArrayList<>();

    public PDFDocMemoryTest() {
        setTitle(R.string.sample_pdfdocmemory_title);
        setDescription(R.string.sample_pdfdocmemory_description);
    }

	@Override
	public void run(OutputListener outputListener) {
		super.run(outputListener);
		mOutputListener = outputListener;
		mFileList.clear();
		printHeader(outputListener);


        // The following sample illustrates how to read/write a PDF document from/to
        // a memory buffer.  This is useful for applications that work with dynamic PDF
        // documents that don't need to be saved/read from a disk.
        try {
            // Read a PDF document in a memory buffer.
            MappedFile file = new MappedFile((Utils.getAssetTempFile(INPUT_PATH + "tiger.pdf").getAbsolutePath()));
            long file_sz = file.fileSize();

            FilterReader file_reader = new FilterReader(file);

            byte[] mem = new byte[(int) file_sz];

            long bytes_read = file_reader.read(mem);
            PDFDoc doc = new PDFDoc(mem);

            doc.initSecurityHandler();
            int num_pages = doc.getPageCount();

            ElementWriter writer = new ElementWriter();
            ElementReader reader = new ElementReader();
            Element element;

            // Create a duplicate of every page but copy only path objects

            for (int i = 1; i <= num_pages; ++i) {
                PageIterator itr = doc.getPageIterator(2 * i - 1);
                Page current = itr.next();
                reader.begin(current);
                Page new_page = doc.pageCreate(current.getMediaBox());
                doc.pageInsert(itr, new_page);

                writer.begin(new_page);
                while ((element = reader.next()) != null)    // Read page contents
                {
                    //if (element.getType() == Element.e_path)
                    writer.writeElement(element);
                }

                writer.end();
                reader.end();
            }

            doc.save(Utils.createExternalFile("doc_memory_edit.pdf").getAbsolutePath(), SDFDoc.SaveMode.REMOVE_UNUSED, null);
            mFileList.add(new File(doc.getFileName()).getName());

            // Save the document to a memory buffer.

            byte[] buf = doc.save(SDFDoc.SaveMode.REMOVE_UNUSED, null);
            // doc.Save(buf, buf_sz, Doc::e_linearized, NULL);

            // Write the contents of the buffer to the disk
            {
                File outfile = new File(Utils.createExternalFile("doc_memory_edit.txt").getAbsolutePath());
                mFileList.add("doc_memory_edit.txt");
                FileOutputStream fop = new FileOutputStream(outfile);
                if (!outfile.exists()) {
                    outfile.createNewFile();
                }
                fop.write(buf);
                fop.flush();
                fop.close();
            }

            // Read some data from the file stored in memory
            reader.begin(doc.getPage(1));
            while ((element = reader.next()) != null) {
                if (element.getType() == Element.e_path) mOutputListener.print("Path, ");
            }
            reader.end();

            doc.close();
            mOutputListener.println("\n\nDone. Result saved in doc_memory_edit.pdf and doc_memory_edit.txt ...");
        } catch (PDFNetException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //This sample illustrates how to open a PDF document
        //from an Java InputStream and how to save to an OutputStream.
        try {
            PDFDoc doc = new PDFDoc(new FileInputStream(Utils.getAssetTempFile(INPUT_PATH + "newsletter.pdf").getAbsolutePath()));
            doc.save(new FileOutputStream(Utils.createExternalFile("StreamTest.pdf").getAbsolutePath()), SDFDoc.SaveMode.NO_FLAGS, null);
            mFileList.add(new File(doc.getFileName()).getName());
            doc.close();
            mOutputListener.println("\n\nDone. Result saved in StreamTest.pdf ...");
        } catch (PDFNetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

		for (String file : mFileList) {
			addToFileList(file);
		}
		printFooter(outputListener);
	}

}
