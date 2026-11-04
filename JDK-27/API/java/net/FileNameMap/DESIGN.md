# FileNameMap 设计文档

## 一、API 功能

接口，根据文件名返回 MIME 内容类型。

## 二、Java 用户如何使用

用户实现 getContentTypeFor(String) 或使用 URLConnection.getFileNameMap() 的默认实现。

## 三、开源代码实际用法调研

JDK 默认 FileNameMap 基于 mime.types 判断类型。

## 四、测试用例设计

验证为接口；URLConnection.getFileNameMap() 非空且对 .txt/.html 返回 text/plain、text/html。
