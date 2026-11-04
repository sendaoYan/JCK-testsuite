# java.util.jar.Attributes.Name 测试方案

白盒结构断言在 `AttributesNameTest.testWhiteBox()`，黑盒行为断言在 `AttributesNameTest.testBlackBox()`。本文件聚焦黑盒方案，自动化实现见 `AttributesNameTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **构造**：`Name(String name)`，名称大小写不敏感、仅限 `[0-9a-zA-Z_-]`、长度 1..70，内部 `name.intern()`。
- **查询/工具**：`toString()`（返回原始大小写字符串）、`equals(Object)`（`equalsIgnoreCase`）、`hashCode()`（大小写不敏感计算）。
- **常量**：`MANIFEST_VERSION`、`SIGNATURE_VERSION`、`CONTENT_TYPE`、`CLASS_PATH`、`MAIN_CLASS`、`SEALED`、`EXTENSION_LIST`、`EXTENSION_NAME`、`EXTENSION_INSTALLATION`、`IMPLEMENTATION_TITLE`、`IMPLEMENTATION_VERSION`、`IMPLEMENTATION_VENDOR`、`IMPLEMENTATION_VENDOR_ID`、`IMPLEMENTATION_URL`、`SPECIFICATION_TITLE`、`SPECIFICATION_VERSION`、`SPECIFICATION_VENDOR`、`MULTI_RELEASE`（JDK-27 `javap` 实测 18 个导出常量）。

## 2. Java 用户如何使用

- 构造：`new Name("Custom-Header")`；直接使用常量 `Name.MANIFEST_VERSION`。
- 典型调用：`name.toString()` 取原名；`new Name("foo").equals(new Name("FOO"))` 大小写不敏感相等。
- 真实组合链路：`Name n = new Name("Key"); n.hashCode() == new Name("key").hashCode(); n.equals(new Name("KEY"))`。

## 3. 开源代码实际用法调研

> 本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源，未凭记忆编造。

- `test/jdk/java/util/jar/Manifest/WriteBinaryStructure.java:46-47`：`mf.getMainAttributes().put(Name.MANIFEST_VERSION, "1.0"); ...put(new Name("Key"), "Value");` —— 常量与自定义 `Name` 并用。
- `test/jdk/java/util/jar/Attributes/TestAttrsNL.java:31`：`import java.util.jar.Attributes.Name;` —— 测试直接使用 `Attributes.Name`。
- `src/java.base/share/classes/java/util/jar/Attributes.java:572-575`：`Name.hash(String)` 大小写不敏感哈希、非法字符抛 `IllegalArgumentException` —— 行为来源。

## 4. 测试用例设计 + 自动化实现

- **常量**：18 个导出常量的 `toString()` 精确字符串逐一断言。
- **相等/哈希**：`new Name("Custom-Header")` 与 `new Name("custom-header"/"CUSTOM-HEADER")` 相等且哈希相等；`equals(null)`、`equals(非 Name)` 为 false；常量与大小写变体 `Name` 相等。
- **边界/异常**：`null` 抛 `NullPointerException`；空名/含空格/含 `*` 抛 `IllegalArgumentException`；70 长名接受、71 长名抛 `IllegalArgumentException`。
- **自动化**：`AttributesNameTest.testBlackBox()` 内 `testConstants/testEquality/testInvalidAndBoundary`。
- **健壮性**：固定输入、无墙钟/随机、无全局状态/文件/网络/线程。
