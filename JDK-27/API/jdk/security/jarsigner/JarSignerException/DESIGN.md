# jdk.security.jarsigner.JarSignerException 设计案

## 1. API 功能
JAR 签名异常（extends RuntimeException）：message+cause。

## 2. Java 用户如何使用
new JarSignerException(msg, cause)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.security.jarsigner/share/classes/jdk/security/jarsigner/JarSignerException.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 message/cause/hierarchy。

## 5. 契约深化（JDK-27）

- 层级锁定：直接超类为 RuntimeException（unchecked 签名失败类别），非 IOException。
- 构造面收窄：仅 public (String, Throwable) 一个构造器；message/cause 原样保持；stackTrace 可替换。
- serialVersionUID 精确 -4732217075689309530L（ObjectStreamClass 反射锁定）。
