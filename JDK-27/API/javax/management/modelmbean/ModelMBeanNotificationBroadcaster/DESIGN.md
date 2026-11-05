# ModelMBeanNotificationBroadcaster 接口设计案

1. API 功能
- 接口：extends NotificationBroadcaster；自有 6 方法：sendNotification(Notification)/sendNotification(String)/sendAttributeChangeNotification(ACN)/sendAttributeChangeNotification(Attribute,Attribute)/addAttributeChangeNotificationListener/ removeAttributeChangeNotificationListener（共 7 个不同名方法）
- sendNotification/sendAttributeChangeNotification 各两个重载；addAttributeChangeNotificationListener 声明 IllegalArgumentException；remove 声明 ListenerNotFoundException
2. Java 用户典型用法
- RequiredModelMBean 实现：sendNotification(String)→jmx.modelmbean.generic；双 Attribute 形态构造 AttributeChangeNotification；ACN 监听器按属性名过滤（属性须存在于 info）
3. 参考来源
- Oracle javadoc: javax.management.modelmbean 包（RequiredModelMBean / ModelMBeanInfo / DescriptorSupport 各类文档，含 descriptor 字段语义表：getMethod/setMethod/currencyTimeLimit/visibility/severity/persistPolicy/log）
- JMX 1.4 Specification（随 JSR-160 分发）Model MBeans 章 与 Descriptor 语义相关章节
- OpenJDK 源码（行为核对，只借鉴思路、代码自写）: jdk8u-dev jdk/src/share/classes/javax/management/modelmbean/*.java; jdk27 src/java.management/share/classes/javax/management/modelmbean/*.java
- 实测优先：所有异常类型/消息/默认字段值均在 JDK-8 (8u372) 与 JDK-27 (27.0.1) 上双侧行为探测后锁定

4. 测试用例设计（<Simple>Test.java）
- 精确 7 个不同名方法 + 重载计数（2/2）+ 受检异常断言
- 经 RequiredModelMBean 实测：同实例透传、String 形态 type/message、双 Attribute ACN 旧/新值精确、ACN 监听注册/移除/二次移除 ListenerNotFoundException、未知属性名注册 → ROE(IAE "The attribute name does not exist")、默认通知信息 2 项
## 版本差异
- 方法集与 RequiredModelMBean 实测行为两侧一致

