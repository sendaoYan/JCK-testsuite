# AbstractChronology 测试设计案

## 1. API 功能
`java.time.chrono.AbstractChronology` 是 `Chronology` 接口的抽象骨架实现，为各历法实现（如 `IsoChronology`、`JapaneseChronology`、`MinguoChronology`、`ThaiBuddhistChronology`）提供公共逻辑。公开成员（`javap -public`）：
- 核心操作：`resolveDate(Map<TemporalField,Long>, ResolverStyle)` —— 把字段值解析为 `ChronoLocalDate`；`compareTo(Chronology)` —— 按历法 ID 字符串比较。
- Object 覆写：`equals`、`hashCode`（基于 `compareTo == 0`，即 ID 相等）、`toString`（返回 `getId()`）。

它是 `public abstract class`，直接继承 `Object` 并实现 `Chronology`。构造受保护，用户通常通过 `IsoChronology.INSTANCE` 等单例使用。

## 2. Java 用户如何使用
用户不直接实例化 `AbstractChronology`（抽象类），而是通过其具体子类单例使用：
```java
AbstractChronology ac = IsoChronology.INSTANCE;          // 具体实现
ChronoLocalDate d = ac.resolveDate(map, ResolverStyle.STRICT); // 解析
int cmp = ac.compareTo(Chronology.of("ThaiBuddhist"));    // 按 ID 排序
boolean same = ac.equals(Chronology.of("ISO"));           // equals 按 ID
```
组合链路：`resolveDate`（字段 → 日期）→ `compareTo`（历法排序）→ `equals`/`hashCode`（去重/判等）。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.base/share/classes/java/time/chrono/IsoChronology.java:132` 定义 `INSTANCE = new IsoChronology()`，是唯一 ISO 实例；`JapaneseChronology`、`MinguoChronology`、`ThaiBuddhistChronology` 同样继承 `AbstractChronology` 并暴露单例。
- `AbstractChronology.java` 内部：`resolveDate` 由 `Chronology.resolveDate` 抽象声明，`compareTo` 实现为 `getId().compareTo(other.getId())`（`AbstractChronology.java:662-664`）；`equals` 实现为 `compareTo((AbstractChronology) obj) == 0`（`:684`）。
- JDK 自带测试 `test/jdk/java/time/tck/java/time/chrono/TCKChronology.java` 对各历法的 `resolveDate`/`compareTo` 做参数化断言（STRICT/SMART/LENIENT 三种解析模式）。

## 4. 测试用例设计 + 自动化实现
场景（对应 `AbstractChronologyTest.java` 黑盒部分）：
- 白盒结构：`Class.forName` + `Modifier.isAbstract`、非接口、非 final、直接父类 `Object`、`Chronology` 可赋值，以及 `resolveDate`/`compareTo`/`equals`/`hashCode`/`toString` 成员存在。
- 行为（经 `IsoChronology.INSTANCE`）：
  - `compareTo` 自比较 == 0，与 `ThaiBuddhist`(<0)、`Hijrah-umalqura`(>0)、`Minguo`(<0)、`Japanese`(<0) 的 ID 排序一致。
  - `equals`/`hashCode`：与 `Chronology.of("ISO")` 相等、与 `Minguo` 不等、hashCode 一致。
  - `toString` == "ISO"。
  - `resolveDate` STRICT：年月日 → `2020-01-01`；`DAY_OF_YEAR=60`（闰年）→ `2020-02-29`；字段缺失 → `DateTimeException`。
