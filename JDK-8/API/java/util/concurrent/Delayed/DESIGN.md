# java.util.concurrent.Delayed 设计案

## 1. API 功能
接口 `Delayed extends Comparable<Delayed>`：单方法 `long getDelay(TimeUnit unit)` 返回剩余延迟（到期 ≤0）。compareTo 约定按到期时刻排序，是 DelayQueue / ScheduledThreadPoolExecutor 调度序的基础。

## 2. Java 用户如何使用（含开源实际用法调研）
- 典型用法：缓存 TTL 条目、延迟任务（`DelayQueue.add(delayedItem)`）、重试退避队列：持 `deadline = nanoTime + delay`，getDelay 用 `unit.convert(deadline - now, NANOSECONDS)`。
- 开源用法：Netty 定时握手超时（HashedWheelTimer 的 Timeout 概念同构）、OkHttp 连接池 keep-alive 清理（SynchronousQueue+delay 语义）、Kafka/RocketMQ 延迟消息的抽象原型。
- DelayQueue 配合的行为放 DelayQueue 批次，此处只锁接口换算与比较契约。

## 3. 参考来源
- Oracle javadoc：Delayed（getDelay/compareTo 语义、与 DelayQueue 的关系）。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/DelayQueue/`（自定义 Delayed 驱动）。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/DelayQueue/`。

## 4. 测试用例设计 + 易错规范细节
实现：`DelayedTest.java`（PASS 31 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 白盒结构 | isInterface；唯一直接父接口 Comparable；单抽象方法 getDelay；compareTo 继承自 Comparable 未重声明 | P0 |
| 单位换算矩阵 | 固定 1 天 = 86400e9 ns 对全部 7 个单位精确锁定（DAYS=1 … NANOSECONDS=86400000000000） | P0 |
| 截断语义 | 1_999_999_999 ns → SECONDS=1（floor 非 round）、MILLIS=1999 | P0 |
| 0/负延迟 | 0 保持 0；-2s → SECONDS=-2、NANOSECONDS<0 | P0 |
| compareTo 矩阵 | 反身 0/等值 0/小于/大于；signum 反对称；三分量传递；极值 Long.MAX_VALUE 序 | P0 |
| compareTo(null) | NPE（Comparable 契约） | P1 |
| 易错细节 | 换算是 **向下取整**；不同实现的 deadline 基准不同（nanoTime 单调钟），测试用固定纳秒实现避免墙钟 | P1 |
