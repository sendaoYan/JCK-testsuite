# java.util.concurrent.ThreadPoolExecutor$AbortPolicy 设计案

## 1. API 功能
AbortPolicy 拒绝策略：`rejectedExecution` 直接抛 `RejectedExecutionException`。

## 2. Java 用户如何使用
线程池饱和时默认拒绝并抛异常，调用方需处理。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ThreadPoolExecutor.AbortPolicy`（默认 handler）；`test/jdk/java/util/concurrent/ThreadPoolExecutor/` 覆盖。

## 4. 测试用例设计 + 自动化实现 + 易错规范细节 + 优先级
实现：`AbortPolicyTest.java`（13 断言；深化前 5）。

### 深化前场景（保留全部断言）
白盒 class/继承/构造器 + 直接调用 rejectedExecution 抛 REE（既有）。

### 深化新增场景

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 饱和驱动 | 1 工作线程阻塞（started-latch）+ ABQ(1) 占满后 execute → REE，**消息含 "rejected from" 与 "ThreadPoolExecutor"** | P0 |
| 已终止执行器 | 直接调用同样抛 REE 且消息含 "rejected from" | P1 |
| 独立可用 | 可 new 后作用于任意 TPE（implements RejectedExecutionHandler） | P1 |
| toString | 非空 | P2 |

易错规范细节：
1. 拒绝消息中任务 toString 因 lambda 实现而异，只断言 contains "rejected from"。
2. 饱和场景必须 latch 同步工作线程已启动后再填队，否则拒绝路径不确定。

## 版本差异（vs JDK-8）
行为一致；REE 消息两侧同构（均含 "rejected from" + 执行器 toString）。
