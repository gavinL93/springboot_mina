package org.my.mina.handler.impl;

import org.apache.commons.lang3.StringUtils;
import org.my.mina.handler.BaseHandler;
import org.my.mina.session.MySession;
import org.my.mina.session.SessionManager;
import org.my.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;

public class BindHandler implements BaseHandler {

    static final Logger logger = LoggerFactory.getLogger(BindHandler.class);

    // 获取会话管理类
    @Autowired
    private SessionManager sessionManager;

    @Override
    public String process(MySession mySession, String content) {

        if (StringUtils.isBlank(content)) {
            return null;
        }

        try {
            JSONObject data = JSONObject.parseObject(content);

            // 检查账号是否存在
            String account = data.getString("account");
            // 可增加数据库、redis之类的认证
            if (StringUtils.isBlank(account)) {
                return null;
            }

            // 检查软件版本号
            String version = data.getString("version");
            // 可增加数据库、redis之类的认证
            if (!Const.VERSION.equals(version)) {
                return null;
            }

            mySession.setAttribute(Const.SESSION_KEY, account);
            mySession.setAttribute(Const.TIME_OUT_KEY, 0); // 超时次数设为0

            // 由于客户端断线服务端可能会无法获知的情况，客户端重连时，需要关闭旧的连接
            MySession oldSession = sessionManager.getSession(account);
            if (oldSession != null && !oldSession.equals(mySession)) {
                // 移除account属性
                oldSession.removeAttribute(Const.SESSION_KEY);
                // 移除超时时间
                oldSession.removeAttribute(Const.TIME_OUT_KEY);
                // 替换oldSession
                sessionManager.replaceSession(account, mySession);
                oldSession.close(false);
                logger.info(">>>>>> oldsession close!");
            }
            if (oldSession == null) {
                sessionManager.addSession(account, mySession);
            }
            logger.info(">>>>>> bind success: " + mySession.getNid());
        } catch (Exception e) {
            logger.error(">>>>>> bind error: " + e.getMessage());
            return null;
        }

        return "bind success";
    }

}
