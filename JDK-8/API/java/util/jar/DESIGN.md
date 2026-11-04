# java.util.jar 包 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

Manifest/Attributes 头部语法、JarFile/JarStreams 容器、Pack200（JDK-8 专属）压缩。

## 2. Java 用户如何使用

Attributes 大小写不敏感；72 字节折行续接；jar 签名属性名常量；Pack200 pack/unpack round-trip。

## 3. 参考来源

JAR File Specification；Pack200 JSR-200。

## 4. 测试用例设计

Attributes/Manifest/JarEntry/JarFile/JarInputStream/JarOutputStream/JarException 各 testContracts + Pack200 三件套。
