const https = require("https");

function callDeepSeek(message) {
    return new Promise((resolve, reject) => {

        const data = JSON.stringify({
            model: "deepseek-flash",

            // 关闭深度思考，优先速度
            thinking: {
                type: "disabled"
            },

            messages: [
                {
                    role: "system",
                    content:
                        "你是销售AI副驾驶。" +
                        "请快速分析客户消息，给销售人员可直接执行的建议。" +
                        "不要编造客户没有提供的信息。" +
                        "回答必须简洁，严格按照以下7项输出：" +
                        "\n1. 客户意向：" +
                        "\n2. 客户需求：" +
                        "\n3. 客户痛点：" +
                        "\n4. 客户顾虑：" +
                        "\n5. 成交可能性：" +
                        "\n6. 下一步跟进策略：" +
                        "\n7. 推荐销售话术：" +
                        "\n每项控制在1-2句话，不要长篇解释。"
                },
                {
                    role: "user",
                    content: message
                }
            ],

            stream: false,

            // 限制输出长度，提高返回速度
            max_tokens: 800,

            // 销售分析不需要随机发挥
            temperature: 0.2
        });

        const options = {
            hostname: "api.deepseek.com",
            path: "/chat/completions",
            method: "POST",

            headers: {
                "Content-Type": "application/json",
                "Authorization":
                    "Bearer " + process.env.DEEPSEEK_API_KEY,
                "Content-Length":
                    Buffer.byteLength(data)
            }
        };

        const req = https.request(
            options,
            (res) => {

                let body = "";

                res.on("data", (chunk) => {
                    body += chunk;
                });

                res.on("end", () => {

                    try {

                        const json =
                            JSON.parse(body);

                        if (
                            json.choices &&
                            json.choices.length > 0
                        ) {

                            resolve(
                                json.choices[0]
                                    .message
                                    .content
                            );

                        } else {

                            reject(
                                new Error(body)
                            );
                        }

                    } catch (e) {

                        reject(e);
                    }
                });
            }
        );

        req.on("error", reject);

        req.write(data);

        req.end();
    });
}


module.exports = async function handler(req, res) {

    // 只允许POST
    if (req.method !== "POST") {

        return res.status(405).json({
            error: "只允许POST请求"
        });
    }

    try {

        const message =
            req.body &&
            req.body.message;

        // 检查客户消息
        if (
            !message ||
            message.trim() === ""
        ) {

            return res.status(400).json({
                error: "客户消息不能为空"
            });
        }

        const result =
            await callDeepSeek(message);

        return res.status(200).json({
            success: true,
            result: result
        });

    } catch (error) {

        console.error(
            "DeepSeek API错误:",
            error
        );

        return res.status(500).json({
            success: false,
            error: error.message
        });
    }
};