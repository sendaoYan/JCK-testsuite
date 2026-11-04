# java.util.concurrent.ThreadPoolExecutor$DiscardOldestPolicy 设计案

## 1. API 功能
DiscardOldestPolicy 拒绝策略：`rejectedExecution` 丢弃队列中最老任务，再尝试提交新任务。

## 2. Java 用户如何使用
线程池饱和时牺牲最老排队任务以接纳新任务。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy`；`test/jdk/java/util/concurrent/ThreadPoolExecutor/` 覆盖。

## 4. 测试用例设计 + 自动化实现 + 易错规范细节 + 优先级
实现：`DiscardOldestPolicyTest.java`（22 断言；深化前 5）。

### 深化前场景（保留全部断言）
白盒 class/继承/构造器 + 直接调用 rejectedExecution 不抛（既有）。

### 深化新增场景

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 满队列场景 | 标记任务：**最老任务出队（!contains）、被拒任务入队（contains）、size 不变**；释放后老任务计数 0、新任务恰执行 1 次 | P0 |
| 空队列场景 | 预启动空闲 core 线程后直接调用：poll 得 null → 重试 execute → **任务实际执行**（latch 有界，实测记录行为） | P1 |
| 已 shutdown 执行器 | isShutdown 检查 → 不丢弃、不重试、不抛异常 | P0 |
| 独立可用/toString | implements 接口、toString 非空 | P2 |

易错规范细节：
1. 满队列场景必须先 latch 确认工作线程已占满再入队标记任务，否则扩容语义会吞掉拒绝路径。
2. "最老"由队列序决定：ABQ FIFO 下队首即最老；用不同标记任务区分身份。

## 版本差异（vs JDK-8）
行为一致（实测两侧相同）。
