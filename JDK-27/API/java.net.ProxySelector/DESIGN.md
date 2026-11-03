# ProxySelector 设计文档

## 一、API 功能

抽象类，根据目标 URI 选择代理列表。

## 二、Java 用户如何使用

用户实现 select(URI)/connectFailed 并通过 ProxySelector.setDefault 注册全局选择器。

## 三、开源代码实际用法调研

JDK 内建 sun.net.spi.DefaultProxySelector 默认实现；框架自定义代理路由。

## 四、测试用例设计

getDefault/setDefault 往返；匿名子类实现 select 返回列表、connectFailed 被调用。
