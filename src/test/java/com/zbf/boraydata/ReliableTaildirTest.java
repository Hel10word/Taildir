package com.zbf.boraydata;

import com.boraydata.ReliableTaildir;
import com.boraydata.utils.TaildirConstant;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

/**
 * @author bufan
 * @data 2021/7/29
 */
public class ReliableTaildirTest {
    String filePath = "test";
    String dirctionPath = "E:\\SoftWare\\A_WORK\\IDEA-workspace\\boraydata-demo\\Taildir\\config\\";
    String POSITION_FILE = TaildirConstant.DEFAULT_POINTER_FILE;


    @Test
    public void testFilePath(){
        ReliableTaildir reliableTaildir = new ReliableTaildir();
        reliableTaildir.getFilesPath(dirctionPath);
    }
}
