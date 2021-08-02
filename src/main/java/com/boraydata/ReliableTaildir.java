package com.boraydata;

import com.boraydata.exception.TaildirException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 用来根据 指定的文件 来解析日志 并  将结果保存   坐标也保存
 * @author bufan
 * @data 2021/7/27
 */
public class ReliableTaildir {
    private static final TaildirFileMatcher taildirFileMatcher = new TaildirFileMatcher();

     // 创建一个  需要监听的文件列表
     private List<String> watchFilePathList = new ArrayList<>();

    // 加载 需要监听的文件信息 至 List
    public void watchFiles(String filePath){
        watchFiles(new String[]{filePath});
    }
    public void watchFiles(String[] filePath){
        for (String path:filePath) {
            getFilesPath(path);
        }
    }
    /**
     * 获取 文件路径
     * @Param filePath : 传入 一个路径 可以是 文件 或者 目录
     * @Return: void : 返回该目录中 的文件绝对路径
     */
    public void getFilesPath(String filePath){
        Path path = Paths.get(filePath);
        // 判断文件是否存在
        if(Files.exists(path)){
            // 如果是 目录
            if(Files.isDirectory(path)){
                try(DirectoryStream<Path> stream = Files.newDirectoryStream(path);) {
                    for(Path entry : stream)
                        watchFilePathList.add(getCanonicalPath(entry));
                } catch (IOException e) {
                    throw new TaildirException(" Failed to get the contents of the directory "+path.toString(),e);
                }
            }else {
            // 如何不是目录
                watchFilePathList.add(getCanonicalPath(path));
            }
        }else {
            throw new TaildirException(" This is not a directory or file ");
        }
    }
    // 用来 根据 Path 来 返回文件的 路径
    public String getCanonicalPath(Path path){
        try {
            return path.toFile().getCanonicalPath();
        }catch (IOException e){
            throw new TaildirException(" Failed to get file path "+path.toString(),e);
        }
    }


    // 用来 初始化 当前监听文件的信息
    public Map<String, TaildirFile> getWatchFilesMap(){
        Map<String, TaildirFile> filesMap = this.watchFilePathList.stream().collect(Collectors.toMap(x -> x, TaildirFile::new));
        return getUpdateWatchFilesMap(filesMap);
    }
    public Map<String, TaildirFile> getUpdateWatchFilesMap(Map<String, TaildirFile> filesMap){
        return getUpdateWatchFilesMap(filesMap,taildirFileMatcher.getPositionInfoMap());
    }
    // 传入坐标信息 并更新文件  监听列表
    public Map<String, TaildirFile> getUpdateWatchFilesMap(Map<String, TaildirFile> filesMap,Map<String, String> pointerMap){
        filesMap.forEach((k,v)->{
            if(pointerMap.containsKey(k)){
                long pointer = Long.parseLong(pointerMap.get(k));
                if(v.getFilePointer()<pointer)
                    v.setFilePointer(pointer);
            }
        });
        return filesMap;
    }



    public void writePointer(Map<String, TaildirFile> filesMap){
        taildirFileMatcher.updatePositionFileInfo(filesMap);
    }






}
