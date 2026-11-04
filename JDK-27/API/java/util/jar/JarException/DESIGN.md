# java.util.jar.JarException 测试方案

白盒结构断言在 `JarExceptionTest.testWhiteBox()`，黑盒行为断言在 `JarExceptionTest.testBlackBox()`。本文件聚焦黑盒方案，自动化实现见 `JarExceptionTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **继承**：`JarException extends java.util.zip.ZipException extends java.io.IOException`，实现 `Serializable`。
- **构造**：`JarException()`、`JarException(String message)`。

## 2. Java 用户如何使用

- 构造：`throw new JarException("bad jar");` 或 `new JarException()`。
- 典型调用：捕获为 `JarException` / `ZipException` / `IOException`；`getMessage()` 取构造消息。

## 3. 开源代码实际用法调研

> 本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源，未凭记忆编造。

- `src/java.base/share/classes/java/util/jar/JarException.java`：`public class JarException extends ZipException`，仅两个构造器 —— 结构与继承来源。
- `src/java.base/share/classes/java/util/zip/ZipException.java`：`public class ZipException extends IOException` —— 间接父类链来源。

## 4. 测试用例设计 + 自动化实现

- **构造**：无参 `getMessage()==null`；`new JarException("bad jar").getMessage()=="bad jar"`；`new JarException((String) null).getMessage()==null`。
- **继承**：`instanceof ZipException`、`instanceof IOException`、`instanceof Exception`。
- **抛出/捕获**：`throw/catch` 保持同一实例与消息；可被 `ZipException` 与 `IOException` 捕获。
- **自动化**：`JarExceptionTest.testBlackBox()` 内 `testConstructors/testThrowCatchRoundTrip`。
- **健壮性**：无墙钟/随机、无全局状态/文件/网络/线程。
