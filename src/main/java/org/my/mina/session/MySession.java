package org.my.mina.session;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.core.session.IoSession;
import org.my.mina.filter.codec.MyPack;

public class MySession implements Serializable {

    private static final long serialVersionUID = 1L;

    // 不参与序列化
    private transient IoSession session;

    // session在本机器的ID
    private Long nid;
    // session绑定的服务ip
    private String host;
    // 访问端口
    private int port;
    // session绑定的设备
    private String account;

    public MySession() {
    }

    public MySession(IoSession session) {
        this.session = session;
        this.host = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
        this.port = ((InetSocketAddress) session.getRemoteAddress()).getPort();
        this.nid = session.getId();
    }

    /**
     * 将key-value自定义属性，存储到IO会话中
     */
    public void setAttribute(String key, Object value) {
        if (null != session) {
            session.setAttribute(key, value);
        }
    }

    /**
     * 从IO的会话中，获取key的value
     */
    public Object getAttribute(String key) {
        if (null != session) {
            return session.getAttribute(key);
        }
        return null;
    }

    /**
     *  在IO的会话中，判断是否存在包含key-value
     */
    public boolean containsAttribute(String key) {
        if (null != session) {
            return session.containsAttribute(key);
        }
        return false;
    }

    /**
     * 从IO的会话中，删除key
     */
    public void removeAttribute(String key) {
        if (null != session) {
            session.removeAttribute(key);
        }
    }

    /**
     *  获取IP地址
     */
    public SocketAddress getRemoteAddress() {
        if (null != session) {
            return session.getRemoteAddress();
        }
        return null;
    }

    /**
     * 将消息对象 message发送到当前连接的对等体（异步）
     * 当消息被真正发送到对等体的时候，IoHandler.messageSent(IoSession,Object)会被调用。
     * @param msg 发送的消息
     */
    public void write(MyPack msg) {
        if (null != session) {
            session.write(msg).isWritten();
        }
    }

    /**
     * 会话是否已经连接
     */
    public boolean isConnected() {
        if (null != session) {
            return session.isConnected();
        }
        return false;
    }

    /**
     * 关闭当前连接。如果参数 immediately为 true的话
     * 连接会等到队列中所有的数据发送请求都完成之后才关闭；否则的话就立即关闭。
     */
    public void close(boolean immediately) {
        if (null != session) {
            if (immediately) {
                session.closeNow();
            } else {
                session.closeOnFlush();
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        // 强转为当前类
        MySession session = (MySession) obj;
        if (session.nid != null && nid != null) {
            return session.nid.longValue() == nid.longValue() && session.host.equals(host) && session.port == port;
        }
        return false;
    }

    public String toString() {
        return "session host:" + this.host + " port:" + this.port + " nid:" + this.nid;
    }

    public IoSession getSession() {
        return session;
    }

    public void setSession(IoSession session) {
        this.session = session;
    }

    public Long getNid() {
        return nid;
    }

    public void setNid(Long nid) {
        this.nid = nid;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
