package com.boraydata;

import com.boraydata.exception.TaildirException;
import com.boraydata.utils.TaildirConstant;

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

/** 用来处理 Taildir 的坐标文件 与  需要监听的文件
 * @author bufan
 * @data 2021/7/27
 */
public class TaildirFileMatcher {
    private static final String  POINTER_FILE = TaildirConstant.DEFAULT_POINTER_FILE;
    private static final String DIVIDE_SIGN = TaildirConstant.DEFAULT_INFO_DIVIDE_SIGN;
    private static final String DIVIDE_SIGN_REGEX = TaildirConstant.DEFAULT_INFO_DIVIDE_SIGN_REGEX;

    private Path positionFilePath;

    // 加载 坐标 配置文件
    public TaildirFileMatcher(){
        if(isFile(POINTER_FILE)) {
            this.positionFilePath = Paths.get(POINTER_FILE);
        }else {
            throw new TaildirException("Failed to new POINTER_FILE");
        }

    }
    // 用来 检验 配置文件是否存在 ，不存在则创建
    public boolean isFile(String failPath){
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

    // 用来 更新 PositionFileInfo 的信息
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
     * @return ex:{C://test1.log=TaildirInfoEntity{filePath='C://test1.log', fileName='test1.log', filePointer=0, fileLength=0}}
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

    /**
     * 用来将   坐标信息   封装为对象
     */
//    private Map<String, Long> unpackInfo(String str){
//        String[] split = str.split(DIVIDE_SIGN_REGEX);
//        if(split.length==2)
//            return new TaildirFile(split[0],split[1],Long.valueOf(split[2]),Long.valueOf(split[3]));
//        return Collections.emptyMap();
//    }

    private String packInfo(TaildirFile entity){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(entity.getFilePath());
        stringBuilder.append(DIVIDE_SIGN);
        stringBuilder.append(entity.getFilePointer());
        return String.valueOf(stringBuilder);
    }
}
