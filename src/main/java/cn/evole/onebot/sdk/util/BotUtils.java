package cn.evole.onebot.sdk.util;

import cn.evole.onebot.sdk.entity.ArrayMsg;
import cn.evole.onebot.sdk.enums.MsgTypeEnum;
import cn.evole.onebot.sdk.event.message.MessageEvent;
import cn.evole.onebot.sdk.util.json.GsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.NonNull;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 13:48
 * Version: 1.0
 */
public class BotUtils {

    private static final Logger log = LoggerFactory.getLogger("BotUtils");
    private final static String CQ_CODE_SPLIT = "(?<=\\[CQ:[^]]{1,99999}])|(?=\\[CQ:[^]]{1,99999}])";

    private final static String CQ_CODE_REGEX = "\\[CQ:([^,\\[\\]]+)((?:,[^,=\\[\\]]+=[^,\\[\\]]*)*)]";

    /**
     * 判断是否为全体at
     *
     * @param msg 消息
     * @return 是否为全体at
     */
    public static boolean isAtAll(String msg) {
        return msg.contains("[CQ:at,qq=all]");
    }

    /**
     * 判断是否为全体at
     *
     * @param arrayMsg 消息链
     * @return 是否为全体at
     */
    public static boolean isAtAll(List<ArrayMsg> arrayMsg) {
        return arrayMsg.stream().anyMatch(it -> "all".equals(it.getData().get("qq")));
    }

    /**
     * 获取消息内所有at对象账号（不包含全体 at）
     *
     * @param arrayMsg 消息链
     * @return at对象列表
     */
    public static List<Long> getAtList(List<ArrayMsg> arrayMsg) {
        return arrayMsg.stream().filter(it -> "at".equals(it.getType()) && !"all".equals(it.getData().get("qq"))).map(it -> Long.parseLong(it.getData().get("qq"))).collect(Collectors.toList());
    }

    /**
     * 获取消息内所有图片链接
     *
     * @param arrayMsg 消息链
     * @return 图片链接列表
     */
    public static List<String> getMsgImgUrlList(List<ArrayMsg> arrayMsg) {
        return arrayMsg.stream().filter(it -> "image".equals(it.getType())).map(it -> it.getData().get("url")).collect(Collectors.toList());
    }

    /**
     * 获取消息内所有视频链接
     *
     * @param arrayMsg 消息链
     * @return 视频链接列表
     */
    public static List<String> getMsgVideoUrlList(List<ArrayMsg> arrayMsg) {
        return arrayMsg.stream().filter(it -> "video".equals(it.getType())).map(it -> it.getData().get("url")).collect(Collectors.toList());
    }

    /**
     * 获取群头像
     *
     * @param groupId 群号
     * @param size    头像尺寸
     * @return 头像链接 （size为0返回真实大小, 40(40*40), 100(100*100), 640(640*640)）
     */
    public static String getGroupAvatar(long groupId, int size) {
        return String.format("https://p.qlogo.cn/gh/%s/%s/%s", groupId, groupId, size);
    }

    /**
     * 获取用户昵称
     *
     * @param userId QQ号
     * @return 用户昵称
     */
    public static String getNickname(long userId) {
        val url = String.format("https://r.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?uins=%s", userId);
        val result = NetUtils.get(url, "GBK");
        if (result != null && !result.isEmpty()) {
            String nickname = result.split(",")[6];
            return nickname.substring(1, nickname.length() - 1);
        }
        return "";
    }

    /**
     * 获取用户头像
     *
     * @param userId QQ号
     * @param size   头像尺寸
     * @return 头像链接 （size为0返回真实大小, 40(40*40), 100(100*100), 640(640*640)）
     */
    public static String getUserAvatar(long userId, int size) {
        return String.format("https://q1.qlogo.cn/g?b=qq&nk=%s&s=%s", userId, size);
    }

    /**
     * 消息解码
     *
     * @param string 需要解码的内容
     * @return 解码处理后的字符串
     */
    public static String unescape(String string) {
        return string.replace("&#44;", ",").replace("&#91;", "[").replace("&#93;", "]").replace("&amp;", "&");
    }

    /**
     * 消息编码
     *
     * @param string 需要编码的内容
     * @return 编码处理后的字符串
     */
    public static String escape(String string) {
        return string.replace("&", "&amp;").replace(",", "&#44;").replace("[", "&#91;").replace("]", "&#93;");
    }

    /**
     * 消息编码（可用于转义CQ码，防止文本注入）
     *
     * @param string 需要编码的内容
     * @return 编码处理后的字符串
     */
    public static String escape2(String string) {
        return string.replace("[", "&#91;").replace("]", "&#93;");
    }

    /**
     * string 消息上报转消息链
     * 建议传入 event.getMessage 而非 event.getRawMessage
     * 例如 go-cq-http rawMessage 不包含图片 url
     *
     * @param msg 需要修改客户端消息上报类型为 string
     * @return 消息链
     */
    public static List<ArrayMsg> rawToList(String msg) {
        return GsonUtils.fromJson(rawToJson(msg), new TypeToken<List<ArrayMsg>>() {
        }.getType());
    }

    /**
     * string 消息上报转消息链
     * 建议传入 event.getMessage 而非 event.getRawMessage
     * 例如 go-cq-http rawMessage 不包含图片 url
     *
     * @param msg 需要修改客户端消息上报类型为 string
     * @return 消息链
     */
    public static JsonArray rawToJson(String msg) {
        val array = new JsonArray();
        try {
            Arrays.stream(msg.split(CQ_CODE_SPLIT)).filter(s -> !s.isEmpty()).forEach(s -> {
                val matcher = RegexUtils.regexMatcher(CQ_CODE_REGEX, s);
                val object = new JsonObject();
                val params = new JsonObject();
                if (matcher == null) {
                    object.addProperty("type", "text");
                    params.addProperty("text", s);
                } else {
                    object.addProperty("type", matcher.group(1));
                    Arrays.stream(matcher.group(2).split(",")).filter(args -> !args.isEmpty()).forEach(args -> {
                        val k = args.substring(0, args.indexOf("="));
                        val v = BotUtils.unescape(args.substring(args.indexOf("=") + 1));
                        params.addProperty(k, v);
                    });
                }
                object.add("data", params);
                array.add(object);
            });
        } catch (Exception e) {
            log.error("Raw message convert failed: {}", e.getMessage());
            return null;
        }
        return array;
    }


    /**
     * 从 ArrayMsg 生成 CQ Code
     *
     * @param o {@link ArrayMsg}
     * @return CQ Code
     */
    public static String arrayMsgToCode(ArrayMsg o) {
        StringBuilder builder = new StringBuilder();
        builder.append("[CQ:").append(o.getType());
        o.getData().forEach((k, v) -> builder.append(",").append(k).append("=").append(v));
        builder.append("]");
        return builder.toString();
    }


    /**
     * 从 List<ArrayMsg> 生成 CQ Code
     *
     * @param arrayMsgs {@link ArrayMsg}
     * @return CQ Code
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static String arrayMsgToCode(List<ArrayMsg> arrayMsgs) {
        StringBuilder builder = new StringBuilder();
        for (ArrayMsg item : arrayMsgs) {
            if (item.getType() != null && !item.getType().equals(MsgTypeEnum.text)) {
                builder.append("[CQ:").append(item.getType());
                item.getData().forEach((k, v) -> builder.append(",").append(k).append("=").append(escape(v)));
                builder.append("]");
            } else {
                builder.append(escape(item.getData().get(MsgTypeEnum.text.toString())));
            }
        }
        return builder.toString();
    }


    /**
     * 从 msg 转换
     *
     * @param rawJson {@link JsonObject}
     * @param event {@link MessageEvent}
     */
    public static void rawConvert(JsonObject rawJson, MessageEvent event) {
        // 支持 array 格式消息上报，如果 msg 是一个有效的 json 字符串则作为 array 上报
        if (rawJson.has("message") && GsonUtils.isArrayNode(rawJson, "message")) {
            List<ArrayMsg> arrayMsg = GsonUtils.convertToList(GsonUtils.getAsString(rawJson, "message"), ArrayMsg.class);
            // 存入 array message
            event.setArrayMsg(arrayMsg);
            // 将 array message 转换回 string message
            event.setMessage(arrayMsgToCode(arrayMsg));
        }
    }



    /**
     * 创建自定义消息合并转发
     *
     * @param uin      发送者QQ号
     * @param name     发送者显示名字
     * @param contents 消息列表，每个元素视为一个消息节点
     *                 <a href="https://docs.go-cqhttp.org/cqcode/#%E5%90%88%E5%B9%B6%E8%BD%AC%E5%8F%91">参考文档</a>
     * @return 转发消息
     */
    public static List<Map<String, Object>> generateForwardMsg(long uin, String name, List<String> contents) {
        val nodes = new ArrayList<Map<String, Object>>();
        contents.forEach(msg -> {
            val node = new HashMap<String, Object>(16) {{
                put("type", "node");
                put("data", new HashMap<String, Object>(16) {{
                    put("name", name);
                    put("uin", uin);
                    put("content", msg);
                }});
            }};
            nodes.add(node);
        });
        return nodes;
    }
}
