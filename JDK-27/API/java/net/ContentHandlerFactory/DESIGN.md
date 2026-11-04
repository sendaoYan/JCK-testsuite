# ContentHandlerFactory 设计文档

## 一、API 功能

工厂接口，根据 MIME 类型返回 ContentHandler。

## 二、Java 用户如何使用

用户实现 createContentHandler(String) 并通过 URLConnection.setContentHandlerFactory 注册。

## 三、开源代码实际用法调研

自定义内容类型处理使用。

## 四、测试用例设计

验证为单抽象方法接口；实现并调用其 SAM 方法。
