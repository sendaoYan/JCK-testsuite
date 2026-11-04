# java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy 设计案（JDK-8）

## 1. API 功能
拒绝策略：`if (!e.isShutdown()) { e.getQueue().poll(); e.execute(r); }` —— 丢弃队首最老任务后重试提交被拒任务；队列已空时 poll 得 null，被拒任务经重试入队（实际执行）。

## 2. Java 用户如何使用 + 开源用法调研
优先保新数据的场景（行情、心跳）。开源调研：OpenJDK 测试覆盖 discard 路径；各类带界队列中间件用同思路。

## 3. 参考来源
Oracle javadoc（Java 8）；OpenJDK 8u 源码；双侧实测空队列重试行为。

## 4. 测试用例设计 + 易错规范细节 + 优先级表
实现：`DiscardOldestPolicyTest.java`（18 断言；深化前 1）。

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 直接调用 | 不抛异常（保留原有） | P0 |
| 满队列场景 | 标记任务：最老任务出队（!contains）、被拒任务入队（contains）、size 不变；释放后老任务计数 0、新任务恰执行 1 次 | P0 |
| 空队列场景 | 预启动空闲 core 线程后直接调用：poll 得 null → 重试 execute → 任务实际执行（latch 有界，实测记录行为） | P1 |
| 已 shutdown 执行器 | isShutdown 检查 → 不丢弃、不重试、不抛异常 | P0 |
| 独立可用/toString | implements 接口、toString 非空 | P2 |

易错：满队列场景必须先 latch 确认工作线程已占满再入队标记任务，否则扩容语义会吞掉拒绝路径。
