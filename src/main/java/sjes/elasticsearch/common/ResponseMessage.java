package sjes.elasticsearch.common;

import lombok.Data;

/**
 *  响应消息
 * Created by mac on 15/8/29.
 */
@Data
public class ResponseMessage {

    /**
     * 类型
     */
    public enum Type {
        /** 成功 */
        success,

        /** 警告 */
        warn,

        /** 错误 */
        error;
    }

    /** 类型 */
    private Type type;

    /** 内容 */
    private String content;

    /**
     * 初始化一个新建的 AjaxResponse 对象，使其表示一个空消息
     */
    public ResponseMessage() {

    }

    /**
     * 初始化一个新创建的 AjaxResponse 对象
     * @param type 类型
     * @param content 内容
     */
    public ResponseMessage(Type type, String content) {
        this.type = type;
        this.content = content;
    }

    /**
     * 返回成功消息
     * @param content 类型
     * @return 成功消息
     */
    public static ResponseMessage success(String content) {
        return new ResponseMessage(Type.success, content);
    }

    /**
     * 返回警告消息
     * @param content 内容
     * @return 警告消息
     */
    public static ResponseMessage warn(String content) {
        return new ResponseMessage(Type.warn, content);
    }

    /**
     * 返回错误消息
     * @param content 内容
     * @return 错误消息
     */
    public static ResponseMessage error(String content) {
        return new ResponseMessage(Type.error, content);
    }
}
