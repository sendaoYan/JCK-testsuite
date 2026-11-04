# java.nio.charset.spi.CharsetProvider 设计文档

## 1. API 功能

为 `Charset.forName` 等入口提供自定义字符集的 SPI 抽象类。

- 抽象方法：`Iterator<Charset> charsets()`（列出该 provider 支持的字符集）、
  `Charset charsetForName(String charsetName)`（按名查找，找不到返回 null）。
- 构造器：protected 无参构造器。

## 2. Java 用户如何使用

- 实现类：继承 `CharsetProvider`，实现 `charsets()`/`charsetForName()`，并经 `META-INF/services` 注册。
- 组合链路：`charsetForName("UTF-8")` → 返回 `Charset` 或 null；`charsets()` → 迭代全部支持项。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/charset/spi/CharsetProvider.java`）：
  标准 provider（如 StandardCharsets、扩展 provider）均通过此 SPI 提供字符集；`Charset.forName` 遍历已安装 provider。

## 4. 测试用例设计 + 自动化实现

文件：`CharsetProviderTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / abstract / 父类 `Object` / protected 无参构造器。
- 黑盒：匿名实现 → `charsetForName` 命中/未命中返回 null → `charsets()` 迭代多个字符集 → 空 provider 行为。
