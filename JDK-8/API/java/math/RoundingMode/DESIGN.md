# java.math.RoundingMode 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
- 枚举常量（文档化 ordinal）：`UP(0)/DOWN(1)/CEILING(2)/FLOOR(3)/HALF_UP(4)/HALF_DOWN(5)/HALF_EVEN(6)/UNNECESSARY(7)`。
- `values()`、`valueOf(String)`、`valueOf(int)`。
- `name()/toString()/ordinal()`（Enum 继承）。

## 2. Java 用户如何使用
```java
RoundingMode rm = RoundingMode.valueOf(4);   // HALF_UP
BigDecimal r = v.divide(d, 2, rm);
```

## 3. 开源代码实际用法调研
- `~/git/jdk8u-dev/jdk/src/share/classes/java/math/RoundingMode.java`（valueOf(int) 映射表与越界契约）。

## 4. 测试用例设计
- 8 常量 + 文档化 ordinal 顺序断言。
- valueOf(int) 0..7 全映射；valueOf(String) 全常量往返；name/toString。
- 异常：valueOf(-1)/valueOf(8)/valueOf("FOO") → IllegalArgumentException；valueOf((String)null) → NPE。
- 确定性；headless-safe（无 `@key`）。
