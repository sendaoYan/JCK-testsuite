# javax.management.MBeanServerInvocationHandler 黑盒行为测试设计

## 1. API 功能
- 静态方法：`newProxyInstance(MBeanServerConnection, ObjectName, Class, boolean)`。
- `invoke(Object, Method, Object[])` 转发到 MBeanServerConnection。

## 2. Java 用户如何使用
创建 MBean 的本地代理，把方法调用翻译成 MBeanServer 的 getAttribute/setAttribute/invoke。
```java
ThingMBean proxy = MBeanServerInvocationHandler.newProxyInstance(mbs, on, ThingMBean.class, false);
int c = proxy.getCount();
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/proxy/JMXProxyTest.java`、`ProxyObjectMethodsTest.java`。
- `test/jdk/javax/management/MBeanServer/MBeanServerInvocationHandlerExceptionTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanServerInvocationHandler.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：newProxyInstance 返回接口代理，getter/setter/操作调用转发到已注册 StandardMBean；invoke 直接调用返回属性值；异常场景包装为 UndeclaredThrowableException 或原异常。
- 白盒：public 类、implements InvocationHandler、含静态 newProxyInstance。
