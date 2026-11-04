# UnsupportedTemporalTypeException 测试设计案

## 1. API 功能
`UnsupportedTemporalTypeException` 表示“该 `Temporal` 不支持所请求的字段/单位/查询”的运行时异常。
- 继承：`java.time.DateTimeException` → `RuntimeException`
- 构造器：`UnsupportedTemporalTypeException(String)`、`UnsupportedTemporalTypeException(String, Throwable)`
- 继承方法：`getMessage()`、`getCause()`、`getLocalizedMessage()` 等

## 2. Java 用户如何使用
通常在越界使用字段时被抛出，用户捕获以优雅降级：
```java
try {
    LocalTime.of(10,0).get(ChronoField.MONTH_OF_YEAR);
} catch (UnsupportedTemporalTypeException e) {
    // 时间对象无“月”字段
}
```
也可直接构造并抛出（自定义 `Temporal` 实现常用）。

## 3. 开源代码实际用法调研
- 权威实现：`src/java.base/share/classes/java/time/temporal/UnsupportedTemporalTypeException.java`（两个构造器，均委托 `super`）。
- JDK 自身抛出点：`src/java.base/share/classes/java/time/LocalDate.java` 第 1227 行 javadoc 说明非 `ChronoField` 的字段会抛 `UnsupportedTemporalTypeException`；`LocalTime.get(MONTH_OF_YEAR)`、`ChronoField`/`ChronoUnit` 对不支持的 `Temporal` 均抛此异常。
- 生态惯例：框架做“日期/时间可选字段”解析时用该异常标识类型不匹配。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`UnsupportedTemporalTypeExceptionTest.testBlackBox()`）：
- 构造 `(String)`：`getMessage()` 返回原消息、`instanceof DateTimeException` 与 `RuntimeException`
- 构造 `(String,Throwable)`：`getMessage()` 与 `getCause()` 正确
- 自然抛出：`LocalTime.of(10,0).get(ChronoField.MONTH_OF_YEAR)` 抛 `UnsupportedTemporalTypeException`
白盒部分（`testWhiteBox()`）：反射断言父类为 `DateTimeException`、`DateTimeException` 父类为 `RuntimeException`、两个构造器存在。
