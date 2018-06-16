package eu.selfhost.mirc0.schichtplaner;

import android.arch.persistence.room.Room;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import eu.selfhost.mirc0.schichtplaner.database.ShiftDatabase;
import eu.selfhost.mirc0.schichtplaner.database.ShiftRepository;

public class MainActivity extends AppCompatActivity
                            implements CalendarFragment.OnFragmentInteractionListener {


    private ShiftEditController _createShiftListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String DATABASE_NAME = "shift_db";
        ShiftDatabase shiftDatabase = Room.databaseBuilder(getApplicationContext(),
                ShiftDatabase.class, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();

        ShiftRepository shiftRepository = new ShiftRepository(shiftDatabase);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        _createShiftListener = new ShiftEditController(shiftRepository);

        // Setup caldroid fragment
        // **** If you want normal CaldroidFragment, use below line ****
        CalendarFragment caldroidFragment = new CalendarFragment();
        caldroidFragment.setRepository(shiftRepository);
        caldroidFragment.setEditShiftListener(_createShiftListener);
        mSectionsPagerAdapter.setCaldroidFragment(caldroidFragment);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> _createShiftListener.createNewShift(view.getRootView().getContext()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private CalendarFragment caldroidFragment;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return caldroidFragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 1;
        }

        void setCaldroidFragment(CalendarFragment caldroidFragment) {
            this.caldroidFragment = caldroidFragment;
        }
    }
}
