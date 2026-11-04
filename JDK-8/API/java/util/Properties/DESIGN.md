# java.util.Properties 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。构造器矩阵（空构造、defaults 链接、`Properties(null defaults)` 等价空、
> **不抛 NPE**）、setProperty/getProperty 只认 String + 精确 null NPE、defaults 回退/覆盖/remove/多级链、
> propertyNames vs stringPropertyNames（后者为**非 live 快照**、省略非 String key/value、Hashtable keySet
> 支持 remove 但不影响 Properties）、store/load round-trip（转义 `=`/`:`/`#`/`!`/前导空格、null comments、
> 不写 defaults）、load 边界（注释/空行/续行/转义分隔符、坏 unicode 转义 IAE、null reader/stream NPE）、
> store 异常（null writer/stream NPE、非 String key/value CCE）、storeToXML/loadFromXML round-trip + NPE +
> InvalidPropertiesFormatException + UnsupportedEncodingException（**JDK-8 storeToXML 静默跳过非 String 条目**，
> 不抛 CCE）、序列化 round-trip 保留 defaults + serialVersionUID、状态不变量。

## 1. API 提供了哪些功能

- **构造器（2）**：`Properties()`、`Properties(Properties defaults)`（defaults 为 null 时等价空，**不抛异常**）。
- **属性读写**：`setProperty(String,String)`（返回旧值，非 String/null 经 Hashtable.put 拒绝）、
  `getProperty(String)`、`getProperty(String,String default)`。
- **持久化**：`load(Reader/InputStream)`、`store(Writer/OutputStream, String)`、
  `loadFromXML(InputStream)`、`storeToXML(OutputStream, String[, encoding])`。
- **键集合**：`propertyNames()`（Enumeration，含 defaults；非 String key 抛 CCE）、
  `stringPropertyNames()`（Set，省略非 String key/value 条目）。
- **契约继承**：extends `Hashtable`（`serialVersionUID == 4112578634029874840L`，defaults 字段随默认序列化保留）。

## 2. Java 用户如何使用

- 读配置：`new Properties()` → `load(reader)` → `getProperty(key, default)`。
- 写配置：`store(writer, "comment")`（只写自身条目，不写 defaults）。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Properties.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Properties.java`
  （构造器不判空；`getProperty` 只认 String；`enumerate` 对非 String key 抛 CCE；`store0` 对非 String
  key/value 抛 CCE；`storeToXML` 经 `jdk.internal.util.xml.PropertiesDefaultHandler` 静默跳过非 String 条目）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/Properties`。

## 4. 测试用例设计（映射到 PropertiesTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器矩阵（含 null defaults 等价空） | testConstructorMatrix |
| P0 | set/getProperty String-only + null NPE | testSetGetProperty |
| P0 | defaults 回退/覆盖/多级链 | testDefaultsChain |
| P0 | propertyNames vs stringPropertyNames | testPropertyNames |
| P0 | store/load round-trip（转义、null comments、不写 defaults） | testStoreLoadRoundTrip |
| P0 | load 边界（注释/续行/转义/坏 unicode IAE/null NPE） | testLoadEdgeCases |
| P0 | store 异常（null NPE、非 String CCE） | testStoreErrors |
| P1 | storeToXML/loadFromXML（NPE/IPFE/UEE、非 String 跳过） | testStoreToXMLLoadFromXML |
| P1 | 序列化 round-trip 保留 defaults + serialVersionUID | testSerialization |
| P1 | 状态不变量 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- **`Properties(null defaults)` 不抛 NPE**（构造器仅 `this.defaults = defaults`），等价于无 defaults 的空 Properties。
- **Hashtable 继承拒绝 null**：`setProperty(null,·)`、`setProperty(k, null)`、`put(k, null)`、`put(null, v)`、
  `getProperty(null)` 均抛 NPE（Hashtable.put/get 的 `requireNonNull`）。
- `getProperty` 只认 String：非 String value（`put("n", Integer)`）被忽略返回 null；`getProperty(key, default)`
  对这种情况返回 default。
- `store(Writer/OutputStream)` 只写自身条目，**不写 defaults**；null writer/stream 抛 NPE；
  非 String key 或 value 抛 CCE。
- `stringPropertyNames()` 返回**非 live 快照**（JDK-8 是新建 Hashtable 的 keySet，remove 只影响该临时表，
  不影响 Properties；也不支持 add）；省略 key 或 value 非 String 的条目。
- `propertyNames()` 对非 String key 抛 CCE（`enumerate` 强转 key），但 value 非 String 不抛。
- `load`：注释 `#`/`!` 与空行忽略；续行 `\`+换行；转义分隔符 `\=`/`\:`；key 中空格需转义（`\ `）；
  坏 unicode 转义（`\uZZZZ`）抛 IllegalArgumentException；null reader/stream 抛 NPE。
- **JDK-8 差异**：`storeToXML` 静默**跳过**非 String key/value 条目（不抛 CCE，JDK-27 会抛 CCE）。
  本测试只锁 JDK-8 的跳过行为，避免跨版本漂移。
- `storeToXML` null os 抛 NPE；`loadFromXML` null in 抛 NPE、坏 XML 抛 InvalidPropertiesFormatException、
  坏 encoding 抛 UnsupportedEncodingException。
- 序列化 round-trip **保留 defaults**（`defaults` 字段 `@serial`）；`serialVersionUID == 4112578634029874840L`。

## 注意事项

- 无 GC/图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。
