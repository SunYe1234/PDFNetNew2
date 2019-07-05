package com.pdftron.demo.navigation.adapter;

import com.pdftron.demo.browser.MockFile;
import com.pdftron.demo.model.FileHeader;
import com.pdftron.demo.utils.FileInfoComparator;
import com.pdftron.pdf.model.BaseFileInfo;
import com.pdftron.pdf.model.FileInfo;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test to ensure files are correctly added to the adapter, including adding/removing headers.
 */
public class AllFilesAdapterHelperTest {

    private AllFilesAdapterHelper mHeaderHelper = new AllFilesAdapterHelper(null);
    private Comparator<FileInfo> mSortMode = FileInfoComparator.fileNameOrder();
    private int mSpanCount = 0;

    @Test
    public void deleteFile_isCorrect_forList() {
        // Setup test data
        List<FileInfo> testData = new ArrayList<>();
        List<FileInfo> expectedData = new ArrayList<>(); // to be filled out later
        mHeaderHelper.addFile(testData, new MockFile("/A", "FileA1"), mSortMode, mSpanCount);
        mHeaderHelper.addFile(testData, new MockFile("/B", "FileB1"), mSortMode, mSpanCount);
        mHeaderHelper.addFile(testData, new MockFile("/B", "FileB2"), mSortMode, mSpanCount);

        expectedData.add(new FileHeader(BaseFileInfo.FILE_TYPE_FOLDER, new File("/A")));
        expectedData.add(new MockFile("/A", "FileA1"));
        expectedData.add(new FileHeader(BaseFileInfo.FILE_TYPE_FOLDER, new File("/B")));
        expectedData.add(new MockFile("/B", "FileB1"));
        expectedData.add(new MockFile("/B", "FileB2"));

        equals(testData, expectedData);

        // Check if deleting file under a header is correct
        mHeaderHelper.deleteFile(testData, new MockFile("/B", "FileB1"), 0);
        expectedData.remove(new MockFile("/B", "FileB1"));

        equals(testData, expectedData);

        // Now check whether deleting last file under header also deletes the header
        mHeaderHelper.deleteFile(testData, new MockFile("/B", "FileB2"), 0);
    }

    @Test
    public void addFile_isCorrect_forList() {
        List<FileInfo> myTestList = new ArrayList<>();
        List<FileInfo> expectedData = new ArrayList<>(); // to be filled out later
        mHeaderHelper.addFile(myTestList, new MockFile("/A", "FileA1"), mSortMode, mSpanCount);
        mHeaderHelper.addFile(myTestList, new MockFile("/C", "FileC2"), mSortMode, mSpanCount);
        mHeaderHelper.addFile(myTestList, new MockFile("/C", "FileC1"), mSortMode, mSpanCount);
        mHeaderHelper.addFile(myTestList, new MockFile("/A", "FileA3"), mSortMode, mSpanCount);
        mHeaderHelper.addFile(myTestList, new MockFile("/B", "FileB2"), mSortMode, mSpanCount);
        mHeaderHelper.addFile(myTestList, new MockFile("/A", "FileA2"), mSortMode, mSpanCount);
        mHeaderHelper.addFile(myTestList, new MockFile("/A", "FileA0"), mSortMode, mSpanCount);
        mHeaderHelper.addFile(myTestList, new MockFile("/C", "FileC3"), mSortMode, mSpanCount);

        expectedData.add(new FileHeader(BaseFileInfo.FILE_TYPE_FOLDER, new File("/A")));
        expectedData.add(new MockFile("/A", "FileA0"));
        expectedData.add(new MockFile("/A", "FileA1"));
        expectedData.add(new MockFile("/A", "FileA2"));
        expectedData.add(new MockFile("/A", "FileA3"));
        expectedData.add(new FileHeader(BaseFileInfo.FILE_TYPE_FOLDER, new File("/B")));
        expectedData.add(new MockFile("/B", "FileB2"));
        expectedData.add(new FileHeader(BaseFileInfo.FILE_TYPE_FOLDER, new File("/C")));
        expectedData.add(new MockFile("/C", "FileC1"));
        expectedData.add(new MockFile("/C", "FileC2"));
        expectedData.add(new MockFile("/C", "FileC3"));

        equals(myTestList, expectedData);
    }

    private void equals(List<FileInfo> testList1, List<FileInfo> testList2) {
        assertEquals(testList1.size(), testList2.size());
        int size = testList1.size();
        for (int i = 0; i < size; i++) {
            assertEquals(testList1.get(i), testList2.get(i));
        }
    }
}
