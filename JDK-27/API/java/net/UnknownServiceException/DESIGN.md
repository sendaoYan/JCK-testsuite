# UnknownServiceException 设计文档

## 一、API 功能

URLConnection 请求了不支持的协议服务(如未知输入/输出)时抛出的异常。

## 二、Java 用户如何使用

用户在 URL.openConnection().getOutputStream() 等不支持的协议操作捕获。

## 三、开源代码实际用法调研

协议不支持特定操作的场景。

## 四、测试用例设计

继承 IOException；构造器。
