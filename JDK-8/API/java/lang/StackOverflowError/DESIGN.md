# StackOverflowError — 设计方案

## 1. API 功能
`java.lang.StackOverflowError` 继承自 `VirtualMachineError`，表示线程栈耗尽（"Thrown when a stack overflow occurs because an application recurses too deeply"）。
JDK-8 javap 构造器面（仅 2 个 public）：`()`、`(String)`——无 cause 形态构造器。
Serializable（经 Error），`serialVersionUID = 8609175038441759607L`（ObjectStreamClass.lookup 实测锁定）。
继承 Throwable 通用契约。

## 2. Java 用户如何使用
```java
try { deep(n); } catch (StackOverflowError soe) { /* 收缩深度、换栈重试；handler 内小分配安全 */ }
```
栈可复用：捕获后线程可再次深递归（再次 SOE 也可再捕获）。

## 3. 开源代码实际用法调研
递归解析器（JSON/XML）、深流管道用 catch-SOE 做深度上限兜底；JDK 测试里大量真触发回归。JVM 抛出时 message 未规定（HotSpot 为 null）。

## 4. 测试用例设计 + 易错规范细节
- **真触发闭环（招牌）**：每层 8 个跨调用存活局部变量的自递归 + 静态深度计数器；断言 instanceof/`getClass()==StackOverflowError.class`/深度 **> 1000 下限（不锁具体值）**/getStackTrace 非空/toString 类名前缀（message 未规定——实测 null 只断类型面）。
- handler 内 `new byte[100]` 成功——堆未耗尽，与 OOM 分界；二次触发同样捕获（栈可复用）；恢复后普通调用正常；真 SOE 按家族柄重抛按 VirtualMachineError 捕获同一对象。
- 相互（间接）递归 ping/pong 同样触发，深度 > 1000。
- 家族模板全矩阵：构造链值语义（2 构造器形态）/cause 一次写/自反 IAE/suppressed/栈迹/printStackTrace/序列化往返。
- 易错：深度随栈配置波动，只设下限；计数用静态字段（SOE 吞返回路径）；局部变量跨调用存活是防帧消除关键。
