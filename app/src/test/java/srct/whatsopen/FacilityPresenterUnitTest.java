package srct.whatsopen;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import io.realm.RealmList;
import srct.whatsopen.model.Facility;
import srct.whatsopen.model.MainSchedule;
import srct.whatsopen.model.OpenTimes;
import srct.whatsopen.ui.presenters.FacilityPresenter;

import static org.junit.Assert.*;

public class FacilityPresenterUnitTest {

    FacilityPresenter mPresenter;
    Facility mFacility;
    Calendar now;

    @Before
    public void setUp() {
        mPresenter = new FacilityPresenter();

        OpenTimes o1 = new OpenTimes(0, 0, "11:00:00", "18:00:00"); // Mondays 11:00-18:00
        OpenTimes o2 = new OpenTimes(1, 1, "13:00:00", "18:00:00"); // Tuesdays 13:00-18:00
        RealmList<OpenTimes> openTimesList = new RealmList<>();
        openTimesList.add(o1);
        openTimesList.add(o2);
        MainSchedule mainSchedule = new MainSchedule(openTimesList);

        mFacility = new Facility("Chef's Table at Brooklyn Fare", "Whitetop Hall",
                mainSchedule, false, true);

        now = Calendar.getInstance();
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

        assertEquals("Opens on Monday at 11:00 AM", statusDuration);
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
    public void testFacilityMessageNoSchedule() {
        // Set date
        now.set(2017, 0, 9, 13, 0); // Monday, 1/9/2017, 13:00:00
        mFacility.setMainSchedule(new MainSchedule(new RealmList<OpenTimes>()));

        String statusDuration = mPresenter.getStatusDuration(mFacility, now);

        assertEquals("No open time on schedule", statusDuration);
    }

    @Test
    public void testFacilitySchedule() {
        String schedule = mPresenter.getSchedule(mFacility);

        assertEquals("<b>Monday</b>: 11:00 AM - 6:00 PM<br/>" +
                     "<b>Tuesday</b>: 1:00 PM - 6:00 PM", schedule);
    }

    @Test
    public void testFacilityScheduleNoSchedule() {
        mFacility.setMainSchedule(new MainSchedule(new RealmList<OpenTimes>()));

        String schedule = mPresenter.getSchedule(mFacility);

        assertEquals("No schedule available", schedule);
    }
}
