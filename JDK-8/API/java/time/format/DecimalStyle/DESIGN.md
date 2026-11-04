# java.time.format.DecimalStyle 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
数字符号集：`getAvailableLocales`、`of(Locale)`、`getDefault()`、`withZeroDigit/withPositiveSign/withNegativeSign/withDecimalPoint`、`getZeroDigit/getPositiveSign/getNegativeSign/getDecimalPoint`、`equals/hashCode/toString`。

## 2. Java 用户如何使用
```java
DecimalStyle d = DecimalStyle.of(Locale.ENGLISH);
d.withZeroDigit('a');   // 0->a, 9->i 映射
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 标准 locale 的四件套 `0 + - .`；withZeroDigit('a') 后 format 的数字变 `f`（对应 5）；equals/hashCode；toString 精确 `DecimalStyle[0+-.]`；convertToDigit/convertNumberToI18N 行为。

### 易错细节（JDK-8 实测锁定）
- toString 是 `DecimalStyle[0+-.]`（无空格）。
- zeroDigit 替换按 ASCII 差值平移，`'a'` 把 `9` 映射到 `i`。
