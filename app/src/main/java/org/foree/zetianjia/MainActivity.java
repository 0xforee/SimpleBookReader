package org.foree.zetianjia;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends ListActivity {
    String url = "http://www.xxbiquge.com/5_5422/";
    private ArrayAdapter<String> adapter;
    private ArrayList<String>  hrefList;
    private ArrayList<String>  titleList;

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Toast.makeText(this,position+"",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("href", hrefList.get(position));
        bundle.putString("title", titleList.get(position));
        intent.putExtras(bundle);

        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hrefList = new ArrayList<>();
        titleList = new ArrayList<>();

        NetRequest.getHtml(url, new NetCallback() {
            @Override
            public void onSuccess(String data) {
                Document doc = Jsoup.parse(data);
                Elements elements_contents = doc.select("dd");
                Document contents = Jsoup.parse(elements_contents.toString());
                Elements elements_a = contents.getElementsByTag("a");
                for(Element link: elements_a){
                    hrefList.add(link.attr("href"));
                    titleList.add(link.text());
                    Log.i("HH", link.text());
                    Log.i("HH", link.attr("href"));
                }

                Collections.reverse(hrefList);
                Collections.reverse(titleList);
                adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1);
                Log.i("HH", titleList.size() + "");
                for(int i = 0; i < titleList.size(); i++){
                    adapter.add(titleList.get(i));
                }

                setListAdapter(adapter);
            }
            @Override
            public void onFail(String msg) {

            }
        });

    }

}
