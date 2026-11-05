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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 加载器链：system==Thread context loader；JDK-8 链 AppClassLoader(sun.misc.Launcher$AppClassLoader)→ExtClassLoader(sun.misc.Launcher$ExtClassLoader)→null(bootstrap)；bootstrap 类 getClassLoader()==null 全表（含数组类）。
- 委托顺序实证：RecordingLoader 重写 loadClass 记录日志——先子后父（loadClass 钩子先于 parent 解析）、父成功不递归回子、findLoadedClass(null-name)==null 证明未在子定义；null 双亲=仅 bootstrap（loadClass("java.lang.Integer") 成功、loadClass(classpath 类) CNFE）。
- loadClass 契约：6 类矩阵逐一 ==、同loader 二次调用同一 Class 对象；数组描述符 "[Ljava.lang.String;" 不是合法 loadClass 名 → CNFE（消息即原文）；loadClass(null) NPE。
- defineClass 禁止包名负例（双侧一致）：读 String.class 真字节 defineClass("java.lang.String"/null 推断名/"java.util.Fake") → SecurityException "Prohibited package name: java.lang|java.util" 精确锁定。
- 资源：Object.class 流前 4 字节 CAFEBABE、缺失资源 null / 空 Enumeration（非 null）、ClassLoader 查找不剥离前导斜杠（"/java/..." → null）、子 loader 资源委托父。
- 包命名空间：子 loader definePackage 的包对 system loader 不可见（Package.getPackage → null）；definePackage 全 getter 回读、sealed（sealBase URL）/bare（全 null 属性）矩阵、toString "package <n>, <specTitle>, version <ver>" 精确、hashCode==name.hashCode()、null specVersion isCompatibleWith → NFE。
- 保护成员：getClassLoadingLock——已注册 parallel-capable（CL.reg()）的 loader 按名分锁；未注册 loader 回退单锁（==loader 对象本身）；setSigners 后 getSigners 非 null 但防御拷贝（!=原数组）；findSystemClass 解析 classpath 类与 CNFE 消息。
- 断言状态闭环：setDefaultAssertionStatus(true)→desiredAssertionStatus true；package 级（""=default 包）覆盖默认；class 级覆盖一切；clearAssertionStatus 复原；**bootstrap 定义的类不咨询 app loader 的设置**。

### 2. 使用方式
静态嵌套 RecordingLoader/PkgLoader fixture；check 计数。

### 3. 参考来源
Oracle javadoc（双亲委托模型、defineClass 禁止包名、getClassLoadingLock 语义、registerAsParallelCapable）；JDK-8 ClassLoader 源码（preDefineClass 前缀截断 "java.*"）；双侧实测。

### 4. 测试用例设计 + 易错规范细节
- **易错 1**：JDK-8 的 ClassLoader.getPackage/getPackages/findLoadedClass 是 protected——用静态 Package.getPackage/getPackages（走 caller 的 system loader）或子类包装器。
- **易错 2**：defineClass 探测不能 setAccessible（模块系统拒绝）——必须经子类继承暴露。
- **易错 3**：registerAsParallelCapable 在首次调用后生效，锁行为测试需区分「已注册」与「未注册」两种 loader。
- **易错 4**：断言状态只影响「该 loader 定义」的类；java.util 等 bootstrap 类读不到 app loader 的设置。
- **易错 5**：loadClass 不接受数组描述符（forName 接受）——消息精确为原名字符串。
- 覆盖：testLoaderIdentityChain/testLoadClassDelegation/testDefineClassProhibited/testResourceDetails/testPackageNamespaces/testFindSystemAndProtected/testAssertionStatusReflection。
