# ModelMBeanConstructorInfo 设计案

1. API 功能
- extends MBeanConstructorInfo implements DescriptorAccess；构造器：Constructor 形态[,Descriptor]、(name,desc,params[,Descriptor])
- name 取 Constructor.getName()（嵌套类为 "Outer$Inner" 形态）
- 默认 Descriptor：name=构造器名、descriptorType=**operation**（复用操作类型的历史设计）、role=constructor、displayName=构造器名
- setDescriptor：名须匹配且 descriptorType=operation（同默认约定），否则 ROE；null → 重置默认
2. Java 用户典型用法
- ModelMBeanInfoSupport 的构造器数组条目来源；描述符 role=constructor 区分于普通操作
3. 参考来源
- Oracle javadoc: javax.management.modelmbean 包（RequiredModelMBean / ModelMBeanInfo / DescriptorSupport 各类文档，含 descriptor 字段语义表：getMethod/setMethod/currencyTimeLimit/visibility/severity/persistPolicy/log）
- JMX 1.4 Specification（随 JSR-160 分发）Model MBeans 章 与 Descriptor 语义相关章节
- OpenJDK 源码（行为核对，只借鉴思路、代码自写）: jdk8u-dev jdk/src/share/classes/javax/management/modelmbean/*.java; jdk27 src/java.management/share/classes/javax/management/modelmbean/*.java
- 实测优先：所有异常类型/消息/默认字段值均在 JDK-8 (8u372) 与 JDK-27 (27.0.1) 上双侧行为探测后锁定

4. 测试用例设计（<Simple>Test.java）
- 无参 Constructor：name/空签名；String(String) 构造器 name="java.lang.String" 签名类型
- 默认描述符 4 字段（重点断言 descriptorType=operation 而非 constructor）
- setDescriptor 正/负/重置；clone/序列化；跨父类 equals false；同配置 equals
## 版本差异
- javap 两侧一致；serialVersionUID=3862947819818064362L 两侧相同；实测行为一致

