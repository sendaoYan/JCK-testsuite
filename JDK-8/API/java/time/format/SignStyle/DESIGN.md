# java.time.format.SignStyle 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
枚举 NORMAL/ALWAYS/NEVER/EXCEEDS_PAD/NOT_NEGATIVE：appendValue 的符号策略。

## 2. Java 用户如何使用
```java
b.appendValue(ChronoField.YEAR, 4, 6, SignStyle.EXCEEDS_PAD);
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 五档正/负数 format 矩阵与 parse 接受度（EXCEEDS_PAD 仅位数>min 出符号；NOT_NEGATIVE 出 `cannot be negative according to the SignStyle` IAE）。

### 易错细节（JDK-8 实测锁定）
- EXCEEDS_PAD 的 `+12345`（minWidth=4）带符号而 `02024` 不带。
- NOT_NEGATIVE 对负值抛的 IAE message 是 `Field ... cannot be negative according to the SignStyle`。
