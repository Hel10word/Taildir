package com.zbf.boraydata;

import com.boraydata.taildir.TaildirFileSource;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** 用来测试 TaildirFile 的可用性
 * @author bufan
 * @data 2021/8/3
 */
public class TaildirFileSourceTest {
    // 创建一个 Source 实例
    public TaildirFileSource taildirFileSource = new TaildirFileSource();

    @Test
    public void test() {
        // 传入 相关配置
        taildirFileSource.open("./test.txt");
        // 开启定时任务 实时监听   日志文件
        ScheduledExecutorService logWatch = Executors.newSingleThreadScheduledExecutor();
        logWatch.scheduleAtFixedRate(()->{
            if(taildirFileSource.hashNext())
                System.out.println(taildirFileSource.next());
        }, 0, 1000, TimeUnit.MILLISECONDS);
        while (true){}
    }
}
