package com.salesaicopilot;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
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
    private final int background = Color.rgb(247, 249, 252);

    private EditText input;
    private TextView result;
    private Button analyzeButton;
    private ScrollView scrollView;
    private LinearLayout root;

    private static final String API_URL =
            "https://sales-ai-copilot-beta.vercel.app/api/analyze";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupStatusBar();
        buildInterface();
    }

    // =========================
    // 状态栏设置
    // =========================
    private void setupStatusBar() {

        Window window = getWindow();

        // 保留系统状态栏
        window.setStatusBarColor(background);

        // 状态栏文字使用深色
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.M) {

            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }

    // =========================
    // 创建界面
    // =========================
    private void buildInterface() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(background);

        // 基础间距
        final int baseLeft = 28;
        final int baseTop = 12;
        final int baseRight = 28;
        final int baseBottom = 35;

        // 先设置基础 Padding
        root.setPadding(
                baseLeft,
                baseTop,
                baseRight,
                baseBottom
        );

        // =========================
        // 自动获取状态栏高度
        // =========================
        root.setOnApplyWindowInsetsListener((v, insets) -> {

            int statusBarHeight = 0;

            if (android.os.Build.VERSION.SDK_INT >=
                    android.os.Build.VERSION_CODES.R) {

                statusBarHeight =
                        insets.getInsets(
                                WindowInsets.Type.statusBars()
                        ).top;

            } else {

                statusBarHeight =
                        insets.getSystemWindowInsetTop();
            }

            // 自动把内容往下移动
            v.setPadding(
                    baseLeft,
                    baseTop + statusBarHeight,
                    baseRight,
                    baseBottom
            );

            return insets;
        });

        // =========================
        // 标题
        // =========================
        TextView title = new TextView(this);

        title.setText("销售AI副驾驶");
        title.setTextColor(dark);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        titleParams.bottomMargin = 8;

        root.addView(title, titleParams);

        // =========================
        // 副标题
        // =========================
        TextView subtitle = new TextView(this);

        subtitle.setText(
                "DeepSeek AI · 客户分析 · 销售策略 · 智能话术"
        );

        subtitle.setTextColor(Color.rgb(100, 110, 125));
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.bottomMargin = 24;

        root.addView(subtitle, subtitleParams);

        // =========================
        // 客户消息标题
        // =========================
        TextView inputTitle = new TextView(this);

        inputTitle.setText("客户消息 / 客户情况");
        inputTitle.setTextColor(dark);
        inputTitle.setTextSize(17);
        inputTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams inputTitleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        inputTitleParams.bottomMargin = 10;

        root.addView(inputTitle, inputTitleParams);

        // =========================
        // 输入框
        // =========================
        input = new EditText(this);

        input.setHint(
                "例如：客户说你们价格有点高，我再考虑一下..."
        );

        input.setTextSize(16);
        input.setTextColor(dark);
        input.setHintTextColor(
                Color.rgb(150, 155, 165)
        );

        input.setGravity(Gravity.TOP | Gravity.START);

        input.setPadding(
                20,
                18,
                20,
                18
        );

        input.setMinHeight(150);

        input.setBackground(
                roundBackground(
                        Color.WHITE,
                        Color.rgb(225, 230, 238),
                        18
                )
        );

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        170
                );

        inputParams.bottomMargin = 18;

        root.addView(input, inputParams);

        // =========================
        // AI分析按钮
        // =========================
        analyzeButton = new Button(this);

        analyzeButton.setText("🤖 AI销售分析");

        analyzeButton.setTextColor(Color.WHITE);
        analyzeButton.setTextSize(18);

        analyzeButton.setGravity(Gravity.CENTER);

        analyzeButton.setAllCaps(false);

        analyzeButton.setPadding(
                10,
                10,
                10,
                10
        );

        analyzeButton.setBackground(
                roundBackground(
                        blue,
                        blue,
                        20
                )
        );

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        100
                );

        buttonParams.bottomMargin = 24;

        root.addView(
                analyzeButton,
                buttonParams
        );

        // =========================
        // 分析结果标题
        // =========================
        TextView resultTitle = new TextView(this);

        resultTitle.setText("AI销售分析");
        resultTitle.setTextColor(dark);
        resultTitle.setTextSize(19);
        resultTitle.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        LinearLayout.LayoutParams resultTitleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        resultTitleParams.bottomMargin = 10;

        root.addView(
                resultTitle,
                resultTitleParams
        );

        // =========================
        // 分析结果
        // =========================
        result = new TextView(this);

        result.setText(
                "请输入客户消息，然后点击“AI销售分析”。\n\n" +
                "AI将帮助你分析：\n" +
                "• 客户意向\n" +
                "• 客户需求\n" +
                "• 客户痛点\n" +
                "• 客户顾虑\n" +
                "• 成交可能性\n" +
                "• 下一步跟进策略\n" +
                "• 推荐销售话术"
        );

        result.setTextColor(dark);
        result.setTextSize(16);
        result.setGravity(Gravity.TOP | Gravity.START);
        result.setLineSpacing(6, 1.0f);

        result.setPadding(
                20,
                20,
                20,
                20
        );

        result.setBackground(
                roundBackground(
                        Color.WHITE,
                        Color.rgb(225, 230, 238),
                        18
                )
        );

        LinearLayout.LayoutParams resultParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        resultParams.bottomMargin = 20;

        root.addView(
                result,
                resultParams
        );

        // =========================
        // 使用提示
        // =========================
        TextView tips = new TextView(this);

        tips.setText(
                "使用提示\n\n" +
                "1. 输入客户真实聊天内容\n" +
                "2. 内容越具体，分析越准确\n" +
                "3. AI不会编造客户没有提供的信息\n" +
                "4. 建议结合实际情况判断AI给出的建议"
        );

        tips.setTextColor(
                Color.rgb(100, 110, 125)
        );

        tips.setTextSize(14);
        tips.setLineSpacing(5, 1.0f);

        tips.setPadding(
                20,
                20,
                20,
                20
        );

        tips.setBackground(
                roundBackground(
                        Color.rgb(240, 244, 249),
                        Color.rgb(225, 230, 238),
                        18
                )
        );

        root.addView(tips);

        // =========================
        // 点击AI分析
        // =========================
        analyzeButton.setOnClickListener(
                v -> analyzeCustomer()
        );

        // =========================
        // ScrollView
        // =========================
        scrollView = new ScrollView(this);

        scrollView.setFillViewport(true);

        scrollView.addView(root);

        setContentView(scrollView);
    }

    // =========================
    // AI分析
    // =========================
    private void analyzeCustomer() {

        String message =
                input.getText()
                        .toString()
                        .trim();

        if (message.isEmpty()) {

            Toast.makeText(
                    this,
                    "请输入客户消息",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        analyzeButton.setEnabled(false);
        analyzeButton.setText("正在分析...");

        result.setText("AI正在分析客户信息，请稍候...");

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                URL url = new URL(API_URL);

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");

                connection.setConnectTimeout(30000);

                connection.setReadTimeout(60000);

                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                JSONObject json =
                        new JSONObject();

                json.put(
                        "message",
                        message
                );

                byte[] data =
                        json.toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                );

                OutputStream output =
                        connection.getOutputStream();

                output.write(data);

                output.flush();
                output.close();

                int responseCode =
                        connection.getResponseCode();

                InputStream inputStream;

                if (responseCode >= 200 &&
                        responseCode < 300) {

                    inputStream =
                            connection.getInputStream();

                } else {

                    inputStream =
                            connection.getErrorStream();
                }

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        inputStream,
                                        StandardCharsets.UTF_8
                                )
                        );

                StringBuilder response =
                        new StringBuilder();

                String line;

                while ((line = reader.readLine())
                        != null) {

                    response.append(line);
                }

                reader.close();

                JSONObject responseJson =
                        new JSONObject(
                                response.toString()
                        );

                if (responseCode >= 200 &&
                        responseCode < 300) {

                    String aiResult =
                            responseJson.optString(
                                    "result",
                                    "AI没有返回分析结果"
                            );

                    runOnUiThread(() -> {

                        result.setText(aiResult);

                        analyzeButton.setEnabled(true);

                        analyzeButton.setText(
                                "🤖 AI销售分析"
                        );
                    });

                } else {

                    String error =
                            responseJson.optString(
                                    "error",
                                    response.toString()
                            );

                    runOnUiThread(() -> {

                        result.setText(
                                "分析失败：\n\n" + error
                        );

                        analyzeButton.setEnabled(true);

                        analyzeButton.setText(
                                "🤖 AI销售分析"
                        );
                    });
                }

            } catch (Exception e) {

                runOnUiThread(() -> {

                    result.setText(
                            "连接AI失败：\n\n" +
                            e.getMessage()
                    );

                    analyzeButton.setEnabled(true);

                    analyzeButton.setText(
                            "🤖 AI销售分析"
                    );
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    // =========================
    // 圆角背景
    // =========================
    private GradientDrawable roundBackground(
            int fillColor,
            int strokeColor,
            int radius
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(fillColor);

        drawable.setCornerRadius(radius);

        drawable.setStroke(
                1,
                strokeColor
        );

        return drawable;
    }
}