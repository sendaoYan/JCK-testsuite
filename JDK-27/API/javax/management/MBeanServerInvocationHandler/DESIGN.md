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

## 深化（2026-11）：代理闭环 + 失败形态

### 新增覆盖
1. **round-trip**（本地 newMBeanServer）：getter 读真值、setter 到 server 与实现双可见、is-getter、带参操作、void 操作、Object 方法本地应答（equals/hashCode 稳定/toString 含 ObjectName）。
2. **构造器与直接 invoke**：getMBeanServerConnection==传入 server、getObjectName 精确、isMXBean 默认 false/显式 true；`InvocationHandler.invoke(proxy, Method, args)` 直接可用。
3. **失败形态（实测锁定）**：
   - 目标未注册 → `UndeclaredThrowableException`(cause `InstanceNotFoundException`)。
   - 代理接口比 MBean 宽（幽灵 getter）→ `UndeclaredThrowableException`(cause `AttributeNotFoundException`)。
   - 接口方法声明 throws 的业务异常：server 层是 `MBeanException`(cause==原始实例)；**代理层把原始异常原样重抛**（不重新包装，e==Meter.BOOM）——MBeanServerInvocationHandler 的关键契约。

### 参考来源
- Oracle javadoc: MBeanServerInvocationHandler（异常重抛规则）。
- OpenJDK `test/jdk/javax/management/MBeanServer/MBeanServerInvocationHandlerExceptionTest.java`（思路）。
