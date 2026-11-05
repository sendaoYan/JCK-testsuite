# ModelMBeanNotificationInfo 设计案

1. API 功能
- extends MBeanNotificationInfo implements DescriptorAccess；构造器：(notifTypes,name,desc[,Descriptor])、拷贝
- getNotifTypes 返回防御拷贝（改返回数组不影响内部）
- 默认 Descriptor：name、descriptorType=notification、severity=6、displayName
- equals 按类型数组**逐位置**比较（顺序敏感；子集/超集均 false）
2. Java 用户典型用法
- RequiredModelMBean.getNotificationInfo = 声明项 + 默认 GENERIC(jmx.modelmbean.generic)/ATTRIBUTE_CHANGE(jmx.attribute.change)（自声明同名不重复添加）
3. 参考来源
- Oracle javadoc: javax.management.modelmbean 包（RequiredModelMBean / ModelMBeanInfo / DescriptorSupport 各类文档，含 descriptor 字段语义表：getMethod/setMethod/currencyTimeLimit/visibility/severity/persistPolicy/log）
- JMX 1.4 Specification（随 JSR-160 分发）Model MBeans 章 与 Descriptor 语义相关章节
- OpenJDK 源码（行为核对，只借鉴思路、代码自写）: jdk8u-dev jdk/src/share/classes/javax/management/modelmbean/*.java; jdk27 src/java.management/share/classes/javax/management/modelmbean/*.java
- 实测优先：所有异常类型/消息/默认字段值均在 JDK-8 (8u372) 与 JDK-27 (27.0.1) 上双侧行为探测后锁定

4. 测试用例设计（<Simple>Test.java）
- 类型内容 + 防御拷贝 + 空类型数组合法；默认描述符 4 字段（severity=6）
- setDescriptor 正/负（错名/错 descriptorType ROE）/null 重置
- equals 矩阵：同序 true、换序 false、子集/超集 false；clone/拷贝构造；序列化 round-trip（类型数组保留）；跨父类 equals false
## 版本差异
- javap 两侧一致；serialVersionUID=-7445681389570207141L 两侧相同；实测行为一致

