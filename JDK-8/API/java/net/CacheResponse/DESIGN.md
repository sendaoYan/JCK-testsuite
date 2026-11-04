# CacheResponse 设计文档（JDK-8 侧）

## 一、API 功能

协议无关的缓存响应读取抽象：getHeaders() 返回响应头映射，getBody() 返回体流。

## 二、Java 用户如何使用

由 ResponseCache.get 返回；协议栈读头与体。

## 三、开源代码实际用法调研

OkHttp Cache 命中路径同型。

## 四、测试用例设计

白盒成员集与抽象性、IOException 声明、头映射保真、体流独立性与 EOF、异常传播。

## 五、契约深化补充（批次 D，探针验证于 JDK-8）

**已深化断言（40 条）覆盖：** 精确成员集 {getHeaders, getBody} 均抽象、均声明 IOException；每次 getBody 独立新流（各自 offset 0）；Java-8 安全的全量读辅助（无 InputStream.readAllBytes，手工循环）；耗尽后恒 -1；空体即 EOF；多值/空列表/null 值头保真；不可修改映射透传（返回即传入实例）；IOException 消息保真；Object 契约。原始断言保留于 testLegacy。
