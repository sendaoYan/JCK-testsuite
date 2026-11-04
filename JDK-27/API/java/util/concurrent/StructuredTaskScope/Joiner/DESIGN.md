# java.util.concurrent.StructuredTaskScope$Joiner 设计案

## 1. API 功能
结构化并发 Joiner 接口（预览）。`result()`/`timeout()` 聚合结果；静态工厂 `allSuccessfulOrThrow`/`anySuccessfulOrThrow`/`awaitAllSuccessfulOrThrow`/`allUntil`。

## 2. Java 用户如何使用
选择聚合策略：`allSuccessfulOrThrow` 全部成功、`anySuccessfulOrThrow` 任一成功、`allUntil` 满足谓词即返回。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.StructuredTaskScope.Joiner`（JEP 480）；`test/jdk/java/util/concurrent/structured/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：接口 + onFork/onComplete/result/timeout + 7 个静态工厂签名（含异常函数重载）。
2. 黑盒主干：四工厂非空 + anySuccessfulOrThrow 单任务。
3. 契约深化：自定义 RecordingJoiner（onFork 在 fork() 内同步调用并收到 Subtask、onComplete 每完成一次、
   join 返回 result() 计算值、回调返回 false 永不取消）（testCustomJoinerCallbacks）；
   allSuccessfulOrThrow(MyEx::new) 自定义异常函数 → join 直接抛 MyEx 且 cause 为原始异常
   （testCustomExceptionFunction）；工厂 Joiner 各路径（按提交序/失败 cause 恒等/allUntil 谓词恒假等全部完成）
   （testFactoryJoiners）；timeout 钩子两分支（返回值 → join 返回该值；抛 MyEx → join 直接抛）
   （testJoinerTimeoutHook）。

实现：`JoinerTest.java`（34 断言）。

## 关键规范细节（实测锁定）
- onFork 由 fork() **同步**调用；onComplete 在子任务完成时调用（join 返回前可见）。
- R_X 是 throws 界定（timeout()/result() 声明 throws R_X），不是返回类型。
- anySuccessfulOrThrow 全败 → cause 为**先提交者**的异常。
