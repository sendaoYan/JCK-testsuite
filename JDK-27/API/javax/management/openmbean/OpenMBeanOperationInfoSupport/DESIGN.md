# javax.management.openmbean.OpenMBeanOperationInfoSupport — 测试设计

## 1. API 功能
OpenMBeanOperationInfo 接口实现：操作名、描述、签名（OpenMBeanParameterInfo[]）、返回类型 openType 与影响
（ACTION/INFO/ACTION_INFO/UNKNOWN）。构造器含返回类型、影响枚举与签名；重载构造可省影响（默认 UNKNOWN）。
查询 getName/getDescription/getSignature/getReturnOpenType/getImpact/equals/hashCode/toString。

## 2. Java 用户如何使用
描述 OpenMBean 的操作：new OpenMBeanOperationInfoSupport(name, desc, signature, returnOpenType, impact)。
链路：构造 → getName/getSignature/getReturnOpenType/getImpact → equals/toString。

## 3. 开源代码实际用法调研
- JDK 自用：MXBean 映射用 OpenMBeanOperationInfoSupport 描述操作（含返回类型与 impact）
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/OpenMBeanOperationInfoSupportTest.java`
  覆盖影响枚举与 equals。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（OpenMBeanOperationInfoSupportTest.java）
- 白盒：public、implements OpenMBeanOperationInfo、get 系列方法存在。
- 黑盒：各构造器 getter 组合；getReturnOpenType/getImpact（含默认 UNKNOWN 重载）；getSignature 副本；
  equals/hashCode（等值相等、impact/返回类型不同不等）；toString 前缀；异常精确 message（null name NPE/IAE、
  null returnType IAE、null signature IAE）。
