package org.my.mina.config;

import java.beans.PropertyEditor;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.integration.beans.InetSocketAddressEditor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.my.mina.filter.KeepAliveFactoryImpl;
import org.my.mina.filter.codec.MyProtocolCodecFactory;
import org.my.mina.handler.BaseHandler;
import org.my.mina.handler.ServerHandler;
import org.my.mina.handler.impl.BindHandler;
import org.my.mina.handler.impl.TimeCheckHandler;
import org.my.util.Const;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinaServerConfig {

    /**
     * 设置I/O接收器
     * @return
     */
    private static Map<Class<?>, Class<? extends PropertyEditor>> customEditors = new HashMap<>();

    private HashMap<Integer, BaseHandler> handlers = new HashMap<>();

    @Bean
    public ServerHandler serverHandler() {
        handlers.put(Const.AUTHEN, new BindHandler());
        handlers.put(Const.TIME_CHECK, new TimeCheckHandler());
        ServerHandler serverHandler = new ServerHandler();
        serverHandler.setHandlers(handlers);
        return serverHandler;
    }

    @Bean
    public static CustomEditorConfigurer customEditorConfigurer() {
        customEditors.put(SocketAddress.class, InetSocketAddressEditor.class);
        CustomEditorConfigurer configurer = new CustomEditorConfigurer();
        configurer.setCustomEditors(customEditors);
        return configurer;
    }

    /**
     * 线程池filter
     */
    @Bean
    public ExecutorFilter executorFilter() {
        return new ExecutorFilter();
    }

    /**
     * 日志信息注入过滤器，MDC(Mapped Diagnostic Context有译作线程映射表)是日志框架维护的一组信息键值对，可向日志输出信息中插入一些想要显示的内容。
     *
     */
    @Bean
    public MdcInjectionFilter mdcInjectionFilter() {
        return new MdcInjectionFilter(MdcInjectionFilter.MdcKey.remoteAddress);
    }

    /**
     * 编解码器filter
     */
    @Bean
    public ProtocolCodecFilter protocolCodecFilter() {
        return new ProtocolCodecFilter(new MyProtocolCodecFactory());
    }

    /**
     * 日志filter
     */
    @Bean
    public LoggingFilter loggingFilter() {
        return new LoggingFilter();
    }

    @Bean
    public KeepAliveFactoryImpl keepAliveFactoryImpl() {
        return new KeepAliveFactoryImpl();
    }

    /**
     * 心跳filter
     */
    @Bean
    public KeepAliveFilter keepAliveFilter(KeepAliveFactoryImpl keepAliveFactory) {
        // 注入心跳工厂，读写空闲
        KeepAliveFilter filter = new KeepAliveFilter(keepAliveFactory, IdleStatus.BOTH_IDLE);
        // 设置是否forward到下一个filter
        filter.setForwardEvent(true);
        // 设置心跳频率
        filter.setRequestInterval(Const.IDELTIMEOUT);
        return filter;
    }

    /**
     * 过滤器链
     */
    @Bean
    public DefaultIoFilterChainBuilder defaultIoFilterChainBuilder(ExecutorFilter executorFilter,
            MdcInjectionFilter mdcInjectionFilter, ProtocolCodecFilter protocolCodecFilter, LoggingFilter loggingFilter,
            KeepAliveFilter keepAliveFilter) {
        DefaultIoFilterChainBuilder filterChainBuilder = new DefaultIoFilterChainBuilder();
        Map<String, IoFilter> filters = new LinkedHashMap<>();
        filters.put("mdcInjectionFilter", mdcInjectionFilter);
        // filters.put("loggingFilter", loggingFilter);
        filters.put("protocolCodecFilter", protocolCodecFilter);
        filters.put("executor", executorFilter);
        filters.put("keepAliveFilter", keepAliveFilter);
        filterChainBuilder.setFilters(filters);
        return filterChainBuilder;
    }

    /**
     * 创建连接
     * @return
     */
    @Bean(initMethod = "init", destroyMethod = "dispose")
    public NioSocketAcceptor nioSocketAcceptor(ServerHandler serverHandler,
            DefaultIoFilterChainBuilder defaultIoFilterChainBuilder) {
        NioSocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, Const.IDELTIMEOUT);
        // 绑定过滤器链
        acceptor.setFilterChainBuilder(defaultIoFilterChainBuilder);
        acceptor.setHandler(serverHandler);
        return acceptor;
    }

}
