package org.my;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.my.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;

//@WebListener
public class MinaRunServletContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(MinaRunServletContextListener.class);

    @Autowired
    private NioSocketAcceptor acceptor;

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
//        logger.info("---server acceptor unbind---");
//        System.out.println("---server acceptor unbind---" + Thread.currentThread().getName());
//        acceptor.unbind();
//        logger.info("---server acceptor dispose---");
//        System.out.println("---server acceptor dispose---" + Thread.currentThread().getName());
//        acceptor.dispose();
        System.out.println("---test contextDestroyed nethod---");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext())
                .getAutowireCapableBeanFactory().autowireBean(this);
        try {
            acceptor.bind(new InetSocketAddress(Const.PORT));
            logger.info("---springboot mina server start---");
        } catch (IOException e) {
            logger.error("---springboot mina server start error : ", e.getMessage() + "---");
        }
    }

}
