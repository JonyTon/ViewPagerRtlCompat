package com.smile.compat;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smile.rtlcompat.RtlCompatViewPager;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private RtlCompatViewPager rtlCompatViewPager;

    private int curRtlFlag = RtlCompatViewPager.FLAG_NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.id_update_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (curRtlFlag) {
                    case RtlCompatViewPager.FLAG_FORCE_RTL:
                        curRtlFlag = RtlCompatViewPager.FLAG_FORCE_LTR;
                        break;
                    case RtlCompatViewPager.FLAG_FORCE_LTR:
                    case RtlCompatViewPager.FLAG_NONE:
                        curRtlFlag = RtlCompatViewPager.FLAG_FORCE_RTL;
                        break;
                }
                rtlCompatViewPager.updateForceFlag(curRtlFlag);
            }
        });
        rtlCompatViewPager = (RtlCompatViewPager) findViewById(R.id.id_view_pager);
        rtlCompatViewPager.setAdapter(new TestAdapter(this));
    }

    // newPage
    private static View newPage(Context context, int position, int bgColor) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(20);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(bgColor);

        textView.setText("Page: " + position);
        return textView;
    }

    /**
     * TestAdapter
     */
    private static class TestAdapter extends PagerAdapter {

        private List<View> viewList = new ArrayList<>();

        TestAdapter(Context context) {
            viewList.add(newPage(context, 0, Color.GRAY));
            viewList.add(newPage(context, 1, Color.RED));
            viewList.add(newPage(context, 2, Color.CYAN));
            viewList.add(newPage(context, 3, Color.MAGENTA));
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = viewList.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
