package org.my.mina.filter.codec;

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 二进制转java对象
 * @author LiangJiawen
 *
 */
public class MyProtocolDecoder extends CumulativeProtocolDecoder {

    static final Logger logger = LoggerFactory.getLogger(MyProtocolDecoder.class);

    private final Charset charset;

    public MyProtocolDecoder() {
        this.charset = Charset.defaultCharset();
    }

    public MyProtocolDecoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {

        if (in.remaining() < MyPack.PACK_HEAD_LEN) {
            return false;
        }

        if (in.remaining() > MyPack.MAX_LEN) {
            // 数据长度大于最大长度，出现数据错误，丢弃
            clearData(in);
            return false;
        }

        else {
            in.mark();

            try {

                // 数据长度4字节
                int length = in.getInt();
                logger.info("decode length:" + length);

                if (length < MyPack.PACK_HEAD_LEN) {
                    // 数据长度小于包头长度，出现数据错误，丢弃
                    clearData(in);
                    return false;
                }

                int i = in.remaining();
                logger.info("remaing:" + i);
                if (i < (length - 4)) {
                    // 内容不够， 重置position到操作前，进行下一轮接受新数据
                    in.reset();
                    return false;
                } else {
                    in.reset();

                    // 数据传输类型
                    byte type = in.get();
                    if (type != MyPack.REQUEST) {
                        // 不是客户端发送的数据，出现数据错误，丢弃
                        clearData(in);
                        return false;
                    }

                    // 模块id
                    int module = in.getInt();

                    // 序列号
                    String seq_no = in.getString(4, charset.newDecoder());

                    String body = in.getString(length - MyPack.PACK_HEAD_LEN, charset.newDecoder());

                    MyPack myPack = new MyPack(module, seq_no, body);
                    logger.info(">>>>>> server decode result: " + myPack.toString());
                    out.write(myPack);
                    return in.remaining() > 0;
                }
            } catch (Exception e) {
                // 解析有异常，抛弃异常数据，不能影响正常通信
                logger.error(">>>>>> decode error: " + e.toString());
                clearData(in);
                return false;
            }
        }
    }

    private void clearData(IoBuffer in) {
        byte[] bytes = new byte[in.remaining()];
        in.get(bytes);
        bytes = null;
    }
}
