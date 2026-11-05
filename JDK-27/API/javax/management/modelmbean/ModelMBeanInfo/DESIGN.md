# ModelMBeanInfo 接口设计案

1. API 功能
- 描述 ModelMBean 元数据的接口（不继承 MBeanInfo，而是平行声明 getClassName/getDescription/getAttributes()/getConstructors()/getOperations()/getNotifications()/clone）
- Descriptor 面：getDescriptors(type)/setDescriptors/getDescriptor(name,type)/setDescriptor/getMBeanDescriptor/setMBeanDescriptor（多数声明受检 MBeanException+RuntimeOperationsException）
- **quirk**：接口有 getAttribute/getOperation/getNotification 却没有 getConstructor(String)（ModelMBeanInfoSupport 类补充了它）
2. Java 用户典型用法
- ModelMBeanInfoSupport 为标准实现；RequiredModelMBean.setModelMBeanInfo(ModelMBeanInfo) 消费之
3. 参考来源
- Oracle javadoc: javax.management.modelmbean 包（RequiredModelMBean / ModelMBeanInfo / DescriptorSupport 各类文档，含 descriptor 字段语义表：getMethod/setMethod/currencyTimeLimit/visibility/severity/persistPolicy/log）
- JMX 1.4 Specification（随 JSR-160 分发）Model MBeans 章 与 Descriptor 语义相关章节
- OpenJDK 源码（行为核对，只借鉴思路、代码自写）: jdk8u-dev jdk/src/share/classes/javax/management/modelmbean/*.java; jdk27 src/java.management/share/classes/javax/management/modelmbean/*.java
- 实测优先：所有异常类型/消息/默认字段值均在 JDK-8 (8u372) 与 JDK-27 (27.0.1) 上双侧行为探测后锁定

4. 测试用例设计（<Simple>Test.java）
- 精确 16 个方法 + getConstructor 缺席 quirk 断言 + 受检异常声明断言（getDescriptor/setDescriptor/clone）
- 经接口引用全操作：getMBeanDescriptor/getDescriptors/getDescriptor 未知名 null/setMBeanDescriptor 往返/setDescriptors 空数组 no-op/clone equals/类型化查找 null/被 RequiredModelMBean 接受
## 版本差异
- 方法集两侧一致（16 个；Declared 顺序无关——名字集合断言）

