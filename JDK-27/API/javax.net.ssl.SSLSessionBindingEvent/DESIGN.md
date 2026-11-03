# javax.net.ssl.SSLSessionBindingEvent 黑盒行为测试设计

## 1. API 功能
事件类，extends EventObject：`SSLSessionBindingEvent(SSLSession, String name)`；`getName()`/`getSession()`。

## 2. Java 用户如何使用
SSLSession.putValue/removeValue 触发 SSLSessionBindingListener 时携带该事件。

## 3. 开源代码实际用法调研
JDK `SSLSessionImpl.putValue` 构造该事件调用 valueBound/valueUnbound。

## 4. 测试用例设计 + 自动化实现
黑盒：getName/getSession/getSource 往返（用 Proxy 会话）。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
