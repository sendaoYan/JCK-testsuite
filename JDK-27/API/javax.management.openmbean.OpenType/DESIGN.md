# javax.management.openmbean.OpenType — 测试设计

## 1. API 功能
open data 类型的抽象基类：protected 构造 `OpenType(className, typeName, description)`（校验非空、className
在 ALLOWED_CLASSNAMES 白名单内）。查询 getClassName/getTypeName/getDescription/isArray；
抽象 isValue/equals/hashCode/toString。常量 ALLOWED_CLASSNAMES_LIST（14 标量 + CompositeData + TabularData）
与 @Deprecated ALLOWED_CLASSNAMES 数组。

## 2. Java 用户如何使用
用户不直接 new OpenType，而是通过子类 SimpleType/ArrayType/CompositeType/TabularType 获得实例，
统一以 OpenType<?> 作为 CompositeType/TabularType 的 item 类型，调用 getClassName/isValue 做通用处理。
链路：子类实例 → getClassName/getTypeName/getDescription/isArray → isValue 判值。

## 3. 开源代码实际用法调研
- JDK 自用：`com.sun.jmx.mbeanserver.OpenType`（映射层）大量以 OpenType 为基类型做 MXBean 映射与校验；
  `OpenMBean*InfoSupport` 以 `OpenType<?> openType` 字段存储（`~/git/jdk25u-dev/src/java.management/share/classes/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/OpenTypeDescriptorTest.java`
  覆盖 getDescriptor/openType 字段。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（OpenTypeTest.java）
- 白盒：public abstract、implements Serializable、protected 构造器、抽象 isValue/equals/hashCode/toString、
  ALLOWED_CLASSNAMES_LIST 内容（16 项）、ALLOWED_CLASSNAMES 字段反射存在。
- 黑盒：经 SimpleType/ArrayType/CompositeType/TabularType 实例断言 getClassName/getTypeName/getDescription/isArray
  与 isValue；跨子类 equals/hashCode/toString 契约。
