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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 精确消息逐字表（实测，两侧一致）：负精度 → IllegalArgumentException "Digits < 0"（不是直觉的 "precision < 0"）；(int,null)/(String)null → NPE "null RoundingMode"/"null String"；坏串（"garbage"/缺舍入模式/非数字精度/未知模式/缺精度）→ "bad string format"。
- toString 全等矩阵：DECIMAL32/64/128/UNLIMITED 与 MC(5) 全等；toString → String 构造解析往返（equals/precision/roundingMode 三重保留）。
- equals/hashCode 语义：DECIMAL32.equals(new MathContext(7,HALF_EVEN)) 等 4 组恒等式；equals ⇒ hashCode 相等；两次调用稳定；getRoundingMode 返回同一枚举实例。
- 序列化补充：precision/mode/toString 保留；UNLIMITED(0,HALF_UP) 合法。
- 与 BigDecimal 联动：DECIMAL32 round("123456789")→"1.234568E+8"、UNLIMITED 不舍入、MC(4,DOWN)。

### 2. 使用方式
新增 `checkMsg/testExactMessages/testToStringParseMatrix/testEqualsHashSemantics/testSerializationExtra` 并在 main 追加调用（只追加不删除）。

### 3. 参考来源
两侧探针实测；Oracle javadoc（MathContext.toString 格式、Digits < 0）；JDK 源码 MathContext.java。

### 4. 测试用例设计 + 易错规范细节
- 断言数 31 → 95。易错点：①DECIMAL32.hashCode() 数值两侧不同（1889937827 vs -1155810595，未规范）——只锁 equals 一致性不锁数值；②消息是 "Digits < 0" 不是 "precision < 0"；③MC(0) 与 MC(0,UNNECESSARY) 合法（UNLIMITED 即 precision 0）。
