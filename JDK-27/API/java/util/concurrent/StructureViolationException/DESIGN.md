# java.util.concurrent.StructureViolationException 设计案

## 1. API 功能
结构化并发结构破坏异常。构造 `()` 与 `(String)`；`final` 继承 `RuntimeException`。

## 2. Java 用户如何使用
结构化并发作用域关闭后仍 fork、或作用域线程逃离时抛 `StructureViolationException`。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.StructuredTaskScope` 预览 API 实现抛此异常；`test/jdk/java/util/concurrent/structured/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景（深化批次追加 Throwable/序列化/确定性嵌套违规场景；注意本类属 Structured Concurrency **preview API**，测试用 jtreg `@enablePreview`）：
1. 白盒：`Class.forName` 断言继承层次（extends RuntimeException、final）、公共构造器 ()/(String)。
2. 黑盒：实例化各公共构造器，断言 `getMessage()`/`getCause()`/`instanceof` 等可观察行为。
3. Throwable 契约：toString 两种形态、initCause 返回 this/生效、fillInStackTrace 返回 this、getSuppressed 空。
4. 序列化：serialVersionUID == -7705327650798235468（实测锁定）；round-trip 消息/null cause/toString 保持。
5. **确定性嵌套违规**（JDK-27 `StructuredTaskScope.open()` 三类型参 API）：同线程先后 open 两个 scope（父子），先 close 父 → StructureViolationException（message null、cause null、栈非空）；先子后父的正常关闭不抛。非 owner 线程 close → WrongThreadException（非本类，实测记录）。

实现：`StructureViolationExceptionTest.java`（`testWhiteBox` + `testBlackBox` + `testThrowableContract` + `testSerializationContract` + `testScopeNestingViolation`，PASS 23 断言）。
