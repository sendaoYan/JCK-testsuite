# javax.management.PersistentMBean 契约测试设计

## 1. API 功能
- 持久化钩子接口：load()/store()，各抛 MBeanException、RuntimeOperationsException、InstanceNotFoundException。

## 2. Java 用户典型用法
```java
class P implements CounterMBean, PersistentMBean {
    public void load() { ... }
    public void store() throws MBeanException { ... }
}
```

## 3. 参考来源
- Oracle javadoc：javax.management.PersistentMBean。

## 4. 测试用例设计
- 既有：接口结构反射。

## 深化（2026-11，批次 e）
新增维度：接口形态（throws 签名）、记录型实现、store 异常传播、与标准 MBean 共存。
- 接口：load/store 各声明 3 个受检异常（MBeanException、RuntimeOperationsException、InstanceNotFoundException），反射锁定。
- PPersist implements PPersistMBean, PersistentMBean：load()/store() 直接调用记录；store() 抛 MBeanException（cause 同一传播）。
- 作为标准 MBean 注册成功（命名约定 PPersist→PPersistMBean）；属性/invoke 路由正常；**load/store 不出现在 getMBeanInfo().getOperations()**（持久化钩子不是管理操作）。
### 版本差异
- 无。
