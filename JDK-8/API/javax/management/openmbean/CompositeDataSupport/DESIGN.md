# javax.management.openmbean.CompositeDataSupport — 测试设计

## 1. API 功能
CompositeData 参考实现（Serializable）：`(CompositeType, String[] itemNames, Object[] itemValues)` 与
`(CompositeType, Map<String,?>)`；get/getAll/containsKey/containsValue/values（不可变）。

## 2. Java 用户如何使用
按 type 逐字段给值构造行数据；Map 形态便于动态组装；null 值合法（仅非 null 值做类型校验）。

## 3. 参考来源
- Oracle javadoc：CompositeDataSupport（异常表、null 值语义、equals 定义）。
- OpenJDK test/jdk/javax/management/openmbean/NullConstructorParamsTest.java（思路借鉴）。
- 本地实测探针（双侧）。

## 4. 测试用例设计（CompositeDataSupportTest.java）
- 正例：get/getAll（请求顺序返回、null/空 → 空数组）、containsKey/containsValue、
  values 内容 + add→UnsupportedOperationException、**null 值对任意字段类型合法**（INTEGER 字段
  存 null 成功）、Map 构造等价、参数顺序无关 equals、toString 结构（compositeType=/contents=
  /name=n/value=5）。
- equals/hashCode：同构同值 true（对称）、异值/异 type false、null/异类 false；
  **参考实现 quirk：内容全等的手写 CompositeData 不相等**（CompositeDataSupport.equals 要求
  instanceof CompositeDataSupport，接口 javadoc 的"建议按内容相等"未被参考实现采纳）。
- 序列化 round-trip（含 null 字段值）。
- 负例精确表：**names/values 长度不等 → IllegalArgumentException "Different lengths: itemNames[2],
  itemValues[1]"（不是 OpenDataException！）**；null 数组 → IAE "Null itemNames or itemValues"；
  值类型错 → OpenDataException "Argument value of wrong type for item <名>: value <值>, type <type>"；
  names 与 type 不匹配 → OpenDataException "Item names do not match CompositeType: … [extra]; … [value]"；
  null type → IAE "Argument compositeType cannot be null."；Map null → IAE "Null or empty items map"、
  Map null key → IAE "Null or empty item name"、Map 缺项/错型 → 同上 OpenDataException；
  get 缺失 → InvalidKeyException "Argument key=\"missing\" is not an existing item name…"；
  get null/空白 key、getAll null 元素 → IAE "Argument key cannot be a null or empty String."。

### 版本差异（javap + 实测，JDK-8 vs JDK-27）
- 公开 API 面双侧完全一致（javap -public 对照）。
- 已锁定的精确异常消息（IAE/OpenDataException/InvalidKeyException/KeyAlreadyExistsException/
  InvalidOpenTypeException 及带消息 NPE）双侧逐字相同。
- 无消息型异常双侧类型一致、消息文本不同：JDK-27 NPE 带 helpful message（如
  "Cannot invoke ... because elementType is null"）、CCE 带 module/loader 说明，JDK-8 无；
  此类只断言异常类型。
- 反射深入度差异：对 java.management 内部位做 setAccessible 时 JDK-9+ 抛
  InaccessibleObjectException（模块未 open），JDK-8 可打开；测试对两种路径均接受。
