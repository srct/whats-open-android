package srct.whatsopen;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import io.realm.RealmList;
import srct.whatsopen.model.Facility;
import srct.whatsopen.model.MainSchedule;
import srct.whatsopen.model.OpenTimes;
import srct.whatsopen.model.SpecialSchedule;
import srct.whatsopen.presenters.MainPresenter;

import static org.junit.Assert.*;

public class MainPresenterUnitTest {

    Calendar now;
    MainPresenter mPresenter;
    Facility mFacility;

    @Before
    public void setUp() {
        mPresenter = new MainPresenter();

        // Set up the Facility
        OpenTimes o1 = new OpenTimes(0, 0, "11:00:00", "18:00:00"); // Mondays 11:00-18:00
        OpenTimes o2 = new OpenTimes(1, 1, "13:00:00", "18:00:00"); // Tuesdays 13:00-18:00
        RealmList<OpenTimes> openTimesList = new RealmList<>();
        openTimesList.add(o1);
        openTimesList.add(o2);
        MainSchedule mainSchedule = new MainSchedule(openTimesList,
                "2017-01-09", "2017-01-15");

        SpecialSchedule s1 = new SpecialSchedule(openTimesList,
            "2017-03-06", "2017-03-15");
        SpecialSchedule s2 = new SpecialSchedule(openTimesList,
                "2017-06-09", "2017-07-15");
        RealmList<SpecialSchedule> specialSchedules = new RealmList<>();
        specialSchedules.add(s1);
        specialSchedules.add(s2);

        mFacility = new Facility("The French Laundry", "Johnson Center",
                mainSchedule, specialSchedules, false, true, "");

        now = Calendar.getInstance();
    }

    @Test
    public void testFacilityIsOpen() {
        // Set date
        now.set(2017, 0, 9, 12, 0); // Monday, 1/9/2017, 12:00:00

        assertTrue(mPresenter.getOpenStatus(mFacility, now));
    }

    @Test
    public void testFacilityIsOpenPastMidnight() {
        // Set date
        now.set(2017, 0, 12, 1, 0); // Thursday, 1/12/2017, 01:00:00

        // Add more open times
        OpenTimes o3 = new OpenTimes(2, 2, "13:00:00", "23:59:59"); // Wednesday 13:00-midnight
        OpenTimes o4 = new OpenTimes(3, 3, "13:00:00", "23:59:59"); // Thursday 13:00-midnight
        OpenTimes o5 = new OpenTimes(3, 3, "00:00:00", "02:00:00"); // Thursday 00:00-02:00
        mFacility.getMainSchedule().getOpenTimesList().add(o3);
        mFacility.getMainSchedule().getOpenTimesList().add(o4);
        mFacility.getMainSchedule().getOpenTimesList().add(o5);

        assertTrue(mPresenter.getOpenStatus(mFacility, now));
    }

    // this tests a different way of representing that a Facility is open past Midnight.
    // No, I don't know why there are multiple ways of representing that.
    @Test
    public void testFacilityIsOpenPastMidnight_2() {
        // Set date
        now.set(2017, 0, 12, 1, 0); // Thursday, 1/12/2017, 01:00:00

        // Add more open times
        OpenTimes o3 = new OpenTimes(2, 3, "13:00:00", "02:00:00"); // Wednesday 13:00-Thursday 2:00
        mFacility.getMainSchedule().getOpenTimesList().add(o3);

        assertTrue(mPresenter.getOpenStatus(mFacility, now));
    }

    @Test
    public void testFacilityIsOpenPastMidnight_3() {
        // Set date
        now.set(2017, 0, 11, 14, 0); // Wednesday, 1/11/2017, 14:00:00

        // Add more open times
        OpenTimes o3 = new OpenTimes(2, 3, "13:00:00", "02:00:00"); // Wednesday 13:00-Thursday 2:00
        mFacility.getMainSchedule().getOpenTimesList().add(o3);

        assertTrue(mPresenter.getOpenStatus(mFacility, now));
    }

    @Test
    public void testFacilityIsClosed() {
        // Set date
        now.set(2017, 1, 10, 12, 0); // Tuesday, 2/10/2017, 12:00:00

        assertFalse(mPresenter.getOpenStatus(mFacility, now));
    }

    @Test
    public void testFacilityIsOpenForSpecialSchedules() {
        // Set date
        now.set(2017, 2, 6, 12, 0); // Monday, 3/6/2017, 12:00:00

        assertTrue(mPresenter.getOpenStatus(mFacility, now));
    }

    @Test
    public void testFacilityMessageOpensToday() {
        // Set date
        now.set(2017, 0, 9, 10, 0); // Monday, 1/9/2017, 10:00:00
        String statusDuration = mPresenter.getStatusDuration(mFacility, now);

        assertEquals("Opens today at 11:00 AM", statusDuration);
    }

    @Test
    public void testFacilityMessageOpensNotToday() {
        // Set date
        now.set(2017, 0, 11, 10, 0); // Wednesday, 1/11/2017, 10:00:00
        String statusDuration = mPresenter.getStatusDuration(mFacility, now);

        assertEquals("Opens next on Monday at 11:00 AM", statusDuration);
    }

    @Test
    public void testFacilityMessageCloses() {
        // Set date
        now.set(2017, 0, 9, 13, 0); // Monday, 1/9/2017, 13:00:00
        mFacility.setOpen(true);
        String statusDuration = mPresenter.getStatusDuration(mFacility, now);

        assertEquals("Closes at 6:00 PM", statusDuration);
    }

    @Test
    public void testFacilityMessageClosesPastMidnight() {
        // Set date
        now.set(2017, 0, 12, 14, 0); // Thursday, 1/12/2017, 14:00:00
        mFacility.setOpen(true);

        // Add more open times
        OpenTimes o3 = new OpenTimes(2, 2, "13:00:00", "23:59:59"); // Wednesday 13:00-midnight
        OpenTimes o4 = new OpenTimes(3, 3, "13:00:00", "23:59:59"); // Thursday 13:00-midnight
        OpenTimes o5 = new OpenTimes(3, 3, "00:00:00", "02:00:00"); // Thursday 00:00-02:00
        mFacility.getMainSchedule().getOpenTimesList().add(o3);
        mFacility.getMainSchedule().getOpenTimesList().add(o4);
        mFacility.getMainSchedule().getOpenTimesList().add(o5);

        String statusDuration = mPresenter.getStatusDuration(mFacility, now);

        assertEquals("Closes at 2:00 AM", statusDuration);
    }

    @Test
    public void testFacilityMessageNoSchedule() {
        // Set date
        now.set(2017, 0, 9, 13, 0); // Monday, 1/9/2017, 13:00:00
        mFacility.setMainSchedule(new MainSchedule(new RealmList<OpenTimes>(),
                "2017-01-09", "2017-01-15"));

        String statusDuration = mPresenter.getStatusDuration(mFacility, now);

        assertEquals("No open time on schedule", statusDuration);
    }

    @Test
    public void testFacilityMessageNeverCloses() {
        RealmList<OpenTimes> openTimesList = new RealmList<>();
        openTimesList.add(new OpenTimes(0, 6, "00:00:00", "23:59:59"));
        mFacility.setOpen(true);
        mFacility.setMainSchedule(new MainSchedule(openTimesList,
                "2017-01-09", "2017-01-15"));

        // Set date
        now.set(2017, 0, 11, 10, 0); // Wednesday, 1/11/2017, 10:00:00

        String statusDuration = mPresenter.getStatusDuration(mFacility, now);

        assertEquals("Open 24/7", statusDuration);
    }

    @Test
    public void testFacilityMessage_2() {
        RealmList<OpenTimes> openTimesList = new RealmList<>();
        openTimesList.add(new OpenTimes(5, 5, "08:00:00", "09:00:00"));
        openTimesList.add(new OpenTimes(6, 6, "08:00:00", "09:00:00"));
        mFacility.setMainSchedule(new MainSchedule(openTimesList,
                "2017-01-09", "2017-01-15"));

        // Set date
        now.set(2017, 0, 11, 10, 0); // Wednesday, 1/11/2017, 10:00:00

        String statusDuration = mPresenter.getStatusDuration(mFacility, now);

        assertEquals("Opens next on Saturday at 8:00 AM", statusDuration);
    }
}
