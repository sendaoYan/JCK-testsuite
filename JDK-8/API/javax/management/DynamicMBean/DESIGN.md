# javax.management.DynamicMBean 契约测试设计

## 1. API 功能
- 通用 MBean 接口：getAttribute/setAttribute/getAttributes/setAttributes/invoke/getMBeanInfo。实现者完全自管元数据（不经标准内省）。

## 2. Java 用户典型用法
```java
class MyDyn implements DynamicMBean { ... getMBeanInfo() 返回自造 info ... }
server.registerMBean(new MyDyn(), on);  // server 全部透传
```

## 3. 参考来源
- Oracle javadoc：javax.management.DynamicMBean（透传语义、异常约定）。

## 4. 测试用例设计
- 既有：接口结构反射。

## 深化（2026-11，批次 e）
新增维度：记录型全方法透传（真实 server 路由）、自造 MBeanInfo（不经内省）、负例透传。
- RecDyn 实现全部 6 个方法并记录调用串：注册后 server.getAttribute/setAttribute/invoke/getAttributes/setAttributes **逐条透传**到 DynamicMBean 方法（调用序列前缀精确断言 + contains 断言）。
- getMBeanInfo 返回完全自造的 info（className=自造类、1 属性、1 操作、构造器为空）——DynamicMBean 不做标准内省（类不实现任何 MBean 接口也能服务属性）。
- isInstanceOf(自造类名)=true、isInstanceOf("javax.management.StandardMBean")=false。
- 负例透传：getAttribute 未知 → AttributeNotFoundException（bean 自己抛的）；setAttribute 未知 → AttributeNotFoundException；invoke 未知 → MBeanException 且 getCause() 是 bean 塞的同一 NoSuchMethodException。
### 版本差异
- 无（两侧调用序列一致）。
