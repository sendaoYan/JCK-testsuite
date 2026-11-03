# URLStreamHandler 设计文档

## 一、API 功能

URL 协议处理器抽象类，负责协议相关 URL 解析(parseURL)与打开连接(openConnection)，供 URL 类回调。

## 二、Java 用户如何使用

用户自定义协议时继承 URLStreamHandler 并实现 openConnection，配合 URLStreamHandlerFactory 或带 handler 的 URL 构造器。

## 三、开源代码实际用法调研

JDK 内建 http/https/ftp/file/jar 处理器均派生自此；自定义 schema 的框架亦常实现。

## 四、测试用例设计

子类暴露 openConnection/getDefaultPort；默认 getDefaultPort 返回 -1；用带 handler 的 URL 构造验证 parseURL 拆分 protocol/host/port/path。
