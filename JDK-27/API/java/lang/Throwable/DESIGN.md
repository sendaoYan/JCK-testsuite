# Throwable — 设计方案

## 1. API 功能
`java.lang.Throwable`（可子类化，`Serializable`）是所有错误/异常的超类：
- 构造：`()`、`(String)`、`(String, Throwable)`、`(Throwable)`
- 诊断：`getMessage/getLocalizedMessage/getCause/toString`
- 栈：`getStackTrace/setStackTrace/fillInStackTrace/printStackTrace`
- 抑制异常：`addSuppressed/getSuppressed`；原因初始化：`initCause`（仅一次）

## 2. Java 用户如何使用
- 自定义异常继承 `Throwable`（或 `Exception`/`RuntimeException`），抛出后 `catch` 诊断。
```java
Throwable t = new Exception("boom");
t.addSuppressed(new Exception("detail"));
t.printStackTrace();       // 打印栈 + 抑制异常
```

## 3. 开源代码实际用法调研
- `try-with-resources` 自动把 `close()` 异常作为抑制异常 `addSuppressed` 到主异常。
- 所有 JDK 异常（`Integer.parseInt` 的 `NumberFormatException` 等）都继承自 `Throwable`。
- JDK `test/jdk/java/lang/Throwable` 验证 `initCause` 仅一次、`setStackTrace` 克隆、`addSuppressed` 校验。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Throwable")` 验证父类 `Object`、非 final、`Serializable`、四个构造器。
黑盒：四构造器消息/原因；`initCause` 仅一次（二次/已初始化抛 `IllegalStateException`、自引用抛 `IllegalArgumentException`）；`addSuppressed/getSuppressed` 校验与克隆；`setStackTrace/getStackTrace/fillInStackTrace`；`toString` 格式；`printStackTrace(PrintStream)` 写入内存流。
对应 `<Simple>Test.java` 黑盒部分。

## 契约深化（2026-11，lcO1 批）

双侧断言 44/40 → 203/203。新增核心闭环：

- **TWR 抑制真闭环**：自造 `FailingClose`（close 抛 E2）+ body 抛 E1 → catch 的 primary 与 E1 同一实例、`getSuppressed()[0]` 与 E2 同一实例；body 正常时 close 异常直接传播且无抑制；双失败资源（x=e3, y=e2）抑制顺序 == 逆关闭顺序 [e2, e3]；被抑制的 close 异常自带的 suppressed 列表原样保留；body 内已捕获再重抛的实例照样挂抑制。
- **TWR 关闭次序**：双资源/三资源 `CloseOrder` 记录 [body, B, A] / [body, C, B, A]（逆声明序）；嵌套 TWR 内层资源先于外层 body 关闭。
- **fillInStackTrace 精确帧**：`getStackTrace()[0]` 的 className/methodName 逐字（ThrowableTest/方法名）；setStackTrace 之后的 fillInStackTrace 会丢弃显式栈重新锚定；清空后 fill 恢复真实帧。
- **cause 契约**：`initCause(this)` → IAE "Self-causation not permitted"（逐字，双侧一致）；二次 initCause → ISE（消息 startsWith "Can't overwrite cause with"）；`initCause(null)` 合法且 cause 保持 null；双异常循环 cause 链（a↔b）可构造、导航安全。
- **suppressed 契约**：`addSuppressed(null)` → NPE "Cannot suppress a null exception."、self → IAE "Self-suppression not permitted"（均逐字双侧一致）；插入序保持；防御拷贝；与 cause 独立共存。
- **消息/toString**：`(String)null` → getMessage null 且 toString 无冒号；空消息 toString 保留 `": "`；`toString`/`printStackTrace` 均走 `getLocalizedMessage()`（override getMessage 时两者联动，override getLocalizedMessage 只影响 toString）。
- **printStackTrace 全格式**：首行 == toString、`\tat ` 帧、"Suppressed: " 段在 "Caused by: " 段之前、无 cause/suppressed 时两段均不出现、PrintWriter 重载、无参版本经 System.err 捕获验证（setErr try/finally 还原）。
- **序列化**：显式 setStackTrace 的帧、suppressed-of-suppressed 嵌套结构、cause 链均 round-trip 保持；SUID -3042686055658047285L 经 ObjectStreamClass 锁定（双侧一致）。
- **protected 4 参构造器组合矩阵**：(true,false)/(false,true) 补全——抑制开关与栈可写开关正交。
- StackTraceElement equals/hashCode/toString("C.m(F.java:1)") 契约；rethrow 不重填栈、在 helper 内 fillInStackTrace 可把顶帧挪到 helper。
