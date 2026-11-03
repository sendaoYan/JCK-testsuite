# javax.management.openmbean.OpenMBeanInfo — 测试设计

## 1. API 功能
OpenMBean 完整元数据接口，继承 MBeanInfo：getClassName/getDescription/getAttributes/getConstructors/getOperations/
getNotifications/equals/hashCode/toString。实现为 OpenMBeanInfoSupport。

## 2. Java 用户如何使用
用户以接口类型接收完整元数据，遍历 getAttributes/getConstructors/getOperations/getNotifications 呈现 MBean。
链路：Support 实现 → OpenMBeanInfo 接口方法 → getClassName/getAttributes/getOperations。

## 3. 开源代码实际用法调研
- JDK 自用：MXBean 映射以 OpenMBeanInfo 接口类型暴露完整 open MBean 元数据
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/OpenMBeanInfoTest.java` 覆盖接口契约。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（OpenMBeanInfoTest.java）
- 白盒：public interface、extends MBeanInfo、方法签名存在（getClassName/getDescription/getAttributes/getConstructors/
  getOperations/getNotifications）。
- 黑盒：经 OpenMBeanInfoSupport 实例调用，验证 getClassName/getDescription/getAttributes/getOperations 轻行为。
