# ValueRange 测试设计案

## 1. API 功能
`ValueRange` 是不可变的值范围（字段取值范围），实现 `Serializable`。
工厂：`of(min,max)`、`of(min,smallestMax,max)`、`of(min,largestMin,smallestMax,max)`
查询：`isFixed()`、`getMinimum()`、`getLargestMinimum()`、`getSmallestMaximum()`、`getMaximum()`、`isIntValue()`
校验：`isValidValue(long)`、`isValidIntValue(long)`、`checkValidValue(long,TemporalField)`、`checkValidIntValue(long,TemporalField)`
对象方法：`equals`、`hashCode`、`toString()`（形如 `1 - 12`、`1 - 28/31`、`1/28 - 30/31`）

## 2. Java 用户如何使用
```java
ValueRange r = ValueRange.of(1, 12);
long min = r.getMinimum();               // 1
boolean fixed = r.isFixed();             // true
boolean ok = r.isValidValue(6);          // true
long v = r.checkValidValue(13, ChronoField.MONTH_OF_YEAR); // throws DateTimeException
String s = r.toString();                 // "1 - 12"
```
组合链路：`of(...) → getMinimum/getMaximum/isValidValue`，或 `checkValidValue` 做带上下文的校验。

## 3. 开源代码实际用法调研
- 权威实现：`src/java.base/share/classes/java/time/temporal/ValueRange.java`（第 427–437 行 `toString()`：`minSmallest[/minLargest] - maxSmallest[/maxLargest]`）。
- JDK 自身：`ChronoField.java` 每个字段常量用 `ValueRange.of(...)` 定义范围（如第 250 行 `ValueRange.of(0, 59)`）；`IsoFields.java` 第 307/396/451 行也用 `ValueRange.of`。
- 其他：`src/java.base/share/classes/java/time/format/DateTimeFormatterBuilder.java`、`DateTimePrintContext.java` 使用 `ValueRange` 校验打印值。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`ValueRangeTest.testBlackBox()`）：
- `of(1,12)`：四极值、`isFixed`/`isIntValue`、`isValidValue/isValidIntValue` 边界与越界、`checkValidValue/checkValidIntValue` 合法与越界（抛 `DateTimeException`）、`toString()=="1 - 12"`
- `of(1,28,31)`：`largestMin=1`/`smallestMax=28`/`max=31`、非 fixed、`isValidValue(29)`、`isValidIntValue(31)`、`toString()=="1 - 28/31"`
- `of(1,28,30,31)`：`largestMin=28`/`smallestMax=30`、`toString()=="1/28 - 30/31"`
- `equals`/`hashCode`
白盒部分（`testWhiteBox()`）：`final`、无公开构造器、实现 `Serializable`、三个 `of` 工厂存在。
