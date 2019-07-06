package org.my.mina.filter;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.my.mina.filter.codec.MyPack;
import org.my.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeepAliveFactoryImpl implements KeepAliveMessageFactory {

    static final Logger logger = LoggerFactory.getLogger(KeepAliveFactoryImpl.class);

    // 用来判断接收到的消息是不是一个心跳请求包，是就返回true[接收端使用]
    @Override
    public boolean isRequest(IoSession session, Object message) {
        if (message instanceof MyPack) {
            MyPack pack = (MyPack) message;
            if (Const.HEART_BEAT == pack.getModule()) {
                return true;
            }
        }
        return false;
    }

    // 用来判断接收到的消息是不是一个心跳回复包，是就返回true[发送端使用]
    @Override
    public boolean isResponse(IoSession session, Object message) {
        // TODO Auto-generated method stub
        return false;
    }

    // 在需要发送心跳时，用来获取一个心跳请求包[发送端使用]
    @Override
    public Object getRequest(IoSession session) {
        // TODO Auto-generated method stub
        return null;
    }

    // 在需要回复心跳时，用来获取一个心跳回复包[接收端使用]
    @Override
    public Object getResponse(IoSession session, Object request) {
        MyPack attendPack = (MyPack) request;
        if (null == session.getAttribute(Const.SESSION_KEY)) {
            // 需要先进行登录
            return new MyPack(Const.AUTHEN, attendPack.getSeq(), "fail");
        }
        // 将超时次数置为0
        session.setAttribute(Const.TIME_OUT_KEY, 0);
        return new MyPack(Const.HEART_BEAT, attendPack.getSeq(), "success");
    }

}
