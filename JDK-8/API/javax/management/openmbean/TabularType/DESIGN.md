# javax.management.openmbean.TabularType — 测试设计

## 1. API 功能
表类型：`TabularType(typeName, description, CompositeType rowType, String[] indexNames)`；
getRowType/getIndexNames（保序、不可变 List）；isValue 按同构 tabularType 判定。

## 2. Java 用户如何使用
声明行结构 + 索引列（如 Map<K,V> 的 MXBean 映射：key/value 两列、indexNames=["key"]）。

## 3. 参考来源
- Oracle javadoc：TabularType（indexNames 必须 ⊆ rowType 字段集）。
- OpenJDK test/jdk/javax/management/openmbean/EqualsTest.java、TabularDataOrderTest.java（思路借鉴）。
- 本地实测探针（双侧）。

## 4. 测试用例设计（TabularTypeTest.java）
- 正例：getter 精确、className==TabularData、getIndexNames 保序（[value,name] 不排序）且
  add→UnsupportedOperationException、多索引顺序保留。
- equals/hashCode：typeName/indexNames/rowType 全参与；**结构等价的不同 rowType 实例 equals true**。
- isValue：同类型/结构等价类型 true、null/异类型 false；toString 精确全串（含 rowType 嵌套串与
  indexNames=(name) 形式）；序列化 round-trip。
- 负例精确表：索引名不在 rowType → **OpenDataException** "Argument's element value indexNames[0]=
  \"nope\" is not a valid item name for rowType."；null rowType → IAE "Argument rowType cannot be null."
  （带句号）；null/空 indexNames 数组 → IAE "Argument indexNames[] cannot be null or empty."；
  null/空索引名元素 → IAE；null/空 typeName、null description → IAE（无句号）。
- **重复索引名不抛异常**（实测允许，getIndexNames 保留 [name, name]）——规范未禁止。

### 版本差异（javap + 实测，JDK-8 vs JDK-27）
- 公开 API 面双侧完全一致（javap -public 对照）。
- 已锁定的精确异常消息（IAE/OpenDataException/InvalidKeyException/KeyAlreadyExistsException/
  InvalidOpenTypeException 及带消息 NPE）双侧逐字相同。
- 无消息型异常双侧类型一致、消息文本不同：JDK-27 NPE 带 helpful message（如
  "Cannot invoke ... because elementType is null"）、CCE 带 module/loader 说明，JDK-8 无；
  此类只断言异常类型。
- 反射深入度差异：对 java.management 内部位做 setAccessible 时 JDK-9+ 抛
  InaccessibleObjectException（模块未 open），JDK-8 可打开；测试对两种路径均接受。
