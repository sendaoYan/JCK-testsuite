# URLClassLoader 设计文档

## 一、API 功能

从一组 URL(目录或 jar)加载类与资源的 ClassLoader，支持 close 释放资源。

## 二、Java 用户如何使用

用户 new URLClassLoader(urls, parent) 构造并 loadClass()，从远程或本地 jar 动态加载类。

## 三、开源代码实际用法调研

应用服务器、插件系统、构建工具(Maven/Gradle 隔离类加载)广泛使用 URLClassLoader。

## 四、测试用例设计

用临时目录中的类字节加载(或用自身 URL)验证 loadClass；getURLs 返回构造时数组副本；close 后资源关闭；继承 ClassLoader。
