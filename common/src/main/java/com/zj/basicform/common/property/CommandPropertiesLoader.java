package com.zj.basicform.common.property;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 配置读取，一般默认读取config下的server.properties，也可以自己输入路径到args[]当中
 * @author zj
 * @since 2019/2/8
 */
public class CommandPropertiesLoader implements PropertiesLoader {
    private String[] args;

    public CommandPropertiesLoader(String[] args) {
        this.args = args;
    }

    @SuppressWarnings("static-access")
    @Override
    public Properties load() throws IOException {
        Properties properties = new Properties();
        Options options = new Options();
        options.addOption(new Option("p", true, "classpath config file."));
        options.addOption(new Option("P", true, "local config file."));
        Option option = OptionBuilder.withArgName("property=value").hasArgs(2).withValueSeparator().withDescription("use: property=value").create("D");
        options.addOption(option);
        CommandLine commandLine;
        try {
            commandLine = new PosixParser().parse(options, args);
            if (commandLine.hasOption("p")) {
                properties.load(this.getClass().getResourceAsStream(commandLine.getOptionValue("p")));
            }
            if (commandLine.hasOption("P")) {
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(new File(commandLine.getOptionValue("P")));
                    properties.load(fileInputStream);
                } finally {
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        properties.putAll(commandLine.getOptionProperties("D"));
        return properties;
    }

}
