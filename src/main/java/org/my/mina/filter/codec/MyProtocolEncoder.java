package org.my.mina.filter.codec;

import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * java对象转二进制
 *
 */
public class MyProtocolEncoder implements ProtocolEncoder {

    private final Charset charset;

    public MyProtocolEncoder() {
        this.charset = Charset.defaultCharset();
    }

    public MyProtocolEncoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        MyPack myPack = (MyPack) message;
        IoBuffer ioBuffer = IoBuffer.allocate(myPack.getLen()).setAutoExpand(true);
        System.out.println("encode length: " + myPack.getLen());
        ioBuffer.putInt(myPack.getLen());
        ioBuffer.put(myPack.getType());
        ioBuffer.putInt(myPack.getModule());
        ioBuffer.putString(myPack.getSeq(), charset.newEncoder());
        if (StringUtils.isNotBlank(myPack.getBody())) {
            System.out.println("encoder body length:" + myPack.getBody().getBytes().length);
            ioBuffer.putString(myPack.getBody(), charset.newEncoder());
        }
        ioBuffer.flip();
        out.write(ioBuffer);
    }

    @Override
    public void dispose(IoSession session) throws Exception {
        // TODO Auto-generated method stub

    }

}
