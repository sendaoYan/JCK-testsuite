# javax.management.openmbean.OpenMBeanAttributeInfo — 测试设计

## 1. API 功能
OpenMBean 属性元数据接口，继承 MBeanAttributeInfo：getName/getDescription/getOpenType/isReadable/isWritable/isIs/
getDefaultValue/getMinValue/getMaxValue/getLegalValues/isValue/hasDefaultValue/hasLegalValues/equals/hashCode/toString。
实现为 OpenMBeanAttributeInfoSupport。

## 2. Java 用户如何使用
用户以接口类型接收属性元数据，用 isReadable/isWritable/isIs 判读写与 is 风格、getOpenType 取类型、
isValue 校验值。链路：Support 实现 → OpenMBeanAttributeInfo 接口方法 → isReadable/getOpenType/isValue。

## 3. 开源代码实际用法调研
- JDK 自用：MXBean 映射以 OpenMBeanAttributeInfo 接口类型保存属性元数据
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/OpenMBeanAttributeInfoTest.java` 覆盖接口契约。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（OpenMBeanAttributeInfoTest.java）
- 白盒：public interface、extends MBeanAttributeInfo、方法签名存在（isReadable/isWritable/isIs/getOpenType/
  isValue/hasDefaultValue/getDefaultValue）。
- 黑盒：经 OpenMBeanAttributeInfoSupport 实例调用，验证 isReadable/isWritable/isIs/getOpenType/isValue 轻行为。
