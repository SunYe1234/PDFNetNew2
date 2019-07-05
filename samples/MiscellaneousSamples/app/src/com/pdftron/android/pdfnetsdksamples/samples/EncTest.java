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
import com.pdftron.filters.FlateEncode;
import com.pdftron.filters.MappedFile;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.sdf.Obj;
import com.pdftron.sdf.SDFDoc;
import com.pdftron.sdf.SecurityHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class EncTest extends PDFNetSample {

	private static OutputListener mOutputListener;

	private static ArrayList<String> mFileList = new ArrayList<>();

    public EncTest() {
        setTitle(R.string.sample_encryption_title);
        setDescription(R.string.sample_encryption_description);
    }

	@Override
	public void run(OutputListener outputListener) {
		super.run(outputListener);
		mOutputListener = outputListener;
		mFileList.clear();
		printHeader(outputListener);

        // Example 1:
        // secure a document with password protection and
        // adjust permissions

        try {
            // Open the test file
            mOutputListener.println("Securing an existing document ...");
            PDFDoc doc = new PDFDoc((Utils.getAssetTempFile(INPUT_PATH + "fish.pdf").getAbsolutePath()));
            doc.initSecurityHandler();

            // Perform some operation on the document. In this case we use low level SDF API
            // to replace the content stream of the first page with contents of file 'my_stream.txt'
            if (true)  // Optional
            {
                mOutputListener.println("Replacing the content stream, use flate compression...");

                // Get the page dictionary using the following path: trailer/Root/Pages/Kids/0
                Obj page_dict = doc.getTrailer().get("Root").value()
                        .get("Pages").value()
                        .get("Kids").value()
                        .getAt(0);

                // Embed a custom stream (file mystream.txt) using Flate compression.
                MappedFile embed_file = new MappedFile((Utils.getAssetTempFile(INPUT_PATH + "my_stream.txt").getAbsolutePath()));
                FilterReader mystm = new FilterReader(embed_file);
                page_dict.put("Contents",
                        doc.createIndirectStream(mystm,
                                new FlateEncode(null)));
            }

            //encrypt the document

            // Apply a new security handler with given security settings.
            // In order to open saved PDF you will need a user password 'test'.
            SecurityHandler new_handler = new SecurityHandler();

            // Set a new password required to open a document
            String user_password = "test";
            new_handler.changeUserPassword(user_password);

            // Set Permissions
            new_handler.setPermission(SecurityHandler.e_print, true);
            new_handler.setPermission(SecurityHandler.e_extract_content, false);

            // Note: document takes the ownership of new_handler.
            doc.setSecurityHandler(new_handler);

            // Save the changes.
            mOutputListener.println("Saving modified file...");
            doc.save((Utils.createExternalFile("secured.pdf").getAbsolutePath()), SDFDoc.SaveMode.NO_FLAGS, null);
            mFileList.add(new File(doc.getFileName()).getName());
            doc.close();
        } catch (PDFNetException e) {
            e.printStackTrace();
        }

        // Example 2:
        // Opens the encrypted document and removes all of
        // its security.
        try {
            PDFDoc doc = new PDFDoc((Utils.createExternalFile("secured.pdf").getAbsolutePath()));

            //If the document is encrypted prompt for the password
            if (!doc.initSecurityHandler()) {
                boolean success = false;
                mOutputListener.println("The password is: test");
                for (int count = 0; count < 3; count++) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
                    mOutputListener.println("A password required to open the document.");
                    mOutputListener.print("Please enter the password: ");
                    // String password = r.readLine();
                    if (doc.initStdSecurityHandler("test")) {
                        success = true;
                        mOutputListener.println("The password is correct.");
                        break;
                    } else if (count < 3) {
                        mOutputListener.println("The password is incorrect, please try again");
                    }
                }
                if (!success) {
                    mOutputListener.println("Document authentication error....");
                }
            }

            //remove all security on the document
            doc.removeSecurity();
            doc.save(Utils.createExternalFile("not_secured.pdf").getAbsolutePath(), SDFDoc.SaveMode.NO_FLAGS, null);
            mFileList.add(new File(doc.getFileName()).getName());
            doc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mOutputListener.println("Test completed.");

		for (String file : mFileList) {
			addToFileList(file);
		}
		printFooter(outputListener);
	}

}
