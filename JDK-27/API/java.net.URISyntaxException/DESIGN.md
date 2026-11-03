# URISyntaxException 设计文档

## 一、API 功能

URI 字符串解析失败时抛出的异常，携带输入串与原因索引。

## 二、Java 用户如何使用

用户在 new URI(spec) 时捕获。

## 三、开源代码实际用法调研

URI 解析校验。

## 四、测试用例设计

继承 Exception；构造器与 getInput/getIndex/getReason；非法 URI 触发。
