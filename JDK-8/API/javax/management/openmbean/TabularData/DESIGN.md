# javax.management.openmbean.TabularData — 测试设计

## 1. API 功能
表数据接口（16 个声明方法）：getTabularType/calculateIndex/size/isEmpty/containsKey/containsValue/
get/put/remove/putAll/clear/keySet/values/equals/hashCode/toString。**无 3 参便捷 put**。

## 2. Java 用户如何使用
按索引列值（Object[]，顺序==indexNames）put/get/remove 行（CompositeData）；MXBean 的 Map 映射
即落在 TabularDataSupport 上。

## 3. 参考来源
- Oracle javadoc：TabularData（索引键语义）。
- OpenJDK test/jdk/javax/management/openmbean/TabularDataOrderTest.java（思路借鉴）。
- 本地实测探针（双侧）。

## 4. 测试用例设计（TabularDataTest.java）
- 白盒：16 方法精确集合锁定、无 3 参 put、不 extends Serializable。
- 参考实现实证：calculateIndex 顺序跟随 indexNames（单/多索引）、put/containsKey/containsValue/
  get/remove/putAll/clear 全生命周期、keySet/values 尺寸、跨实例同内容 equals、toString、
  缺失 get/remove 返回 null。

### 版本差异（javap + 实测，JDK-8 vs JDK-27）
- 公开 API 面双侧完全一致（javap -public 对照）。
- 已锁定的精确异常消息（IAE/OpenDataException/InvalidKeyException/KeyAlreadyExistsException/
  InvalidOpenTypeException 及带消息 NPE）双侧逐字相同。
- 无消息型异常双侧类型一致、消息文本不同：JDK-27 NPE 带 helpful message（如
  "Cannot invoke ... because elementType is null"）、CCE 带 module/loader 说明，JDK-8 无；
  此类只断言异常类型。
- 反射深入度差异：对 java.management 内部位做 setAccessible 时 JDK-9+ 抛
  InaccessibleObjectException（模块未 open），JDK-8 可打开；测试对两种路径均接受。
