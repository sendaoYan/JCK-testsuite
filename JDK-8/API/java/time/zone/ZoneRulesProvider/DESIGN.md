# java.time.zone.ZoneRulesProvider 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
SPI 抽象类：静态 `getAvailableZoneIds/getRules/getVersions/registerProvider/refresh`；受保护 `provideZoneIds/provideRules/provideVersions/provideRefresh`。

## 2. Java 用户如何使用
```java
ZoneRules r = ZoneRulesProvider.getRules("Europe/Berlin", true);
Set<String> ids = ZoneRulesProvider.getAvailableZoneIds();
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 可用 ID 集合（>500，含 UTC/Europe/Berlin/America/New_York）；getRules 与 ZoneId.getRules 等价且缓存；`+01:00`（偏移式 ID）与 Bogus 抛 ZRE 精确 message；getVersions 返回 `NavigableMap<String,ZoneRules>`（版本→规则）；注册自定义 Provider（提供 MyZone/One +07:00）后 getAvailableZoneIds 可见、getRules/getVersions 走自定义实现、refresh 会咨询 provideRefresh；重复注册同 ID 抛 ZRE `Unable to register zone as one already registered with that ID: ...`；registerProvider(null) NPE `provider`。

### 易错细节（JDK-8 实测锁定）
- `getVersions(String)` 返回的是 **版本字符串→ZoneRules**，不是版本列表（易写错泛型）。
- **JDK-8 无 `getProviders()` 方法**（不暴露 provider 清单）。
- 自定义 provider 的 `provideVersions(String)` 是单参签名（JDK-8/27 相同）。
