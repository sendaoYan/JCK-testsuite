# ResponseCache 设计文档

## 一、API 功能

抽象类，缓存 URLConnection 的响应。

## 二、Java 用户如何使用

用户实现 get/put 并通过 ResponseCache.setDefault 注册。

## 三、开源代码实际用法调研

需要离线或性能缓存的 HTTP 客户端使用。

## 四、测试用例设计

getDefault/setDefault 往返；匿名子类实现 get/put 并断言可调用。
