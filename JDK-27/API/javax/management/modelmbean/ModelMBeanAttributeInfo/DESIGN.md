# ModelMBeanAttributeInfo 设计案

1. API 功能
- extends MBeanAttributeInfo implements DescriptorAccess；5 个构造器：全参 (name,type,desc,isR,isW,isIs[,Descriptor])、(name,desc,Method getter,Method setter[,Descriptor])、拷贝
- 默认 Descriptor（未传/传 null）：name=属性名、descriptorType=attribute、displayName=属性名（恰好 3 字段）
- setDescriptor：null → 重置默认；名字大小写不敏感匹配（"count" 配 "Count" 合法存储原串）；错名/错 descriptorType → RuntimeOperationsException
- Method 形态：getter-only→只读、setter-only→只写、getter/setter 互换 → IntrospectionException；descriptor **不会**自动填 getMethod/setMethod（路由需显式字段）
- isIs=true 仅 boolean 类型合法（否则 IllegalArgumentException）
- getDescriptor 返回克隆（改返回值不影响内部）；clone 为含独立 descriptor 的拷贝；与父类 MBeanAttributeInfo 实例跨类 equals false（双向）
2. Java 用户典型用法
- 路由用：new ModelMBeanAttributeInfo("Count","int","d",true,true,false,new DescriptorSupport(new String[]{"name","descriptorType","getMethod","setMethod"},new Object[]{"Count","attribute","getCount","setCount"}))
3. 参考来源
- Oracle javadoc: javax.management.modelmbean 包（RequiredModelMBean / ModelMBeanInfo / DescriptorSupport 各类文档，含 descriptor 字段语义表：getMethod/setMethod/currencyTimeLimit/visibility/severity/persistPolicy/log）
- JMX 1.4 Specification（随 JSR-160 分发）Model MBeans 章 与 Descriptor 语义相关章节
- OpenJDK 源码（行为核对，只借鉴思路、代码自写）: jdk8u-dev jdk/src/share/classes/javax/management/modelmbean/*.java; jdk27 src/java.management/share/classes/javax/management/modelmbean/*.java
- 实测优先：所有异常类型/消息/默认字段值均在 JDK-8 (8u372) 与 JDK-27 (27.0.1) 上双侧行为探测后锁定

4. 测试用例设计（<Simple>Test.java）
- 默认描述符 3 字段精确；null 描述符回退默认；构造器克隆传入描述符（外部改不影响）
- Method 形态读写矩阵 + 互换异常 + 描述符不含 getMethod（路由需显式）
- setDescriptor 规则：错名/错类型 ROE、大小写不敏感、null 重置
- isIs 非 boolean → IAE；clone 独立；序列化 round-trip（含描述符 getMethod）；跨父类 equals false；同配置 equals+hashCode
## 版本差异
- javap 两侧一致；serialVersionUID=6181543027787327345L 两侧相同；实测行为一致

