# java.time.chrono.MinguoEra 测试设计

## 1. API 功能
中华民国纪年年号，enum，实现 `Era`。两个常量：`BEFORE_ROC(0)`、`ROC(1)`。
- 查询：`getValue()`、`getDisplayName(TextStyle,Locale)`。
- 枚举方法：`values()`、`valueOf(String)`、`of(int)`。

## 2. Java 用户如何使用
```java
MinguoEra e = MinguoEra.ROC;            // value 1
int v = MinguoEra.BEFORE_ROC.getValue(); // 0
MinguoEra back = MinguoEra.of(1);       // ROC
MinguoEra[] all = MinguoEra.values();   // [BEFORE_ROC, ROC]
```
组合链路：`getValue() == of(...) 的入参`；`valueOf(name) == 常量`。

## 3. 开源代码实际用法调研（来源：JDK 自测 test/jdk）
- `test/jdk/java/time/tck/java/time/chrono/TCKMinguoChronology.java`：`MinguoEra.ROC`、`MinguoEra.BEFORE_ROC` 作为 `date(...)`/`dateYearDay(...)` 的参数。
- `test/jdk/java/time/tck/java/time/chrono/serial/TCKEraSerialization.java`：MinguoEra 序列化往返。
- 源码实现：`src/java.base/share/classes/java/time/chrono/MinguoEra.java`。
以上均为 JDK 仓库内实际用法，未虚构。

## 4. 测试用例设计 + 自动化实现
黑盒断言（MinguoEraTest.testBlackBox）：
- BEFORE_ROC=0、ROC=1。
- valueOf("ROC")/valueOf("BEFORE_ROC") 等值；values().length==2。
- of(1)=ROC、of(0)=BEFORE_ROC。
- 异常：valueOf 非法名抛 IllegalArgumentException；of(2) 抛 DateTimeException。
白盒：反射校验 enum、父类 Enum、实现 Era、values/valueOf/of/getValue 方法存在。

- **深化补充（契约级）**：CLDR 显示名 `Minguo`/`Minguo`/`Minguo`（ROC）与 `B.R.O.C.`（BEFORE_ROC，三档全同）（JDK-8 借用 `Anno Domini`/`AD`/`A`/`Before Christ`/`BC`/`B`）；eraOf 身份同一；valueOf 非法 IAE。
