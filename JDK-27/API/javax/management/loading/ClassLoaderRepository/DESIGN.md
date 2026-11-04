# javax.management.loading.ClassLoaderRepository — 设计案

## 1. API 功能
- 单一接口：表示 MBeanServer 的类加载器仓库，无实现类约束。
- 两个方法：`loadClass(String className)` 委派给仓库中第一个能加载该类的 ClassLoader；`loadClassWithout(ClassLoader exclude, String className)` 在排除指定 loader 的集合里加载。
- 两者都可能抛 `ClassNotFoundException`；`loadClassWithout` 的 `exclude` 可为 null。

## 2. Java 用户如何使用
用户通常不直接实例化该接口，而是通过 `MBeanServer.getClassLoaderRepository()` 取得，或在自定义 loader 上实现该接口参与类仓库。
```java
ClassLoaderRepository repo = mbs.getClassLoaderRepository();
Class<?> c = repo.loadClass("com.example.ManagedResource");
```

## 3. 开源代码实际用法调研
- JDK 自用：`javax.management.loading.MLet` / `DefaultLoaderRepository` 相关代码；`MBeanServer.getClassLoaderRepository()` 返回该接口实例。
- `test/jdk/javax/management/loading/LoaderRepositoryTest.java` 覆盖 loadClass / loadClassWithout 的委派与 ClassNotFoundException 路径。
- 说明：接口行为集中在“委派加载”和“未命中抛异常”两点，测试据此设计匿名实现来黑盒验证。

## 4. 测试用例设计
- 白盒：public 接口、非类、方法集合含 `loadClass` 与 `loadClassWithout`。
- 黑盒：匿名实现中 `loadClass` 对已知类返回对应 Class，对未知类抛 `ClassNotFoundException`；验证 `loadClass("java.lang.String")` 正确返回、未知类名抛异常、`loadClassWithout` 排除参数透传。
