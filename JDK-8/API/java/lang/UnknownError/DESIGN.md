# UnknownError — 设计方案

## 1. API 功能
`java.lang.UnknownError` 继承自 `VirtualMachineError`，表示未知严重异常（"Thrown when an unknown but serious exception has occurred in the Java Virtual Machine"）。
JDK-8 javap 构造器面（仅 2 个 public）：`()`、`(String)`——无 cause 形态构造器。
Serializable（经 Error），`serialVersionUID = 2524784860676771849L`（ObjectStreamClass.lookup 实测锁定）。
继承 Throwable 通用契约。

## 2. Java 用户如何使用
作为家族兜底的最细分支：
```java
catch (VirtualMachineError v) { if (v instanceof UnknownError) { /* 未知严重故障：记录并终止 */ } }
```

## 3. 开源代码实际用法调研
极罕见；仅作 JVM 无法归类严重故障的语义槽位。无自有成员方法（javap 确认），行为全部来自 Throwable 契约。

## 4. 测试用例设计 + 易错规范细节
- 白盒：恰 2 构造器且断言无 cause 形态；直系父 VirtualMachineError；suid 锁定。
- 构造链矩阵：no-arg 双 null、toString `"java.lang.UnknownError"`（无冒号）/`"java.lang.UnknownError: m"` 逐字/空消息保留 `": "`；等消息两实例不 equals。
- cause/suppressed/栈迹/printStackTrace/序列化：家族模板全矩阵（initCause(null) 消耗一次写；Suppressed 行 tab 前缀；序列化后 cause 按类+消息断言）。
- 易错：勿照抄 InternalError 的 4 构造器假设；本类与 OOM/SOE 同为 2 构造器形态。
