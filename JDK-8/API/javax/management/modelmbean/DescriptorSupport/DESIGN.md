# DescriptorSupport 设计案

1. API 功能
- 可变 Descriptor 标准实现（与 javax.management.ImmutableDescriptor 相对的mutable端），实现 javax.management.Descriptor（extends Serializable+Cloneable）
- 构造器：()`空` / (int 容量<=0 拒绝) / (DescriptorSupport 拷贝) / (String[] names, Object[] values) / (String... "n=v" 解析)；JDK-8 另有 (String xml) XML 构造器
- 可变操作：setField（新增+覆盖，保留首见字段名大小写）、setFields、removeField（不存在静默 no-op，null/空名 no-op）
- 读取：getFieldValue（不存在 null）、getFieldValues（缺名/空名→null 元素；(String[])null→全部；()→零元素）、getFields（"n=v" 串，非 String 值括号化 "n=(5)"，null 值 "n="）、getFieldNames（按名排序）
- 值类型校验（按字段名分派）：name/descriptorType/setMethod/getMethod/role/class 仅非空 String；visibility 1..4；severity 0..6（String 数值或 Integer）；log ∈ T/true/F/false/Boolean；currencyTimeLimit/persistPeriod/lastUpdatedTimeStamp 数值>=-1；persistPolicy 枚举；其余任意字段名接受任意类型值（含 null/数组/Class）
- isValid()：要求 name 与 descriptorType 两个字段都在（值本身不校验）
- equals/hashCode 与 ImmutableDescriptor 跨实现相等（内容同则双向 equals 且 hashCode 相同）；toString 为 "a=1, b=2" 逗号+空格连接
- Serializable，serialVersionUID=-6292969195866300415L（双侧实测相同，round-trip 后仍可变）
- JDK-8 特有：toXMLString()（<Descriptor><field name=.. value=..></field>）与 XML 往返（非 String 值编码 "(java.lang.Integer/5)" 且往返还原）
2. Java 用户典型用法
- ModelMBean 元数据的事实标准载体：属性路由靠 attr Descriptor {name, descriptorType=attribute, getMethod=getX, setMethod=setX}
- 缓存控制：currencyTimeLimit=-1(永不缓存)/0(永缓存)/N 秒；默认值回退字段 value/default
- new DescriptorSupport(new String[]{"name=Count","descriptorType=attribute","getMethod=getCount"})（务必用 String[] 形态：单字符串在 JDK-8 会落进 XML 构造器）
3. 参考来源
- Oracle javadoc: javax.management.modelmbean 包（RequiredModelMBean / ModelMBeanInfo / DescriptorSupport 各类文档，含 descriptor 字段语义表：getMethod/setMethod/currencyTimeLimit/visibility/severity/persistPolicy/log）
- JMX 1.4 Specification（随 JSR-160 分发）Model MBeans 章 与 Descriptor 语义相关章节
- OpenJDK 源码（行为核对，只借鉴思路、代码自写）: jdk8u-dev jdk/src/share/classes/javax/management/modelmbean/*.java; jdk27 src/java.management/share/classes/javax/management/modelmbean/*.java
- 实测优先：所有异常类型/消息/默认字段值均在 JDK-8 (8u372) 与 JDK-27 (27.0.1) 上双侧行为探测后锁定

4. 测试用例设计（<Simple>Test.java）
- 构造器矩阵：容量 0/-3 → RuntimeOperationsException("Descriptor field limit invalid")；(names,values) 长度不等/null 名 → ROE(IAE)；重复名后者胜；varargs 无 '=' → ROE("no equals sign")；空/null 串跳过；拷贝构造独立且接受 null
- 可变性：setField null/空名 → ROE("Field name to be set is null or empty")；受限名 null 值 → ROE("Field value invalid")；removeField 不存在/ null/空名 → no-op；getFieldValue(null/"") → ROE；首见大小写保留（"CamelCase" 后写 "camelcase" 仍是单字段）
- 类型矩阵：受限字段名×值类型全表（正/负各半）；自由字段名接受 String/Integer/Long/Float/Double/Byte/Char/Short/Boolean/数组/Class/Object/null
- isValid 表：空=false、只有 name/只有 descriptorType=false、两者齐=true、descriptorType=bogus 仍 true（值不校验）
- equals/hashCode/toString/clone：跨 ImmutableDescriptor 双向 equals+同 hashCode；clone 修改不影响原
- 序列化：round-trip equals、类保留、反序列化后仍可变
- **对照小节**：同操作 ImmutableDescriptor.setField/removeField → RuntimeOperationsException 而 DescriptorSupport 成功（同文件直接对照）
- JDK-8 XML 小节：toXMLString 形态断言、round-trip（含 Integer "(type/value)" 还原）、坏 XML/纯文本串 → XMLParseException、null 串 → ROE("String in parameter is null")、自闭合 <field/> 不被解析
- JDK-27 版本差异小节：反射断言 toXMLString 与 (String) 构造器不存在、单字符串按 name=value 解析、XMLParseException 类不存在
## 版本差异
- JDK-27 移除了 `DescriptorSupport(String)` XML 构造器与 `toXMLString()`（连带 `XMLParseException` 类），`javax.management.modelmbean.XMLParseException` 目录仅存在于 JDK-8 侧
- 单个 String 字面量实参在 JDK-8 上解析到 XML 构造器（解析失败抛 XMLParseException），在 JDK-27 上解析到 String... 构造器（按 name=value 解析，无 '=' 抛 RuntimeOperationsException）
- 其余 modelmbean 公开 API（javap 对照）与实测行为两侧一致；serialVersionUID 两侧相同

