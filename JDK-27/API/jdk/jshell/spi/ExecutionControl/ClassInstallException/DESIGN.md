# jdk.jshell.spi.ExecutionControl.ClassInstallException 设计案

## 1. API 功能
类装载部分失败：installed() 逐类成功标志。

## 2. Java 用户如何使用
LoaderDelegate.load 抛出；线协议 RESULT_CLASS_INSTALL_EXCEPTION 随消息一起传 boolean[]。

## 3. 开源代码实际用法调研
来源：ExecutionControl.java + ExecutionControlForwarder.java（JDK-27 src.zip）。

## 4. 测试用例设计 + 易错点
installed() 共享数组引用；空表合法；序列化往返保标志位。

