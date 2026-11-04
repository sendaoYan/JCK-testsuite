# java.time.temporal.TemporalQuery 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
函数接口 `queryFrom(TemporalAccessor)`；内建实例来自 TemporalQueries。

## 2. Java 用户如何使用
```java
TemporalQuery<Boolean> q = t -> t.isSupported(ChronoField.HOUR_OF_DAY);
LocalTime.NOON.query(q); // true
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 自定义 query（周几奇偶、字段存在性）；DateTimeFormatter.parse(query) 驱动。

### 易错细节（JDK-8 实测锁定）
- query 返回值允许任意类型（含 null、Boolean）。
- 接口无静态方法（JDK-8），`TemporalQuery.from` 不存在。
