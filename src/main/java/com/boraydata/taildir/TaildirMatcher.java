package com.boraydata.taildir;

import com.boraydata.taildir.exception.TaildirException;
import com.boraydata.taildir.utils.TaildirConstant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** 用来    读取与写入文件坐标信息
 * @author bufan
 * @data 2021/7/27
 */
public class TaildirMatcher {
    // 存放 文件坐标位置的  配置文件
    private static final String  POINTER_FILE = TaildirConstant.DEFAULT_POINTER_FILE;
    // 坐标信息中的分隔符   文件名$DIVIDE_SING$坐标点   ep:E://test.log ?*? 55
    private static final String DIVIDE_SIGN = TaildirConstant.DEFAULT_INFO_DIVIDE_SIGN;
    // 使用正则匹配 DICIDE_SING 获取相关信息
    private static final String DIVIDE_SIGN_REGEX = TaildirConstant.DEFAULT_INFO_DIVIDE_SIGN_REGEX;

    private final Path positionFilePath;

    // 加载 坐标 配置文件
    public TaildirMatcher(){
        if(isFile(POINTER_FILE)) {
            this.positionFilePath = Paths.get(POINTER_FILE);
        }else {
            throw new TaildirException("Failed to new POINTER_FILE");
        }
    }

    // 用来 检验 配置文件是否存在 ，不存在则创建
    private boolean isFile(String failPath){
        File file = new File(failPath);
        try {
            if(!file.exists()){
                File parentFile = file.getParentFile();
                // 目录不存在  并且 创建目录不成功
                if(!parentFile.exists()&&!parentFile.mkdirs())
                        return false;
                // 创建文件
                return file.createNewFile();
            }
            return true;
        }catch (IOException e){
            throw new TaildirException("Failed to create POINTER_FILE",e);
        }
    }

    // 用来 写入 PositionFileInfo 的信息     写入方式是  覆盖写入
    public void updatePositionFileInfo(Map<String, TaildirFile> map){
        try (BufferedWriter writer = Files.newBufferedWriter(
                this.positionFilePath,
                StandardCharsets.UTF_8,
                StandardOpenOption.WRITE)){
            for (TaildirFile entity : map.values())
                writer.write(packInfo(entity)+"\n");
            writer.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 获取  getPositionInfoMap  的相关信息
     * 文件绝对路径  TaildirFile 信息
     * @return  存放的 是   文件的绝对路径   与   上次读取完的位置  ex:{C://test1.log , 233}
     */
    public Map<String, String> getPositionInfoMap(){
        try {
            try(Stream<String> lines = Files.lines(this.positionFilePath)) {
                return lines
                        .map(x->x.split(DIVIDE_SIGN_REGEX))
                        .collect(Collectors.toMap(x->x[0],x->x[1]));
            }
        }catch (Exception e){
            throw new TaildirException("Failed to get PointerFileInfo ",e);
        }
    }

    // 用来  提取文件的 坐标信息
    private String packInfo(TaildirFile entity){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(entity.getFilePath());
        stringBuilder.append(DIVIDE_SIGN);
        stringBuilder.append(entity.getFilePointer());
        return String.valueOf(stringBuilder);
    }
}
