# javax.management.openmbean.OpenMBeanInfoSupport — 测试设计

## 1. API 功能
OpenMBeanInfo 接口实现：聚合 open MBean 的类名、描述、属性、构造器、操作与通知。构造器
（className, description, attributes, constructors, operations, notifications）校验各类数组非 null（通知可 null）。
查询 getClassName/getDescription/getAttributes/getConstructors/getOperations/getNotifications/equals/hashCode/toString。

## 2. Java 用户如何使用
先造各类 InfoSupport 数组，再 new OpenMBeanInfoSupport 聚合为完整 MBean 元数据，用于 OpenMBeanInfo 呈现或注册。
链路：各 Info 数组 → OpenMBeanInfoSupport → getAttributes/getConstructors/getOperations → equals/toString。

## 3. 开源代码实际用法调研
- JDK 自用：MXBean 映射用 OpenMBeanInfoSupport 组装完整元数据
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/OpenMBeanInfoSupportTest.java`
  覆盖聚合 getter 与 equals。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（OpenMBeanInfoSupportTest.java）
- 白盒：public、implements OpenMBeanInfo、get 系列方法存在。
- 黑盒：构造后 getClassName/getDescription；getAttributes/getConstructors/getOperations/getNotifications 副本；
  equals/hashCode（等值相等、字段不同不等）；toString 前缀与字段；异常精确 message（null className NPE/IAE、
  null attributes/constructors/operations IAE）。
