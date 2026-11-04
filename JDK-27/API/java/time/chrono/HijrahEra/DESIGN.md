# java.time.chrono.HijrahEra 测试设计

## 1. API 功能
伊斯兰历年号，enum，实现 `Era`。单常量：`AH(1)`。
- 查询：`getValue()`、`getDisplayName(TextStyle,Locale)`。
- 枚举方法：`values()`、`valueOf(String)`、`of(int)`。

## 2. Java 用户如何使用
```java
HijrahEra e = HijrahEra.AH;       // value 1
int v = e.getValue();             // 1
HijrahEra back = HijrahEra.of(1); // AH
HijrahEra[] all = HijrahEra.values(); // [AH]
```
组合链路：`getValue() == of(...) 的入参`；`valueOf("AH") == AH`。

## 3. 开源代码实际用法调研（来源：JDK 自测 test/jdk）
- `test/jdk/java/time/tck/java/time/chrono/TCKHijrahChronology.java`：`HijrahEra.values()` 遍历用于 resolve 测试（`for (HijrahEra era : HijrahEra.values())`）。
- `test/jdk/java/time/tck/java/time/chrono/serial/TCKEraSerialization.java`：HijrahEra 序列化。
- 源码实现：`src/java.base/share/classes/java/time/chrono/HijrahEra.java`。
以上均为 JDK 仓库内实际用法，未虚构。

## 4. 测试用例设计 + 自动化实现
黑盒断言（HijrahEraTest.testBlackBox）：
- AH value=1。
- valueOf("AH")==AH；values().length==1。
- of(1)=AH。
- 异常：valueOf 非法名抛 IllegalArgumentException；of(2) 抛 DateTimeException。
白盒：反射校验 enum、父类 Enum、实现 Era、values/valueOf/of/getValue 方法存在。
