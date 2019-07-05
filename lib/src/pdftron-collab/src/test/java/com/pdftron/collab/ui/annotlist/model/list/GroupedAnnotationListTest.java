package com.pdftron.collab.ui.annotlist.model.list;

import com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListContent;
import com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListHeader;
import com.pdftron.collab.ui.annotlist.model.list.item.PageNumber;
import com.pdftron.pdf.PDFViewCtrl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GroupedAnnotationListTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    private List<AnnotationListContent> mList;

    private GroupedList<PageNumber> mGroupedList;

    @Before
    public void setUp() {
        // Set up list data, looks something like this
        AnnotationListContent item1 = mock(AnnotationListContent.class);
        when(item1.getPageNum()).thenReturn(5);
        when(item1.getContent()).thenReturn("item1");
        AnnotationListContent item2 = mock(AnnotationListContent.class);
        when(item2.getPageNum()).thenReturn(7);
        when(item2.getContent()).thenReturn("item2");
        AnnotationListContent item3 = mock(AnnotationListContent.class);
        when(item3.getPageNum()).thenReturn(5);
        when(item3.getContent()).thenReturn("item3");
        AnnotationListContent item4 = mock(AnnotationListContent.class);
        when(item4.getPageNum()).thenReturn(1);
        when(item4.getContent()).thenReturn("item4");
        AnnotationListContent item5 = mock(AnnotationListContent.class);
        when(item5.getPageNum()).thenReturn(5);
        when(item5.getContent()).thenReturn("item5");
        AnnotationListContent item6 = mock(AnnotationListContent.class);
        when(item6.getPageNum()).thenReturn(7);
        when(item6.getContent()).thenReturn("item6");

        mList = Arrays.asList(
                item1,
                item2,
                item3,
                item4,
                item5,
                item6
        );

        //noinspection unchecked
        AnnotationEntityMapper<PageNumber> adapter = mock(AnnotationEntityMapper.class);
        when(adapter.fromEntities(any())).thenReturn(mList);
        when(adapter.getHeader(any())).thenReturn(new AnnotationListHeader<>(mock(PageNumber.class)));

        mGroupedList = new GroupedList<>(
                adapter,
                mock(PDFViewCtrl.class),
                item -> new PageNumber(item.getPageNum(), "Page %d")
        );
    }

    @Test
    public void fromList_isCorrect() {
        for (GroupedList.AnnotationListGroup group : mGroupedList.getGroups()) {
            assertNotNull(group);
            assertNotEquals(group.size(), 0);
            int page = group.getItem(0).getPageNum();
            switch (page) {
                case 1: // page 1 there should be 1 annot
                    assertEquals(group.size(), 1);
                    break;
                case 5: // page 5 there should be 3 annots
                    assertEquals(group.size(), 3);
                    break;
                case 7: // page 7 there should be 2 annots
                    assertEquals(group.size(), 2);
                    break;
            }
        }
    }

    @Test
    public void get_isCorrect() {
        // Check that the headers are correct
        assertTrue(mGroupedList.get(0).isHeader());
        assertTrue(!mGroupedList.get(1).isHeader());
        assertTrue(mGroupedList.get(2).isHeader());
        assertTrue(!mGroupedList.get(3).isHeader());
        assertTrue(!mGroupedList.get(4).isHeader());
        assertTrue(!mGroupedList.get(5).isHeader());
        assertTrue(mGroupedList.get(6).isHeader());
        assertTrue(!mGroupedList.get(7).isHeader());
        assertTrue(!mGroupedList.get(8).isHeader());
    }

    @Test
    public void size_isCorrect() {
        assertEquals(mGroupedList.size(), 6 + 3); // 6 items and 3 groups
    }
}