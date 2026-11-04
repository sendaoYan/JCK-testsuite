# javax.management.MBeanServerInvocationHandler 契约测试设计（2026-11 新建）

## 1. API 功能
- InvocationHandler：把接口调用翻译为 MBeanServerConnection 的 getAttribute/setAttribute/invoke；newProxyInstance 便捷工厂。

## 2. Java 用户典型用法
见 JDK-27 侧同目录设计与下节深化用例（双侧同代码）。

## 3. 参考来源
- Oracle javadoc: javax.management.MBeanServerInvocationHandler。
- OpenJDK `test/jdk/javax/management/` 对应目录（思路借鉴，代码自写）。
## 4. 测试用例设计
见下节深化清单。

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
