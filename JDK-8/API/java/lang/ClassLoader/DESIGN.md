# java.lang.ClassLoader 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「getSystemClassLoader 读一下」的冒烟。重点补：白盒结构（abstract、
> Object 超类、protected defineClass）、系统类加载器、父链终止于 null（bootstrap）、资源查找
> （getResource/getResourceAsStream/getSystemResource*/getResources，缺失返回 null/空）、loadClass
> 已知类与数组类型、缺失类抛 ClassNotFoundException、断言状态控制，以及通过具体子类暴露的
> protected 成员（findSystemClass/findLoadedClass/getClassLoadingLock/registerAsParallelCapable/
> resolveClass/setSigners/findResource/findResources/findLibrary/getPackage/getPackages/definePackage/
> findClass 默认行为）。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **抽象类**：`java.lang.ClassLoader`（abstract，Object 超类）。
- **类加载**：`loadClass(String)`（public）、`loadClass(String,boolean)`（protected）、
  `findClass`（protected 默认抛 CNFE）、`defineClass`（protected final，多重重载）、
  `findSystemClass`/`findLoadedClass`/`resolveClass`（protected final）、`getClassLoadingLock`。
- **资源**：`getResource`/`getResources`/`getResourceAsStream` + 静态 `getSystemResource`/
  `getSystemResources`/`getSystemResourceAsStream`；protected `findResource`/`findResources`/
  `findLibrary`。
- **层级**：`getParent`（final）、`getSystemClassLoader`（static）。
- **断言状态**：`setDefaultAssertionStatus`/`setPackageAssertionStatus`/`setClassAssertionStatus`/
  `clearAssertionStatus`。
- **包**：protected `getPackage`/`getPackages`/`definePackage`；`setSigners`/`registerAsParallelCapable`。

## 2. Java 用户如何使用

- `ClassLoader.getSystemClassLoader().loadClass(name)` 显式加载；`getResourceAsStream("path")` 读 classpath 资源。
- 自定义类加载器继承 `ClassLoader`，重写 `findClass` 后用 `defineClass` 定义字节码。
- 双亲委派：`loadClass` 先委托父加载器，父链最终到 bootstrap（getParent 返回 null）。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/ClassLoader.html`）：loadClass 委派、资源查找、
  protected 成员契约。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/ClassLoader.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：`getPackage("java.lang")` 经父链找到、`definePackage` 重复抛 IAE、
  `findClass` 默认 CNFE、`findResource` 默认 null、`findResources` 默认空、`findLibrary` 默认 null。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/ClassLoader`。

## 4. 测试用例设计（映射到 ClassLoaderTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：abstract、Object 超类、defineClass protected | testWhiteBox |
| P0 | 系统类加载器非空 | testSystemLoader |
| P0 | 父链存在且终止于 null（bootstrap） | testParentChain |
| P0 | 资源查找：标准资源存在、缺失返回 null/空 | testResources |
| P0 | loadClass 已知类/数组类型/缺失类 CNFE | testLoadClass |
| P1 | 断言状态控制（set*/clear 可调用） | testAssertionStatus |
| P0 | protected 成员：findSystemClass/findLoadedClass/getClassLoadingLock/registerAsParallelCapable/resolveClass/setSigners/findResource/findResources/findLibrary/getPackage/getPackages/definePackage(重复 IAE)/findClass 默认 CNFE | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- **父链最终为 null**：bootstrap 加载器没有 Java 对象（`getParent()` 返回 null）。
- **`findClass` 默认抛 `ClassNotFoundException`**；`findResource` 默认 null、`findResources` 默认空、
  `findLibrary` 默认 null（子类需覆写）。
- **`getClassLoadingLock(name)` 对同名返回同一锁**。
- **`definePackage` 重复定义同包抛 `IllegalArgumentException`**。
- **`getPackage("java.lang")` 经父链找到 bootstrap 定义的包**。
- **JDK-8 无 `getName`/`getPlatformClassLoader`/`getDefinedPackage(s)`/`getUnnamedModule`/
  `resources()` 流/`findClass(moduleName,name)`/`ClassLoader(String,ClassLoader)` 公共构造器**（Java 9+）。

## 注意事项

- 不真正 defineClass 任意字节码（避免 ClassFormatError 与类命名污染）；仅断言 protected 成员默认行为。
- headless 安全，不加 `@key`；结果确定。
