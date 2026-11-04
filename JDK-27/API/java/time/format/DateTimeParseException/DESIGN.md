# java.time.format.DateTimeParseException

## 1. API 功能
解析日期时间文本失败时抛出的运行时异常，继承 `java.time.DateTimeException`（→ `RuntimeException`）。

- 构造器：`(String message, CharSequence parsedString, int errorIndex)`、`(String, CharSequence, int, Throwable cause)`
- 查询：`getParsedString()`、`getErrorIndex()`、继承 `getMessage()`/`getCause()`

## 2. Java 用户如何使用
```java
try {
    DateTimeFormatter.ISO_LOCAL_DATE.parse("2024-13-01", LocalDate::from);
} catch (DateTimeParseException e) {
    e.getParsedString(); // "2024-13-01"
    e.getErrorIndex();   // 出错位置
}
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.base/share/classes/java/time/format/DateTimeParseException.java`：两个构造器定义。
- `jdk25u-dev/src/java.base/share/classes/java/time/format/DateTimeFormatter.java`：解析失败时 `new DateTimeParseException(...)`。
- `jdk25u-dev/src/java.base/share/classes/java/time/format/Parsed.java`：组合消息并抛出。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/format/TCKDateTimeParseResolver.java`：覆盖错误索引与解析异常。

## 4. 测试用例设计 + 自动化实现
- 白盒：反射断言非接口/抽象/枚举、父类 `DateTimeException`、是 `RuntimeException`、存在 `(String,CharSequence,int)` 与 `(String,CharSequence,int,Throwable)` 构造器。
- 黑盒：构造器 `getMessage`/`getParsedString`/`getErrorIndex`；带 cause 构造器 `getErrorIndex`/`getCause`。
- 组合：`DateTimeFormatter.ISO_LOCAL_DATE.parse("2024-13-01", LocalDate::from)` 自然抛 `DateTimeParseException`，断言 `getParsedString`、`getErrorIndex >= 0`、`getMessage` 非空。
- 全部固定输入、headless-safe。
