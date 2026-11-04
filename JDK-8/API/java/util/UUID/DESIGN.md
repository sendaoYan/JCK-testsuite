# java.util.UUID 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。randomUUID 仅断言结构（version/variant/长度）不做具体值；
> 其余用固定 UUID 字符串与固定 (most,least) 构造保证确定性。

## 1. API 功能

- **工厂**：`randomUUID()`、`nameUUIDFromBytes(byte[])`、`fromString(String)`。
- **构造器**：`UUID(long, long)`。
- **位访问**：`getMostSignificantBits()`、`getLeastSignificantBits()`。
- **字段**：`version()`、`variant()`、`timestamp()`、`clockSequence()`、`node()`。
- **对象**：`toString()`、`hashCode()`、`equals()`、`compareTo()`。
- **序列化**：`Serializable`。

## 2. 使用方式

`UUID.randomUUID()`；`UUID.fromString("123e4567-...")`；`UUID.nameUUIDFromBytes(bytes)`；
`uuid.compareTo(other)`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/UUID`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/UUID.java`：
  `fromString` 按 `-` split 成 5 段并 `Long.decode`；version 1 才允许 `timestamp/clockSequence/node`。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | randomUUID 结构（v4/variant2/长度） | testRandomUUID |
| P0 | fromString/toString 往返 + 位访问 + 非法 IAE | testFromStringToString |
| P0 | fromString null NPE/空/无连字符 IAE + 大写 | testFromStringErrors |
| P0 | nameUUIDFromBytes 确定性 + v3 | testNameUUID |
| P0 | equals/hashCode（含 null/异构） | testEqualsHashCode |
| P0 | (long,long) 构造器 | testConstructorBits |
| P1 | v1 的 timestamp/clockSequence/node + v4 UOE | testVersion1Accessors |
| P1 | compareTo 排序 | testCompareTo |
| P1 | Serializable round-trip | testSerialization |
| P0 | 契约深化：MD5 已知向量 + nil UUID + 位运算 | 见 Test 内「契约深化」区块 |

## 关键规范细节

- `fromString(null)` → NPE；非 `8-4-4-4-12` 连字符格式 → IAE（大写 hex 接受）。
- `timestamp()/clockSequence()/node()` 仅 version 1 可用，否则 `UnsupportedOperationException`。
- `compareTo` 先比 mostSignificantBits 再比 leastSignificantBits。

## 注意事项

固定 UUID 字符串/构造位；randomUUID 只断言结构；无全局副作用；headless-safe 不加 `@key`。
