# javax.management.openmbean.OpenType — 测试设计

## 1. API 功能
open 类型系统的抽象根：`OpenType(String typeName, String description, String className)`（protected），
实现 Serializable；公开方法 getClassName/getTypeName/getDescription/isArray/isValue/equals/
hashCode/toString（后四个 abstract）；静态常量 ALLOWED_CLASSNAMES_LIST（16 项，不可变 List）与
已弃用数组 ALLOWED_CLASSNAMES。**javap 双侧确认无 isValid/getDisplayName/setDisplayName**。

## 2. Java 用户如何使用
不直接实例化；经 SimpleType/ArrayType/CompositeType/TabularType 子类获得实例，向 CompositeType/
TabularType/ArrayType 递归组装，用 getClassName 判目标 Java 类、isValue 验证实例。

## 3. 参考来源
- Oracle javadoc：javax.management.openmbean.OpenType（ALLOWED_CLASSNAMES 16 项清单、构造约束）。
- OpenJDK test/jdk/javax/management/openmbean/IsValueTest.java、EqualsTest.java、
  OpenTypeDescriptorTest.java（~/git/jdk8u-dev 与 ~/git/jdk 同名目录，仅借鉴思路）。
- 本地实测探针（双侧 JDK 8u372 / 27.0.1 行为对照）。

## 4. 测试用例设计（OpenTypeTest.java）
- 白盒：abstract、Serializable、protected (String,String,String) 构造器、另有 1 个包级 4 参构造
  （共 2 个 declared ctor）、isValue/equals/hashCode/toString 均 abstract、
  isValid/getDisplayName/setDisplayName 不存在于公开面（反射断言）。
- 常量：ALLOWED_CLASSNAMES_LIST 精确 16 项内容、set→UnsupportedOperationException、
  弃用数组内容与 List 一致（数组本身可写——记录）。
- 子类实证：SimpleType/ArrayType/CompositeType/TabularType 的 getClassName/getTypeName/
  getDescription/isArray 精确值；isValue(null) 四子类一律 false；跨类型 equals false；
  序列化 round-trip（SimpleType 复活为同一单例）。
- 抽象不可实例化：反射 newInstance → JDK-8 InstantiationException（直接抛或 InvocationTargetException
  包装）；JDK-27 setAccessible 即 InaccessibleObjectException。两种路径都视为通过并记录。

### 版本差异（javap + 实测，JDK-8 vs JDK-27）
- 公开 API 面双侧完全一致（javap -public 对照）。
- 已锁定的精确异常消息（IAE/OpenDataException/InvalidKeyException/KeyAlreadyExistsException/
  InvalidOpenTypeException 及带消息 NPE）双侧逐字相同。
- 无消息型异常双侧类型一致、消息文本不同：JDK-27 NPE 带 helpful message（如
  "Cannot invoke ... because elementType is null"）、CCE 带 module/loader 说明，JDK-8 无；
  此类只断言异常类型。
- 反射深入度差异：对 java.management 内部位做 setAccessible 时 JDK-9+ 抛
  InaccessibleObjectException（模块未 open），JDK-8 可打开；测试对两种路径均接受。
