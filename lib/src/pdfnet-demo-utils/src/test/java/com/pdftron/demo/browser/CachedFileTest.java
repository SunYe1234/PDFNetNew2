package com.pdftron.demo.browser;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CachedFileTest {

    @Test
    public void onDayOld_isCorrect() {
        assertTrue(CachedFiles.isOneDayOld(getDateWithHourDiff(-131)));
        assertTrue(CachedFiles.isOneDayOld(getDateWithHourDiff(-31)));
        assertFalse(CachedFiles.isOneDayOld(getDateWithHourDiff(-21)));
        assertFalse(CachedFiles.isOneDayOld(getDateWithHourDiff(200)));
    }

    // Returns the current date/time plus "hourDiff" hours
    private Date getDateWithHourDiff(int hourDiff) {
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTime(new Date());
        currentTime.add(Calendar.HOUR, hourDiff);
        return currentTime.getTime();
    }
}
