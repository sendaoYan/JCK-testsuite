# java.time.format.DateTimeFormatterBuilder 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
建造者：`appendValue/appendText/appendFraction/appendOffset/appendZoneId/appendPattern/appendLiteral/appendOptional/append(Formatter)/padNext/parseDefaulting/optionalStart/optionalEnd`、`toFormatter([Locale])`、`getLocalizedDateTimePattern`（JDK-8 为 DateTimeFormatter 的静态方法）、`LocalizedOffset` 等。

## 2. Java 用户如何使用
```java
DateTimeFormatter f = new DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd")
    .optionalStart().appendOffset("+HH:MM", "Z").optionalEnd()
    .toFormatter(Locale.ENGLISH);
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- appendValue SignStyle 四态矩阵（NORMAL/ALWAYS/NEVER/EXCEEDS_PAD：`02024` 补零、`+12345` 仅当位数>minWidth 才出符号、parse 拒收裸 `+` DPE index 0）；appendFraction min/max 位数与 `.5` 失败 `.500` 成功；appendOffset(+HH:MM, Z)；appendPattern 非法字母 IAE 精确 message（`Unknown pattern letter: X`、`Too many pattern letters: W`）；DecimalStyle 注入。

### 易错细节（JDK-8 实测锁定）
- SignStyle EXCEEDS_PAD 的符号仅在位数超过 minWidth 时出现（最容易写错）。
- `30.5` 解析错误的文本 JDK-8/JDK-27 不同——按侧锁定。
