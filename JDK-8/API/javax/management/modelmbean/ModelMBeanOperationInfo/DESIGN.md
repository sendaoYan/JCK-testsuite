# ModelMBeanOperationInfo 设计案

1. API 功能
- extends MBeanOperationInfo implements DescriptorAccess；构造器：Method 形态[,Descriptor]、全参 (name,desc,params,returnType,impact[,Descriptor])、拷贝
- 默认 Descriptor（Method 形态）：name=方法名、descriptorType=operation、role=operation、displayName=方法名；impact 默认 UNKNOWN(3)
- getSignature 来自 Method 参数（名字 p1/p2...）且返回防御拷贝；impact 常量 INFO=0/ACTION=1/ACTION_INFO=2/UNKNOWN=3
- setDescriptor：名须匹配且 descriptorType=operation，否则 ROE；null → 重置默认
2. Java 用户典型用法
- RequiredModelMBean 路由要求 getMethod/setMethod 对应操作已在操作数组中；role=getter 与 role=operation 等价可路由
3. 参考来源
- Oracle javadoc: javax.management.modelmbean 包（RequiredModelMBean / ModelMBeanInfo / DescriptorSupport 各类文档，含 descriptor 字段语义表：getMethod/setMethod/currencyTimeLimit/visibility/severity/persistPolicy/log）
- JMX 1.4 Specification（随 JSR-160 分发）Model MBeans 章 与 Descriptor 语义相关章节
- OpenJDK 源码（行为核对，只借鉴思路、代码自写）: jdk8u-dev jdk/src/share/classes/javax/management/modelmbean/*.java; jdk27 src/java.management/share/classes/javax/management/modelmbean/*.java
- 实测优先：所有异常类型/消息/默认字段值均在 JDK-8 (8u372) 与 JDK-27 (27.0.1) 上双侧行为探测后锁定

4. 测试用例设计（<Simple>Test.java）
- impact 四常量精确值；Method 形态默认描述符 4 字段精确 + UNKNOWN 默认 + 签名参数名/类型 + 防御拷贝
- 全参形态 impact/签名/descriptor 保留；setDescriptor 往返 + 错名/错类型 ROE
- 拷贝构造/clone/序列化 round-trip（含 role）；跨父类 equals false；toString 含操作名
## 版本差异
- javap 两侧一致；serialVersionUID=6532732096650090465L 两侧相同；实测行为一致

