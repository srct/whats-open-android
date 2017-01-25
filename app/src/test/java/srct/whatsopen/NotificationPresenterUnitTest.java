package srct.whatsopen;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import srct.whatsopen.model.NotificationSettings;
import srct.whatsopen.presenters.NotificationPresenter;

import static org.junit.Assert.*;

public class NotificationPresenterUnitTest {

    Calendar now;
    NotificationPresenter mPresenter;

    @Before
    public void setUp() {
        mPresenter = new NotificationPresenter();

        now = Calendar.getInstance();
        now.set(2017, 0, 9, 13, 0); // Monday, 1/9/2017, 13:00:00
    }

    @Test
    public void testTimeHasPassed() {
        boolean hasPassed = mPresenter.timeHasPassed("11:00:00", 2, now);

        assertTrue(hasPassed);
    }

    @Test
    public void testTimeHasNotPassed() {
        boolean hasPassed = mPresenter.timeHasPassed("14:00:00", 2, now);

        assertFalse(hasPassed);
    }

    @Test
    public void testParseTimeStringToMs() {
        now.setTimeZone(TimeZone.getTimeZone("EST"));
        Long timeInMs = mPresenter.parseTimeStringToMs("14:00:00", 3, now);

        assertEquals(1484074800000L, timeInMs.longValue());
    }

    @Test
    public void testSetFromNotificationSettings() {
        NotificationSettings n = new NotificationSettings(true, true, true, true, true, true);

        // Set up expected set
        Set<String> expected = new HashSet<>(6);
        expected.add("opening");
        expected.add("closing");
        expected.add("interval_on");
        expected.add("interval_15");
        expected.add("interval_30");
        expected.add("interval_hour");

        Set<String> actual = mPresenter.getSetFromNotificationSettings(n);

        assertEquals(expected, actual);
    }

    @Test
    public void testNotificationSettingsFromSet() {
        NotificationSettings expected = new NotificationSettings(true, true,
                true, true, true, true);

        // Set up set
        Set<String> set = new HashSet<>(6);
        set.add("opening");
        set.add("closing");
        set.add("interval_on");
        set.add("interval_15");
        set.add("interval_30");
        set.add("interval_hour");

        NotificationSettings actual = mPresenter.getNotificationSettingsFromSet(set);

        assertEquals(expected, actual);
    }
}
