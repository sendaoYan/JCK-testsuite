# javax.management.loading.PrivateClassLoader — 设计案

## 1. API 功能
- 单一标记接口（marker interface），无方法、无字段。
- 语义：标识某个 ClassLoader 不应被放入 MBeanServer 的默认类加载器仓库（即“私有” loader）。

## 2. Java 用户如何使用
用户让自定义 ClassLoader 实现该接口，表示它不参与 `ClassLoaderRepository` 的共享委派。
```java
class PrivateLoader extends ClassLoader implements PrivateClassLoader {}
```

## 3. 开源代码实际用法调研
- JDK `javax.management.loading.MLet` 文档与相关源码将实现该接口的 loader 排除出默认仓库。
- 该接口在开源项目中使用极少，仅作为能力标记，测试聚焦接口结构与可组合性。

## 4. 测试用例设计
- 白盒：public 接口、`getDeclaredMethods().length == 0`、`getDeclaredFields().length == 0`。
- 黑盒：定义 `class PrivateLoader extends ClassLoader implements PrivateClassLoader`，断言实例同时是 `ClassLoader` 与 `PrivateClassLoader`。
