package org.my.mina.handler;

import org.my.mina.session.MySession;

/**
 * Mina的请求处理接口，必须实现此接口
 *
 */
public interface BaseHandler {
    String process(MySession mySession, String content);
}
