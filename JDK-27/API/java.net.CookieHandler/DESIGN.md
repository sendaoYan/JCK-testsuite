# CookieHandler 设计文档

## 一、API 功能

抽象类，处理 HTTP Cookie 的存取(get/put)与策略。

## 二、Java 用户如何使用

用户继承实现 get/put 或使用 CookieManager，通过 CookieHandler.setDefault 注册。

## 三、开源代码实际用法调研

JDK 网络栈在收到/发送请求时回调默认 CookieHandler。

## 四、测试用例设计

getDefault/setDefault 往返；匿名子类实现 get/put 并断言被回调。
