package org.foree.zetianji.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

import org.foree.zetianji.R;

public class BookShelfActivity extends AppCompatActivity implements CardView.OnClickListener{
    CardView cardView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_shelf);

        cardView = (CardView) findViewById(R.id.novel_card);
        cardView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(BookShelfActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
