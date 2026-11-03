# ProtocolException 设计文档

## 一、API 功能

底层协议违反(如不支持的请求方法)时抛出的异常。

## 二、Java 用户如何使用

用户在 URLConnection 设置非法请求方法等场景捕获。

## 三、开源代码实际用法调研

HTTP 客户端协议错误处理。

## 四、测试用例设计

继承 IOException；构造器；setRequestMethod 非法值时触发。
