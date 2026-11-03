# javax.management.openmbean.OpenMBeanConstructorInfoSupport — 测试设计

## 1. API 功能
OpenMBeanConstructorInfo 接口实现：构造函数名、描述与签名（OpenMBeanParameterInfo[]）。构造器校验签名数组非 null。
查询 getName/getDescription/getSignature/equals/hashCode/toString。

## 2. Java 用户如何使用
描述 OpenMBean 的构造器签名：先造 OpenMBeanParameterInfoSupport 数组，再 new OpenMBeanConstructorInfoSupport(name,
desc, signature)。链路：构造 → getName/getDescription/getSignature → equals/toString。

## 3. 开源代码实际用法调研
- JDK 自用：MXBean 映射用 OpenMBeanConstructorInfoSupport 描述构造器
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/OpenMBeanConstructorInfoSupportTest.java`
  覆盖签名与 equals。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（OpenMBeanConstructorInfoSupportTest.java）
- 白盒：public、implements OpenMBeanConstructorInfo、getName/getDescription/getSignature 方法存在。
- 黑盒：构造后 getter 组合；getSignature 返回副本（改动原数组不影响）；equals/hashCode（等值相等、签名不同不等）；
  toString 前缀与字段；异常精确 message（null name NPE/IAE、null signature IAE）。
