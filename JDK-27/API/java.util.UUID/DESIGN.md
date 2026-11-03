# java.util.UUID 测试设计

## 1. API 功能
- 工厂：`randomUUID()`、`fromString`、`nameUUIDFromBytes`、`ofEpochMillis`。
- 构造器：`UUID(long msb, long lsb)`。
- 位访问：`getMostSignificantBits`、`getLeastSignificantBits`、`version`、`variant`、`timestamp`、`clockSequence`、`node`。
- 转换/比较：`toString`、`equals`、`hashCode`、`compareTo`。

## 2. Java 用户如何使用
- 生成随机 ID：`UUID.randomUUID()`。
- 解析/序列化：`UUID.fromString(s)` / `u.toString()`。
- 命名派生：`UUID.nameUUIDFromBytes(bytes)`。
- 排序：`a.compareTo(b)`、`TreeSet<UUID>`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/UUID.java`：`randomUUID()` 委托 `SecureRandom`。
- JDK 自带测试 `test/jdk/java/util/UUID/UUIDTest.java`：解析、variant/version、nameUUID 一致性。
- 广泛用法（数据库主键/分布式 ID）：`UUID.randomUUID().toString()` 作唯一标识。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言 final 类、实现 Serializable/Comparable、关键方法存在。
- 黑盒（确定性部分固定输入）：
  - `randomUUID` 版本 4/变体 2、36 位长度、随机差异。
  - `fromString`/`toString` 往返、位解析、非法输入抛 IAE。
  - `(msb,lsb)` 构造器与 version/variant 推导。
  - `nameUUIDFromBytes` 确定性与版本 3/变体 2。
  - equals/hashCode、compareTo 符号与相等。
