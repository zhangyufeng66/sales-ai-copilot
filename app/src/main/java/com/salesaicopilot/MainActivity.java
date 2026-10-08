package com.salesaicopilot;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
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
import java.util.ArrayList;

public class MainActivity extends Activity {

    private final int blue = Color.rgb(22, 119, 255);
    private final int dark = Color.rgb(30, 30, 30);
    private final int background = Color.rgb(247, 249, 252);

    private EditText input;
    private TextView result;
    private Button analyzeButton;
    private Button voiceButton;
    private ScrollView scrollView;
    private LinearLayout root;

    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private boolean isListening = false;

    private static final String API_URL =
            "https://sales-ai-copilot-beta.vercel.app/api/analyze";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupStatusBar();
        buildInterface();
        setupSpeechRecognizer();
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(background);

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.M) {

            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }

    private void buildInterface() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(background);

        final int baseLeft = 28;
        final int baseTop = 12;
        final int baseRight = 28;
        final int baseBottom = 35;

        root.setPadding(
                baseLeft,
                baseTop,
                baseRight,
                baseBottom
        );

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

            v.setPadding(
                    baseLeft,
                    baseTop + statusBarHeight,
                    baseRight,
                    baseBottom
            );

            return insets;
        });

        TextView title = new TextView(this);
        title.setText("销售AI副驾驶");
        title.setTextColor(dark);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        titleParams.bottomMargin = 8;

        root.addView(
                title,
                titleParams
        );

        TextView subtitle = new TextView(this);

        subtitle.setText(
                "DeepSeek AI · 客户分析 · 销售策略 · 智能话术"
        );

        subtitle.setTextColor(
                Color.rgb(100, 110, 125)
        );

        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.bottomMargin = 24;

        root.addView(
                subtitle,
                subtitleParams
        );

        TextView inputTitle = new TextView(this);

        inputTitle.setText(
                "客户消息 / 客户情况"
        );

        inputTitle.setTextColor(dark);
        inputTitle.setTextSize(17);

        inputTitle.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        LinearLayout.LayoutParams inputTitleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        inputTitleParams.bottomMargin = 10;

        root.addView(
                inputTitle,
                inputTitleParams
        );

        input = new EditText(this);

        input.setHint(
                "例如：客户说你们价格有点高，我再考虑一下..."
        );

        input.setTextSize(16);
        input.setTextColor(dark);

        input.setHintTextColor(
                Color.rgb(150, 155, 165)
        );

        input.setGravity(
                Gravity.TOP | Gravity.START
        );

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

        inputParams.bottomMargin = 12;

        root.addView(
                input,
                inputParams
        );

        voiceButton = new Button(this);

        voiceButton.setText(
                "🎙️ 语音输入"
        );

        voiceButton.setTextColor(blue);
        voiceButton.setTextSize(17);
        voiceButton.setGravity(Gravity.CENTER);
        voiceButton.setAllCaps(false);

        voiceButton.setBackground(
                roundBackground(
                        Color.WHITE,
                        blue,
                        18
                )
        );

        LinearLayout.LayoutParams voiceParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        70
                );

        voiceParams.bottomMargin = 12;

        root.addView(
                voiceButton,
                voiceParams
        );

        voiceButton.setOnClickListener(
                v -> toggleSpeech()
        );

        analyzeButton = new Button(this);

        analyzeButton.setText(
                "🤖 AI销售分析"
        );

        analyzeButton.setTextColor(
                Color.WHITE
        );

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

        analyzeButton.setOnClickListener(
                v -> analyzeCustomer()
        );

        TextView resultTitle = new TextView(this);

        resultTitle.setText(
                "AI销售分析"
        );

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

        result = new TextView(this);

        result.setText(
                "请输入客户消息，或者点击“🎙️语音输入”。\n\n" +
                "然后点击“AI销售分析”。\n\n" +
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

        result.setGravity(
                Gravity.TOP | Gravity.START
        );

        result.setLineSpacing(
                6,
                1.0f
        );

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

        TextView tips = new TextView(this);

        tips.setText(
                "使用提示\n\n" +
                "1. 可以直接输入客户消息\n" +
                "2. 也可以点击语音输入\n" +
                "3. 语音内容会自动进入客户消息框\n" +
                "4. 再点击AI销售分析即可分析"
        );

        tips.setTextColor(
                Color.rgb(100, 110, 125)
        );

        tips.setTextSize(14);

        tips.setLineSpacing(
                5,
                1.0f
        );

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

        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.addView(root);

        setContentView(scrollView);
    }

    private void setupSpeechRecognizer() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            voiceButton.setEnabled(false);

            voiceButton.setText(
                    "🎙️ 当前设备不支持语音识别"
            );

            return;
        }

        speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(this);

        speechIntent =
                new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "zh-CN"
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
        );

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(
                            Bundle params) {

                        runOnUiThread(() ->
                                voiceButton.setText(
                                        "🔴 正在听，请说话..."
                                )
                        );
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                    }

                    @Override
                    public void onRmsChanged(
                            float rmsdB) {
                    }

                    @Override
                    public void onBufferReceived(
                            byte[] buffer) {
                    }

                    @Override
                    public void onEndOfSpeech() {

                        runOnUiThread(() ->
                                voiceButton.setText(
                                        "🎙️ 正在处理语音..."
                                )
                        );
                    }

                    @Override
                    public void onError(
                            int error) {

                        isListening = false;

                        runOnUiThread(() -> {

                            voiceButton.setText(
                                    "🎙️ 语音输入"
                            );

                            Toast.makeText(
                                    MainActivity.this,
                                    getSpeechErrorMessage(error),
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }

                    @Override
                    public void onResults(
                            Bundle results) {

                        isListening = false;

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (matches != null &&
                                !matches.isEmpty()) {

                            String text =
                                    matches.get(0);

                            runOnUiThread(() -> {

                                input.setText(text);

                                input.setSelection(
                                        input.length()
                                );

                                voiceButton.setText(
                                        "🎙️ 语音输入"
                                );
                            });
                        }
                    }

                    @Override
                    public void onPartialResults(
                            Bundle partialResults) {

                        ArrayList<String> matches =
                                partialResults.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (matches != null &&
                                !matches.isEmpty()) {

                            String text =
                                    matches.get(0);

                            runOnUiThread(() -> {

                                input.setText(text);

                                input.setSelection(
                                        input.length()
                                );
                            });
                        }
                    }

                    @Override
                    public void onEvent(
                            int eventType,
                            Bundle params) {
                    }
                }
        );
    }

    private void toggleSpeech() {

        if (isListening) {

            speechRecognizer.stopListening();

            isListening = false;

            voiceButton.setText(
                    "🎙️ 语音输入"
            );

            return;
        }

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.M) {

            if (checkSelfPermission(
                    Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.RECORD_AUDIO
                        },
                        1001
                );

                return;
            }
        }

        startSpeechRecognition();
    }

    private void startSpeechRecognition() {

        if (speechRecognizer == null) {

            Toast.makeText(
                    this,
                    "当前设备暂不支持语音识别",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        isListening = true;

        voiceButton.setText(
                "🔴 正在听，请说话..."
        );

        speechRecognizer.startListening(
                speechIntent
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == 1001) {

            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {

                startSpeechRecognition();

            } else {

                Toast.makeText(
                        this,
                        "需要允许麦克风权限才能使用语音输入",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private String getSpeechErrorMessage(
            int error) {

        switch (error) {

            case SpeechRecognizer.ERROR_AUDIO:
                return "麦克风出现问题";

            case SpeechRecognizer.ERROR_CLIENT:
                return "语音识别客户端错误";

            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "没有麦克风权限";

            case SpeechRecognizer.ERROR_NETWORK:
                return "语音识别网络错误";

            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "语音识别网络超时";

            case SpeechRecognizer.ERROR_NO_MATCH:
                return "没有听清，请再说一次";

            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "语音识别正在使用";

            case SpeechRecognizer.ERROR_SERVER:
                return "语音识别服务器错误";

            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "没有检测到说话";

            default:
                return "语音识别失败，请重试";
        }
    }

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

        analyzeButton.setText(
                "正在分析..."
        );

        result.setText(
                "AI正在分析客户信息，请稍候..."
        );

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                URL url =
                        new URL(API_URL);

              