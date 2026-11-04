# java.util.jar.Pack200（JDK-8 专属） 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

packer()/unpacker() 单例工厂；JSR-200 压缩。

## 2. Java 用户如何使用

Packer.EFFORT 默认 "5"、KNOWN/CHANGEABLE 属性面、PASS_FILE_PTX；pack(JarInputStream,OutputStream)/unpack(InputStream,JarOutputStream) round-trip 保留 Main-Class 与条目内容。

## 3. 参考来源

JSR-200；JDK-14 移除（JDK-27 侧以 ClassNotFoundException 探针锁定缺席）。

## 4. 测试用例设计

Pack200Test：属性面 + round-trip（JarInputStream→pack→unpack→JarInputStream 读回）。
