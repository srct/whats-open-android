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
                mainSchedule, specialSchedules, false, true);

        now = Calendar.getInstance();
    }

    @Test
    public void testFacilityIsOpen() {
        // Set date
        now.set(2017, 0, 9, 12, 0); // Monday, 1/9/2017, 12:00:00

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
}