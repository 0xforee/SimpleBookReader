package org.foree.zetianji.ui.activity;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.igexin.sdk.PushManager;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.foree.zetianji.base.BaseApplication;
import org.foree.zetianji.R;
import org.foree.zetianji.dao.NovelDao;
import org.foree.zetianji.ui.fragment.ItemListFragment;
import org.foree.zetianji.helper.WebSiteInfo;

public class MainActivity extends AppCompatActivity implements Drawer.OnDrawerItemClickListener{
    Toolbar toolbar;
    NovelDao novelDao;

    private AccountHeader headerResult = null;
    private Drawer result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        novelDao = new NovelDao(this);
        // TODO 增加下载单章与下载全部按钮

        if (savedInstanceState == null) {
            Fragment f = ItemListFragment.newInstance(1);
            getFragmentManager().beginTransaction().replace(R.id.content_main, f).commit();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PushManager.getInstance().initialize(this.getApplicationContext());

        initWebSites();
        setUpDrawerLayout(savedInstanceState);


    }
    private void setUpDrawerLayout(Bundle savedInstanceState){

        // Create a few sample profile
        // NOTE you have to define the loader logic too. See the CustomApplication for more details
        final IProfile profile = new ProfileDrawerItem().withName("").withEmail("").withIcon("").withIdentifier(100);

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(MainActivity.this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        profile,
                        //don't ask but google uses 14dp for the add account icon in gmail but 20dp for the normal icons (like manage account)
                        new ProfileSettingDrawerItem().withName("Add Account").withDescription("Add new GitHub Account").withIdentifier(1001),
                        new ProfileSettingDrawerItem().withName("Manage Account").withIdentifier(100001)
                )
                .withSavedInstance(savedInstanceState)
                .build();

        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withAccountHeader(headerResult)
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .withOnDrawerItemClickListener(this)
                .build();

        // init website
        if ( novelDao.findAllWebSites().size() > 0){
            for(WebSiteInfo wb: novelDao.findAllWebSites()){
                result.addItem(new PrimaryDrawerItem().withName(wb.getName()).withIdentifier(wb.getId()));
            }
        }

    }

    private void initWebSites(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
        if(sp.getBoolean("first_run", true)) {
            WebSiteInfo webSiteInfo1 = new WebSiteInfo("笔趣阁", "http://www.biquge.com", "/0_168/", "utf-8");
            WebSiteInfo webSiteInfo2 = new WebSiteInfo("笔趣阁LA", "http://www.biquge.la", "/book/168/", "gbk");

            novelDao.insertWebSite(webSiteInfo1);
            novelDao.insertWebSite(webSiteInfo2);

            sp.edit().putBoolean("first_run", false).apply();
        }
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        if( drawerItem != null){
            Fragment f = ItemListFragment.newInstance(position);
            getFragmentManager().beginTransaction().replace(R.id.content_main, f).commit();
        }
        return false;
    }
}
