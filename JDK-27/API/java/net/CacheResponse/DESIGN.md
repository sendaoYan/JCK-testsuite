# CacheResponse 设计文档

## 一、API 功能

抽象类，表示从缓存读取的响应(体与头)。

## 二、Java 用户如何使用

用户继承实现 getBody() 返回输入流、getHeaders() 返回头 Map。

## 三、开源代码实际用法调研

ResponseCache.get 返回 CacheResponse 供网络栈读取。

## 四、测试用例设计

验证 abstract 及 getBody/getHeaders；匿名子类返回空流与头。

## 五、契约深化补充（批次 D，探针验证于 JDK-27）

**已深化断言（37 条）覆盖：**
- 精确成员集 = {getHeaders, getBody}；均抽象；均声明 IOException；返回 Map/InputStream。
- 体流语义：每次 getBody 独立新流（各自从 0 开始）、readAllBytes 往返、耗尽后恒 -1、空体即 EOF。
- 头语义：多值列表、空列表、null 值键、不可修改映射原样透传（返回值即传入实例）。
- IOException 精确传播（getHeaders/getBody 消息保真）；Object 契约。
