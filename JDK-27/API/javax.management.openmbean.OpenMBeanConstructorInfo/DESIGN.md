# javax.management.openmbean.OpenMBeanConstructorInfo — 测试设计

## 1. API 功能
OpenMBean 构造器元数据接口：getName/getDescription/getSignature/equals/hashCode/toString。实现为
OpenMBeanConstructorInfoSupport。

## 2. Java 用户如何使用
用户以接口类型接收构造器元数据，用 getName 取名、getSignature 取参数签名数组。链路：Support 实现 →
OpenMBeanConstructorInfo 接口方法 → getName/getSignature。

## 3. 开源代码实际用法调研
- JDK 自用：MXBean 映射以 OpenMBeanConstructorInfo 接口类型保存构造器元数据
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/OpenMBeanConstructorInfoTest.java` 覆盖接口契约。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（OpenMBeanConstructorInfoTest.java）
- 白盒：public interface、方法签名存在（getName/getDescription/getSignature）。
- 黑盒：经 OpenMBeanConstructorInfoSupport 实例调用，验证 getName/getDescription/getSignature 轻行为。
