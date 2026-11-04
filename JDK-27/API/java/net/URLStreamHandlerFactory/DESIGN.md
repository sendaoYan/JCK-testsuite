# URLStreamHandlerFactory 设计文档

## 一、API 功能

工厂接口，根据 protocol 字符串返回对应的 URLStreamHandler。

## 二、Java 用户如何使用

用户实现 createURLStreamHandler(String) 并通过 URL.setURLStreamHandlerFactory 注册全局协议。

## 三、开源代码实际用法调研

某些框架注册自定义协议(如 classpath:)时实现此接口。

## 四、测试用例设计

验证为单抽象方法接口；实现一个返回 null/自定义 handler 的工厂并调用其 SAM 方法。
