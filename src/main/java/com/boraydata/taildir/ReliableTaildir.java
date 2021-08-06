package com.boraydata.taildir;

import com.boraydata.taildir.exception.TaildirException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 用来根据  指定的文件 来解析日志 并  将文件坐标保存
 * @author bufan
 * @data 2021/7/27
 */
public class ReliableTaildir {
    private static final TaildirMatcher taildirFileMatcher = new TaildirMatcher();

     // 创建一个  需要监听的文件列表
     private List<String> watchFilePathList = new ArrayList<>();

    // 加载 需要监听的文件信息 至 List
    public void watchFiles(String filePath){
        watchFiles(new String[]{filePath});
    }
//    为了后续 拓展 支持 同时监听 多个文件及目录
    public void watchFiles(String[] filePath){
        for (String path:filePath)
            getFilesPath(path);
    }
    /**
     * 获取 文件的绝对路径
     * @Param filePath : 传入 一个路径 可以是 文件 或者 目录
     * @Return: void : 返回该目录中 的文件绝对路径
     */
    private void getFilesPath(String filePath){
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
    // 用来 根据 Path 来 返回文件的 绝对路径
    private String getCanonicalPath(Path path){
        try {
            return path.toFile().getCanonicalPath();
        }catch (IOException e){
            throw new TaildirException(" Failed to get file path "+path.toString(),e);
        }
    }



    // 获取 当前 配置文件的信息
    public Map<String,String> getPointerMap(){
        return taildirFileMatcher.getPositionInfoMap();
    }

    // 获取   需要监听的文件信息
    public Map<String,TaildirFile> getWatchFilesMap(){
        // 用来 初始化 当前监听文件的信息   默认指针起始位置 都为0
        Map<String,TaildirFile> filesMap = this.watchFilePathList.stream().collect(Collectors.toMap(x -> x, TaildirFile::new));
        return getUpdateWatchFilesMap(filesMap);
    }

    // 传入坐标加载信息  并更新当前文件  监听列表
    private Map<String,TaildirFile> getUpdateWatchFilesMap(Map<String,TaildirFile> filesMap){
        return getUpdateWatchFilesMap(filesMap,getPointerMap());
    }
    /**
     *
     * @Param filesMap : 当前通过 传入目录 加载的文件 以及其 文件对象  默认指针起始从 0 开始
     * @Param pointerMap : 获取 本地存储的 文件坐标信息
     * @Return: Map : <String : ,TaildirFile> : 根据 本地存储 的 坐标位置  更新 监听文件列表
     */
    private Map<String,TaildirFile> getUpdateWatchFilesMap(Map<String, TaildirFile> filesMap, Map<String, String> pointerMap){
        if(pointerMap.size()<=0)
            return filesMap;
        filesMap.forEach((k,v)->{
            if(pointerMap.containsKey(k)){
                long pointer = Long.parseLong(pointerMap.get(k));
                if(v.getFilePointer() < pointer && pointer < v.getFileLength())
                    v.setFilePointer(pointer);
            }
        });
        return filesMap;
    }

    // 将当前  读取文件的信息  写入到本地文件配置中
    public void writePointer(Map<String,TaildirFile> filesMap){
        taildirFileMatcher.updatePositionFileInfo(filesMap);
    }

}
