package org.my.mina.handler;

import java.net.InetSocketAddress;
import java.util.HashMap;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.my.mina.filter.codec.MyPack;
import org.my.mina.session.MySession;
import org.my.mina.session.SessionManager;
import org.my.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ServerHandler extends IoHandlerAdapter {
    static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private HashMap<Integer, BaseHandler> handlers = new HashMap<>();

    @Autowired
    SessionManager sessionManager;

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        MySession MySession = new MySession(session);
        MyPack MyPack = (MyPack) message;
        logger.info(MySession.toString() + ">>>>>> server received:" + message);

        MyPack response;

        // 如果是心跳包接口，则说明处理失败
        if (Const.HEART_BEAT == MyPack.getModule()) {
            logger.info(MySession.toString() + ">>>>>> server handler heartbeat error!");
            response = new MyPack(Const.AUTHEN, MyPack.getSeq(), "authen fail");
            MySession.write(response);
            MySession.close(false);
            return;
        }

        // 终端在未认证时连接进来，SERVER端要发送认证失败的包给终端，然后再断开连接，防止未知设备连到服务器
        if (null == MySession.getAttribute(Const.SESSION_KEY) && Const.AUTHEN != MyPack.getModule()) {
            logger.info(MySession.toString() + ">>>>>> need device authen!");
            response = new MyPack(Const.AUTHEN, MyPack.getSeq(), "authen fail");
            MySession.write(response);
            MySession.close(false);
            return;
        }

        BaseHandler handler = handlers.get(MyPack.getModule());
        String result = handler.process(MySession, MyPack.getBody());
        if (result == null) {
            logger.info(MySession.toString() + ">>>>>> need authen!");
            response = new MyPack(Const.AUTHEN, MyPack.getSeq(), "deal error");
            MySession.write(response);
            MySession.close(false);
        } else {
            logger.info(MySession.toString() + ">>>>>> succeed!");
            response = new MyPack(MyPack.getModule(), MyPack.getSeq(), result);
            MySession.write(response);
        }
    }

    /**
     * 心跳包超时处理
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        if (session.getAttribute(Const.TIME_OUT_KEY) == null) {
            session.closeNow();
            logger.error(
                    session.getAttribute(Const.SESSION_KEY) + " nid: " + session.getId() + " >>>>>> time_out_key null");
            return;
        }
        try {
            int isTimeoutNum = (int) session.getAttribute(Const.TIME_OUT_KEY);
            isTimeoutNum++;
            // 没有超过最大次数，超时次数加1
            if (isTimeoutNum < Const.TIME_OUT_NUM) {
                session.setAttribute(Const.TIME_OUT_KEY, isTimeoutNum);
            } else {
                // 超过最大次数，关闭会话连接
                String account = (String) session.getAttribute(Const.SESSION_KEY);
                // 移除device属性
                session.removeAttribute(Const.SESSION_KEY);
                // 移除超时属性
                session.removeAttribute(Const.TIME_OUT_KEY);
                sessionManager.removeSession(account);
                session.closeOnFlush();
                logger.info(">>>>>> client user: " + account + " more than " + Const.TIME_OUT_NUM
                        + " times have no response, connection closed! >>>>>>");
            }
        } catch (Exception e) {
            logger.error(
                    session.getAttribute(Const.SESSION_KEY) + " nid: " + session.getId() + " >>>>>> " + e.getMessage());
            session.closeNow();
        }
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        logger.info(session.getAttribute(Const.SESSION_KEY) + " nid: " + session.getId() + " >>>>>> sessionClosed ");
        // 移除account属性
        session.removeAttribute(Const.SESSION_KEY);
        // 移除超时属性
        session.removeAttribute(Const.TIME_OUT_KEY);
        String account = (String) session.getAttribute(Const.SESSION_KEY);
        sessionManager.removeSession(account);
        session.closeNow();
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        // IP
        String address = isa.getAddress().getHostAddress();
        session.setAttribute("address", address);
        logger.info(">>>>>> 来自" + address + " 的终端上线，sessionId：" + session.getId());
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        logger.info("Open a session ...");
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        logger.error(
                ">>>>>> 终端用户：" + session.getAttribute(Const.SESSION_KEY) + "连接发生异常，即将关闭连接，原因：" + cause.getMessage());
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        logger.info(">>>>>>>>>>>>>>>>>>>> 发送消息成功 >>>>>>>>>>>>>>>>>>>>");
    }

    public HashMap<Integer, BaseHandler> getHandlers() {
        return handlers;
    }

    public void setHandlers(HashMap<Integer, BaseHandler> handlers) {
        this.handlers = handlers;
        logger.info(">>>>>> server handlers set success!");
    }

}
