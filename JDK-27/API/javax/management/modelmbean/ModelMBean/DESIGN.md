# ModelMBean 接口设计案

1. API 功能
- 接口：extends DynamicMBean + PersistentMBean + ModelMBeanNotificationBroadcaster；自身仅声明 setModelMBeanInfo/setManagedResource
- 共 17 个不同名方法（getMethods 全集）；setManagedResource 声明受检异常 MBeanException/RuntimeOperationsException/InstanceNotFoundException/InvalidTargetObjectTypeException
- 标准实现 RequiredModelMBean
2. Java 用户典型用法
- ModelMBean ref = new RequiredModelMBean(); ref.setManagedResource(...); ref.setModelMBeanInfo(...); 同一实例可经 DynamicMBean/PersistentMBean/ModelMBeanNotificationBroadcaster 四种视图使用并注册进 MBeanServer
3. 参考来源
- Oracle javadoc: javax.management.modelmbean 包（RequiredModelMBean / ModelMBeanInfo / DescriptorSupport 各类文档，含 descriptor 字段语义表：getMethod/setMethod/currencyTimeLimit/visibility/severity/persistPolicy/log）
- JMX 1.4 Specification（随 JSR-160 分发）Model MBeans 章 与 Descriptor 语义相关章节
- OpenJDK 源码（行为核对，只借鉴思路、代码自写）: jdk8u-dev jdk/src/share/classes/javax/management/modelmbean/*.java; jdk27 src/java.management/share/classes/javax/management/modelmbean/*.java
- 实测优先：所有异常类型/消息/默认字段值均在 JDK-8 (8u372) 与 JDK-27 (27.0.1) 上双侧行为探测后锁定

4. 测试用例设计（<Simple>Test.java）
- 精确 17 个不同名方法 + 分组断言（DynamicMBean 6/PersistentMBean 2/广播器 7/自有 2）
- 多态真用：四超类型引用同一实例全部可用；DynamicMBean 视图 getAttribute 空 info → AttributeNotFoundException；PersistentMBean 视图 load → MBeanException；广播器视图 sendNotification 同实例送达；经接口引用注册进真实 MBeanServer
## 版本差异
- getMethods 集合两侧一致（17 名；无顺序依赖——Set 断言）

