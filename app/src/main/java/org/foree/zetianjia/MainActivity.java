package org.foree.zetianjia;

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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    String url = "http://www.biquge.com/0_168/";
    private ArrayAdapter<String> adapter;
    private ArrayList<String>  hrefList;
    private ArrayList<String>  titleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ListView lvContent = (ListView)findViewById(R.id.lv_content);
        final TextView tvUpdate = (TextView)findViewById(R.id.tv_update);
        hrefList = new ArrayList<>();
        titleList = new ArrayList<>();

        NetRequest.getHtml(url, new NetCallback() {
            @Override
            public void onSuccess(String data) {
                Document doc = Jsoup.parse(data);
                Elements elements_contents = doc.select("dd");
                Elements updates = doc.select("[property~=.*update_time]");
                for(Element update: updates){
                    Log.i("MM", update.toString());
                    tvUpdate.setText("最后更新时间：" + update.attr("content"));
                }
                Document contents = Jsoup.parse(elements_contents.toString());
                Elements elements_a = contents.getElementsByTag("a");
                for(Element link: elements_a){
                    hrefList.add(link.attr("href"));
                    titleList.add(link.text());
//                    Log.i("HH", link.text());
//                    Log.i("HH", link.attr("href"));
                }

                Collections.reverse(hrefList);
                Collections.reverse(titleList);
                adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1);
                Log.i("HH", titleList.size() + "");
                for(int i = 0; i < titleList.size(); i++){
                    adapter.add(titleList.get(i));
                }

                lvContent.setAdapter(adapter);
            }
            @Override
            public void onFail(String msg) {

            }
        });

        lvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this,i+"",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("href", hrefList.get(i));
                bundle.putString("title", titleList.get(i));
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });
    }

}
