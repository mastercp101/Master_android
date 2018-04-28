package nom.cp101.master.master.Master;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import nom.cp101.master.master.CourseArticle.CourseArticleFragment;
import nom.cp101.master.master.ExperienceArticle.ExperienceArticleFragment;
import nom.cp101.master.master.InformationFragment;
import nom.cp101.master.master.Message.MessageFragment;
import nom.cp101.master.master.Notification.NotificationFragment;
import nom.cp101.master.master.R;

//Master APP Activity
public class Master extends AppCompatActivity {
    //置入主頁面所需元件
    Toolbar toolbar_master;
    TabLayout tab_master;
    ViewPager viewpager_master;
    SearchView search_master;
    AutoCompleteTextView autoCompleteTextView_master;

    //課程文章frag
    CourseArticleFragment courseArticleFragment;
    //心得文章frag
    ExperienceArticleFragment experienceArticleFragment;
    //私訊frag
    MessageFragment messageFragment;
    //通知frag
    NotificationFragment notificationFragment;
    //資料frag
    InformationFragment informationFragment;

    //記錄置入TabLayout內,與Viewpager橋接的個主功能Fragment
    List<Fragment> list_master;
    //連接ViewPager與主功能Fragment的橋接器
    MasterFragmentPagerAdapter masterFragmentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.master_main);
        //初始化元件
        findViews();
        //設定自訂ToolBar套為ActionBar
        setSupportActionBar(toolbar_master);
        //主功能頁面Fragment與搭載在TabLayout內ViewPager的橋接設定
        setViewPager();
        //設定置入TabLayout的圖片
        setTabLayout();
    }

    //設定置入TabLayout的圖片
    private void setTabLayout() {
        //tablayout圖示
        int[] imgs={R.drawable.tab_article,
                R.drawable.tab_experience,
                R.drawable.tab_message,
//                R.drawable.tab_notice,
                R.drawable.tab_information};
        //TabLayout接上viewPager
        tab_master.setupWithViewPager(viewpager_master, true);
        //設置tablayout圖示
        for (int i = 0; i < list_master.size(); i++) {
            tab_master.getTabAt(i).setIcon(imgs[i]);
        }
        //取得tabLayout各鈕的position以便判斷顯示或隱藏toolBar,並設其標題名
        tab_master.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewpager_master){
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                int position=tab.getPosition();
                if(position==0){
                    toolbar_master.setVisibility(View.VISIBLE);
                    setTitle(R.string.courseArticle);
                }else if(position==1){
                    toolbar_master.setVisibility(View.VISIBLE);
                    setTitle(R.string.experienceArticle);
                }
                else
                    toolbar_master.setVisibility(View.GONE);
            }
        });

    }

    //主功能頁面Fragment與搭載在TabLayout內ViewPager的橋接設定
    private void setViewPager() {
        list_master = new ArrayList<>();

        //此區添加個主功能的Fragment,設置完成請將替代的Fragment移除
        courseArticleFragment = new CourseArticleFragment();
        experienceArticleFragment=new ExperienceArticleFragment();
        messageFragment = new MessageFragment();
        notificationFragment = new NotificationFragment();
        informationFragment = new InformationFragment();

        //此區置換個主功能的Fragment,設置完成請將添加對應的Fragment移除
        list_master.add(courseArticleFragment);
        list_master.add(experienceArticleFragment);
        list_master.add(messageFragment);
//        list_master.add(notificationFragment);
        list_master.add(informationFragment);

        //主畫面Master-期內掛載在TabLayout內的ViewPager與包裝過的list橋接設置
        masterFragmentPagerAdapter = new MasterFragmentPagerAdapter(getSupportFragmentManager(), list_master);
        viewpager_master.setAdapter(masterFragmentPagerAdapter);
    }


    //初始化元件-TabLayout, ViewPager
    private void findViews() {
        toolbar_master=(Toolbar)findViewById(R.id.toolbar_master);
        tab_master = (TabLayout) findViewById(R.id.tab_master);
        viewpager_master = (ViewPager) findViewById(R.id.viewpager_master);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //取得toolbar的menu樣式檔
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        //通過getActionView()將menu上的item轉為view使用
        search_master=(SearchView)menu.findItem(R.id.search_master).getActionView();
        //抓取隱藏在searchView內的AuyoCompleteTextView
        autoCompleteTextView_master=(AutoCompleteTextView)search_master.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        //設定置入於SearchView內的AutoCompleteTextView橋接器
        setSearchAutoComplete();

        autoCompleteTextView_master.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //取得點擊autoCompleteTextView內的Item值
                String searchContent=parent.getItemAtPosition(position).toString();
                //設置查詢數據給予searchView並提交(true)
                search_master.setQuery(searchContent,true);
            }
        });

        return true;
    }

    //設置SearcjAutoComplete
    private void setSearchAutoComplete() {
        String[] searchContent=getResources().getStringArray(R.array.languages);
        //autoCompleteTextView橋接自定viewItem
        autoCompleteTextView_master.setAdapter(new ArrayAdapter<>(this,
                R.layout.master_search_autocomplete_item,
                R.id.tv_autocomplete,
                searchContent));
        //設置提示條件-輸入長度取決於何時顯示
        autoCompleteTextView_master.setThreshold(1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //item為addText時(新增文章),先做Toast
            case R.id.addText_master:
                Toast.makeText(getApplicationContext(),"Click addText",Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }
}