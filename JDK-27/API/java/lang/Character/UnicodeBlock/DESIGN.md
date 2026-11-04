# Character.UnicodeBlock — 设计方案

## 1. API 功能
`java.lang.Character.UnicodeBlock`（final，继承 `Character.Subset`）表示 Unicode 字符块；静态工厂 `of(char)/of(int)` 按码点查块、`forName(String)` 按名称（原始名或常量名）查块，另暴露大量 `public static final` 块常量（BASIC_LATIN、GREEK、CJK_UNIFIED_IDEOGRAPHS、EMOTICONS…）。

## 2. Java 用户如何使用
- 文本处理中判断字符属于哪个 Unicode 块（如判断是否 CJK 文本）：
```java
if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) { ... }
```
- `forName("Basic Latin")` 反查块实例。

## 3. 开源代码实际用法调研
- JetBrains IntelliJ、Eclipse、Lucene 的 CJK 分词以 `UnicodeBlock.of()` 区分 CJK 字符；JDK `test/jdk/java/lang/Character/UnicodeBlock.java` 验证 of/forName 与未定义码点返回 null。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Character$UnicodeBlock")` 验证 final、继承 Subset、`of(char)/of(int)/forName` 为 public static、常量字段可反射读取。
黑盒：`of('A')`→BASIC_LATIN、`of(0x4E00)`→CJK_UNIFIED_IDEOGRAPHS、`of(0x1F600)`→EMOTICONS；`forName` 原始名/常量名等价、非法名抛 IAE；常量 toString/身份/相等语义。
对应 `UnicodeBlockTest.java` 白盒 + 黑盒部分。
