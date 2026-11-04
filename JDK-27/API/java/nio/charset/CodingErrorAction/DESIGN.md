# java.nio.charset.CodingErrorAction 设计文档

## 1. API 功能

描述编解码出错时应对策略的类型安全枚举（非 enum，是 final 类 + 私有构造器 + 三个常量）。

- 常量：`IGNORE`（忽略非法输入/不可映射字符）、`REPLACE`（替换为替代序列）、`REPORT`（抛异常报告）。
- 查询：`toString()` 返回常量名（`"IGNORE"`/`"REPLACE"`/`"REPORT"`）。

## 2. Java 用户如何使用

- 组合链路：`CharsetDecoder.onMalformedInput(CodingErrorAction.REPORT)` / `onUnmappableCharacter(...)`，
  指定遇到错误时的行为；再 `decode(...)`。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/charset/CodingErrorAction.java`）：
  三个常量通过私有构造器 `CodingErrorAction(String name)` 建立，`toString()` 返回 name。
- JDK 内部 charset 编解码路径广泛使用这三个常量作为默认策略。

## 4. 测试用例设计 + 自动化实现

文件：`CodingErrorActionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / final / 父类 `Object` / 唯一声明构造器为 private / 恰好三个 public 常量。
- 黑盒：三个常量的 `toString()`、两两不等、常量自等。
