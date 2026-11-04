# java.util.EventListenerProxy — 测试设计案

## 1. API 功能
包装一个具体 `EventListener` 并实现 `EventListener` 的抽象骨架：让事件源既能持有真实监听器，
又能附加代理语义（如序列化占位）。

- 构造：`EventListenerProxy(T listener)`（public）。
- 查询：`T getListener()`。

## 2. Java 用户如何使用
`class MyProxy extends EventListenerProxy<MyListener> { MyProxy(MyListener l){super(l);} }` 用于
在事件分发链路上包装/识别监听器。
链路：new Proxy(listener) -> getListener() 取回 -> instanceof EventListener -> 分发。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/EventListenerProxy.java`（本地 `~/git/jdk25u-dev`）：
  仅 getListener + 构造，javadoc 说明用于序列化监听器。
- OpenJDK `src/java.desktop/share/classes/java/awt/Toolkit.java#AWTEventListenerProxy`：继承
  EventListenerProxy 的实现样例。
- OpenJDK `test/jdk/java/lang/Class/GenericStringTest.java` 等间接引用。

## 4. 测试用例设计 + 自动化实现（EventListenerProxyTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| getListener | 返回包装的同一实例（身份） |
| 独立性 | proxy != listener |
| null | 构造接受 null、getListener 返回 null |
| 泛型 | 类型化 getListener 保留具体类型 |
| 多代理 | 多个代理共享同一监听器 |

共 5 组场景、约 6 个 check（`testBlackBox()`）。
