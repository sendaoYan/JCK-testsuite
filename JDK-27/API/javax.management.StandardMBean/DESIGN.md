# javax.management.StandardMBean 黑盒行为测试设计

## 1. API 功能
- 实现 DynamicMBean 与 MBeanRegistration。
- 构造：`(T implementation, Class<T> mbeanInterface)`、`(Class<T> mbeanInterface, boolean isMXBean)`。
- 查询：`getMBeanInterface()`、`getImplementation()`、`getImplementationClass()`、`getMBeanInfo()`；get/setAttribute、invoke。

## 2. Java 用户如何使用
把标准 MBean 接口与实现包装后注册到 MBeanServer；内省自动生成元数据。
```java
StandardMBean m = new StandardMBean(impl, ThingMBean.class);
mbs.registerMBean(m, on);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/standardmbean/DeadlockTest.java`。
- `test/jdk/javax/management/Introspector/FeatureOrderTest.java`、`IsMethodTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/StandardMBean.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getMBeanInterface/getImplementation/getImplementationClass；getMBeanInfo 的 className 为实现类名、属性/操作内省正确；getAttribute/setAttribute/invoke 往返；注册后 MBeanServer 调用与 isInstanceOf（实现类名）；注销。
- 白盒：public 类、implements DynamicMBean、implements MBeanRegistration。
