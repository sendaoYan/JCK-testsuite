# InternalError — 设计方案

## 1. API 功能
`java.lang.InternalError` 继承自 `VirtualMachineError`，表示 JVM 内部错误（"Thrown to indicate that some unexpected internal error has occurred"）。
JDK-8 javap 构造器面（4 个全 public）：`()`、`(String)`、`(String, Throwable)`、`(Throwable)`。
Serializable（经 Error），`serialVersionUID = -9062593416125562365L`（ObjectStreamClass.lookup 实测锁定）。
继承 Throwable 通用契约（cause/suppressed/栈迹/序列化/printStackTrace）。

## 2. Java 用户如何使用
```java
try {
    throw new InternalError("unexpected state", cause);
} catch (VirtualMachineError v) {
    if (v instanceof InternalError) { /* 记录并快速失败 */ }
}
```
`(Throwable)` 形态便捷构造：message 自动取 `cause.toString()`。

## 3. 开源代码实际用法调研
主要由 JVM/反射机制抛出；用户侧罕见主动构造，测试里偶作"不可能分支"信号。无自有成员方法（javap 确认），行为全部来自 Throwable 契约。

## 4. 测试用例设计 + 易错规范细节
- 白盒：恰 4 构造器逐形态精确断言（参数类型+public）；直系父 VirtualMachineError；suid 锁定。
- 构造链矩阵：no-arg 双 null 且 toString 无尾冒号 `"java.lang.InternalError"`；`("m")` toString 逐字 `"java.lang.InternalError: m"`；空串消息保留 `": "`；`("m",null)` cause 容忍 null；`(cause)` message==cause.toString() 且同一对象；等消息两实例不 equals（恒等语义）。
- cause 契约：initCause 返回 this/一次写；二次 ISE；自反 IAE 精确消息 `"Self-causation not permitted"`；**构造器已设 cause 再 initCause 亦 ISE**。
- suppressed：默认空、保序、防御拷贝；栈迹：顶帧类/方法名精确、setStackTrace 回读值相等、防御拷贝、空数组合法。
- printStackTrace：首行逐字、`	Suppressed:`（tab 前缀）与 `Caused by:`（顶层无前缀）精确行；无参形态写 System.err（setErr + finally 还原）。
- 序列化往返：message/cause（类+消息，图拷贝）/suppressed/栈迹/toString 全保持。
- 易错：Suppressed 行有前导 tab、Caused by 顶层无前缀；序列化后 cause 非同一对象。
