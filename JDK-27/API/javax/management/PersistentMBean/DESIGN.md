# javax.management.PersistentMBean 黑盒行为测试设计

## 1. API 功能
- 接口：`load()`、`store()`。

## 2. Java 用户如何使用
持久 MBean 标记接口，实现类负责把自身状态保存/加载到持久存储。
```java
public class MyMBean implements PersistentMBean {
    public void load() { ... }
    public void store() { ... }
}
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanServer/MBeanTest.java`：注册流程中的持久化接口。
- 实现权威：`src/java.management/share/classes/javax/management/PersistentMBean.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：用简单实现验证 load/store 可被调用且不影响注册。
- 白盒：public 接口、含 load/store。

## 深化（2026-11，批次 e）
新增维度：接口形态（throws 签名）、记录型实现、store 异常传播、与标准 MBean 共存。
- 接口：load/store 各声明 3 个受检异常（MBeanException、RuntimeOperationsException、InstanceNotFoundException），反射锁定。
- PPersist implements PPersistMBean, PersistentMBean：load()/store() 直接调用记录；store() 抛 MBeanException（cause 同一传播）。
- 作为标准 MBean 注册成功（命名约定 PPersist→PPersistMBean）；属性/invoke 路由正常；**load/store 不出现在 getMBeanInfo().getOperations()**（持久化钩子不是管理操作）。
### 版本差异
- 无。
