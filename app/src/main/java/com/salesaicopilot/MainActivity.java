package com.salesaicopilot;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaRecorder;
import android.os.Bundle;
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

    private final int BG = Color.rgb(247, 249, 252);
    private final int CARD = Color.WHITE;
    private final int TEXT = Color.rgb(30, 35, 45);
    private final int SUB = Color.rgb(100, 110, 125);
    private final int BLUE = Color.rgb(35, 105, 220);

    private EditText input;
    private TextView result;
    private Button analyzeButton;
    private Button voiceButton;
    private ScrollView scrollView;
    private LinearLayout root;

    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private String audioPath;

    private static final int RECORD_AUDIO_PERMISSION = 1001;

    private static final String API_URL =
            "https://sales-ai-copilot-beta.vercel.app/api/analyze";

    private static final String TRANSCRIBE_URL =
            "https://sales-ai-copilot-beta.vercel.app/api/transcribe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupStatusBar();
        buildInterface();
    }

    private void setupStatusBar() {
        Window window = getWindow();

        window.setStatusBarColor(BG);

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }

    private void buildInterface() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        int statusBarHeight = 0;

        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsets insets =
                    getWindow().getDecorView().getRootWindowInsets();

            if (insets != null) {
                statusBarHeight =
                        insets.getInsets(WindowInsets.Type.statusBars()).top;
            }
        }

        if (statusBarHeight <= 0) {
            int resourceId = getResources()
                    .getIdentifier(
                            "status_bar_height",
                            "dimen",
                            "android"
                    );

            if (resourceId > 0) {
                statusBarHeight =
                        getResources().getDimensionPixelSize(resourceId);
            }
        }

        root.setPadding(
                28,
                statusBarHeight + 12,
                28,
                35
        );

        TextView title = new TextView(this);
        title.setText("销售AI副驾驶");
        title.setTextColor(TEXT);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        55
                )
        );

        TextView subtitle = new TextView(this);
        subtitle.setText(
                "DeepSeek AI · 客户分析 · 销售策略 · 智能话术"
        );
        subtitle.setTextColor(SUB);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        -1,
                        35
                );

        subtitleParams.bottomMargin = 10;

        root.addView(subtitle, subtitleParams);

        TextView inputTitle = new TextView(this);
        inputTitle.setText("客户消息 / 客户情况");
        inputTitle.setTextColor(TEXT);
        inputTitle.setTextSize(17);
        inputTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        root.addView(
                inputTitle,
                new LinearLayout.LayoutParams(
                        -1,
                        40
                )
        );

        input = new EditText(this);
        input.setHint(
                "例如：客户说你们价格有点高，我再考虑一下..."
        );
        input.setTextSize(16);
        input.setTextColor(TEXT);
        input.setHintTextColor(Color.rgb(160, 168, 180));
        input.setGravity(Gravity.TOP | Gravity.LEFT);
        input.setPadding(20, 18, 20, 18);
        input.setSingleLine(false);
        input.setMinLines(5);

        GradientDrawable inputBg =
                new GradientDrawable();

        inputBg.setColor(CARD);
        inputBg.setCornerRadius(20);

        input.setBackground(inputBg);

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        -1,
                        180
                );

        inputParams.bottomMargin = 12;

        root.addView(input, inputParams);

        voiceButton = new Button(this);
        voiceButton.setText("🎙️  点击录音");
        voiceButton.setTextSize(16);
        voiceButton.setTextColor(TEXT);
        voiceButton.setGravity(Gravity.CENTER);
        voiceButton.setIncludeFontPadding(true);
        voiceButton.setPadding(10, 5, 10, 5);

        voiceButton.setOnClickListener(
                v -> toggleRecording()
        );

        LinearLayout.LayoutParams voiceParams =
                new LinearLayout.LayoutParams(
                        -1,
                        90
                );

        voiceParams.bottomMargin = 12;

        root.addView(voiceButton, voiceParams);

        analyzeButton = new Button(this);
        analyzeButton.setText("🤖 AI销售分析");
        analyzeButton.setTextSize(18);
        analyzeButton.setTextColor(Color.WHITE);
        analyzeButton.setGravity(Gravity.CENTER);

        GradientDrawable buttonBg =
                new GradientDrawable();

        buttonBg.setColor(BLUE);
        buttonBg.setCornerRadius(22);

        analyzeButton.setBackground(buttonBg);

        analyzeButton.setOnClickListener(
                v -> analyzeCustomer()
        );

        root.addView(
                analyzeButton,
                new LinearLayout.LayoutParams(
                        -1,
                        100
                )
        );

        TextView resultTitle = new TextView(this);
        resultTitle.setText("AI销售分析");
        resultTitle.setTextColor(TEXT);
        resultTitle.setTextSize(20);
        resultTitle.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        LinearLayout.LayoutParams resultTitleParams =
                new LinearLayout.LayoutParams(
                        -1,
                        50
                );

        resultTitleParams.topMargin = 18;

        root.addView(
                resultTitle,
                resultTitleParams
        );

        result = new TextView(this);
        result.setText(
                "输入客户情况后，点击“AI销售分析”"
        );
        result.setTextColor(TEXT);
        result.setTextSize(16);
        result.setGravity(Gravity.TOP | Gravity.LEFT);
        result.setPadding(20, 20, 20, 20);

        GradientDrawable resultBg =
                new GradientDrawable();

        resultBg.setColor(CARD);
        resultBg.setCornerRadius(20);

        result.setBackground(resultBg);

        LinearLayout.LayoutParams resultParams =
                new LinearLayout.LayoutParams(
                        -1,
                        420
                );

        root.addView(result, resultParams);

        TextView tips = new TextView(this);

        tips.setText(
                "使用方法：\n" +
                "① 点击“点击录音”\n" +
                "② 对着手机说客户情况\n" +
                "③ 再次点击停止录音\n" +
                "④ 系统自动转成文字\n" +
                "⑤ 点击“AI销售分析”获取销售建议"
        );

        tips.setTextColor(SUB);
        tips.setTextSize(14);
        tips.setPadding(5, 18, 5, 0);

        root.addView(
                tips,
                new LinearLayout.LayoutParams(
                        -1,
                        150
                )
        );

        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.addView(
                root,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        scrollView.addView(container);

        setContentView(scrollView);
    }

    private void toggleRecording() {

        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    private void startRecording() {

        if (android.os.Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(
                    Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.RECORD_AUDIO
                        },
                        RECORD_AUDIO_PERMISSION
                );

                return;
            }
        }

        try {

            audioPath =
                    getExternalCacheDir()
                            .getAbsolutePath()
                            + "/sales_record.m4a";

            mediaRecorder = new MediaRecorder();

            mediaRecorder.setAudioSource(
                    MediaRecorder.AudioSource.MIC
            );

            mediaRecorder.setOutputFormat(
                    MediaRecorder.OutputFormat.MPEG_4
            );

            mediaRecorder.setAudioEncoder(
                    MediaRecorder.AudioEncoder.AAC
            );

            mediaRecorder.setAudioEncodingBitRate(
                    128000
            );

            mediaRecorder.setAudioSamplingRate(
                    44100
            );

            mediaRecorder.setOutputFile(
                    audioPath
            );

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;

            voiceButton.setText(
                    "⏹️  停止录音"
            );

            Toast.makeText(
                    this,
                    "正在录音，请说话...",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            isRecording = false;

            voiceButton.setText(
                    "🎙️  点击录音"
            );

            Toast.makeText(
                    this,
                    "无法开始录音：" + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void stopRecording() {

        try {

            if (mediaRecorder != null) {

                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }

            isRecording = false;

            voiceButton.setText(
                    "⏳  正在识别..."
            );

            voiceButton.setEnabled(false);

            uploadAudio();

        } catch (Exception e) {

            isRecording = false;

            if (mediaRecorder != null) {

                try {
                    mediaRecorder.release();
                } catch (Exception ignored) {
                }

                mediaRecorder = null;
            }

            voiceButton.setEnabled(true);

            voiceButton.setText(
                    "🎙️  点击录音"
            );

            Toast.makeText(
                    this,
                    "录音结束失败：" + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void uploadAudio() {

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                URL url =
                        new URL(TRANSCRIBE_URL);

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");

                connection.setDoOutput(true);
                connection.setDoInput(true);

                connection.setConnectTimeout(
                        30000
                );

                connection.setReadTimeout(
                        60000
                );

                connection.setRequestProperty(
                        "Content-Type",
                        "audio/mp4"
                );

                java.io.File file =
                        new java.io.File(audioPath);

                byte[] audioBytes =
                        java.nio.file.Files.readAllBytes(
                                file.toPath()
                        );

                OutputStream output =
                        connection.getOutputStream();

                output.write(audioBytes);
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

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONObject json =
                        new JSONObject(
                                response.toString()
                        );

                if (responseCode >= 200 &&
                        responseCode < 300) {

                    final String text =
                            json.optString(
                                    "text",
                                    ""
                            );

                    runOnUiThread(() -> {

                        voiceButton.setEnabled(true);

                        voiceButton.setText(
                                "🎙️  点击录音"
                        );

                        if (text.trim().isEmpty()) {

                            Toast.makeText(
                                    MainActivity.this,
                                    "没有识别到语音内容",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        input.setText(text);
                        input.setSelection(
                                input.length()
                        );

                        Toast.makeText(
                                MainActivity.this,
                                "语音识别完成",
                                Toast.LENGTH_SHORT
                        ).show();
                    });

                } else {

                    final String error =
                            json.optString(
                                    "error",
                                    "语音识别失败"
                            );

                    runOnUiThread(() -> {

                        voiceButton.setEnabled(true);

                        voiceButton.setText(
                                "🎙️  点击录音"
                        );

                        Toast.makeText(
                                MainActivity.this,
                                error,
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }

            } catch (Exception e) {

                final String error =
                        e.getMessage() == null
                                ? "网络或语音识别异常"
                                : e.getMessage();

                runOnUiThread(() -> {

                    voiceButton.setEnabled(true);

                    voiceButton.setText(
                            "🎙️  点击录音"
                    );

                    Toast.makeText(
                            MainActivity.this,
                            "语音识别失败：" + error,
                            Toast.LENGTH_LONG
                    ).show();
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    private void analyzeCustomer() {

        String message =
                input.getText()
                        .toString()
                        .trim();

        if (message.isEmpty()) {

            Toast.makeText(
                    this,
                    "请先输入客户情况，或者使用语音录入",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        analyzeButton.setEnabled(false);
        analyzeButton.setText("⏳ AI分析中...");

        result.setText(
                "正在分析客户，请稍候..."
        );

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                URL url =
                        new URL(API_URL);

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");

                connection.setDoOutput(true);
                connection.setDoInput(true);

                connection.setConnectTimeout(
                        30000
                );

                connection.setReadTimeout(
                        60000
                );

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                JSONObject body =
                        new JSONObject();

                body.put(
                        "message",
                        message
                );

                byte[] data =
                        body.toString()
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

                BufferedReader read