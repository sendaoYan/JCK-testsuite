# javax.management.openmbean.OpenMBeanInfo — 测试设计

## 1. API 功能
开放 MBean 整体元数据接口：类名 + 属性/构造器/操作/通知四段数组。方法集 9 个：getClassName、
getDescription、getAttributes、getOperations、getConstructors、getNotifications、equals、hashCode、
toString。数组元素类型分别是 OpenMBean*Info（读出时以接口类型接收）。

## 2. Java 用户典型用法
new OpenMBeanInfoSupport(className, desc, attrs, ctors, ops, notifs[, descriptor])；null 数组归一化
为空数组；客户端遍历 getAttributes 再逐个 getOpenType 渲染。MXBean 注册后 MBeanServer.getMBeanInfo
返回的即是 OpenMBeanInfoSupport（属性为 OpenMBeanAttributeInfo、复合类型映射 CompositeType）。

## 3. 参考来源
- Oracle javadoc：javax.management.openmbean.OpenMBeanInfo。
- OpenJDK 源码 + test/jdk/javax/management/mxbean（MXBean 开放映射权威）。
- 双侧 javap 实测。

## 4. 测试用例设计 + 易错细节（OpenMBeanInfoTest，批 G 深化）
- 结构：精确方法集 9 个；无公开字段。
- 契约：四段经接口引用取真值；null 数组 → 空（不抛）；equals 经接口引用成立/失败两条路径；
  getAttributes 元素是 OpenMBeanAttributeInfo。
- 版本差异：两侧一致。
