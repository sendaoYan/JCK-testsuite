# javax.management.QueryEval 契约测试设计

## 1. API 功能
- 查询上下文载体（abstract）：setMBeanServer(MBeanServer)（ThreadLocal、非静态方法但与实例无关）、static getMBeanServer()。

## 2. Java 用户典型用法
```java
new QueryEval() {}.setMBeanServer(server);   // 为 AttributeValueExp.apply 提供上下文
```

## 3. 参考来源
- Oracle javadoc：javax.management.QueryEval（InheritableThreadLocal 语义）。

## 4. 测试用例设计
- 既有：结构反射。

## 深化（2026-11，批次 e）
新增维度：ThreadLocal 语义、InheritableThreadLocal 继承、server 查询的上下文注入与恢复。
- setMBeanServer/getMBeanServer：同线程设置读取同一实例；setMBeanServer(null) 清空。
- **子线程继承**：父线程设置后创建的子线程 getMBeanServer()==server（InheritableThreadLocal 语义）；父清空后新子线程为 null。
- **server 查询契约**（实测两侧一致）：查询求值期间 server 通过 query.setMBeanServer(server) 注入上下文（自定义 ContextExp.apply 内读到非 null）；求值结束 finally **恢复查询前的旧值**（不是无条件清空——查询前为 null 则查询后 null，查询前为 server 则查询后仍 server；JDK 源码 DefaultMBeanServerInterceptor 注释明确说明该恢复语义）。
- 匿名 QueryEval 子类 + QueryExp.apply 用 getMBeanServer 实证上下文可见性。
### 版本差异
- 无行为差异；但 JDK-27 侧原有黑盒用例在主线程设置了 ThreadLocal 未清理，深化断言写成"恢复查询前值"以两侧通用。
