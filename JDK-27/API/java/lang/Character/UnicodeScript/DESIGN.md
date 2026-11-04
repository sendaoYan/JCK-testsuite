# Character.UnicodeScript — 设计方案

## 1. API 功能
`java.lang.Character.UnicodeScript`（final 枚举，继承 `Enum<UnicodeScript>`）表示 Unicode 文字系统（Script）；静态方法 `of(int)` 按码点查文字、`values()/valueOf(String)` 为枚举机制，常量含 COMMON/LATIN/GREEK/CYRILLIC/HEBREW/HAN…（160+ 个）。

## 2. Java 用户如何使用
- 按文字（而非块）粗分类文本，例如检测中文/西里尔内容：
```java
if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) { ... }
```

## 3. 开源代码实际用法调研
- Lucene `CJKAnalyzer`、ICU4J、字体回退逻辑（IntelliJ）用 Script 判断；JDK `test/jdk/java/lang/Character/UnicodeScript.java` 验证 of() 与枚举机制。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Character$UnicodeScript")` 验证 final 枚举、继承 Enum、`of/values/valueOf` public static。
黑盒：`of('A')`→LATIN、`of(0x4E00)`→HAN、`of('(')`/emoji→COMMON、西里尔/希伯来映射；`values()` 规模、`valueOf` 往返与非法名抛 IAE、`name()/compareTo`。
对应 `UnicodeScriptTest.java` 白盒 + 黑盒部分。
