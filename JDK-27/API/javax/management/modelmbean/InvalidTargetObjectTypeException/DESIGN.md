# InvalidTargetObjectTypeException 设计案

1. API 功能
- 受检异常（extends Exception，非 RuntimeException）；3 个公共构造器：()/ (String)/ (Exception,String)
- 消息格式（实测锁定）：() → "InvalidTargetObjectTypeException: "；(String) → "InvalidTargetObjectTypeException: <s>"；(Exception,String) → "InvalidTargetObjectTypeException: <s>\n\t triggered by:<cause.toString()>"（null cause 时省略 triggered by）
- 无特有 getter（消息即全部信息）；可序列化，serialVersionUID=1190536278266811217L（双侧实测相同）
2. Java 用户典型用法
- 真实触发源：RequiredModelMBean.setManagedResource(target, type) 的 type 非 "objectReference"（忽略大小写）或 null
3. 参考来源
- Oracle javadoc: javax.management.modelmbean 包（RequiredModelMBean / ModelMBeanInfo / DescriptorSupport 各类文档，含 descriptor 字段语义表：getMethod/setMethod/currencyTimeLimit/visibility/severity/persistPolicy/log）
- JMX 1.4 Specification（随 JSR-160 分发）Model MBeans 章 与 Descriptor 语义相关章节
- OpenJDK 源码（行为核对，只借鉴思路、代码自写）: jdk8u-dev jdk/src/share/classes/javax/management/modelmbean/*.java; jdk27 src/java.management/share/classes/javax/management/modelmbean/*.java
- 实测优先：所有异常类型/消息/默认字段值均在 JDK-8 (8u372) 与 JDK-27 (27.0.1) 上双侧行为探测后锁定

4. 测试用例设计（<Simple>Test.java）
- 三构造消息格式精确（含 triggered by 前缀与 null cause 分支）
- 真实触发：setManagedResource("Bogus") 消息含 "Bogus"；null 类型同异常；受检性：声明于 ModelMBean.setManagedResource 的 throws
- 恰 3 个公共构造器；非 RuntimeException；序列化 round-trip 消息保留；toString 含类名
## 版本差异
- 构造器集/消息格式/行为两侧完全一致（javap+实测）

