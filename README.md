# Taildir

​        最近在看 Flume 框架，深入了解 [Flume][Flume] 框架后对他的历史有所了解，它最开始是由Cloudera软件公司提供的一个高可用的，高可靠的，分布式的海量日志采集、聚合和传输的系统，后于2009年被捐赠了Apache软件基金会，为Hadoop相关组件之一。Apache的项目最开始都会经过一段时间的孵化，等到满足Apache的一系列质量后才可以毕业，从中毕业的项目要么成为顶级项目，或者称为其他顶级项目的子项目，每个顶级项目都会有独立的委员会来管理，随着近几年Flume的不断完善，内部的组件不断的丰富，使其便利性得到很大的改善，逐渐成为了[Apache的顶级项目][Apache Project]之一，[Github上可以查看其源码][Flume Github]。

​        现在所谓的大数据分析，大多数都是基于[OLAP][OLAP]类型的系统的；而我们常用的数据都是基于[OLTP][OLTP]的系统，基本上是对一些结构性比较强的数据表进行增删改查等操作，而这类系统的数据并不适合数据的分析与提炼。

​        举个例子，公司有一个商城系统，有下面三张表：

| 商品表 | 商品名       | 商品价格           | 商品销量       |
| ---------- | ------------ | ------------------ | ------------ |
| **会员表** | **会员名**   | **会员等级**       | **入会时间** |
| **活动表** | **活动形式** | **参与活动的商品** | **活动日期** |

​       可能单看商品表，能知道**哪个商品最便宜**；单看会员表，能知道**哪个等级的会员数量最多**。单看活动表，能知道**什么商品经常参与活动**。但是我们很难知道**开设活动与会员入会之间的联系**，可能通过查询能知道哪些活动带来的新会员最多；也不知道**不同等级的会员，经常购买什么商品**；我们也不知道**某一商品价格的历史变动**，当然这也跟大多数OLTP系统存储的只是当前时刻的信息有关，不过总的来说基于OLTP系统的数据分析只能利用有一定关联的字段来分析，毫无关联的字段是没法联系到一起分析的。

​        与此不同的是，OLAP类型中的数据是以列为单位的，可以单看商品名这一列，将其与活动表的活动形式关系起来，在大量的数据支撑下，寻找他们之间的规律，得到相关结论。因为每个字段间是没有任何关系的，只是对单纯的数据进行分析。如果公司体量较大的话，可以结合多个系统的数据来进行分析。例如可以知道在什么天气环境下，卖什么商品，能够让高等级的会员购买率上升。亦可以知道什么样的活动信息在什么样的时间段能让线上用户停留时间最长或点击次数最多。诸如此类，这便是当代**大数据的应用场景之一**，但是这些数据通常都是保存在结构化的数据库中，还有些数据的历史信息或用户的事件信息是存在系统日志或者数据库日志里。因此Flume 的作用就在于此，将各个地方的资源信息收集起来，供上层OLAP系统进行解析与分析。

​        接下来介绍Flume 的使用，它有三个主要组件（Source、Channel、Sink），Source就是用来从各个地方收集数据（例如某些日志路径、或者从日志服务器的端口、或则从消息队列中获取、序列化文件中获取、Http请求中获取。。。。）。然后将不同渠道收集的数据封装为统一的对象放入Channel中（可以理解为一个管道，或者消息队列，主要用来收集各个Source中的消息）。使用配置中指定的Sink与Channel对接，使收集的数据能接入到其他平台上（Sink可以将数据接入到HDFS中、还可以接入到Hive、存储为本地文件、发送到某端口、写入HBase。。。。），Source与Channel都是可以多对多的，但是一个Sink只能对接一个Channel的数据，而一个Channel可以同时对接多个Sink。通过一些配置文件，来声明一套规则，将上述的三个组件组装运用起来，这便是一个Agent。Flume就通过加载Agent 的配置，来运行相关的组件，完场数据的采集与转发等工作。

​       可以在[用户手册][Flume User Guide]中查看各个组件的功能与配置信息。在 Exec Source 模块下，Flume 给出了一些警告与建议，说 Source 端是并不知道 Channel 满了，或者故障的，他只会按照要求一直收集数据，这样当 Channel 恢复后，会有一段时间内的数据消失，所以为了提高可用性，官方推荐使用：Spooling Directory Source，Taildir Source  ，基于 Flume SDK 实现自定义的 Source。

​       我看了看Spooling Directory Source 的介绍，他是监控一个目录，同步目录中的新文件到Channel,被同步完的文件可被立即删除或被打上标记。适合用于同步新文件，但不适合对实时追加日志的文件进行监听并同步。然后又看了看[基于 Flume 的 SDK][Flume Dev Guide]，需要添加 `flume-ng-core`的依赖，然后实现相应的接口，最后打包放入Flume的目录中，在Agent中指定自己编写的Jar即可实现自定义的功能，[可以参考][Flume SDK 相关例子]。

​       但是这些都不是我想要的，这两者都需要依赖于Flume框架，而我只想实现其中的功能，于是直接上源码，在阅读了 `flume-taildir-source` 的源码后，我算是清楚实现原理了，它底层利用了Java的 [RandomAccessFile][RandomAccessFile] 类，通过指针从指定位置读取文件，每次访问文件后会记录一下当前的坐标，这样系统崩溃重启后，依然能加载Json文件中的坐标信息，来从指定位置读取文件。

​       于是根据自己的设计与实现，有了现在这个 Taildir 项目，主要是实现了对文件的断点续传与尾跟新功能。



- `TaildirFile` 类：包装了 java 的 RandomAccessFile 对象，主要处理文件的创建、从指定位置 读取文件、读取一行信息等文件相关的基本操作
- `TaildirMatcher` 类：用来从本地路径中  创建、读取、写入 文件的坐标信息  
- `ReliableTaildir` 类：用来 根据 目录路径 或 文件路径 来加载需要监听的文件 并根据 本地的坐标信息 来更新 当前监的文件 ，使其能接着上次坐标位置开始读取
- `TaildirFileSource` 类：用来实现 Source 接口  并实现接口中的 相关功能  主要是 通过 `taildir.ReliableTaildir` 提供的相关信息 来操作

用例代码：

```java
public void test() {
    // 传入 文件路径
    taildirFileSource.open("./test.txt");
    // 开启定时任务 实时监听   日志文件
    ScheduledExecutorService logWatch = Executors.newSingleThreadScheduledExecutor();
    logWatch.scheduleAtFixedRate(()->{
        if(taildirFileSource.hashNext())
        	System.out.println(taildirFileSource.next());
    }, 0, 1000, TimeUnit.MILLISECONDS);
    while (true){}
}
```

该示例在 test 包中，通过一个定时任务，隔一秒来刷新一下文件的信息，并将更新的内容打印到控制台中，然后还可以定一个定时任务，隔一段时间，将 文件的坐标信息 写入到本地，这样便实现了 Flume 中 TaildirSource 的基本功能。













---

[Flume]:http://flume.apache.org/
[Apache Project]:https://projects.apache.org/project.html?flume
[Flume Github]:https://github.com/apache/flume
[OLAP]:https://en.wikipedia.org/wiki/Online_analytical_processing
[OLTP]:https://en.wikipedia.org/wiki/Online_transaction_processing
[Flume User Guide]:http://flume.apache.org/releases/content/1.9.0/FlumeUserGuide.html
[Flume User taildir-source]:http://flume.apache.org/releases/content/1.9.0/FlumeUserGuide.html#taildir-source

[Flume Dev Guide]:http://flume.apache.org/releases/content/1.9.0/FlumeDeveloperGuide.html
[Flume SDK 相关例子]:https://www.cnblogs.com/jhxxb/p/11582804.html
[RandomAccessFile]:https://docs.oracle.com/javase/8/docs/api/?xd_co_f=47c934d9-e663-4eba-819c-b726fc2d0847
