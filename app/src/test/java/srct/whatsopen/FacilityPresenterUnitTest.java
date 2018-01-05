package srct.whatsopen;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import io.realm.RealmList;
import srct.whatsopen.model.Facility;
import srct.whatsopen.model.Location;
import srct.whatsopen.model.MainSchedule;
import srct.whatsopen.model.OpenTimes;
import srct.whatsopen.model.Schedule;
import srct.whatsopen.model.SpecialSchedule;
import srct.whatsopen.presenters.FacilityPresenter;

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
        MainSchedule mainSchedule = new MainSchedule(openTimesList,
                "2017-01-09", "2017-01-15", false);

        Location location = new Location("Whitetop Hall");
        mFacility = new Facility("Chef's Table at Brooklyn Fare", location,
                mainSchedule, new RealmList<>(), false, true, "");

        now = Calendar.getInstance();
        now.set(2017, 0, 9, 13, 0); // Monday, 1/9/2017, 13:00:00
    }


    @Test
    public void testFacilitySchedule() {
        // Set date
        now.set(2017, 0, 9, 13, 0); // Monday, 1/9/2017, 13:00:00
        Schedule schedule = mPresenter.getActiveSchedule(mFacility, now);
        String scheduleText = mPresenter.getScheduleText(schedule, now);

        assertEquals("<strong><b>Monday</b>: 11:00 AM - 6:00 PM</strong><br/>" +
                     "<b>Tuesday</b>: 1:00 PM - 6:00 PM", scheduleText);
    }

    @Test
    public void testFacilityScheduleNoSchedule() {
        mFacility.setMainSchedule(new MainSchedule(new RealmList<OpenTimes>(),
        "2017-01-09", "2017-01-15", false));

        Schedule schedule = mPresenter.getActiveSchedule(mFacility, now);
        String scheduleText = mPresenter.getScheduleText(schedule, now);

        assertEquals("No schedule available", scheduleText);
    }

    @Test
    public void testFacilityScheduleForSpecialSchedule() {
        RealmList<OpenTimes> openTimesList = mFacility.getMainSchedule().getOpenTimesList();

        // Set SpecialSchedule
        SpecialSchedule s1 = new SpecialSchedule(openTimesList,
                "2017-03-06", "2017-03-15", false);
        SpecialSchedule s2 = new SpecialSchedule(openTimesList,
                "2017-06-09", "2017-07-15", false);
        RealmList<SpecialSchedule> specialSchedules = new RealmList<>();
        specialSchedules.add(s1);
        specialSchedules.add(s2);

        mFacility.setSpecialSchedules(specialSchedules);

        // Set date
        now.set(2017, 2, 9, 12, 0); // Thursday, 3/9/2017, 12:00:00

        Schedule schedule = mPresenter.getActiveSchedule(mFacility, now);
        String scheduleText = mPresenter.getScheduleText(schedule, now);

        assertEquals("<b>Monday</b>: 11:00 AM - 6:00 PM<br/>" +
                "<b>Tuesday</b>: 1:00 PM - 6:00 PM", scheduleText);
    }

    @Test
    public void testFacilityScheduleNeverCloses() {
        RealmList<OpenTimes> openTimesList = new RealmList<>();
        openTimesList.add(new OpenTimes(0, 6, "00:00:00", "23:59:59"));
        mFacility.setOpen(true);
        mFacility.setMainSchedule(new MainSchedule(openTimesList,
                "2017-01-09", "2017-01-15", true));

        Schedule schedule = mPresenter.getActiveSchedule(mFacility, now);
        String scheduleText = mPresenter.getScheduleText(schedule, now);

        assertEquals("This facility is always open", scheduleText);
    }

    @Test
    public void testFacilitySchedule_2() {
        RealmList<OpenTimes> openTimesList = new RealmList<>();
        openTimesList.add(new OpenTimes(5, 5, "08:00:00", "09:00:00"));
        openTimesList.add(new OpenTimes(6, 6, "08:00:00", "09:00:00"));
        mFacility.setMainSchedule(new MainSchedule(openTimesList,
                "2017-01-09", "2017-01-15", false));

        Schedule schedule = mPresenter.getActiveSchedule(mFacility, now);
        String scheduleText = mPresenter.getScheduleText(schedule, now);

        assertEquals("<b>Saturday</b>: 8:00 AM - 9:00 AM<br/>" +
                "<b>Sunday</b>: 8:00 AM - 9:00 AM", scheduleText);
    }
}
