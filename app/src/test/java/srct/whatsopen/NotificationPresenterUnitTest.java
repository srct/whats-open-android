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
}
