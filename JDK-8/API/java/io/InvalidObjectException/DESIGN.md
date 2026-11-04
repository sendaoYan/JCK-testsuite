# java.io.InvalidObjectException 测试方案 (DESIGN)

## 1. API 功能
- 单构造器 `(String)`（JDK 8 无 cause 构造，JDK 9+ 才加入 `(String, Throwable)`）；继承 ObjectStreamException；表示反序列化得到的对象不合法。

## 2. Java 用户如何使用
- readObject 校验回调抛出以拒绝不合法的对象图。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/InvalidObjectException.java`；`ObjectInputStream.readUnshared` 遇反向引用抛出。

## 4. 测试用例设计 + 自动化实现（InvalidObjectExceptionTest.java，纯黑盒）
黑盒场景:
1. `(String)` 构造消息、无 cause。
2. registerValidation 校验抛出的异常以原消息传播。
3. readUnshared 遇反向引用抛 InvalidObjectException。
4. 作为 IOException 捕获。
- 健壮性: 无反射白盒；serialVersionUID 显式固定。
