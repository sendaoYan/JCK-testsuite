# java.util.concurrent.ForkJoinWorkerThread 设计案（JDK-8）

## 1. API 功能
ForkJoinPool 的工作线程，继承 `Thread`。构造器 `protected ForkJoinWorkerThread(ForkJoinPool)`；公开 `getPool()`/`getPoolIndex()`；`run()`；生命周期钩子 `protected onStart()`/`protected onTermination(Throwable)`（**8u 已回移植 JDK-9 钩子并在 8u372 实测被调用**）。JDK-8 缺少实例方法 `getQueuedTaskCount()`（JDK-9+ 独有）。

## 2. Java 用户如何使用
- 通过 `ForkJoinPool.ForkJoinWorkerThreadFactory.newThread(pool)` 定制：命名前缀、UEH、daemon、子类覆写 onStart/onTermination 做指标埋点。
- `getPoolIndex()` 为池内 worker 索引（构造时分配，>=0）。
- 不直接 start()：由池在需要时启动。

## 3. 开源代码实际用法调研
- Netty/Elasticsearch 等通过自定义工厂给 FJP worker 加监控名与 UncaughtExceptionHandler。
- JDK 自带测试：`tck/ForkJoinPoolTest.java` 用 DefaultFactory 与自定义工厂断言 worker 命名/守护位。

## 4. 测试用例设计 + 自动化实现
场景（24 断言）：
1. 白盒：类/extends Thread/protected 构造器/getPool/getPoolIndex/run 存在；**getQueuedTaskCount 缺席（JDK-8 差异断言）**；**onStart/onTermination 存在（8u372 回移植）**。
2. 工厂创建未启动 worker：ForkJoinPool- 名前缀、daemon、getPoolIndex>=0、绑定池。
3. 运行中身份：pool.invoke 的 task 内 getPool==pool、daemon、默认名前缀、poolIndex>=0。
4. 自定义命名 worker：Cw- 前缀贯穿到 currentThread().getName()。
5. 生命周期钩子：onStart>=1（执行任务后）；shutdownNow+awaitTermination 后 onTermination>=1。

易错规范细节：身份断言只走自定义池 `pool.invoke`（确定性在 worker 上执行）；钩子计数用 AtomicInteger；池 finally shutdownNow。

实现：`ForkJoinWorkerThreadTest.java`（24 断言）。优先级：身份/钩子契约 > 命名 > 差异断言。
