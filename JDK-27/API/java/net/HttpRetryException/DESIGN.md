# HttpRetryException 设计文档

## 一、API 功能

HTTP 重试时抛出的异常，携带响应码与原因短语。

## 二、Java 用户如何使用

用户在 URLConnection 客户端设置响应缓存重试逻辑时捕获。

## 三、开源代码实际用法调研

HTTP 缓存重试场景使用。

## 四、测试用例设计

继承 IOException；构造(int responseCode, String detail) 与带 location 构造；getResponseCode/getReason/getLocation。
