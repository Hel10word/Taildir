package com.boraydata.taildir;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bufan
 * @data 2021/8/2
 */
public class TaildirFileSource {

    private ReliableTaildir reliableTaildir;
    private Map<String, TaildirFile> watchFilesMap;

    public void open(String config) {
        // 初始化 控制器
        this.reliableTaildir = new ReliableTaildir();
        // 加载监听日志
        this.reliableTaildir.watchFiles(config);
        // 根据 本地的  标签文件   更新   读取文件的位置
        this.watchFilesMap = reliableTaildir.getWatchFilesMap();

    }

    public void close() {
        // 关闭 集合中的  每个 文件
        for(String k : watchFilesMap.keySet())
            watchFilesMap.get(k).close();
        // 将文件  写入  本地配置
        reliableTaildir.writePointer(watchFilesMap);
    }

    public String next() {
        for(TaildirFile t : this.watchFilesMap.values())
            if (t.hasNext()) {
                return t.readLine();
            }
        return null;
    }

    public boolean hashNext() {
        for(TaildirFile t : this.watchFilesMap.values())
            if (t.hasNext())
                return true;
        return false;
    }

    public Iterator<String> iterator() {

        return this.watchFilesMap.entrySet()
                .stream().map(e -> e.getValue().readAllLine())
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
                .iterator();

    }
}
