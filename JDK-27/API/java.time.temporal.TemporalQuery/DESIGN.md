# TemporalQuery 测试设计案

## 1. API 功能
`TemporalQuery<R>` 是泛型函数式接口，从 `TemporalAccessor` 提取任意类型 `R` 的信息。
- 唯一抽象方法：`R queryFrom(TemporalAccessor temporal)`
典型用法：`LocalDate::from`、`LocalTime::from`、`ZoneId::from` 等静态工厂方法引用本身就是 `TemporalQuery`；`TemporalQueries` 提供常用常量。

## 2. Java 用户如何使用
```java
TemporalQuery<LocalDate> q = LocalDate::from;
LocalDate d = q.queryFrom(LocalDate.of(2024,3,1));
// 或经 TemporalAccessor.query：
LocalDate d2 = LocalDate.of(2024,3,1).query(LocalDate::from);
TemporalQuery<Long> year = ta -> ta.getLong(ChronoField.YEAR);
```
组合链路：`TemporalAccessor.query(query)` → 内部 `query.queryFrom(this)`。

## 3. 开源代码实际用法调研
- 权威实现：`src/java.base/share/classes/java/time/temporal/TemporalQuery.java`（单抽象方法 `queryFrom`）。
- JDK 自身：`TemporalQueries` 用匿名类实现 `TemporalQuery`（如 `ZONE_ID`/`CHRONO`/`PRECISION`/`OFFSET`，见 `TemporalQueries.java`）；`Parsed.java` 依据 query 常量返回对应解析结果。
- 生态惯例：`TemporalAccessor.query(LocalDate::from)` 是“任意日期时间对象→LocalDate”的标准转换写法。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`TemporalQueryTest.testBlackBox()`）：
- 方法引用 `LocalDate::from` 作为 `TemporalQuery`，`queryFrom(LocalDate)` 返回原日期
- lambda 提取 `YEAR` 返回 `Long`
- `TemporalQueries.localDate()` 对 `LocalTime` 返回 null（查询允许 null 而不抛异常）
白盒部分（`testWhiteBox()`）：`isInterface`、单类型参数、`queryFrom` 返回类型变量（擦除为 `Object`）。
