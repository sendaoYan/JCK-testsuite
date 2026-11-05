# java.lang.runtime.ExactConversionsSupport — 设计方案

## 1. API 功能
final 工具类（JDK 21+ preview 特性 `float/double → 整型` 精确转换的支撑）：
- 21 个 public static 谓词：`isIntToByte/Short/Char/FloatExact`、`isLongToByte/Short/Char/Int/Float/DoubleExact`、`isFloatToByte/Short/Char/Int/LongExact`、`isDoubleToByte/Short/Char/Int/Long/FloatExact`
- 语义：`n == (目标)((源)n)` 往返表示等价；float→int 排除 0x1p31f、float→long 排除 0x1p63f、double→long 排除 0x1p63（饱和边界）；负零一律非精确

## 2. Java 用户如何使用
`float x -> int` 转换前判断是否无损（避免隐式丢精度）：
```java
if (ExactConversionsSupport.isFloatToIntExact(f)) int i = (int) f;
else /* fallback */;
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/runtime/ExactConversionsSupport.java`：实现即 `n == (float)(short)n && !isNegativeZero(n)` 形态；边界排除项在源码可见。
- JDK `test/jdk/java/lang/runtime/ExactnessConversionsSupportTest.java`：按源类型分组的精确/失真用例（2 的幂边界、0.1/0.5、NaN/Inf、负零）。
- 该类主要被 javac 为“精确转换 instanceof/switch 模式”（preview）生成的代码调用。

## 4. 测试用例设计 + 自动化实现
白盒：final、无 public 构造器、恰 21 个 `is*Exact` public static boolean 谓词。
黑盒（按 4 组源类型全覆盖，边界取自实现语义）：
- int：字节/短整/字符边界值（127/-128/32767/65535/-1）与 2^24 浮点尾数边界；
- long：int 边界、2^53 double 边界、Long.MAX_VALUE→double 失真；
- float：精确表示值（2e9f、-32768.0f）、小数（100.5f）失真、NaN/±Inf、-0.0f 排除；
- double：int/long 饱和边界（0x1p63 排除、1e19 溢出）、2^53 精确、0.5→float 精确 vs 0.1 失真、NaN。
对应 `ExactConversionsSupportTest.java` 的 `testBlackBox()`。

## 5. 契约深化（本批）
- 21 个谓词 × 边界值矩阵：期望值由 JLS 5.1.3 规则公式计算（整型目标=范围检查、FP 目标=round-trip、统一排除负零、long→float/double 排除 Long.MAX_VALUE、double→long 排除 2^63）。
- 公式先在 jdk-27.0.1 上与实现逐值对照 438/438 全一致后嵌入（断言的是规范公式，不是实现镜像）。
- NaN/±Infinity 对所有目标均为 false；isDoubleToFloatExact 仅要求 (double)(float)v == v。
- 边界点覆盖：各目标类型的 MIN/MAX±1、2^24/2^53（float/double 尾数边界）、-0.0、0.5（非整）。
