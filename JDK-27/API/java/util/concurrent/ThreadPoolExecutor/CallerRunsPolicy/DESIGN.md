# java.util.concurrent.ThreadPoolExecutor$CallerRunsPolicy 设计案

## 1. API 功能
CallerRunsPolicy 拒绝策略：`rejectedExecution` 在调用线程直接运行任务。

## 2. Java 用户如何使用
线程池饱和时由提交线程自执行，实现背压降级。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy`；`test/jdk/java/util/concurrent/ThreadPoolExecutor/` 覆盖。

## 4. 测试用例设计 + 自动化实现 + 易错规范细节 + 优先级
实现：`CallerRunsPolicyTest.java`（14 断言；深化前 5）。

### 深化前场景（保留全部断言）
白盒 class/继承/构造器 + 直接调用 rejectedExecution 任务在调用线程运行（既有）。

### 深化新增场景

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 饱和驱动反压 | 池满后 execute **同步在 main 线程执行：execute 返回前 ran==true 且线程名==main**（无需等待，天然确定性） | P0 |
| 已 shutdown 执行器 | isShutdown 检查 → 任务不运行、不抛异常（静默丢弃） | P0 |
| 独立可用/toString | implements 接口、toString 非空 | P2 |

易错规范细节：
1. execute() 返回即证明同步执行（CallerRuns 在返回前 run()），不要 sleep 等待。
2. shutdown 分支实测为静默丢弃而非回退执行，先探测再断言。

## 版本差异（vs JDK-8）
行为一致（实测两侧相同）。
