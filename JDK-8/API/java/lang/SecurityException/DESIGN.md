# java.lang.SecurityException 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。4 构造器 + 边角（空消息、`(Throwable)null` 锁 cause 槽、镜像消息）、
> cause 语义全矩阵（二次 initCause ISE/self IAE/initCause(null) 各状态）、suppressed 顺序/身份/防御拷贝、
> stack trace 设置往返与 3 种 NPE、打印（PrintStream/Writer + Suppressed 段）、序列化 svuid、形态。

## 1. API 功能

- **类**：`java.lang.SecurityException`（继承 `RuntimeException`，非 final，4 公开构造器）。
- **构造器**：`()`、`(String)`、`(String, Throwable)`、`(Throwable)`。
- **无额外访问器**；继承 Throwable 通用契约（message/cause/suppressed/stack trace/打印/序列化）。

## 2. Java 用户如何使用

- 由安全检查失败路径抛出；用户代码常捕获诊断或再抛出包装。

```java
catch (SecurityException e) {
    throw new SecurityException("wrap: " + op, e);  // cause 链
}
```

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/SecurityException.html`、`Throwable.html` cause/suppressed 契约）。
- **OpenJDK 8u-dev 源码** `jdk/src/share/classes/java/lang/SecurityException.java`（svuid=6878364983674394167）。
- **实测 JDK-8 运行时**（Temurin 8u372，探针 /tmp/lcF）：全部异常消息文本、cause 槽锁定语义、打印格式实测后锁。

## 4. 测试用例设计（映射到 SecurityExceptionTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 层级（RuntimeException/Exception/Throwable） | testHierarchy |
| P0 | 4 构造器 message/cause | testConstructors |
| P0 | 空消息、`(Throwable)null` 锁槽、`(String,null)`/`(null,Throwable)`、cause-only 镜像消息 | testConstructorEdges |
| P0 | initCause 全矩阵（二次 ISE 前缀 / self IAE 精确 / 构造器 cause 不可覆写 / initCause(null) 各状态） | testCauseSemantics |
| P0 | suppressed 默认空/顺序/身份/防御拷贝/addSuppressed(null) NPE 精确 | testSuppressed |
| P0 | 顶帧类名/fillInStackTrace this/防御拷贝/setStackTrace 往返四字段/3 种 NPE/空数组合法 | testStackTraceHandling |
| P1 | toString 两种精确形 + PrintStream/PrintWriter 打印 + Suppressed 段 | testToStringAndPrinting |
| P1 | svuid 锁定 + 全量 round-trip | testSerialization |
| P2 | 形态（非 final/4 ctor/可赋值性） | testWhiteBox |

## 关键规范细节（易错，已锁入断言）

- **`(Throwable)null` 构造后 initCause 抛 ISE**——cause 槽已被「初始化为 null」，不是仍可设置。
- **`initCause(null)`**：fresh 上合法且此后锁槽（再 initCause ISE）；已设 cause 后调它是 ISE（`Can't overwrite cause with a null`）。
- **self-cause 精确消息** `Self-causation not permitted`；二次 initCause 消息前缀 `Can't overwrite cause with`。
- **`addSuppressed(null)` NPE 精确消息** `Cannot suppress a null exception.`；`setStackTrace({null})` 消息前缀 `stackTrace[0]`；空数组 setStackTrace 合法。
- **未知行号（-1）的 StackTraceElement.toString()** 形如 `BarClass.barMethod(BarClass.java)`（无行号段）。
- **serialVersionUID = 6878364983674394167**（JDK-8 与 JDK-27 相同，实测锁定）。

## 注意事项

- 打印断言用 ByteArrayOutputStream + UTF-8，不依赖平台 charset；不触碰全局 System.err。
