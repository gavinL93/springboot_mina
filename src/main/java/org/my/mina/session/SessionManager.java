package org.my.mina.session;

public interface SessionManager {

    /**
     * 添加session
     */
    void addSession(String device, MySession session);

    /**
     * 获取session
     */
    MySession getSession(String device);

    /**
     * 替换Session
     */
    void replaceSession(String device, MySession session);

    /**
     * 删除session
     */
    void removeSession(String device);

    /**
     * 删除session
     */
    void removeSession(MySession session);

}
