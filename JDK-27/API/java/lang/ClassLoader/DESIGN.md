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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 与 JDK-8 侧共通：委托顺序实证（RecordingLoader 日志）、loadClass 矩阵与缓存同一性、数组描述符 CNFE、defineClass 禁止包名 SecurityException（具名与 null 推断名）、资源 CAFEBABE/空枚举/前导斜杠、子 loader 包命名空间隔离、definePackage getter/sealed/bare 矩阵、setSigners 防御拷贝、getClassLoadingLock 分锁与回退、断言状态 desiredAssertionStatus 闭环。
- 27 专属：platform loader 链——system name "app"（jdk.internal.loader.ClassLoaders$AppClassLoader）、parent==getPlatformClassLoader()（name "platform"）、platform.getParent()==null；getUnnamedModule 每 loader 独立且 unnamed；getDefinedPackage(s) 只报本 loader 定义的包（java.lang 对 app loader 为 null，definePackage 后可见）；resources() Stream 缺失→空流；protected (String,ClassLoader) 构造器命名 loader。
- defineClass(null 推断名) 消息差异：27 为 "Class loader (instance of): ...@hash tried to load prohibited package name: java.lang"（含不确定哈希）——只断类型+包含 "prohibited package name: java.lang"；具名路径两侧消息完全一致可精确锁定。

### 2. 使用方式
同 8 侧；27 专属 API 直接编译。

### 3. 参考来源
Oracle javadoc（JEP 261 平台加载器、BuiltinClassLoader、getDefinedPackage 语义）；双侧实测。

### 4. 测试用例设计 + 易错规范细节
- **易错 1**：getPackage/getPackages/findLoadedClass 在 27 仍是 protected（从未转 public）——同 8 侧处理。
- **易错 2**：definePackage/getPackage 在 27 弃用（编译警告无害，@SuppressWarnings 抑制）。
- **易错 3**：27 的 defineClass(null名) 异常消息带 loader 身份哈希，不可逐字断言。
- **易错 4**：getDefinedPackage("java.lang") 在 app loader 上为 null（bootstrap 拥有）≠ getPackage（沿双亲找得到）。
- 覆盖：testPlatformLoaderChain/testLoadClassDelegation/testDefineClassProhibited/testResourceDetails/testPackageNamespaces/testFindSystemAndProtected/testAssertionStatusReflection。
