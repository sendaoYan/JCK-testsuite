# javax.management.openmbean.CompositeType — 测试设计

## 1. API 功能
复合类型：`CompositeType(typeName, description, itemNames[], itemDescriptions[], itemTypes[])`；
containsKey/getType/getDescription(String)/keySet（升序、不可变）；isValue 按"可赋值子集"判定。

## 2. Java 用户如何使用
描述结构化数据（如 MXBean 复合返回值）：字段名→OpenType 表 + keySet 驱动 CompositeDataSupport 构造。

## 3. 参考来源
- Oracle javadoc：CompositeType（构造异常表、isValue 可赋值性语义）。
- OpenJDK test/jdk/javax/management/openmbean/IsValueTest.java、EqualsTest.java、
  NullConstructorParamsTest.java（思路借鉴）。
- 本地实测探针（双侧）。

## 4. 测试用例设计（CompositeTypeTest.java）
- 正例：全 getter 精确、containsKey/getType/getDescription 命中与缺失（missing/null → null/false）、
  keySet 升序 + add→UnsupportedOperationException、**item name 存入前 trim**（" name "→name）。
- isValue 矩阵：同类型 true、含 null 字段值 true、null/非 CompositeData false、子集类型接受超集
  实例 / 全类型拒绝缺字段、错字段类型 false、异 typeName false。
- equals/hashCode：不同顺序 names 相同集合 equals true；typeName/字段集/字段类型参与；
  **描述（type 级与 item 级）不参与 equals**（双侧实测）。
- 序列化 round-trip equals+hashCode；toString 精确全串（items 按名升序）。
- 负例精确表（消息双侧逐字相同）：长度不等 → IAE "Array arguments itemNames[], itemDescriptions[]
  and itemTypes[] should be of same length (got 1, 1 and 2)."；null/空 names 数组 → IAE "Argument
  itemNames[] cannot be null or empty."（**0 字段类型不允许**）；null name 元素 → IAE "…itemNames[0]
  cannot be null."；空 name → IAE "…cannot be an empty string."；null 描述元素 → IAE；null 类型元素
  → IAE "…itemTypes[0] cannot be null."；重复名 → **OpenDataException** "…itemNames[1]=\"a\"
  duplicates a previous item names."；null/空 typeName、null description → IAE（无句号结尾变体，注意
  与 TabularType 的 "rowType cannot be null." 带句号不同）。

### 版本差异（javap + 实测，JDK-8 vs JDK-27）
- 公开 API 面双侧完全一致（javap -public 对照）。
- 已锁定的精确异常消息（IAE/OpenDataException/InvalidKeyException/KeyAlreadyExistsException/
  InvalidOpenTypeException 及带消息 NPE）双侧逐字相同。
- 无消息型异常双侧类型一致、消息文本不同：JDK-27 NPE 带 helpful message（如
  "Cannot invoke ... because elementType is null"）、CCE 带 module/loader 说明，JDK-8 无；
  此类只断言异常类型。
- 反射深入度差异：对 java.management 内部位做 setAccessible 时 JDK-9+ 抛
  InaccessibleObjectException（模块未 open），JDK-8 可打开；测试对两种路径均接受。
