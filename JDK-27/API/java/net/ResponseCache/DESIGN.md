# ResponseCache 设计文档

## 一、API 功能

抽象类，缓存 URLConnection 的响应。

## 二、Java 用户如何使用

用户实现 get/put 并通过 ResponseCache.setDefault 注册。

## 三、开源代码实际用法调研

需要离线或性能缓存的 HTTP 客户端使用。

## 四、测试用例设计

getDefault/setDefault 往返；匿名子类实现 get/put 并断言可调用。

## 五、契约深化补充（批次 D，探针验证于 JDK-27）

**已深化断言（34 条）覆盖：**
- 精确成员集 = {get, put, getDefault, setDefault}；get/put 抽象、声明 IOException、返回 CacheResponse/CacheRequest；static 工厂对。
- 子类往返：get/put 收到精确 URI/method/headers（日志逐字段断言）、返回值即缓存实例（==）、IOException 精确传播。
- put 可接收 file: URLConnection（协议无关）。
- 默认生命周期：新 JVM null、替换/置 null/重装、finally 恢复。
