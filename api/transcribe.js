export default async function handler(req, res) {
  if (req.method !== "POST") {
    return res.status(405).json({
      error: "只允许POST请求"
    });
  }

  try {
    const contentType = req.headers["content-type"] || "";

    if (!contentType.includes("audio/")) {
      return res.status(400).json({
        error: "没有收到有效的音频文件"
      });
    }

    const chunks = [];

    for await (const chunk of req) {
      chunks.push(Buffer.from(chunk));
    }

    const audioBuffer = Buffer.concat(chunks);

    if (!audioBuffer.length) {
      return res.status(400).json({
        error: "录音文件为空"
      });
    }

    const formData = new FormData();

    const audioBlob = new Blob(
      [audioBuffer],
      { type: contentType }
    );

    formData.append(
      "file",
      audioBlob,
      "recording.m4a"
    );

    formData.append(
      "model",
      "gpt-4o-mini-transcribe"
    );

    formData.append(
      "language",
      "zh"
    );

    const response = await fetch(
      "https://api.openai.com/v1/audio/transcriptions",
      {
        method: "POST",
        headers: {
          "Authorization":
            `Bearer ${process.env.OPENAI_API_KEY}`
        },
        body: formData
      }
    );

    const data = await response.json();

    if (!response.ok) {
      return res.status(response.status).json({
        error: data.error?.message || "语音识别失败"
      });
    }

    return res.status(200).json({
      text: data.text || ""
    });

  } catch (error) {
    console.error("TRANSCRIBE ERROR:", error);

    return res.status(500).json({
      error: error.message || "服务器语音识别失败"
    });
  }
