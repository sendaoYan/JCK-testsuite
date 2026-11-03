# javax.management.openmbean.OpenMBeanParameterInfo — 测试设计

## 1. API 功能
OpenMBean 操作/构造器参数的元数据接口：getName/getDescription/getOpenType/getDefaultValue/getMinValue/
getMaxValue/getLegalValues/isValue/hasDefaultValue/hasLegalValues/equals/hashCode/toString。实现为
OpenMBeanParameterInfoSupport。

## 2. Java 用户如何使用
用户以接口类型接收参数元数据（通常由 Support 实现），用 getOpenType 取类型、isValue 校验值、
hasDefaultValue/getDefaultValue 取默认值。链路：Support 实现 → OpenMBeanParameterInfo 接口方法 → getOpenType/isValue。

## 3. 开源代码实际用法调研
- JDK 自用：MXBean 映射以 OpenMBeanParameterInfo 接口类型保存参数元数据
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/OpenMBeanParameterInfoTest.java` 覆盖接口契约。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（OpenMBeanParameterInfoTest.java）
- 白盒：public interface、方法签名存在（getName/getDescription/getOpenType/getDefaultValue/getMinValue/getMaxValue/
  getLegalValues/isValue/hasDefaultValue/hasLegalValues）。
- 黑盒：经 OpenMBeanParameterInfoSupport 实例调用，验证 getOpenType/isValue/hasDefaultValue/getDefaultValue
  轻行为。
