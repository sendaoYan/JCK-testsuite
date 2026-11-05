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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 精确消息逐字表（两侧一致）：valueOf(8)/valueOf(-1)/valueOf(MAX) → "argument out of range"；valueOf("FOO") → "No enum constant java.math.RoundingMode.FOO"；valueOf((String)null) → NPE "Name is null"。
- 旧 BigDecimal.ROUND_* 常量（0..7）→ RoundingMode.valueOf(int) 全映射表（ROUND_UP=0 ... ROUND_UNNECESSARY=7）。
- setScale(0,mode) ±2.5 八模式核心表（CEILING/FLOOR 关于 0 不对称：2.5→3/-2.5→-2 vs FLOOR 2/-3）；HALF_EVEN 就偶表（3.5→4、4.5→4、5.5→6）；UP/DOWN/CEILING/FLOOR 方向样例；UNNECESSARY → "Rounding necessary" 逐字、精确时恒等。
- 枚举契约：values() 每次新数组、compareTo==ordinal 差、valueOf(name) 同一实例、toString==name、Enum.valueOf 泛型形态、getDeclaringClass。

### 2. 使用方式
新增 `checkMsg/testExactMessages/testLegacyConstantMapping/testSetScaleCoreTable/testEnumContract` 并在 main 追加调用（只追加不删除；新增 import java.math.BigDecimal 供 ROUND_* 常量引用）。

### 3. 参考来源
两侧探针实测；Oracle javadoc（RoundingMode 八模式语义表、valueOf(int) 旧常量映射）；JDK 源码 RoundingMode.java。

### 4. 测试用例设计 + 易错规范细节
- 断言数 40 → 112。易错点：①valueOf(int) 越界消息是 "argument out of range"（Object.hashCode 式措辞）；②大小写敏感（"half_up" 报 No enum constant）；③±2.5 表中 HALF_UP/HALF_DOWN/HALF_EVEN 对 2.5 分别 3/2/2 而 -2.5 是 -3/-2/-2；④UNNECESSARY 的 AE 消息复用 BigDecimal 的 "Rounding necessary"。
