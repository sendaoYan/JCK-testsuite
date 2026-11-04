# java.util.concurrent.ThreadFactory 设计案

## 1. API 功能
函数式接口（按形态，无 @FunctionalInterface 注解——实测）：单方法 `Thread newThread(Runnable r)`。把线程创建（命名/优先级/daemon/组）从 Executor 解耦；返回 null 表示工厂拒绝创建。

## 2. Java 用户如何使用（含开源实际用法调研）
- 典型用法：`Executors.newFixedThreadPool(n, threadFactory)`；自定义命名工厂用于线程 dump 排障；daemon 工厂用于不阻退出。
- 开源用法：Netty `DefaultThreadFactory`（前缀+递增编号+优先级）、Guava `ThreadFactoryBuilder`（命名格式/daemon/UncaughtExceptionHandler）、Spring `CustomizableThreadFactoryBean`——均以本接口为产物类型。
- `Executors.defaultThreadFactory()` 产物契约：非 daemon、NORM_PRIORITY、未启动、名字 `pool-N-thread-M`（全局静态 poolNumber 递增）。

## 3. 参考来源
- Oracle javadoc：ThreadFactory；Executors.defaultThreadFactory。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/ThreadPoolExecutor/`（工厂注入路径）。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/Thread/`（线程工厂相关）。

## 4. 测试用例设计 + 易错规范细节
实现：`ThreadFactoryTest.java`（PASS 23 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 白盒结构 | isInterface；唯一声明方法 newThread→Thread；无 @FunctionalInterface（实测） | P0 |
| Runnable 转发 | 工厂产物 start 后 5s latch 内跑完、且跑在工厂命名的线程里 | P0 |
| 配置保持 | 自定义 name/daemon 在产物上精确生效；产物 state==NEW 未启动 | P0 |
| defaultThreadFactory | daemon=false、priority==NORM_PRIORITY、state==NEW、名字 matches `pool-\d+-thread-\d+`；同工厂线程号递增（解析名字数字差 1） | P0 |
| newThread(null) | defaultThreadFactory 返回 Thread 不抛（**实测记录**，8/27 一致） | P2 |
| 工厂组合 | 装饰器工厂（计数 + 委托 base）产出数正确 | P1 |
| 易错细节 | 产物不得自动 start；同 JVM 内 pool 序号取决于全局计数，只能用模式断言；测试线程均 daemon | P1 |
