# MalformedURLException 设计文档

## 一、API 功能

URL 字符串格式非法时抛出的异常。

## 二、Java 用户如何使用

用户在 new URL(spec) 时捕获。

## 三、开源代码实际用法调研

所有解析用户输入 URL 的代码都需要处理。

## 四、测试用例设计

继承 IOException；构造器；new URL("http://example.com:abc/") 触发。
