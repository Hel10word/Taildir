package com.zbf.boraydata;

import com.boraydata.TaildirFile;
import com.boraydata.TaildirFileMatcher;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/** 测试 TaildirFileMatcher 类
 * @author bufan
 * @data 2021/7/29
 */
public class TaildirFileMatcherTest {
    TaildirFileMatcher taildirFileMatcher;

    @Test
    // 往坐标轴 写入文件信息
    public void updatePointerFile(){
        taildirFileMatcher = new TaildirFileMatcher();
        Map<String, TaildirFile> map = new HashMap<>();
        map.put("test1.log",new TaildirFile("test1.log","test1.log",0,0));
        map.put("test2.log",new TaildirFile("test2.log","test2.log",2,20));
        map.put("test3.log",new TaildirFile("test3.log","test3.log",45,500));
        taildirFileMatcher.updatePositionFileInfo(map);
//        Map<String, TaildirFile> positionFileInfo = taildirFileMatcher.getPositionInfoMap();
//        System.out.println(positionFileInfo.toString());
    }


}
