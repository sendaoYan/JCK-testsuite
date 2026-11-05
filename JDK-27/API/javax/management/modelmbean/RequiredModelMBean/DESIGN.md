# RequiredModelMBean 设计案

1. API 功能
- ModelMBean 的标准实现（DynamicMBean+PersistentMBean+ModelMBeanNotificationBroadcaster+MBeanRegistration+NotificationEmitter）
- 两个公共构造器：无参（生成默认 ModelMBeanInfo「Default ModelMBean」）与 RequiredModelMBean(ModelMBeanInfo)（直接以给定元数据初始化）
- 路由核心：属性 Descriptor 的 getMethod/setMethod 指向**必须已声明在 ModelMBeanInfo 操作列表中**的方法名；invoke 直接按操作名路由到 managedResource
- setManagedResource(target, "ObjectReference")：目标类型串忽略大小写，其余/null → InvalidTargetObjectTypeException
- setModelMBeanInfo：null → ROE(IAE)；注册后调用 → ROE(IllegalStateException)；注册前合法（内部 clone）
- 缓存语义（实测）：currencyTimeLimit=0 → 取一次后永久粘滞（target 改动不可见）；-1 → 永不缓存（每次真调 getMethod）；无 getMethod → 返回 descriptor value/default 字段
- load/store：不支持 → MBeanException(ServiceNotFoundException)
- 通知：sendNotification(Notification) 原实例透传；sendNotification(String)→type jmx.modelmbean.generic/source=mmb/message=文本；sendAttributeChangeNotification(Attribute,Attribute)→ACN(type jmx.attribute.change)；setAttribute 自动发 ACN；getNotificationInfo = 声明项 + 默认 GENERIC/ATTRIBUTE_CHANGE（自声明同名不重复）
2. Java 用户典型用法
- 真闭环：mmb.setManagedResource(res,"ObjectReference") → setModelMBeanInfo(info 含属性+操作) → server.registerMBean → server.getAttribute/setAttribute/invoke 全部路由到 res 的真实方法
- ObjectInstance.getClassName() 返回 ModelMBeanInfo 的 className（DynamicMBean 规则），即受管资源类名
3. 参考来源
- Oracle javadoc: javax.management.modelmbean 包（RequiredModelMBean / ModelMBeanInfo / DescriptorSupport 各类文档，含 descriptor 字段语义表：getMethod/setMethod/currencyTimeLimit/visibility/severity/persistPolicy/log）
- JMX 1.4 Specification（随 JSR-160 分发）Model MBeans 章 与 Descriptor 语义相关章节
- OpenJDK 源码（行为核对，只借鉴思路、代码自写）: jdk8u-dev jdk/src/share/classes/javax/management/modelmbean/*.java; jdk27 src/java.management/share/classes/javax/management/modelmbean/*.java
- 实测优先：所有异常类型/消息/默认字段值均在 JDK-8 (8u372) 与 JDK-27 (27.0.1) 上双侧行为探测后锁定

4. 测试用例设计（<Simple>Test.java）
- 路由真闭环：getAttribute("Count")==res.getCount() 真值；setAttribute(Count,7) 后 res 内部真变 7；invoke(add,5)==12 精确；void 操作返回 null；批量 getAttributes/setAttributes 跳过未知名
- 前置条件负例：getMethod 未声明为操作 → MBeanException(ServiceNotFoundException "Operation getCount not in ModelMBeanInfo")；未 setManagedResource → MBeanException(ServiceNotFoundException)；invoke 未声明操作 → 同；未知属性 get/set → AttributeNotFoundException；错值类型 set → RuntimeOperationsException(IAE)
- 生命周期：setModelMBeanInfo(null)/注册后再 set → ROE(IAE/IllegalStateException)；"Bogus"/null 目标类型 → ITOTE（"objectReference" 大小写不敏感三种全过）；load/store → MBeanException
- 新建默认：getMBeanInfo()="Default ModelMBean" 空信息；getAttribute → AttributeNotFoundException；默认通知 2 项（GENERIC/ATTRIBUTE_CHANGE，log=T severity=6）
- 缓存：ctl=0 粘滞（target→99 后仍回 3）vs ctl=-1 实时（target→42 后回 42）；无 getMethod 回 value 字段
- 通知：同实例透传（== 断言）、String 形态字段精确、双 Attribute 构造 ACN 旧/新值精确、setAttribute 自动 ACN、ACN 监听器（未知属性名注册 → ROE(IAE)）、remove 未注册监听器 → ListenerNotFoundException
## 版本差异
- 两侧（javap + 实测）公开方法集与路由/缓存/通知行为完全一致；本包各 Serializable 类的 serialVersionUID 两侧相同（DescriptorSupport=-6292969195866300415L、ModelMBeanInfoSupport=-1935722590756516193L 等，ObjectStreamClass.lookup 实测）
- RequiredModelMBean 本身未实现 Serializable（ObjectStreamClass.lookup 返回 null，两侧一致）

