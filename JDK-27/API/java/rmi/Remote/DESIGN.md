# java.rmi.Remote 行为测试设计

## 1. API 功能
`Remote` 是一个标记接口，用于标识其方法可从另一 JVM 调用的对象。没有任何方法。

## 2. Java 用户如何使用
- 声明远程接口：`public interface Foo extends Remote { void bar() throws RemoteException; }`。
- 任何远程实现类都（间接）`instanceof Remote`。

## 3. 开源代码实际用法调研
- `java.rmi.registry.Registry` 与 `java.rmi.dgc.DGC` 均 `extends Remote`，见 `src/java.rmi/share/classes/java/rmi/registry/Registry.java`、`dgc/DGC.java`。
- JDK 测试大量自定义 `interface X extends Remote`，如 `test/jdk/java/rmi/Naming/legalRegistryNames/LegalRegistryNames.java`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（RemoteTest.testBlackBox）：匿名实现 `new Remote() {}` 非空、`instanceof Remote`、两个实例不同。
白盒部分（testWhiteBox）：接口、无父接口、无声明方法（标记接口形态）。
