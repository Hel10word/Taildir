package com.zbf.boraydata;

import com.boraydata.TaildirFileSource;
import com.boraydata.logparser.parser.LogParser;
import com.boraydata.logparser.parser.LogParserFactory;
import com.boraydata.logparser.util.constant.ParserType;

import java.util.Map;

/**
 * @author bufan
 * @data 2021/7/30
 */
public class MainTest {
    private static String logPath = "./test.txt";
    public static void main(String[] args) {
        LogParser parser = LogParserFactory.create(ParserType.Syslog);

        TaildirFileSource taildirFileSource = new TaildirFileSource();

        taildirFileSource.config(logPath,x -> parser.toMap(x),5000);

        taildirFileSource.start();
    }
}
