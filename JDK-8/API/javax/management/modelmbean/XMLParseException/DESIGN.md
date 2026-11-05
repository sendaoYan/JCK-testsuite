# XMLParseException 设计案（仅 JDK-8）

1. API 功能
- 受检异常（extends Exception）；3 个公共构造器：()/ (String)/ (Exception,String)
- 消息格式（实测锁定）：() → "XML Parse Exception."；(String) → "XML Parse Exception: <s>"；(Exception,String) → "XML Parse Exception: <s>:<cause.toString()>"
- **与 InvalidTargetObjectTypeException 的差异**：null cause 直接 NullPointerException（构造器无条件 e.toString()），不省略 triggered-by
- Serializable，serialVersionUID=3176664577895105181L
2. Java 用户典型用法
- 真实触发源：JDK-8 的 new DescriptorSupport(xmlString)——缺少 <Descriptor></Descriptor> 对、非 XML 纯文本（单字符串字面量会路由到 XML 构造器）
3. 参考来源
- Oracle javadoc: javax.management.modelmbean 包（RequiredModelMBean / ModelMBeanInfo / DescriptorSupport 各类文档，含 descriptor 字段语义表：getMethod/setMethod/currencyTimeLimit/visibility/severity/persistPolicy/log）
- JMX 1.4 Specification（随 JSR-160 分发）Model MBeans 章 与 Descriptor 语义相关章节
- OpenJDK 源码（行为核对，只借鉴思路、代码自写）: jdk8u-dev jdk/src/share/classes/javax/management/modelmbean/*.java; jdk27 src/java.management/share/classes/javax/management/modelmbean/*.java
- 实测优先：所有异常类型/消息/默认字段值均在 JDK-8 (8u372) 与 JDK-27 (27.0.1) 上双侧行为探测后锁定

4. 测试用例设计（<Simple>Test.java）
- 三构造消息格式精确 + null cause → NPE 断言（对照记录）
- 真实触发："<bad" → 消息含 "No <descriptor>"；"plainstring" → 同异常；良构空 <Descriptor></Descriptor> 不抛
- 受检性：声明于 DescriptorSupport(String) 的 throws；恰 3 构造器；序列化 round-trip；toString 含类名
## 版本差异
- **JDK-27 已整体移除**（连同 DescriptorSupport(String) 与 toXMLString()）：javap "class not found"；本目录仅存在于 JDK-8 侧；JDK-27 侧单字符串实参改走 String... 构造器（ROE 而非 XMLParseException）

