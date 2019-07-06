package org.my.mina.handler.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.my.mina.handler.BaseHandler;
import org.my.mina.session.MySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TimeCheckHandler implements BaseHandler {

    static final Logger logger = LoggerFactory.getLogger(TimeCheckHandler.class);

    @Autowired

    @Override
    public String process(MySession mySession, String content) {

        if (StringUtils.isBlank(content)) {
            return null;
        }

        try {
            
            // 平台系统时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String time = sdf.format(new Date());
            return time;

        } catch (Exception e) {
            logger.error(">>>>>> time check error: " + e.getMessage());
            return null;
        }
    }

}
