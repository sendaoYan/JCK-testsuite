# ClassLoader — 设计方案

## 1. API 功能
`java.lang.ClassLoader`（abstract）加载类与资源：
- 类加载：`loadClass(String)`、`defineClass`（protected）
- 资源：`getResource/getResources/resources/getResourceAsStream` + 静态 `getSystem*` 版本
- 层级：`getParent`、`getSystemClassLoader`、`getPlatformClassLoader`
- 其他：`getName`、`getDefinedPackage(s)`、`setDefaultAssertionStatus`

## 2. Java 用户如何使用
- `ClassLoader.getSystemClassLoader().loadClass(name)` 显式加载；`getResourceAsStream("path")` 读取 classpath 资源。
- 自定义类加载器继承 `ClassLoader`，重写 `findClass` 后用 `defineClass` 定义字节码。
```java
ClassLoader cl = Thread.currentThread().getContextClassLoader();
InputStream in = cl.getResourceAsStream("config/app.properties");
```

## 3. 开源代码实际用法调研
- Spring `ClassPathResource`/`DefaultResourceLoader` 依赖 `ClassLoader.getResourceAsStream`。
- `ServiceLoader` 内部用 `ClassLoader.getResources("META-INF/services/...")`。
- JDK `test/jdk/java/lang/ClassLoader` 系列验证双亲委派、资源查找、自定义 defineClass。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.ClassLoader")` 验证 abstract、父类 `Object`、`defineClass` 为 protected。
黑盒：系统/平台类加载器非空；父子链存在且最终为 null（bootstrap）；读取标准资源 `java/lang/Object.class` 存在、缺失资源为 null；`loadClass` 加载已知类与数组类型、缺失类抛 `ClassNotFoundException`（基本类型用 `Class.forName` 解析）。
对应 `<Simple>Test.java` 黑盒部分。
