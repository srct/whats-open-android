package srct.whatsopen;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import io.realm.RealmList;
import srct.whatsopen.model.Facility;
import srct.whatsopen.model.MainSchedule;
import srct.whatsopen.model.OpenTimes;
import srct.whatsopen.ui.presenters.MainPresenter;

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
        MainSchedule mainSchedule = new MainSchedule(openTimesList);

        mFacility = new Facility("The French Laundry", "Johnson Center",
                mainSchedule, false, true);
    }

    @Test
    public void testFacilityIsOpen() {
        // Set date
        now = Calendar.getInstance();
        now.set(2017, 0, 9, 12, 0); // Monday, 1/9/2017, 12:00:00

        assertTrue(mPresenter.getOpenStatus(mFacility, now));
    }

    @Test
    public void testFacilityIsClosed() {
        // Set date
        now = Calendar.getInstance();
        now.set(2017, 1, 10, 12, 0); // Tuesday, 1/10/2017, 12:00:00

        assertFalse(mPresenter.getOpenStatus(mFacility, now));
    }
}
