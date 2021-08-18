package com.shuzijun.leetcode.plugin.utils;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.StringWriter;
import java.util.Properties;

/**
 * @author shuzijun
 */
public class VelocityUtils {

    private static String VM_LOG_TAG = "Leetcode VelocityUtils";
    private static String VM_CONTEXT = "question";
    private static VelocityEngine engine;


    static {
        engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.PARSER_POOL_SIZE, 20);
        engine.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8");
        engine.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8");

        Properties props = new Properties();
        props.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
        props.put("runtime.log.logsystem.log4j.category", "velocity");
        props.put("runtime.log.logsystem.log4j.logger", "velocity");

        engine.init(props);
    }

    public static String convert(String template, Object data) {

        StringWriter writer = new StringWriter();
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(VM_CONTEXT, data);
        velocityContext.put("velocityTool", new VelocityTool());
        velocityContext.put("vt", new VelocityTool());
        boolean isSuccess = engine.evaluate(velocityContext, writer, VM_LOG_TAG, template);
        if (!isSuccess) {

        }
        return writer.toString();
    }
}
