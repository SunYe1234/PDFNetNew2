package com.pdftron.demo.browser;

import com.pdftron.pdf.model.FileInfo;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GroupedFilesTest {

    private GroupedFiles mGroupedFiles;
    private GroupedFiles.Group mTestGroupA;

    @Before
    public void resetGroupedFiles() {
        mGroupedFiles = new GroupedFiles();
        String dirA = "/A";
        List<FileInfo> mTestGroupAFiles = new ArrayList<>();
        mTestGroupAFiles.add(new MockFile(dirA, "A1"));
        mTestGroupAFiles.add(new MockFile(dirA, "A2"));
        mTestGroupAFiles.add(new MockFile(dirA, "A3"));
        mTestGroupAFiles.add(new MockFile(dirA, "A4"));
        mTestGroupAFiles.add(new MockFile(dirA, "A5"));
        mTestGroupA = new GroupedFiles.Group(mTestGroupAFiles, dirA);
    }

    @Test
    public void deleteFile_isCorrect() {
        // Setup files to delete
        FileInfo file1 = new MockFile("/A", "A1");
        FileInfo file2 = new MockFile("/A", "A3");
        FileInfo file3 = new MockFile("/C", "C3");
        Set<FileInfo> deletedFiles = new HashSet<>();
        deletedFiles.add(file1);
        deletedFiles.add(file2);
        deletedFiles.add(file3);

        // Lets remove some files
        int oldSize = mTestGroupA.size();
        GroupedFiles.removeDeletedFiles(mTestGroupA.mFileGroup, deletedFiles);

        assertEquals(oldSize - 2, mTestGroupA.size());
        assertEquals(1, deletedFiles.size());

        assertFalse(mTestGroupA.contains(file1));
        assertFalse(mTestGroupA.contains(file2));
        assertTrue(deletedFiles.contains(file3));
    }

    @Test
    public void addFile_isCorrect() {
        // Setup files to add
        FileInfo file1 = new MockFile("/A", "A1");
        FileInfo file2 = new MockFile("/B", "B3");
        FileInfo file3 = new MockFile("/C", "C3");
        Set<FileInfo> addedFiles = new HashSet<>();
        addedFiles.add(file1);
        addedFiles.add(file2);
        addedFiles.add(file3);

        // Lets add some files
        int oldSize = mTestGroupA.size();
        GroupedFiles.insertAddedFiles(mTestGroupA.mFileGroup, addedFiles);

        assertEquals(oldSize + 1, mTestGroupA.size());
        assertEquals(2, addedFiles.size());

        assertTrue(mTestGroupA.contains(file1));
        assertFalse(addedFiles.contains(file1));
    }

    @Test
    public void flatten_isCorrect() {
        // Create more file groups to test flatten
        List<FileInfo> groupB = new ArrayList<>();
        groupB.add(new MockFile("/B", "B2"));
        groupB.add(new MockFile("/B", "B3"));
        List<FileInfo> groupC = new ArrayList<>();
        groupC.add(new MockFile("/C", "C2"));
        mGroupedFiles.addGroupedData(groupB);
        mGroupedFiles.addGroupedData(groupC);

        assertEquals(groupB.size() + groupC.size(), mGroupedFiles.getFlattenedList().size());
    }
}
