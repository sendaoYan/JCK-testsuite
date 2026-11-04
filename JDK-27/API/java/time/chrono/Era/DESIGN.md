# Era 测试设计案

## 1. API 功能
`java.time.chrono.Era` 是历法纪元接口，扩展 `TemporalAccessor`、`TemporalAdjuster`。公开成员（`javap -public`）：
- 查询：`getValue()`（纪元数值）、`isSupported(TemporalField)`、`range(TemporalField)`、`get/getLong(TemporalField)`、`query(TemporalQuery)`。
- 调整/显示：`adjustInto(Temporal)`、`getDisplayName(TextStyle,Locale)`。

ISO 实现是 `IsoEra`（CE=1、BCE=0）；其它实现有 `JapaneseEra`、`HijrahEra`、`MinguoEra`、`ThaiBuddhistEra`。

## 2. Java 用户如何使用
```java
Era era = IsoEra.CE;
int v = era.getValue();                        // 1
int erafield = era.get(ChronoField.ERA);       // 1
LocalDate adjusted = (LocalDate) era.adjustInto(LocalDate.of(0,1,1)); // BCE->CE
String name = era.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);    // "AD"
```
组合链路：`getValue`/`get(ERA)` → `adjustInto`（纪元翻转）→ `getDisplayName`。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.base/share/classes/java/time/chrono/Era.java:320` 的默认 `getDisplayName` 用 `DateTimeFormatterBuilder().appendText(ERA, style)` 生成文本；`adjustInto`（`:299`）实现为 `temporal.with(ERA, getValue())`。
- `src/java.base/share/classes/java/time/LocalDate.java` 的 `with(ERA, ...)` 分支用 `getLong(ERA) == newValue ? this : withYear(1 - year)`，即纪元翻转实现。
- JDK 自带测试 `test/jdk/java/time/test/java/time/chrono/TestEraDisplayName.java:60-66` 断言 IsoEra 的 FULL/SHORT 英文名（"Anno Domini"/"Before Christ"、"AD"/"BC"）。
- `test/jdk/java/time/tck/java/time/chrono/TCKIsoEra.java` 断言 `valueOf`/`of`/`getValue`/`range(ERA)`。

## 4. 测试用例设计 + 自动化实现
场景（对应 `EraTest.java` 黑盒部分）：
- 白盒结构：接口、扩展 `TemporalAccessor`/`TemporalAdjuster`，关键方法名存在。
- 行为（经 `IsoEra`）：
  - 数值：CE==1、BCE==0。
  - 字段：`isSupported(ERA)`、`get/getLong(ERA)`、`range(ERA)==ValueRange[0,1]`。
  - `adjustInto`：CE 无操作、BCE→CE、CE→BCE 翻转。
  - `getDisplayName`：SHORT "AD"/"BC"，FULL "Anno Domini"/"Before Christ"（仅英文）。
  - `query`：precision==`ChronoUnit.ERAS`、chronology==null。
  - 异常：`get(YEAR)` → `UnsupportedTemporalTypeException`。

- **深化补充（契约级）**：五历 eras() 有序表；getValue 全映射（JapaneseEra -1..3）；getEra 往返（Reiwa/ROC/BE/CE/AH）；query(precision)=`Eras`；**所有纪元 range(YEAR_OF_ERA) 抛 UTE `Unsupported field: YearOfEra`**；CLDR 显示名按侧锁定（HijrahEra `Anno Hegirae`/`AH`/`AH`、MinguoEra `Minguo`、ThaiBuddhistEra `BE`、Showa SHORT `Shōwa`）。
