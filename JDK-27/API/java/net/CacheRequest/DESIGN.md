# CacheRequest 设计文档

## 一、API 功能

抽象类，表示向缓存写入响应体的流。

## 二、Java 用户如何使用

用户继承实现 getBody() 返回输出流、abort() 终止。

## 三、开源代码实际用法调研

ResponseCache.put 返回 CacheRequest 供网络栈写入。

## 四、测试用例设计

验证 abstract 及 getBody/abort 方法；匿名子类返回 ByteArrayOutputStream。

## 五、契约深化补充（批次 D，探针验证于 JDK-27）

**已深化断言（32 条）覆盖：**
- 精确成员集 = {getBody, abort}；均抽象；getBody → OutputStream 且声明 IOException；abort → void 无受检异常。
- 黑盒：写入体字节往返、abort 无基类状态机（连按两次回调两次）、getBody 可返回稳定引用或每次新流（子类自决）、abort 后 getBody 仍可用（子类允许时）、IOException 精确传播。
- Object 契约：身份 equals/hashCode/toString；子类可捕获 abort 先于 getBody 的状态。
