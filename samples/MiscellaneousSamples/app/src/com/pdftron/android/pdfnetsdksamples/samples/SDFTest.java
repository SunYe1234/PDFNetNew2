//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.android.pdfnetsdksamples.samples;

import com.pdftron.android.pdfnetsdksamples.OutputListener;
import com.pdftron.android.pdfnetsdksamples.PDFNetSample;
import com.pdftron.android.pdfnetsdksamples.R;
import com.pdftron.android.pdfnetsdksamples.util.Utils;
import com.pdftron.filters.FilterReader;
import com.pdftron.filters.MappedFile;
import com.pdftron.sdf.DictIterator;
import com.pdftron.sdf.Obj;
import com.pdftron.sdf.SDFDoc;

import java.io.File;
import java.util.ArrayList;

public class SDFTest extends PDFNetSample {

	private static OutputListener mOutputListener;

	private static ArrayList<String> mFileList = new ArrayList<>();

    public SDFTest() {
        setTitle(R.string.sample_sdf_title);
        setDescription(R.string.sample_sdf_description);
    }

	@Override
	public void run(OutputListener outputListener) {
		super.run(outputListener);
		mOutputListener = outputListener;
		mFileList.clear();
		printHeader(outputListener);

        try {
            mOutputListener.println("Opening the test file...");

            // Here we create a SDF/Cos document directly from PDF file. In case you have
            // PDFDoc you can always access SDF/Cos document using PDFDoc.GetSDFDoc() method.
            SDFDoc doc = new SDFDoc((Utils.getAssetTempFile(INPUT_PATH + "fish.pdf").getAbsolutePath()));
            doc.initSecurityHandler();

            mOutputListener.println("Modifying info dictionary, adding custom properties, embedding a stream...");
            Obj trailer = doc.getTrailer();            // Get the trailer

            // Now we will change PDF document information properties using SDF API

            // Get the Info dictionary.
            DictIterator itr = trailer.find("Info");
            Obj info;
            if (itr.hasNext()) {
                info = itr.value();
                // Modify 'Producer' entry.
                info.putString("Producer", "PDFTron PDFNet");

                // Read title entry (if it is present)
                itr = info.find("Author");
                if (itr.hasNext()) {
                    String oldstr = itr.value().getAsPDFText();

                    info.putText("Author", oldstr + "- Modified");
                } else {
                    info.putString("Author", "Me, myself, and I");
                }
            } else {
                // Info dict is missing.
                info = trailer.putDict("Info");
                info.putString("Producer", "PDFTron PDFNet");
                info.putString("Title", "My document");
            }

            // Create a custom inline dictionary within Info dictionary
            Obj custom_dict = info.putDict("My Direct Dict");
            custom_dict.putNumber("My Number", 100);     // Add some key/value pairs
            custom_dict.putArray("My Array");

            // Create a custom indirect array within Info dictionary
            Obj custom_array = doc.createIndirectArray();
            info.put("My Indirect Array", custom_array);    // Add some entries

            // Create indirect link to root
            custom_array.pushBack(trailer.get("Root").value());

            // Embed a custom stream (file mystream.txt).
            MappedFile embed_file = new MappedFile(Utils.getAssetTempFile(INPUT_PATH + "my_stream.txt").getAbsolutePath());
            FilterReader mystm = new FilterReader(embed_file);
            custom_array.pushBack(doc.createIndirectStream(mystm));

            // Save the changes.
            mOutputListener.println("Saving modified test file...");
            doc.save(Utils.createExternalFile("sdftest_out.pdf").getAbsolutePath(), SDFDoc.SaveMode.NO_FLAGS, null, "%PDF-1.4");
            mFileList.add(new File(doc.getFileName()).getName());
            doc.close();

            mOutputListener.println("Test completed.");
        } catch (Exception e) {
            mOutputListener.println(e.getStackTrace());
        }

		for (String file : mFileList) {
			addToFileList(file);
		}
		printFooter(outputListener);
	}

}
