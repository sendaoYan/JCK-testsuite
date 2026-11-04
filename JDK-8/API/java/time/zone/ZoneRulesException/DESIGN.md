# java.time.zone.ZoneRulesException 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
ZoneId 解析失败异常（DateTimeException 子类）：`(String)`、`(String,Throwable)`。

## 2. Java 用户如何使用
```java
try { ZoneId.of("Bogus/Zone"); }
catch (ZoneRulesException e) { e.getMessage(); }
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 构造/message/cause；`ZoneId.of("Bogus/Zone")` 与三字母未映射 `EST` 抛 `Unknown time-zone ID: ...` 精确 message；toString 全限定名。

### 易错细节（JDK-8 实测锁定）
- ZRE 是 DateTimeException 的子类（catch 顺序敏感）。
- `EST` 这类 ID 在 SHORT_IDS 映射里才可用，直接 of 抛 ZRE。
