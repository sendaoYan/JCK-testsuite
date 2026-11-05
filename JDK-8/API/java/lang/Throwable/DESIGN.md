# java.lang.Throwable 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「new 一个异常打印一下」的冒烟。重点补：白盒结构（父类 Object、
> 可子类化、Serializable、四公共构造器 + serialVersionUID 稳定值）、initCause 仅一次与自引用校验、
> addSuppressed/getSuppressed 校验 + 防御拷贝、setStackTrace null/元素 null 校验 + 防御拷贝、
> protected 4 参构造器（enableSuppression/writableStackTrace 语义）、序列化往返（message/cause/
> suppressed 保留）。断言不依赖 Locale/Charset/墙钟，headless 安全。

## 1. API 提供了哪些功能

- **构造器（4 公共 + 1 protected）**：`Throwable()`、`Throwable(String)`、`Throwable(String,Throwable)`、
  `Throwable(Throwable)`，以及 protected `Throwable(String,Throwable,boolean,boolean)`。
- **诊断**：`getMessage`/`getLocalizedMessage`/`getCause`/`toString`。
- **栈**：`getStackTrace`/`setStackTrace`/`fillInStackTrace`/`printStackTrace`(×3)。
- **抑制异常**：`addSuppressed`/`getSuppressed`；原因初始化：`initCause`（仅一次）。
- **序列化**：实现 `Serializable`，`serialVersionUID = -3042686055658047285L`（JDK 1.0.2 起稳定）。

## 2. Java 用户如何使用

- 自定义异常继承 `Throwable`（或 `Exception`/`RuntimeException`），抛出后 `catch` 诊断。
- `try-with-resources` 自动把 `close()` 异常 `addSuppressed` 到主异常。
- `initCause` 只能在 cause 尚未设置时调用一次；`addSuppressed` 不能为 null 或自身。
- `setStackTrace` 用于（框架层）重写异常栈；`fillInStackTrace` 记录当前栈。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Throwable.html`）：四公共构造器语义、initCause 仅一次、
  `addSuppressed`/`setStackTrace` 的 null/self 校验、protected 4 参构造器语义。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Throwable.java`：
  `serialVersionUID = -3042686055658047285L`、`stackTrace` 非 transient（序列化）、`backtrace` transient、
  `getSuppressed` 返回克隆、`getStackTrace` 返回克隆。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Throwable` + `~/git/jdk/test/jdk/java/lang/Throwable`。

## 4. 测试用例设计（映射到 ThrowableTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：父类 Object、非 final、Serializable、四公共构造器、serialVersionUID | testWhiteBox |
| P0 | 四公共构造器消息/原因语义 | testConstructors |
| P0 | initCause 仅一次（二次/已初始化→ISE、自引用→IAE） | testInitCause |
| P0 | addSuppressed(null→NPE/self→IAE)/getSuppressed 克隆 | testSuppressed |
| P0 | setStackTrace null→NPE/元素 null→NPE/克隆；fillInStackTrace 返回 this 且记录帧 | testStackTrace |
| P1 | getMessage/getLocalizedMessage/toString（含子类） | testToStringAndMessage |
| P1 | printStackTrace(PrintStream) 写内存流 | testPrintStackTrace |
| P0 | protected 4 参构造器：enableSuppression=false 忽略 addSuppressed、writableStackTrace=false 清空栈 | testBlackBox |
| P1 | 序列化往返保留 message/cause/suppressed | testSerialization |

## 关键规范细节（易错，已锁入断言）

- **`serialVersionUID = -3042686055658047285L`**（JDK 1.0.2 起稳定，跨 JDK 互操作）。
- **`initCause` 仅一次**：已设置 cause 后再 initCause（含构造器带 cause）→ `IllegalStateException`；
  自引用 `initCause(this)` → `IllegalArgumentException`。
- **`addSuppressed` 校验**：null → NPE，自身 → IAE；`getSuppressed` 返回防御拷贝（改返回值不影响内部）。
- **`setStackTrace` 校验**：null 数组或含 null 元素 → NPE；返回 `getStackTrace()` 是克隆。
- **protected 4 参构造器**：`enableSuppression=false` 时 `addSuppressed` 静默忽略；
  `writableStackTrace=false` 时 `setStackTrace` 后栈仍为空。
- **`(Throwable)` 构造器的 message == cause.toString()**。

## 注意事项

- 序列化断言仅用自定义 `Custom`/`Exception`，不依赖栈帧具体内容（栈内容序列化后可能为空，故只断言
  message/cause/suppressed）。
- headless 安全，不加 `@key`；printStackTrace 写内存流，不污染 stdout 断言。

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
