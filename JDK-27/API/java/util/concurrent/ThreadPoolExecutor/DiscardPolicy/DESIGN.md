# java.util.concurrent.ThreadPoolExecutor$DiscardPolicy 设计案

## 1. API 功能
DiscardPolicy 拒绝策略：`rejectedExecution` 静默丢弃任务，不抛异常不执行。

## 2. Java 用户如何使用
线程池饱和时直接丢弃新任务（谨慎使用）。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ThreadPoolExecutor.DiscardPolicy`；`test/jdk/java/util/concurrent/ThreadPoolExecutor/` 覆盖。

## 4. 测试用例设计 + 自动化实现 + 易错规范细节 + 优先级
实现：`DiscardPolicyTest.java`（15 断言；深化前 5）。

### 深化前场景（保留全部断言）
白盒 class/继承/构造器 + 直接调用 rejectedExecution 不抛（既有）。

### 深化新增场景

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 饱和驱动 | execute 正常返回无异常；队列长度不变（未入队）；**池终止后计数仍 0（从未执行——终态不可能性证明）** | P0 |
| 已 shutdown 执行器 | 同样静默、不运行 | P1 |
| 独立可用/toString | implements 接口、toString 非空 | P2 |

易错规范细节：
1. "未执行"必须在 awaitTermination 之后断言（终止后任务永不可能执行，才是确定性证明）。
2. 不得用"等待 N 秒后计数为 0"（弱证明）。

## 版本差异（vs JDK-8）
行为一致（实测两侧相同）。
