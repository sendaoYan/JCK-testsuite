# javax.management.openmbean.OpenMBeanOperationInfo — 测试设计

## 1. API 功能
OpenMBean 操作元数据接口：getName/getDescription/getSignature/getReturnOpenType/getImpact/equals/hashCode/toString。
实现为 OpenMBeanOperationInfoSupport。

## 2. Java 用户如何使用
用户以接口类型接收操作元数据，用 getSignature 取参数、getReturnOpenType 取返回类型、getImpact 取影响枚举。
链路：Support 实现 → OpenMBeanOperationInfo 接口方法 → getSignature/getReturnOpenType/getImpact。

## 3. 开源代码实际用法调研
- JDK 自用：MXBean 映射以 OpenMBeanOperationInfo 接口类型保存操作元数据
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/OpenMBeanOperationInfoTest.java` 覆盖接口契约。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（OpenMBeanOperationInfoTest.java）
- 白盒：public interface、方法签名存在（getName/getDescription/getSignature/getReturnOpenType/getImpact）。
- 黑盒：经 OpenMBeanOperationInfoSupport 实例调用，验证 getName/getSignature/getReturnOpenType/getImpact 轻行为。
