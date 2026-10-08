const https = require("https");

function callDeepSeek(message) {
    return new Promise((resolve, reject) => {

        const data = JSON.stringify({
            model: "deepseek-flash",
            messages: [
                {
                    role: "system",
                    content:
                        "你是一名专业的企业销售AI副驾驶。" +
                        "请分析客户消息，并给销售人员提供可直接执行的建议。" +
                        "必须输出：客户意向、客户需求、客户痛点、客户顾虑、成交可能性、下一步跟进策略、推荐销售话术。" +
                        "不要编造客户没有提供的信息。"
                },
                {
                    role: "user",
                    content: message
                }
            ],
            stream: false
        });

        const options = {
            hostname: "api.deepseek.com",
            path: "/chat/completions",
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + process.env.DEEPSEEK_API_KEY,
                "Content-Length": Buffer.byteLength(data)
            }
        };

        const req = https.request(options, (res) => {

            let body = "";

            res.on("data", chunk => {
                body += chunk;
            });

            res.on("end", () => {

                try {
                    const json = JSON.parse(body);

                    if (json.choices && json.choices.length > 0) {
                        resolve(json.choices[0].message.content);
                    } else {
                        reject(new Error(body));
                    }

                } catch (e) {
                    reject(e);
                }
            });
        });

        req.on("error", reject);

        req.write(data);
        req.end();
    });
}

module.exports = async function handler(req, res) {

    if (req.method !== "POST") {
        return res.status(405).json({
            error: "只允许POST请求"
        });
    }

    try {

        const message = req.body && req.body.message;

        if (!message || message.trim() === "") {
            return res.status(400).json({
                error: "客户消息不能为空"
            });
        }

        const result = await callDeepSeek(message);

        res.status(200).json({
            success: true,
            result: result
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            error: error.message
        });
    }
};
