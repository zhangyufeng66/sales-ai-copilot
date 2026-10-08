package com.salesaicopilot;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    int blue = Color.rgb(22, 119, 255);
    int green = Color.rgb(34, 180, 100);
    int dark = Color.rgb(30, 30, 30);

    EditText input;
    TextView result;
    TextView status;

    SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildInterface();
        initSpeech();

        if (android.os.Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    100
            );
        }
    }

    private void buildInterface() {

        ScrollView scrollView = new ScrollView(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 35, 28, 35);
        root.setBackgroundColor(Color.rgb(247, 249, 252));

        TextView title = new TextView(this);
        title.setText("销售AI副驾驶");
        title.setTextSize(27);
        title.setTextColor(dark);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, 1);

        root.addView(title, new LinearLayout.LayoutParams(
                -1, 75
        ));

        TextView subtitle = new TextView(this);
        subtitle.setText("客户语音 → 实时转文字 → AI分析 → 建议回答");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.GRAY);
        subtitle.setGravity(Gravity.CENTER);

        root.addView(subtitle, new LinearLayout.LayoutParams(
                -1, 55
        ));

        status = new TextView(this);
        status.setText("● 语音助手待机中");
        status.setTextSize(15);
        status.setTextColor(Color.GRAY);
        status.setGravity(Gravity.CENTER);

        root.addView(status, new LinearLayout.LayoutParams(
                -1, 45
        ));

        TextView customerTitle = new TextView(this);
        customerTitle.setText("客户正在说：");
        customerTitle.setTextSize(17);
        customerTitle.setTextColor(dark);
        customerTitle.setTypeface(null, 1);

        LinearLayout.LayoutParams ct =
                new LinearLayout.LayoutParams(-1, 45);
        ct.setMargins(0, 15, 0, 0);

        root.addView(customerTitle, ct);

        input = new EditText(this);
        input.setHint("语音识别后的客户内容会显示在这里...");
        input.setTextSize(17);
        input.setGravity(Gravity.TOP);
        input.setPadding(22, 20, 22, 20);
        input.setBackground(roundBackground(Color.WHITE, 20));

        root.addView(input, new LinearLayout.LayoutParams(
                -1, 190
        ));

        Button voice = new Button(this);
        voice.setText("🎙 开始语音识别");
        voice.setTextSize(17);
        voice.setTextColor(Color.WHITE);
        voice.setBackground(roundBackground(green, 20));

        LinearLayout.LayoutParams vp =
                new LinearLayout.LayoutParams(-1, 70);
        vp.setMargins(0, 18, 0, 12);

        root.addView(voice, vp);

        Button analyze = new Button(this);
        analyze.setText("🤖 AI分析客户");
        analyze.setTextSize(17);
        analyze.setTextColor(Color.WHITE);
        analyze.setBackground(roundBackground(blue, 20));

        root.addView(analyze, new LinearLayout.LayoutParams(
                -1, 70
        ));

        TextView resultTitle = new TextView(this);
        resultTitle.setText("AI销售建议");
        resultTitle.setTextSize(18);
        resultTitle.setTextColor(dark);
        resultTitle.setTypeface(null, 1);

        LinearLayout.LayoutParams rt =
                new LinearLayout.LayoutParams(-1, 50);
        rt.setMargins(0, 25, 0, 0);

        root.addView(resultTitle, rt);

        result = new TextView(this);
        result.setText("等待客户语音或消息...");
        result.setTextSize(16);
        result.setTextColor(Color.DKGRAY);
        result.setPadding(22, 22, 22, 22);
        result.setGravity(Gravity.TOP);
        result.setBackground(roundBackground(Color.WHITE, 20));

        root.addView(result, new LinearLayout.LayoutParams(
                -1, 360
        ));

        TextView tip = new TextView(this);
        tip.setText(
                "\n使用流程\n" +
                "① 点击“开始语音识别”\n" +
                "② 客户说话\n" +
                "③ 识别结果自动进入客户消息\n" +
                "④ 点击“AI分析客户”\n" +
                "⑤ 获取客户意图、销售策略和推荐话术"
        );

        tip.setTextSize(14);
        tip.setTextColor(Color.GRAY);

        LinearLayout.LayoutParams tipParams =
                new LinearLayout.LayoutParams(-1, -2);
        tipParams.setMargins(5, 20, 5, 20);

        root.addView(tip, tipParams);

        scrollView.addView(root);

        setContentView(scrollView);

        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechRecognition();
            }
        });

        analyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = input.getText().toString();

                if (message.trim().isEmpty()) {
                    result.setText("请先输入客户消息，或者点击“开始语音识别”。");
                    return;
                }

                analyzeCustomer(message);
            }
        });
    }

    private void initSpeech() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(Bundle params) {
                        status.setText("● 正在听客户说话...");
                        status.setTextColor(green);
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        status.setText("● 正在识别...");
                    }

                    @Override
                    public void onRmsChanged(float rmsdB) {
                    }

                    @Override
                    public void onBufferReceived(byte[] buffer) {
                    }

                    @Override
                    public void onEndOfSpeech() {
                        status.setText("● 正在处理语音...");
                    }

                    @Override
                    public void onError(int error) {
                        status.setText("● 语音识别结束");
                        status.setTextColor(Color.GRAY);
                    }

                    @Override
                    public void onResults(Bundle results) {

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (matches != null && matches.size() > 0) {

                            String text = matches.get(0);

                            input.setText(text);
                            input.setSelection(text.length());

                            status.setText("● 识别完成");
                            status.setTextColor(blue);
                        }
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {

                        ArrayList<String> matches =
                                partialResults.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (matches != null && matches.size() > 0) {

                            input.setText(matches.get(0));
                            input.setSelection(input.length());
                        }
                    }

                    @Override
                    public void onEvent(int eventType, Bundle params) {
                    }
                }
        );
    }

    private void startSpeechRecognition() {

        if (speechRecognizer == null) {
            result.setText("当前手机暂不支持语音识别。");
            return;
        }

        Intent intent =
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.CHINESE.toString()
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
        );

        speechRecognizer.startListening(intent);
    }

    private void analyzeCustomer(String message) {

        String lower = message;

        if (lower.contains("贵") ||
                lower.contains("价格") ||
                lower.contains("便宜") ||
                lower.contains("太高")) {

            result.setText(
                    "【客户意图】\n" +
                    "价格异议 / 正在比较供应商\n\n" +

                    "【客户意向】\n" +
                    "🟠 中高意向\n\n" +

                    "【AI分析】\n" +
                    "客户已经开始关注价格，说明对产品存在一定兴趣。" +
                    "目前不建议直接降价，应先了解客户比较的具体维度。\n\n" +

                    "【销售建议】\n" +
                    "① 不要立即主动降价\n" +
                    "② 询问客户对比的是价格、配置还是服务\n" +
                    "③ 强调产品价值和实际使用成本\n\n" +

                    "【推荐回答】\n" +
                    "“我理解您的考虑。价格确实很重要，我想先了解一下，" +
                    "您现在主要是单纯比较价格，还是产品配置、服务和后期成本也在一起比较？”"
            );

        } else if (lower.contains("考虑") ||
                lower.contains("再看看") ||
                lower.contains("商量")) {

            result.setText(
                    "【客户意图】\n" +
                    "暂时犹豫 / 尚未决定\n\n" +

                    "【客户意向】\n" +
                    "🟡 中等意向\n\n" +

                    "【AI分析】\n" +
                    "客户没有明确拒绝，但目前缺少推动成交的理由。" +
                    "建议进一步寻找客户真正的顾虑。\n\n" +

                    "【推荐回答】\n" +
                    "“没问题，我也不着急让您现在决定。" +
                    "我想先了解一下，您目前主要还有哪方面需要再考虑？" +
                    "我针对这个问题给您详细说明一下。”"
            );

        } else {

            result.setText(
                    "【客户意图】\n" +
                    "需要进一步判断\n\n" +

                    "【AI建议】\n" +
                    "建议继续询问客户需求、预算、采购时间和当前使用方案。\n\n" +

                    "【推荐问题】\n" +
                    "“方便了解一下您目前主要想解决哪方面的问题？" +
                    "以及大概什么时候计划采购？”"
            );
        }
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

    @Override
    protected void onDestroy() {

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        super.onDestroy();
    }
}
