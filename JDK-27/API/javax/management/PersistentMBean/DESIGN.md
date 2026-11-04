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
