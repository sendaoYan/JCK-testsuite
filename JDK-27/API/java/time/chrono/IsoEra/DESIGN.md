# IsoEra 测试设计案

## 1. API 功能
`java.time.chrono.IsoEra` 是 ISO-8601 历法的纪元枚举，`public final class` 继承 `Enum<IsoEra>` 并实现 `Era`。公开成员（`javap -public`）：
- 常量：`BCE`（value 0）、`CE`（value 1）。
- 静态：`values()`、`valueOf(String)`、`of(int)`。
- 查询：`getValue()`（继承 `Era` 的 `isSupported`/`range`/`get`/`getLong`/`query`/`adjustInto`/`getDisplayName`）。

## 2. Java 用户如何使用
```java
IsoEra ce = IsoEra.CE;
int v = ce.getValue();                     // 1
IsoEra fromInt = IsoEra.of(1);             // CE
IsoEra fromName = IsoEra.valueOf("CE");    // CE
String shortName = ce.getDisplayName(TextStyle.SHORT, Locale.ENGLISH); // "AD"
```
组合链路：`valueOf`/`of`/`values` → `getValue`/`get(ERA)` → `adjustInto`/`getDisplayName`。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.base/share/classes/java/time/chrono/IsoEra.java` 定义 `BCE`/`CE`，`of(int)` 用 switch 实现（0→BCE、1→CE，否则抛 `DateTimeException`），`getValue()` 返回 `ordinal()`。
- `src/java.base/share/classes/java/time/chrono/IsoChronology.java` 的 `eraOf(int)` 委托 `IsoEra.of`，`eras()` 返回 `Arrays.asList(IsoEra.values())`。
- JDK 自带测试 `test/jdk/java/time/tck/java/time/chrono/TCKIsoEra.java` 断言 `valueOf`/`of`/`getValue`/`values` 与 `range(ERA)`。
- `test/jdk/java/time/test/java/time/chrono/TestEraDisplayName.java:60-66` 断言英文 FULL/SHORT 显示名。

## 4. 测试用例设计 + 自动化实现
场景（对应 `IsoEraTest.java` 黑盒部分）：
- 白盒结构：enum、final、父类 `Enum`、实现 `Era`，`values`/`valueOf`/`of`/`getValue` 方法存在。
- 行为：
  - 数值：BCE==0、CE==1。
  - 工厂：`valueOf("CE"/"BCE")`、`values().length==2`（顺序 BCE,CE）、`of(0/1)`。
  - 字段：`isSupported(ERA)`、`get/getLong(ERA)`、`range(ERA)==[0,1]`。
  - `getDisplayName`：SHORT "AD"/"BC"，FULL "Anno Domini"/"Before Christ"。
  - `adjustInto`：CE 翻转 BCE→CE、BCE 翻转 CE→BCE。
  - 异常：`valueOf("XX")` → `IllegalArgumentException`、`of(2)` → `DateTimeException`。

- **深化补充（契约级）**：values `[BCE, CE]`、值 0/1；**LocalDate.of(-5,...).get(YEAR_OF_ERA)==6**（无 0 年）；display `Anno Domini`/`BC`/`AD`；range(YEAR_OF_ERA) UTE。
