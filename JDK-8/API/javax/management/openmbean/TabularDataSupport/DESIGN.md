# javax.management.openmbean.TabularDataSupport — 测试设计

## 1. API 功能
TabularData 唯一 JDK 实现，同时 implements Map<Object,Object>/Cloneable/Serializable。
构造仅 `(TabularType)` 与 `(TabularType, int, float)`（**无无参构造**；**null type 在构造期即抛
IAE**）；put/get/remove/containsKey 双重载（Object[] 语义版 + Map 兼容版）、calculateIndex、
putAll(CompositeData[])/putAll(Map)、keySet/values/entrySet（内部键为 List 形式的索引）、clone。

## 2. Java 用户如何使用
new TabularDataSupport(tabularType) → put(CompositeDataSupport 行) → 按 calculateIndex 结果
get/remove；Map 视图用于与 Map 生态互操作（注意 Map.get/remove 只认 Object[] 键）。

## 3. 参考来源
- Oracle javadoc：TabularDataSupport（异常表、Map 视图行为、put 返回值注记 bug 5090566）。
- OpenJDK 源码 ~/git/jdk8u-dev/jdk/src/share/classes/javax/management/openmbean/
  TabularDataSupport.java 与 ~/git/jdk 同文件（containsKey(Object) 吞 CCE 的实现）；test/jdk
  TabularDataOrderTest.java。
- 本地实测探针（双侧）。

## 4. 测试用例设计（TabularDataSupportTest.java）
- 白盒：implements TabularData+Map+Cloneable+Serializable；**无无参构造、无 put(CompositeType,
  String[],Object[]) 便捷形态**（javap 双侧一致，反射断言）。
- 生命周期：put 后 size/containsKey/get/containsValue 精确；remove 返回被删行且逐项验证；
  clear 后逐 key 验证全空（keySet/values/entrySet 同步空）。
- Map 视图（重点）：内部键为索引 List，keySet.contains(Arrays.asList(idx)) true；entrySet 键==
  索引 List、值==行；**Map.put 忽略 key、返回所放行本身**（源码注记 5090566），重复索引仍
  KeyAlreadyExistsException；Map.putAll(Map) 值须为行；**Map.containsKey(非数组键) 返回 false
  （CCE 被吞）而 Map.get/remove(非数组键) 抛 ClassCastException**——同一"键不合法"两种结局；
  keySet add→UnsupportedOperationException；values 为活视图（remove 后缩）。
- equals/hashCode/clone：同内容跨实例 equals、clone 深拷贝独立、序列化 round-trip 保留 type+行。
- 负例精确表：put 重复索引 → KeyAlreadyExistsException "Argument value's index, calculated
  according to this TabularData instance's tabularType, already refers to a value in this table."；
  putAll 内部重复 → KeyAlreadyExistsException **不同消息** "Argument elements values[1] and
  values[0] have the same indexes, …"；行类型不符 → InvalidOpenTypeException "Argument value's
  composite type [<t>] is not assignable to this TabularData instance's row type [<t>]."（calculateIndex
  同消息）；put/calculateIndex null → NPE "Argument value cannot be null."；get null/空键 → NPE
  "Argument key cannot be null or empty."；键长度错 → InvalidKeyException "Argument key's length=2
  is different from…"；键元素类型错 → InvalidKeyException "Argument element key[0] is not a value
  for the open type expected…"；(type,-1,.75)/(type,16,-1f) → IAE "Illegal initial capacity: -1"/
  "Illegal load factor: -1.0"（HashMap 语义）；new TabularDataSupport(null) → 构造期 IAE
  "Argument tabularType cannot be null."。

### 版本差异（javap + 实测，JDK-8 vs JDK-27）
- 公开 API 面双侧完全一致（javap -public 对照）。
- 已锁定的精确异常消息（IAE/OpenDataException/InvalidKeyException/KeyAlreadyExistsException/
  InvalidOpenTypeException 及带消息 NPE）双侧逐字相同。
- 无消息型异常双侧类型一致、消息文本不同：JDK-27 NPE 带 helpful message（如
  "Cannot invoke ... because elementType is null"）、CCE 带 module/loader 说明，JDK-8 无；
  此类只断言异常类型。
- 反射深入度差异：对 java.management 内部位做 setAccessible 时 JDK-9+ 抛
  InaccessibleObjectException（模块未 open），JDK-8 可打开；测试对两种路径均接受。
