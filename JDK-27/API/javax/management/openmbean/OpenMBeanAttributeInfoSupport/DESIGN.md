# javax.management.openmbean.OpenMBeanAttributeInfoSupport — 测试设计

## 1. API 功能
OpenMBeanAttributeInfo 接口实现：属性名、描述、openType、isReadable/isWritable/isIs 与默认值/合法值
（继承 MBeanAttributeInfo 的语义 + openType）。构造器含 boolean isReadable/isWritable/isIs。
查询 getName/getDescription/getOpenType/isReadable/isWritable/isIs/getDefaultValue/getMinValue/getMaxValue/
getLegalValues/isValue/hasDefaultValue/hasLegalValues/equals/hashCode/toString。

## 2. Java 用户如何使用
描述 OpenMBean 的可读/可写属性：new OpenMBeanAttributeInfoSupport(name, desc, openType, isReadable, isWritable,
isIs[, defaultValue...])。链路：构造 → isReadable/isWritable/isIs → getOpenType → isValue/hasDefaultValue → equals/toString。

## 3. 开源代码实际用法调研
- JDK 自用：MXBean 映射用 OpenMBeanAttributeInfoSupport 描述属性（含读写位与 is 标志）
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/OpenMBeanAttributeInfoSupportTest.java`、
  `EqualsTest.java` 覆盖读写位与 equals。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（OpenMBeanAttributeInfoSupportTest.java）
- 白盒：public、implements OpenMBeanAttributeInfo、继承 MBeanAttributeInfo、get/is 系列方法存在。
- 黑盒：构造（含/不含默认值）getter 组合；isReadable/isWritable/isIs 取值；getOpenType；isValue；
  equals/hashCode（等值相等、读写位/is 不同不等）；toString 前缀；异常精确 message（null name NPE/IAE、默认值非法）。
