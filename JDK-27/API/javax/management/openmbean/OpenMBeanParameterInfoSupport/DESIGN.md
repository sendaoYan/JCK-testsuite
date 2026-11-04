# javax.management.openmbean.OpenMBeanParameterInfoSupport — 测试设计

## 1. API 功能
OpenMBeanParameterInfo 接口实现：携带参数名、描述、openType 与默认值/合法值（defaultValue、minValue、maxValue、
legalValues）。构造器校验 defaultValue/legalValues 的合法性与 openType 匹配。查询 getName/getDescription/getOpenType/
getDefaultValue/getMinValue/getMaxValue/getLegalValues/isValue/hasDefaultValue/hasLegalValues/equals/hashCode/toString。

## 2. Java 用户如何使用
描述某个 MBean 操作的输入参数：new OpenMBeanParameterInfoSupport(name, desc, openType[, defaultValue[, min[, max]]] | legalValues)。
链路：构造（默认值/合法值）→ getName/getOpenType → isValue/hasDefaultValue/hasLegalValues → equals/toString。

## 3. 开源代码实际用法调研
- JDK 自用：MXBean 映射用 OpenMBeanParameterInfoSupport 描述操作参数
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/OpenMBeanParameterInfoSupportTest.java`、
  `EqualsTest.java` 覆盖默认值/合法值校验与 equals。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（OpenMBeanParameterInfoSupportTest.java）
- 白盒：public、implements OpenMBeanParameterInfo、get 系列方法存在。
- 黑盒：各构造器（含 defaultValue、min/max、legalValues）getter 组合；hasDefaultValue/hasLegalValues；
  isValue 用 SimpleType 校验；equals/hashCode（等值相等、异值不等）；toString 前缀与字段；
  异常精确 message（defaultValue 与类型不匹配、min>max、legalValues 空/类型不匹配、null name NPE/IAE）。
