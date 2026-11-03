# jdk.security.jarsigner.JarSigner 设计案

## 1. API 功能
JAR 签名器（final，Builder 构建）。

## 2. Java 用户如何使用
JarSigner.Builder(PrivateKey, CertPath).build()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.security.jarsigner/share/classes/jdk/security/jarsigner/JarSigner.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 Builder 静态默认算法。
