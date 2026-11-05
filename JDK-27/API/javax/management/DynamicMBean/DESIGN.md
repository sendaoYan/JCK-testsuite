# javax.management.DynamicMBean 黑盒行为测试设计

## 1. API 功能
- 接口：`getAttribute/setAttribute/getAttributes/setAttributes/invoke/getMBeanInfo`。

## 2. Java 用户如何使用
动态 MBean 在运行时通过 getMBeanInfo 提供元数据，并由 MBeanServer 调 get/set/invoke 完成操作。
```java
public class MyMBean implements DynamicMBean { ... }
mbs.registerMBean(new MyMBean(), on);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanServer/MBeanTest.java`：DynamicMBean 注册与调用。
- `test/jdk/javax/management/MBeanInfo/*Test.java`：元数据一致性。
- 实现权威：`src/java.management/share/classes/javax/management/DynamicMBean.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：用一个最小 DynamicMBean 注册到平台服务器，验证 getAttribute/setAttribute 往返、getAttributes/setAttributes 批量、invoke 调用、getMBeanInfo 返回元数据；finally 注销。
- 白盒：public 接口、含 getAttribute/setAttribute/invoke/getMBeanInfo。

## 深化（2026-11，批次 e）
新增维度：记录型全方法透传（真实 server 路由）、自造 MBeanInfo（不经内省）、负例透传。
- RecDyn 实现全部 6 个方法并记录调用串：注册后 server.getAttribute/setAttribute/invoke/getAttributes/setAttributes **逐条透传**到 DynamicMBean 方法（调用序列前缀精确断言 + contains 断言）。
- getMBeanInfo 返回完全自造的 info（className=自造类、1 属性、1 操作、构造器为空）——DynamicMBean 不做标准内省（类不实现任何 MBean 接口也能服务属性）。
- isInstanceOf(自造类名)=true、isInstanceOf("javax.management.StandardMBean")=false。
- 负例透传：getAttribute 未知 → AttributeNotFoundException（bean 自己抛的）；setAttribute 未知 → AttributeNotFoundException；invoke 未知 → MBeanException 且 getCause() 是 bean 塞的同一 NoSuchMethodException。
### 版本差异
- 无（两侧调用序列一致）。
