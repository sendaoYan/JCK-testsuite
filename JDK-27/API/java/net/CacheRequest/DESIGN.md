# CacheRequest 设计文档

## 一、API 功能

抽象类，表示向缓存写入响应体的流。

## 二、Java 用户如何使用

用户继承实现 getBody() 返回输出流、abort() 终止。

## 三、开源代码实际用法调研

ResponseCache.put 返回 CacheRequest 供网络栈写入。

## 四、测试用例设计

验证 abstract 及 getBody/abort 方法；匿名子类返回 ByteArrayOutputStream。
