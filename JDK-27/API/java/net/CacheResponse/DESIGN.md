# CacheResponse 设计文档

## 一、API 功能

抽象类，表示从缓存读取的响应(体与头)。

## 二、Java 用户如何使用

用户继承实现 getBody() 返回输入流、getHeaders() 返回头 Map。

## 三、开源代码实际用法调研

ResponseCache.get 返回 CacheResponse 供网络栈读取。

## 四、测试用例设计

验证 abstract 及 getBody/getHeaders；匿名子类返回空流与头。
