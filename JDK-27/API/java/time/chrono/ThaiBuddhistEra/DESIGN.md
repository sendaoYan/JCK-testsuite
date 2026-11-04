# java.time.chrono.ThaiBuddhistEra 测试设计

## 1. API 功能
泰佛历年号，enum，实现 `Era`。两个常量：`BEFORE_BE(0)`、`BE(1)`。
- 查询：`getValue()`、`getDisplayName(TextStyle,Locale)`。
- 枚举方法：`values()`、`valueOf(String)`、`of(int)`。

## 2. Java 用户如何使用
```java
ThaiBuddhistEra e = ThaiBuddhistEra.BE;          // value 1
int v = ThaiBuddhistEra.BEFORE_BE.getValue();     // 0
ThaiBuddhistEra back = ThaiBuddhistEra.of(1);     // BE
ThaiBuddhistEra[] all = ThaiBuddhistEra.values(); // [BEFORE_BE, BE]
```
组合链路：`getValue() == of(...) 的入参`；`valueOf(name) == 常量`。

## 3. 开源代码实际用法调研（来源：JDK 自测 test/jdk）
- `test/jdk/java/time/tck/java/time/chrono/TCKThaiBuddhistChronology.java`：`ThaiBuddhistEra.BE`、`ThaiBuddhistEra.BEFORE_BE` 作为 `date(...)`/`dateYearDay(...)` 参数。
- `test/jdk/java/time/tck/java/time/chrono/serial/TCKEraSerialization.java`：ThaiBuddhistEra 序列化。
- 源码实现：`src/java.base/share/classes/java/time/chrono/ThaiBuddhistEra.java`。
以上均为 JDK 仓库内实际用法，未虚构。

## 4. 测试用例设计 + 自动化实现
黑盒断言（ThaiBuddhistEraTest.testBlackBox）：
- BEFORE_BE=0、BE=1。
- valueOf("BE")/valueOf("BEFORE_BE") 等值；values().length==2。
- of(1)=BE、of(0)=BEFORE_BE。
- 异常：valueOf 非法名抛 IllegalArgumentException；of(2) 抛 DateTimeException。
白盒：反射校验 enum、父类 Enum、实现 Era、values/valueOf/of/getValue 方法存在。

- **深化补充（契约级）**：CLDR 显示名 `BE`/`BE` 与 BEFORE_BE FULL `BC`（JDK-8 借用 `Anno Domini`/`A`/`Before Christ`）；eraOf/date.getEra 身份同一；valueOf 非法 IAE；range UTE。
