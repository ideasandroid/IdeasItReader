package com.ideasandroid.itreader;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TextView;

public class AboutActivity extends Activity {

    ViewPager mViewPager;
    
    TabsAdapter mTabsAdapter;

    private TextView mbtAbout = null;
    TabHost mTabHost = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
	super.onCreate(savedInstanceState);
	mViewPager = new ViewPager(this);
	mViewPager.setId(R.id.pager);
	setContentView(mViewPager);
	final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        mTabsAdapter = new TabsAdapter(this, mViewPager);
        Bundle args = new Bundle();
        args.putString("text", getText(R.string.mbt_about).toString());
        mTabsAdapter.addTab(bar.newTab().setText("关于"),
        	TextFragment.class, args);
        Bundle args1 = new Bundle();
        args1.putString("text", getText(R.string.mbt_partner).toString());
        mTabsAdapter.addTab(bar.newTab().setText("合作"),
        	TextFragment.class,args1);
        Bundle args2 = new Bundle();
        args2.putString("text", getText(R.string.mbt_thanks).toString());
        mTabsAdapter.addTab(bar.newTab().setText("感谢"),
        	TextFragment.class,args2);
        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
    }

    public static class TabsAdapter extends FragmentPagerAdapter implements
	    ActionBar.TabListener, ViewPager.OnPageChangeListener {
	private final Context mContext;
	private final ActionBar mActionBar;
	private final ViewPager mViewPager;
	private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

	static final class TabInfo {
	    private final Class<?> clss;
	    private final Bundle args;

	    TabInfo(Class<?> _class, Bundle _args) {
		clss = _class;
		args = _args;
	    }
	}

	public TabsAdapter(Activity activity, ViewPager pager) {
	    super(activity.getFragmentManager());
	    mContext = activity;
	    mActionBar = activity.getActionBar();
	    mViewPager = pager;
	    mViewPager.setAdapter(this);
	    mViewPager.setOnPageChangeListener(this);
	}

	public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
	    TabInfo info = new TabInfo(clss, args);
	    tab.setTag(info);
	    tab.setTabListener(this);
	    mTabs.add(info);
	    mActionBar.addTab(tab);
	    notifyDataSetChanged();
	}

	@Override
	public int getCount() {
	    return mTabs.size();
	}

	@Override
	public Fragment getItem(int position) {
	    TabInfo info = mTabs.get(position);
	    return Fragment.instantiate(mContext, info.clss.getName(),
		    info.args);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
		int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
	    mActionBar.setSelectedNavigationItem(position);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
	    Object tag = tab.getTag();
	    for (int i = 0; i < mTabs.size(); i++) {
		if (mTabs.get(i) == tag) {
		    mViewPager.setCurrentItem(i);
		}
	    }
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}
    }

}
