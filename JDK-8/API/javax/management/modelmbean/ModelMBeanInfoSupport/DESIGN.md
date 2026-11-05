# ModelMBeanInfoSupport 设计案

1. API 功能
- ModelMBeanInfo 的标准实现，extends MBeanInfo；6/7 参构造（第 7 参为 MBean Descriptor）
- 默认 MBean Descriptor：descriptorType=mbean, name=className, displayName=className, persistPolicy=never, log=F, visibility=1
- Descriptor 视图：getDescriptors(type)（attribute/operation/constructor/notification/mbean/all，类型串忽略大小写，all=各 entries+mbean）；getDescriptor(name,type)；getDescriptor(name) 跨类型查找；getDescriptor()=MBean 描述符
- 变更：setDescriptor(desc,type)（名字须匹配既有条目）、setDescriptors（批量，不能引入无对应 ModelMBean*Info 的新名 → ROE）、setMBeanDescriptor
- 类型化查找：getAttribute/getOperation/getNotification（接口无 getConstructor——Support 类补充了 getConstructor）；未知名返回 null；getAttribute(null) → ROE
- copy 构造与 clone 均为浅拷贝：经副本 setDescriptor 会改到共享的 ModelMBeanAttributeInfo；但 MBean 描述符本身隔离
2. Java 用户典型用法
- 构造 (className, desc, attrs, ctors, ops, notifs[, Descriptor]) 后交给 RequiredModelMBean.setModelMBeanInfo
- getAttributes() 返回防御拷贝（改元素不影响二次 get）；元素类型保持 ModelMBeanAttributeInfo
3. 参考来源
- Oracle javadoc: javax.management.modelmbean 包（RequiredModelMBean / ModelMBeanInfo / DescriptorSupport 各类文档，含 descriptor 字段语义表：getMethod/setMethod/currencyTimeLimit/visibility/severity/persistPolicy/log）
- JMX 1.4 Specification（随 JSR-160 分发）Model MBeans 章 与 Descriptor 语义相关章节
- OpenJDK 源码（行为核对，只借鉴思路、代码自写）: jdk8u-dev jdk/src/share/classes/javax/management/modelmbean/*.java; jdk27 src/java.management/share/classes/javax/management/modelmbean/*.java
- 实测优先：所有异常类型/消息/默认字段值均在 JDK-8 (8u372) 与 JDK-27 (27.0.1) 上双侧行为探测后锁定

4. 测试用例设计（<Simple>Test.java）
- 全 getter 精确（类名/描述/四个数组长度与内容）；默认 MBean 描述符全字段断言（6 字段）
- getDescriptors 每类型计数 + all=5（4 条目+mbean）+ 大小写不敏感 + bogus → ROE；getDescriptor 未知名 → null
- setDescriptor/setDescriptors 更新持久化；新名/错名 → ROE("Descriptor name is invalid")；null → ROE
- equals/hashCode：同构两实例相等；className 不同不等
- 浅共享：copy 构造与 clone 的 setDescriptor 可见性（两侧实测一致）；setMBeanDescriptor 隔离
- 序列化 round-trip：equals、MBean 描述符字段、属性 getMethod、ModelMBean 类型数组保留
## 版本差异
- javap 对照两侧公开方法集一致；实测行为一致；serialVersionUID=-1935722590756516193L 两侧相同

