# CacheRequest 设计文档（JDK-8 侧）

## 一、API 功能

协议无关的响应体写入通道：getBody() 返回写入流，abort() 放弃写入。

## 二、Java 用户如何使用

由 ResponseCache.put 返回给协议栈；协议栈写体或 abort。

## 三、开源代码实际用法调研

OkHttp Cache、HttpClient 缓存层同型抽象。

## 四、测试用例设计

白盒成员集与抽象性、IOException 声明、写入往返、abort 语义、流引用稳定性。

## 五、契约深化补充（批次 D，探针验证于 JDK-8）

**已深化断言（34 条）覆盖：** 精确成员集 {getBody, abort} 均抽象；getBody→OutputStream 且声明 IOException、abort→void 无受检异常；体字节写入往返；abort 无基类状态机（两次回调两次）；getBody 稳定引用与每调用新流两种子类形态；abort 后 getBody 仍可用；IOException 精确传播；Object 身份契约；abort 先于 getBody 的状态捕获。原始断言保留于 testLegacy。
