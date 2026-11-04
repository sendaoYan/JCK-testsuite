# java.util.jar.Pack200.Unpacker（JDK-8 专属） 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

解包引擎属性接口。

## 2. Java 用户如何使用

unpack(InputStream,JarOutputStream) 目标须为普通 JarOutputStream（manifest 重复会 ZipException）。

## 3. 参考来源

JSR-200 Unpacker javadoc。

## 4. 测试用例设计

UnpackerTest + Pack200Test round-trip。
