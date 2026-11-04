# java.util.concurrent.Future$State 设计案

## 1. API 功能
Future 状态枚举：`RUNNING`/`SUCCESS`/`FAILED`/`CANCELLED`。`Future.state()` 报告当前状态。

## 2. Java 用户如何使用
`future.state()` 判断任务未完成/成功/失败/已取消，避免阻塞式 `get()`。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.Future.state()` 默认实现与 `CompletableFuture.state()` 覆盖；`test/jdk/java/util/concurrent/Future/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景（深化批次追加枚举内蕴契约）：
1. 白盒：`Class.forName` 断言 enum + values/valueOf 方法签名。
2. 黑盒：四值存在性 + 四种 future 的 state() 映射（RUNNING/SUCCESS/FAILED/CANCELLED）。
3. 枚举内蕴契约：**声明序锁定 RUNNING(0)/SUCCESS(1)/FAILED(2)/CANCELLED(3)**（ordinal 断言）；name() 四值精确；valueOf round-trip + Enum.valueOf；自然序==ordinal 序（Comparable）；单例恒等（valueOf 与常量 ==）；getDeclaringClass；values() 返回防御性拷贝（改首元素不影响再次取值）；valueOf("bogus") → IllegalArgumentException；valueOf(null) → NullPointerException；toString==name。

实现：`StateTest.java`（`testWhiteBox` + `testBlackBox` + `testEnumContract`，PASS 36 断言）。
