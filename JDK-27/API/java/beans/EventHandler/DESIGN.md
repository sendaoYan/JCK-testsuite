# java.beans.EventHandler — 设计案

## 1. API 功能
事件处理器（implements InvocationHandler）。构造器 `(target, action, eventPropertyName, listenerMethodName)`；静态 `create` 返回动态代理，getAction/getTarget/getEventPropertyName/getListenerMethodName。

## 2. Java 用户如何使用
`EventHandler.create(ActionListener.class, target, "fire")` 把监听器事件转发到 target.fire。

## 3. 开源代码实际用法调研
JDK 源码 `EventHandler.java`；JavaBeans/IDE 绑定工具使用。

## 4. 测试用例设计 + 自动化实现
白盒：implements InvocationHandler。黑盒（`testBlackBox`）：构造器 getter 与 create 动态代理调用。
