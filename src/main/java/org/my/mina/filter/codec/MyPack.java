package org.my.mina.filter.codec;

import org.apache.commons.lang3.StringUtils;

public class MyPack {

    // 数据总长度
    private int len;

    // 数据传输类型：0x00-设备到服务端 0x01-服务端到设备
    private byte type = 0x01;

    // 模块代码
    private int module;

    /**
     *  此域表示一个序列号，使用在异步通信模式下，由消息发起者设定，应答者对应给回此序列号。
     *  序列号范围：0000－9999，循环使用。
     *  同步方式下该域保留。
    **/
    private String seq;

    // 包体
    private String body;

    /**
     * 0x00表示客户端到服务端
     */
    public static final byte REQUEST = 0x00;
    /**
     * 0x01表示服务端到客户端
     */
    public static final byte RESPONSE = 0x01;

    // 包头长度
    public static final int PACK_HEAD_LEN = 13;

    // 最大长度
    public static final int MAX_LEN = 9999;

    public MyPack(int module, String seq, String body) {
        this.module = module;
        this.seq = seq;
        this.body = body;
        // 总长度
        this.len = PACK_HEAD_LEN + (StringUtils.isBlank(body) ? 0 : body.getBytes().length);
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getModule() {
        return module;
    }

    public void setModule(int module) {
        this.module = module;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "len:" +len + " type:" + type + " module" + module + " seq:" + seq + " body:" + body;
    }
}
