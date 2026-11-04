# java.util.concurrent.ThreadPoolExecutor.DiscardPolicy 设计案（JDK-8）

## 1. API 功能
拒绝策略：`rejectedExecution` 为空实现——静默丢弃被拒任务，无异常无执行。

## 2. Java 用户如何使用 + 开源用法调研
可容忍丢任务的场景（日志、指标）。开源调研：OpenJDK 测试；Netty 事件池丢弃旧事件思路。

## 3. 参考来源
Oracle javadoc（Java 8）；双侧实测。

## 4. 测试用例设计 + 易错规范细节 + 优先级表
实现：`DiscardPolicyTest.java`（11 断言；深化前 1）。

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 直接调用 | 不抛异常（保留原有） | P0 |
| 饱和驱动 | execute 正常返回无异常；队列长度不变（未入队）；池终止后计数仍 0（从未执行） | P0 |
| 已 shutdown 执行器 | 同样静默、不运行 | P1 |
| 独立可用/toString | implements 接口、toString 非空 | P2 |

易错："未执行"必须在 awaitTermination 之后断言（终止后任务永不可能执行，才是确定性证明）。
