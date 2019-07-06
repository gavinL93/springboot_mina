package org.my.mina.session.impl;

import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.my.mina.session.MySession;
import org.my.mina.session.SessionManager;
import org.my.util.Const;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultSessionManagerImpl extends Observable implements SessionManager {

    /**
     * 存放session的线程安全的map集合
     */
    private static ConcurrentHashMap<String, MySession> sessions = new ConcurrentHashMap<>();

    /**
     * 线程安全的自增类，用于统计连接数
     */
    private static final AtomicInteger connectionsCounter = new AtomicInteger(0);

    /**
     * 添加session
     */
    @Override
    public void addSession(String account, MySession session) {
        if (null != session) {
            sessions.put(account, session);
            connectionsCounter.incrementAndGet();
            // 被观察者方法，拉模型
            setChanged();
            notifyObservers();
        }
    }

    /**
     * 获取session
     */
    @Override
    public MySession getSession(String account) {
        return sessions.get(account);
    }

    /**
     * 替换session，通过账号
     */
    @Override
    public void replaceSession(String account, MySession session) {
        sessions.put(account, session);
        // 被观察者方法，拉模型
        setChanged();
        notifyObservers();
    }

    /**
     * 移除session通过账号
     */
    @Override
    public void removeSession(String account) {
        sessions.remove(account);
        connectionsCounter.decrementAndGet();
        // 被观察者方法，拉模型
        setChanged();
        notifyObservers();
    }

    /**
     * 移除session通过session
     */
    @Override
    public void removeSession(MySession session) {
        String account = (String) session.getAttribute(Const.SESSION_KEY);
        removeSession(account);
    }

    public static ConcurrentHashMap<String, MySession> getSessions() {
        return sessions;
    }
}
