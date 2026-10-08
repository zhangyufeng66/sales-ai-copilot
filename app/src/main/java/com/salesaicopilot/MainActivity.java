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
import java.io.File;
import java.io.FileInputStream;
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

    private int dp(float value) {

        return (int) (
                value *
                getResources()
                        .getDisplayMetrics()
                        .density
                + 0.5f
        );
    }

    private GradientDrawable createCardBackground(
            int color,
            float radius
    ) {

        GradientDrawable bg =
                new GradientDrawable();

        bg.setColor(color);
        bg.setCornerRadius(dp(radius));

        return bg;
    }

    private void buildInterface() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(BG);

        int statusBarHeight = 0;

        if (android.os.Build.VERSION.SDK_INT >= 30) {

            WindowInsets insets =
                    getWindow()
                            .getDecorView()
                            .getRootWindowInsets();

            if (insets != null) {

                statusBarHeight =
                        insets.getInsets(
                                WindowInsets.Type.statusBars()
                        ).top;
            }
        }

        if (statusBarHeight <= 0) {

            int id =
                    getResources().getIdentifier(
                            "status_bar_height",
                            "dimen",
                            "android"
                    );

            if (id > 0) {

                statusBarHeight =
                        getResources()
                                .getDimensionPixelSize(id);
            }
        }

        /*
         * Mate XT：
         * 左右不要太窄，避免展开后内容过宽。
         */
        int screenWidth =
                getResources()
                        .getDisplayMetrics()
                        .widthPixels;

        int sidePadding;

        if (screenWidth >= dp(700)) {

            sidePadding = dp(80);

        } else if (screenWidth >= dp(500)) {

            sidePadding = dp(50);

        } else {

            sidePadding = dp(24);
        }

        root.setPadding(
                sidePadding,
                statusBarHeight + dp(12),
                sidePadding,
                dp(30)
        );

        /*
         * =========================
         * 标题
         * =========================
         */

        TextView title =
                new TextView(this);

        title.setText("销售AI副驾驶");
        title.setTextColor(TEXT);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        title.setIncludeFontPadding(true);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                );

        titleParams.bottomMargin = dp(2);

        root.addView(
                title,
                titleParams
        );

        /*
         * =========================
         * 副标题
         * =========================
         */

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "DeepSeek AI · 客户分析 · 销售策略 · 智能话术"
        );

        subtitle.setTextColor(SUB);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setIncludeFontPadding(true);
        subtitle.setSingleLine(false);
        subtitle.setMaxLines(2);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(42)
                );

        subtitleParams.bottomMargin = dp(10);

        root.addView(
                subtitle,
                subtitleParams
        );

        /*
         * =========================
         * 输入标题
         * =========================
         */

        TextView inputTitle =
                new TextView(this);

        inputTitle.setText(
                "客户消息 / 客户情况"
        );

        inputTitle.setTextColor(TEXT);
        inputTitle.setTextSize(17);
        inputTitle.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        inputTitle.setGravity(
                Gravity.CENTER_VERTICAL
        );

        inputTitle.setIncludeFontPadding(true);

        root.addView(
                inputTitle,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(42)
                )
        );

        /*
         * =========================
         * 客户输入框
         * =========================
         */

        input =
                new EditText(this);

        input.setHint(
                "例如：客户说你们价格有点高，我再考虑一下..."
        );

        input.setTextSize(16);
        input.setTextColor(TEXT);

        input.setHintTextColor(
                Color.rgb(160, 168, 180)
        );

        input.setGravity(
                Gravity.TOP | Gravity.LEFT
        );

        input.setPadding(
                dp(18),
                dp(16),
                dp(18),
                dp(16)
        );

        input.setSingleLine(false);
        input.setMinLines(5);
        input.setMaxLines(8);

        input.setIncludeFontPadding(true);

        input.setBackground(
                createCardBackground(
                        CARD,
                        18
                )
        );

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(180)
                );

        inputParams.bottomMargin = dp(14);

        root.addView(
                input,
                inputParams
        );

        /*
         * =========================
         * 语音按钮
         * =========================
         */

        voiceButton =
                new Button(this);

        voiceButton.setText(
                "🎙️  点击录音"
        );

        voiceButton.setTextSize(16);
        voiceButton.setTextColor(TEXT);
        voiceButton.setGravity(Gravity.CENTER);

        voiceButton.setIncludeFontPadding(true);

        voiceButton.setSingleLine(false);

        voiceButton.setPadding(
                dp(12),
                dp(6),
                dp(12),
                dp(6)
        );

        voiceButton.setBackground(
                createCardBackground(
                        CARD,
                        18
                )
        );

        voiceButton.setOnClickListener(
                v -> toggleRecording()
        );

        LinearLayout.LayoutParams voiceParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(76)
                );

        voiceParams.bottomMargin = dp(14);

        root.addView(
                voiceButton,
                voiceParams
        );

        /*
         * =========================
         * AI分析按钮
         * =========================
         */

        analyzeButton =
                new Button(this);

        analyzeButton.setText(
                "🤖 AI销售分析"
        );

        analyzeButton.setTextSize(18);
        analyzeButton.setTextColor(Color.WHITE);
        analyzeButton.setGravity(Gravity.CENTER);

        analyzeButton.setIncludeFontPadding(true);
        analyzeButton.setSingleLine(false);

        analyzeButton.setPadding(
                dp(12),
                dp(8),
                dp(12),
                dp(8)
        );

        analyzeButton.setBackground(
                createCardBackground(
                        BLUE,
                        20
                )
        );

        analyzeButton.setOnClickListener(
                v -> analyzeCustomer()
        );

        LinearLayout.LayoutParams analyzeParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(82)
                );

        analyzeParams.bottomMargin = dp(22);

        root.addView(
                analyzeButton,
                analyzeParams
        );

        /*
         * =========================
         * AI分析标题
         * =========================
         */

        TextView resultTitle =
                new TextView(this);

        resultTitle.setText(
                "AI销售分析"
        );

        resultTitle.setTextColor(TEXT);
        resultTitle.setTextSize(20);

        resultTitle.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        resultTitle.setGravity(
                Gravity.CENTER_VERTICAL
        );

        resultTitle.setIncludeFontPadding(true);

        root.addView(
                resultTitle,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                )
        );

        /*
         * =========================
         * AI结果
         * =========================
         */

        result =
                new TextView(this);

        result.setText(
                "输入客户情况后，点击“AI销售分析”"
        );

        result.setTextColor(TEXT);
        result.setTextSize(16);

        result.setGravity(
                Gravity.TOP | Gravity.LEFT
        );

        result.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
        );

        result.setIncludeFontPadding(true);

        result.setSingleLine(false);

        result.setLineSpacing(
                dp(4),
                1.0f
        );

        result.setBackground(
                createCardBackground(
                        CARD,
                        18
                )
        );

        /*
         * 不再固定死高度。
         * 让结果区域有足够空间，
         * 内容多的时候整个页面继续向下滚动。
         */
        LinearLayout.LayoutParams resultParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(520)
                );

        resultParams.bottomMargin = dp(18);

        root.addView(
                result,
                resultParams
        );

        /*
         * =========================
         * 使用说明
         * =========================
         */

        TextView tips =
                new TextView(this);

        tips.setText(
                "使用方法\n\n" +
                "① 点击“点击录音”\n" +
                "② 对着手机说客户情况\n" +
                "③ 再次点击停止录音\n" +
                "④ 自动转成文字\n" +
                "⑤ 点击“AI销售分析”"
        );

        tips.setTextColor(SUB);
        tips.setTextSize(14);

        tips.setGravity(
                Gravity.TOP | Gravity.LEFT
        );

        tips.setIncludeFontPadding(true);

        tips.setSingleLine(false);

        tips.setLineSpacing(
                dp(3),
                1.0f
        );

        tips.setPadding(
                dp(5),
                dp(10),
                dp(5),
                dp(10)
        );

        root.addView(
                tips,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(190)
                )
        );

        /*
         * =========================
         * ScrollView
         * =========================
         */

        ScrollView scrollView =
                new ScrollView(this);

        scrollView.setFillViewport(true);

        scrollView.setClipToPadding(false);

        scrollView.addView(root);

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

        try {

            audioPath =
                    getExternalCacheDir()
                            .getAbsolutePath()
                            + "/sales_record.m4a";

            mediaRecorder =
                    new MediaRecorder();

            mediaRecorder.setAudioSource(
                    MediaRecorder.AudioSource.MIC
            );

            mediaRecorder.setOutputFormat(
                    MediaRecorder.OutputFormat.MPEG_4
            );

            mediaRecorder.setAudioEncoder(
                    MediaRecorder.AudioEncoder.AAC
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
                    "无法开始录音：" +
                            e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void stopRecording() {

        try {

            mediaRecorder.stop();

            mediaRecorder.release();

            mediaRecorder = null;

            isRecording = false;

            voiceButton.setEnabled(false);

            voiceButton.setText(
                    "⏳  正在识别..."
            );

            uploadAudio();

        } catch (Exception e) {

            isRecording = false;

            if (mediaRecorder != null) {

                mediaRecorder.release();

                mediaRecorder = null;
            }

            voiceButton.setEnabled(true);

            voiceButton.setText(
                    "🎙️  点击录音"
            );

            Toast.makeText(
                    this,
                    "录音结束失败：" +
                            e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void uploadAudio() {

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                URL url =
                        new URL(
                                TRANSCRIBE_URL
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod(
                        "POST"
                );

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

                File file =
                        new File(audioPath);

                OutputStream output =
                        connection.getOutputStream();

                FileInputStream fileInput =
                        new FileInputStream(file);

                byte[] buffer =
                        new byte[8192];

                int length;

                while (
                        (length =
                                fileInput.read(buffer))
                                != -1
                ) {

                    output.write(
                            buffer,
                            0,
                            length
                    );
                }

                fileInput.close();

                output.flush();
                output.close();

                int code =
                        connection.getResponseCode();

                InputStream stream;

                if (code >= 200 &&
                        code < 300) {

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

                while (
                        (line =
                                reader.readLine())
                                != null
                ) {

                    response.append(line);
                }

                reader.close();

                JSONObject json =
                        new JSONObject(
                                response.toString()
                        );

                if (code >= 200 &&
                        code < 300) {

                    String text =
                            json.optString(
                                    "text",
                                    ""
                            );

                    runOnUiThread(() -> {

                        voiceButton.setEnabled(
                                true
                        );

                        voiceButton.setText(
                                "🎙️  点击录音"
                        );

                        if (text.trim().isEmpty()) {

                            Toast.makeText(
                                    this,
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
                                this,
                                "语音识别完成",
                                Toast.LENGTH_SHORT
                        ).show();
                    });

                } else {

                    String error =
                            json.optString(
                                    "error",
                                    "语音识别失败"
                            );

                    runOnUiThread(() -> {

                        voiceButton.setEnabled(
                                true
                        );

                        voiceButton.setText(
                                "🎙️  点击录音"
                        );

                        Toast.makeText(
                                this,
                                error,
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }

            } catch (Exception e) {

                String error =
                        e.getMessage() == null
                                ? "网络或语音识别异常"
                                : e.getMessage();

                runOnUiThread(() -> {

                    voiceButton.setEnabled(
                            true
                    );

                    voiceButton.setText(
                            "🎙️  点击录音"
                    );

                    Toast.makeText(
                            this,
                            "语音识别失败：" +
                                    error,
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

        analyzeButton.setText(
                "⏳ AI分析中..."
        );

        result.setText(
                "正在分析客户，请稍候..."
        );

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                URL url =
                        new URL(API_URL);

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod(
                        "POST"
                );

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

                int code =
                        connection.getResponseCode();

                InputStream stream;

                if (code >= 200 &&
                        code < 300) {

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

                while (
                        (line =
                                reader.readLine())
                                != null
                ) {

                    response.append(line);
                }

                reader.close();

                JSONObject json =
                        new JSONObject(
                                response.toString()
                        );

                String aiResult;

                if (json.has("result")) {

                    aiResult =
                            json.optString(
                                    "result",
                                    ""
                            );

                } else {

                    aiResult =
                            json.optString(
                                    "error",
                                    "AI分析失败"
                            );
                }

                final String finalResult =
                        aiResult;

                runOnUiThread(() -> {

                    result.setText(
                            finalResult
                    );

                    analyzeButton.setEnabled(
                            true
                    );

                    analyzeButton.setText(
                            "🤖 AI销售分析"
                    );
                });

            } catch (Exception e) {

                final String error =
                        e.getMessage() == null
                                ? "网络连接异常"
                                : e.getMessage();

                runOnUiThread(() -> {

                    result.setText(
                            "AI分析失败：\n" +
                                    error
                    );

                    analyzeButton.setEnabled(
                            true
                    );

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

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode ==
                RECORD_AUDIO_PERMISSION) {

            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {

                startRecording();

            } else {

                Toast.makeText(
                        this,
                        "需要麦克风权限才能使用语音录入",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    @Override
    protected void onDestroy() {

        if (mediaRecorder != null) {

            try {

                mediaRecorder.stop();

            } catch (Exception ignored) {
            }

            try {

                mediaRecorder.release();

            } catch (Exception ignored) {
            }

            mediaRecorder = null;
        }

        super.onDestroy();
    }
}