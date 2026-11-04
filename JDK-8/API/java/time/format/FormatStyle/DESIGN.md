# java.time.format.FormatStyle 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
枚举 FULL/LONG/MEDIUM/SHORT：`values/valueOf`，配合 ofLocalizedDateTime/ofLocalizedDate。

## 2. Java 用户如何使用
```java
DateTimeFormatter f = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 四档格式化同一 LocalDateTime 的精确串（MEDIUM 含 `T`→本地化形式）；ofLocalizedDate 与 ofLocalizedDateTime 组合。

### 易错细节（JDK-8 实测锁定）
- JDK-8 与 JDK-27 的 MEDIUM/SHORT 千分位/逗号细节不同（CLDR）——按侧锁定。
- enum 本身无状态方法，覆盖 values 顺序即可。
