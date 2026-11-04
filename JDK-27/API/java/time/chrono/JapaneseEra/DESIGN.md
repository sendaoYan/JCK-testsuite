# java.time.chrono.JapaneseEra 测试设计

## 1. API 功能
日本年号。注意：是 final 类（非 enum），实现 `Era`/`Serializable`，因为年号会随新天皇登基增长。
- 常量：`MEIJI(-1)`、`TAISHO(0)`、`SHOWA(1)`、`HEISEI(2)`、`REIWA(3)`。
- 查询：`getValue()`、`getDisplayName(TextStyle,Locale)`、`range(TemporalField)`。
- 工厂：`of(int)`、`valueOf(String)`、`values()`（注意 JDK 19+ `valueOf` 用罗马字名而非常量名）。

## 2. Java 用户如何使用
```java
JapaneseEra e = JapaneseEra.of(2);       // HEISEI
int v = JapaneseEra.SHOWA.getValue();    // 1
JapaneseEra[] all = JapaneseEra.values(); // 含 MEIJI..REIWA（>=4）
```
组合链路：`of(value) == 常量` 的往返校验是稳定契约；`getValue()` 与 `JapaneseChronology.eraOf(value)` 一致。

## 3. 开源代码实际用法调研（来源：JDK 自测 test/jdk）
- `test/jdk/java/time/tck/java/time/chrono/TCKJapaneseEra.java`：专门校验 `JapaneseEra.of(int)`/`getValue()` 及各年号边界（如 `JapaneseEra.of(1)==SHOWA`）。
- `test/jdk/java/time/tck/java/time/chrono/TCKJapaneseChronology.java`：`JapaneseEra.REIWA/HEISEI/...` 常量在 data provider 中被大量引用。
- `test/jdk/java/time/test/java/time/chrono/TestJapaneseChronology.java`：校验 `eraOf` 与 `JapaneseEra` 映射。
- 源码实现：`src/java.base/share/classes/java/time/chrono/JapaneseEra.java`。
以上均为 JDK 仓库内实际用法，未虚构。

## 4. 测试用例设计 + 自动化实现
黑盒断言（JapaneseEraTest.testBlackBox）：
- 五个常量 getValue（-1/0/1/2/3）。
- `of(-1..3)` 与常量一一对应。
- `values().length >= 4` 且含 HEISEI（不硬编码精确值，防未来新增年号）。
- 异常：`of(99)`/`of(-99)` 抛 DateTimeException。
- 明确不调用 `valueOf(String)`、不断言 displayName（JDK 19+ 罗马字名会破坏旧常量名假设）。
白盒：反射校验 final、非 enum、父类 Object、实现 Era/Serializable、`of/getValue/values/valueOf` 方法存在。

- **深化补充（契约级）**：values `[-1 Meiji, 0 Taisho, 1 Showa, 2 Heisei, 3 Reiwa]`；valueOf **驼峰大小写敏感**（`Reiwa` ✓，`REIWA`/`heisei` IAE `japaneseEra is invalid`，null NPE `japaneseEra`）；REIWA 公共常量（JDK-8 中 private）；SHORT `Shōwa`（macron）；range UTE；query(precision)=Eras。
