# java.math.MathContext 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
- 常量：`UNLIMITED`(0,HALF_UP)、`DECIMAL32`(7,HALF_EVEN)、`DECIMAL64`(16,HALF_EVEN)、`DECIMAL128`(34,HALF_EVEN)。
- 构造：`MathContext(int)`、`MathContext(int,RoundingMode)`、`MathContext(String)`（格式 `precision=<digits> roundingMode=<word>`）。
- 查询：`getPrecision/getRoundingMode`。
- 对象：`equals/hashCode/toString`。
- `Serializable`（serialVersionUID 5579720004786848255L）。

## 2. Java 用户如何使用
```java
MathContext mc = new MathContext(4, RoundingMode.HALF_UP);
BigDecimal r = value.round(mc);              // 限定精度并舍入
MathContext.d32 = MathContext.DECIMAL32;      // 预定义 7 位精度
```

## 3. 开源代码实际用法调研
- `~/git/jdk8u-dev/jdk/src/share/classes/java/math/MathContext.java`（String 解析与异常契约）。
- `~/git/jdk8u-dev/jdk/test/java/math/BigDecimal/`（配合 BigDecimal 舍入回归）。

## 4. 测试用例设计
- 常量精度/舍入模式；构造器（int 默认 HALF_UP、0 精度允许、String 需同时含 precision 与 roundingMode）。
- equals/hashCode/toString（含不同精度/模式不等）。
- 序列化 round-trip + serialVersionUID。
- 异常：负精度 → IllegalArgumentException；null RoundingMode/String → NPE；String 缺 roundingMode/负精度/垃圾 → IllegalArgumentException。
- 确定性：固定输入；headless-safe（无 `@key`）。
