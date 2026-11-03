# jdk.security.jarsigner.JarSignerException 设计案

## 1. API 功能
JAR 签名异常（extends RuntimeException）：message+cause。

## 2. Java 用户如何使用
new JarSignerException(msg, cause)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.security.jarsigner/share/classes/jdk/security/jarsigner/JarSignerException.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 message/cause/hierarchy。
