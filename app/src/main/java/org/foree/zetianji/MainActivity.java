package org.foree.zetianji;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.foree.zetianji.book.Chapter;
import org.foree.zetianji.book.Novel;
import org.foree.zetianji.helper.BQGWebSiteHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    // TODO:增加切换来源的按钮
    private ArrayAdapter<String> adapter;
    private List<Chapter> chapterList;
    ListView lvContent;
    TextView tvUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // TODO 增加下载单章与下载全部按钮
        lvContent = (ListView)findViewById(R.id.lv_content);
        tvUpdate = (TextView)findViewById(R.id.tv_update);

        BQGWebSiteHelper absWebSiteHelper  = new BQGWebSiteHelper();
        absWebSiteHelper.getNovel(new NetCallback<Novel>() {
            @Override
            public void onSuccess(Novel data) {
                updateUI(data);
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(MainActivity.this, "getContentListError: " + msg, Toast.LENGTH_LONG).show();
            }
        });

        lvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("chapter", chapterList.get(i));
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });
    }

    private void parseTest(String data){
        Document doc = Jsoup.parse(data);
        Elements elements_contents = doc.select("dt");
        for(Element link: elements_contents){
            Log.i("HH", link.text());
        }
    }

    private void updateUI(Novel data){
        if (data.getUpdate_time() != null){
            tvUpdate.setText(getString(R.string.update_string) + data.getUpdate_time());
        }

        chapterList = data.getChapter_list();
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1);
        Log.i("HH", chapterList.size() + "");
        for(int i = 0; i < chapterList.size(); i++){
            adapter.add(chapterList.get(i).getTitle());
        }
        lvContent.setAdapter(adapter);
    }

}
