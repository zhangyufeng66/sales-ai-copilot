package com.salesaicopilot;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    int blue = Color.rgb(22, 119, 255);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 40, 32, 32);
        root.setBackgroundColor(Color.rgb(247, 249, 252));

        TextView title = new TextView(this);
        title.setText("销售AI副驾驶");
        title.setTextSize(26);
        title.setTextColor(Color.rgb(30, 30, 30));
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, 1);

        root.addView(title, new LinearLayout.LayoutParams(
                -1, 70
        ));

        TextView subtitle = new TextView(this);
        subtitle.setText("客户消息 → AI判断 → 智能回复");
        subtitle.setTextSize(15);
        subtitle.setTextColor(Color.GRAY);
        subtitle.setGravity(Gravity.CENTER);

        root.addView(subtitle, new LinearLayout.LayoutParams(
                -1, 50
        ));

        EditText input = new EditText(this);
        input.setHint("请输入客户刚刚发来的消息...");
        input.setTextSize(16);
        input.setPadding(24, 20, 24, 20);
        input.setBackground(roundBackground(Color.WHITE, 20));

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(-1, 180);
        inputParams.setMargins(0, 30, 0, 20);

        root.addView(input, inputParams);

        Button analyze = new Button(this);
        analyze.setText("AI分析客户意图");
        analyze.setTextSize(17);
        analyze.setTextColor(Color.WHITE);
        analyze.setBackground(roundBackground(blue, 20));

        root.addView(analyze, new LinearLayout.LayoutParams(
                -1, 65
        ));

        TextView result = new TextView(this);
        result.setText("等待分析...");
        result.setTextSize(17);
        result.setTextColor(Color.DKGRAY);
        result.setPadding(24, 24, 24, 24);
        result.setBackground(roundBackground(Color.WHITE, 20));

        LinearLayout.LayoutParams resultParams =
                new LinearLayout.LayoutParams(-1, 240);
        resultParams.setMargins(0, 25, 0, 0);

        root.addView(result, resultParams);

        analyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = input.getText().toString();

                if (message.trim().isEmpty()) {
                    result.setText("请先输入客户消息。");
                    return;
                }

                result.setText(
                        "【客户意图】\n价格/购买意向判断\n\n" +
                        "【AI建议】\n" +
                        "客户可能存在价格顾虑，建议不要立即降价。\n\n" +
                        "【推荐回复】\n" +
                        "“理解您的考虑，我们的价格主要是基于产品配置和服务保障。如果您方便，我可以根据您的实际需求再帮您优化一下方案。”"
                );
            }
        });

        setContentView(root);
    }

    private GradientDrawable roundBackground(int color, float radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }
}