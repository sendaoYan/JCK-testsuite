# java.time.ZoneOffset 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
值类 偏移：常量 `UTC/MIN/MAX`、`of(String)`、`ofHours/ofHoursMinutes/ofHoursMinutesSeconds/ofTotalSeconds`、`getTotalSeconds/getId`、`query/adjustInto`、`compareTo`。

## 2. Java 用户如何使用
```java
ZoneOffset o = ZoneOffset.ofHours(2);   // +02:00
ZoneOffset u = ZoneOffset.of("+02:00");
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- of 串格式矩阵（`Z/+HH:MM/+HHMM/+HH:MM:SS/±HHMMSS`）与非法串 NPE/DTEx 的精确 message（`+99` 超界 DTE、`Bogus` DTE）；总秒数 ±18 小时边界；compareTo 是原始秒差（大偏移排后）；adjustInto(LocalTime) UTE。

### 易错细节（JDK-8 实测锁定）
- `ZoneOffset.of("")` 抛 `Invalid ID for ZoneOffset, invalid format:`（结尾带空格冒号）。
- compareTo 返回原始差值而非 -1/0/1（`ofHours(1).compareTo(ofHours(2))` 是 3600 的负值）。
