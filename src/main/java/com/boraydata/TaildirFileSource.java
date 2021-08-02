package com.boraydata;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/** 用来将 Matcher、TaildirFile 组合起来 实现日志的 爬取 与 解析
 * @author bufan
 * @data 2021/7/27
 */
public class TaildirFileSource {

    private int writePointerInitDelay = 5000;
    private String logFilePath = "";
    private Function function;
    private Map<String, TaildirFile> watchFilesMap;

    private ReliableTaildir reliableTaildir = new ReliableTaildir();


    public <T> void config(String path,Function<String,T> function,int time){
        this.logFilePath = path;
        this.function = function;
        this.writePointerInitDelay = time;
    }



    public void start(){
        reliableTaildir.watchFiles(logFilePath);
        this.watchFilesMap = reliableTaildir.getWatchFilesMap();

        // 设置 读取 日志的定时任务
        ScheduledExecutorService logWatch = Executors.newSingleThreadScheduledExecutor();
        logWatch.scheduleAtFixedRate(new WatchLogsRunnable(),0,writePointerInitDelay, TimeUnit.MILLISECONDS);

    }

    /**
     * 用来 更新 Position 文件
     */
    private class WatchLogsRunnable implements Runnable{
        @Override
        public void run() {
            watchLogs();
        }
        // 日志的 信息 存储在  watchFilesMap 中
        public void watchLogs(){
            reliableTaildir.getUpdateWatchFilesMap(watchFilesMap);
            for(String key : watchFilesMap.keySet()){
                TaildirFile val = watchFilesMap.get(key);
                val.setRaf(val.getFilePath(),val.getFilePointer());
                while (val.getFilePointer()<val.getFileLength()){
                    System.out.println(function.apply(val.readLine()));
                }
                val.close();
            }
            reliableTaildir.writePointer(watchFilesMap);

        }
    }


}
