# javax.management.openmbean.OpenMBeanInfoSupport — 测试设计

## 1. API 功能
OpenMBeanInfo 实现（extends MBeanInfo）。构造 (className,desc,OpenMBeanAttributeInfo[],
OpenMBeanConstructorInfo[],OpenMBeanOperationInfo[],MBeanNotificationInfo[][,Descriptor])；
构造期把三段 open 数组转换/拷贝（attributeArray 等）；四段 getter 每次返回 clone；
equals 为**集合语义**（数组顺序无关、description 不参与、className 参与）。

## 2. Java 用户典型用法
开放 MBean/MXBean 元数据容器。MXBean 注册后 MBeanServer.getMBeanInfo 返回本类：属性为
OpenMBeanAttributeInfo、复合类型映射 CompositeType、descriptor 含 mxbean=true 与 openType 字段。

## 3. 参考来源
- Oracle javadoc；OpenJDK 源码（构造数组转换、equals 的 set 语义实现）。
- test/jdk/javax/management/mxbean（MXBean 开放映射闭环思路）。

## 4. 测试用例设计 + 易错细节（OpenMBeanInfoSupportTest，批 G：72→70 断言）
- null 四段 → 空数组；getter 防御拷贝（改返回数组不影响后续读）；构造拷贝调用方数组。
- equals 集合语义：属性数组换序仍等、hashCode 一致；desc 不参与；className 参与；
  plain MBeanInfo 同内容不等（非 OpenMBeanInfo）。
- Descriptor 形态自定义字段保留。
- **MXBean 真闭环**（文件内嵌 PointMXBean/Point/PointImpl）：registerMBean → getMBeanInfo 的
  Point 属性 instanceof OpenMBeanAttributeInfo、getOpenType()==CompositeType(x,y INTEGER)、
  descriptor openType==getOpenType()、mxbean=true；server.getAttribute 返回 CompositeData
  (x=1,y=2)。
- 序列化：round-trip 后属性仍是 open 类型、SimpleType 单例稳定、SUID=4349395935420511492L。
- 版本差异：两侧零差异（SUID、消息、MXBean 行为一致）。
