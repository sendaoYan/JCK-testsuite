# javax.net.ssl.SSLSessionBindingListener 黑盒行为测试设计

## 1. API 功能
接口，extends EventListener：`valueBound(SSLSessionBindingEvent)`/`valueUnbound(...)`。

## 2. Java 用户如何使用
对象作为 session value 放入时实现该接口，接收绑定/解绑回调。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/SSLSession/` 有绑定事件用例；`SSLSessionImpl` 在 put/remove/overwrite 时触发。

## 4. 测试用例设计 + 自动化实现
黑盒：is EventListener；真实握手 session 上验证（见 SSLSocket/SSLSession 测试）。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
