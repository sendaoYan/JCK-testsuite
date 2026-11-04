# java.util.concurrent.ExecutorService 设计案（JDK-8）

## 1. API 功能
接口，JDK-8 版仅扩展 `Executor`（**不含 AutoCloseable/close()——JDK-19 才加入，版本差异**）。方法：生命周期 shutdown/shutdownNow/isShutdown/isTerminated/awaitTermination；submit 三变体；invokeAll/invokeAny（无/有超时）。

## 2. Java 用户如何使用 + 开源用法调研
面向接口编程：`ExecutorService es = ...; es.submit(...).get(); finally es.shutdown(); es.awaitTermination(...)`。开源调研：几乎所有并发框架以该接口为注入点（Spring `@Async`、CompletableFuture 默认执行器、Netty 快速返回）。

## 3. 参考来源
Oracle javadoc（Java 8）；OpenJDK 测试；双侧 javap（JDK-27 侧 extends AutoCloseable + default close）。

## 4. 测试用例设计 + 易错规范细节 + 优先级表
实现：`ExecutorServiceTest.java`（41 断言；深化前 2）。

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 接口方法面 | extends Executor、8 个抽象方法存在；**JDK-8 断言不扩展 AutoCloseable 且无 close()（版本差异）** | P0 |
| 行为经 TPE 驱动 | instanceof Executor、submit 三变体值精确、execute 经接口执行 | P0 |
| Future 契约 | get(100ms)→TimeoutException（不影响 isDone）、cancel(true)→isCancelled+isDone、get→CancellationException | P0 |
| invokeAll/invokeAny 经接口 | 结果按序 / 阻塞+立即成功→返回成功值 | P0 |
| shutdown 后拒绝 | execute 抛 REE 消息含 "rejected from" | P0 |
| shutdownNow | 返回滞留任务列表（含标记任务）、运行任务被中断 | P0 |
| awaitTermination | 未 shutdown 时（超时后）false、不改状态 | P1 |
| 异常透传 | 任务异常 get 抛 ExecutionException、cause 原样、done 未取消 | P1 |

易错：接口层不 new；JDK-8 侧绝不能引用 close()（编译不过）；TimeoutException/CancellationException 均为 unchecked 之外的类型需精确捕获。

## 版本差异（JDK-8 vs JDK-27）
JDK-19+ ExecutorService extends AutoCloseable 并有 default close()（shutdown+无限等待）；JDK-8 无。JDK-27 侧补测 close 幂等/等待在途任务/close 后 submit REE。
