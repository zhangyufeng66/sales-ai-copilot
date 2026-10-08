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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    private final int blue = Color.rgb(22, 119, 255);
    private final int dark = Color.rgb(30, 30, 30);

    private EditText input;
    private TextView result;
    private Button analyzeButton;

    private static final String API_URL =
            "https://sales-ai-copilot-beta.vercel.app/api/analyze";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildInterface();
    }

    private void buildInterface() {

        ScrollView scrollView = new ScrollView(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 30, 28, 30);
        root.setBackgroundColor(Color.rgb(247, 249, 252));

        TextView title = new TextView(this);
        title.setText("销售AI副驾驶");
        title.setTextSize(28);
        title.setTextColor(dark);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, 1);

        root.addView(title, new LinearLayout.LayoutParams(
                -1, 70
        ));

        TextView subtitle = new TextView(this);
        subtitle.setText("DeepSeek AI · 客户分析 · 销售策略 · 智能话术");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.GRAY);
        subtitle.setGravity(Gravity.CENTER);

        root.addView(subtitle, new LinearLayout.LayoutParams(
                -1, 55
        ));

        TextView customerTitle = new TextView(this);
        customerTitle.setText("客户消息 / 客户情况");
        customerTitle.setTextSize(18);
        customerTitle.setTextColor(dark);
        customerTitle.setTypeface(null, 1);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(-1, 50);

        titleParams.setMargins(0, 20, 0, 8);

        root.addView(customerTitle, titleParams);

        input = new EditText(this);
        input.setHint(
                "例如：客户说你们价格有点高，我再考虑一下..."
        );
        input.setTextSize(17);
        input.setGravity(Gravity.TOP);
        input.setPadding(22, 20, 22, 20);
        input.setSingleLine(false);
        input.setBackground(
                roundBackground(Color.WHITE, 20)
        );

        root.addView(input, new LinearLayout.LayoutParams(
                -1, 220
        ));

        analyzeButton = new Button(this);
        analyzeButton.setText("🤖 DeepSeek AI分析");
        analyzeButton.setTextSize(17);
        analyzeButton.setTextColor(Color.WHITE);
        analyzeButton.setBackground(
                roundBackground(blue, 20)
        );

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(-1, 72);

        buttonParams.setMargins(0, 18, 0, 20);

        root.addView(analyzeButton, buttonParams);

        TextView resultTitle = new TextView(this);
        resultTitle.setText("AI销售分析");
        resultTitle.setTextSize(19);
        resultTitle.setTextColor(dark);
        resultTitle.setTypeface(null, 1);

        root.addView(resultTitle, new LinearLayout.LayoutParams(
                -1, 50
        ));

        result = new TextView(this);
        result.setText(
                "等待输入客户消息...\n\n" +
                "AI将分析：\n" +
                "• 客户意向\n" +
                "• 客户需求\n" +
                "• 客户痛点\n" +
                "• 客户顾虑\n" +
                "• 成交可能性\n" +
                "• 下一步跟进策略\n" +
                "• 推荐销售话术"
        );

        result.setTextSize(16);
        result.setTextColor(Color.DKGRAY);
        result.setPadding(22, 22, 22, 22);
        result.setGravity(Gravity.TOP);
        result.setBackground(
                roundBackground(Color.WHITE, 20)
        );

        root.addView(result, new LinearLayout.LayoutParams(
                -1, 520
        ));

        TextView tip = new TextView(this);

        tip.setText(
                "\n使用方法\n" +
                "① 输入客户说的话或客户情况\n" +
                "② 点击“DeepSeek AI分析”\n" +
                "③ AI分析客户意向和需求\n" +
                "④ 给出下一步销售策略\n" +
                "⑤ 自动生成推荐话术"
        );

        tip.setTextSize(14);
        tip.setTextColor(Color.GRAY);

        LinearLayout.LayoutParams tipParams =
                new LinearLayout.LayoutParams(-1, -2);

        tipParams.setMargins(5, 20, 5, 20);

        root.addView(tip, tipParams);

        scrollView.addView(root);

        setContentView(scrollView);

        analyzeButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        String message =
                                input.getText().toString().trim();

                        if (message.isEmpty()) {

                            Toast.makeText(
                                    MainActivity.this,
                                    "请先输入客户消息",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        analyzeCustomer(message);
                    }
                }
        );
    }

    private void analyzeCustomer(String message) {

        analyzeButton.setEnabled(false);
        analyzeButton.setText("AI正在分析...");

        result.setText(
                "DeepSeek 正在分析客户信息，请稍候..."
        );

        new Thread(new Runnable() {

            @Override
            public void run() {

                try {

                    URL url = new URL(API_URL);

                    HttpURLConnection connection =
                            (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty(
                            "Content-Type",
                            "application/json"
                    );

                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(30000);

                    connection.setDoOutput(true);

                    JSONObject request =
                            new JSONObject();

                    request.put("message", message);

                    String json =
                            request.toString();

                    OutputStream output =
                            connection.getOutputStream();

                    output.write(
                            json.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

                    output.flush();
                    output.close();

                    int responseCode =
                            connection.getResponseCode();

                    InputStream stream;

                    if (responseCode >= 200 &&
                            responseCode < 300) {

                        stream =
                                connection.getInputStream();

                    } else {

                        stream =
                                connection.getErrorStream();
                    }

                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            stream,
                                            StandardCharsets.UTF_8
                                    )
                            );

                    StringBuilder response =
                            new StringBuilder();

                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();

                    JSONObject jsonResponse =
                            new JSONObject(
                                    response.toString()
                            );

                    final String aiResult;

                    if (jsonResponse.has("result")) {

                        aiResult =
                                jsonResponse.getString("result");

                    } else if (jsonResponse.has("error")) {

                        aiResult =
                                "AI接口错误：\n" +
                                jsonResponse.getString("error");

                    } else {

                        aiResult =
                                "AI返回数据异常：\n" +
                                response.toString();
                    }

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            result.setText(aiResult);

                            analyzeButton.setEnabled(true);
                            analyzeButton.setText(
                                    "🤖 DeepSeek AI分析"
                            );
                        }
                    });

                    connection.disconnect();

                } catch (Exception e) {

                    final String error =
                            e.getMessage();

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            result.setText(
                                    "连接AI失败\n\n" +
                                    "错误信息：\n" +
                                    error +
                                    "\n\n请检查网络连接和服务器配置。"
                            );

                            analyzeButton.setEnabled(true);
                            analyzeButton.setText(
                                    "🤖 DeepSeek AI分析"
                            );
                        }
                    });
                }
            }

        }).start();
    }

    private GradientDrawable roundBackground(
            int color,
            float radius) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(radius);

        return drawable;
    }
}