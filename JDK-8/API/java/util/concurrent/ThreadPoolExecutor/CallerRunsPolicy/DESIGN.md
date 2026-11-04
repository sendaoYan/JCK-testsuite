# java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy 设计案（JDK-8）

## 1. API 功能
拒绝策略：`if (!e.isShutdown()) r.run();` —— 在提交者线程同步运行被拒任务；对已 shutdown 的池静默丢弃。

## 2. Java 用户如何使用 + 开源用法调研
天然背压：池满时调用线程自己执行，减慢提交速率。开源调研：OpenJDK 测试覆盖；Kafka/Netty 提交路径常见用法（注意提交线程阻塞副作用）。

## 3. 参考来源
Oracle javadoc（Java 8）；OpenJDK 8u 源码；双侧实测。

## 4. 测试用例设计 + 易错规范细节 + 优先级表
实现：`CallerRunsPolicyTest.java`（10 断言；深化前 1）。

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 直接调用 | 任务在调用线程运行（保留原有） | P0 |
| 饱和驱动 | 池满后 execute 同步在 main 线程执行：execute 返回前 ran==true 且线程名==main（无需等待，天然确定性） | P0 |
| 已 shutdown 执行器 | isShutdown 检查 → 任务不运行、不抛异常 | P0 |
| 独立可用/toString | implements 接口、toString 非空 | P2 |

易错：execute() 返回即证明同步执行（CallerRuns 在返回前 run()），不要 sleep 等待。
