# java.lang.Package 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「getPackage 读个名字」的冒烟。重点补：白盒结构（非 final、
> 实现 AnnotatedElement、无公开构造器）、静态查找 getPackage/getPackages、classpath 下 java.lang
> 不密封、rt.jar 清单属性非空、注解查询方法全空、isCompatibleWith 版本比较 + 畸形版本抛
> NumberFormatException。断言确定，headless 安全，不依赖具体构建商实现版本。

## 1. API 提供了哪些功能

- **类**：`java.lang.Package`（JDK-8 非 final，实现 `AnnotatedElement`，无公开构造器）。
- **名称/查找**：`getName()`、静态 `getPackage(String)`、`getPackages()`。
- **密封**：`isSealed()`、`isSealed(URL)`。
- **兼容**：`isCompatibleWith(String)`（版本比较，畸形版本抛 `NumberFormatException`）。
- **清单属性**：`getSpecificationTitle/Version/Vendor`、`getImplementationTitle/Version/Vendor`。
- **注解**：`getAnnotation/getAnnotations/getDeclaredAnnotation/getDeclaredAnnotations/...`。
- **对象**：`toString`/`hashCode`。

## 2. Java 用户如何使用

- `SomeClass.class.getPackage().getName()` 获取包名；`Package.getPackage(name)` 按名查找。
- 框架读取 `Package.getImplementationVersion()` 显示依赖库版本。
- 注解查询用于读取 `package-info.java` 上的包级注解。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Package.html`）：静态查找、isSealed/isCompatibleWith、
  清单属性、AnnotatedElement 注解查询。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Package.java`。
- **实测 JDK-8 运行时**：classpath 加载的 `java.lang` 包 `isSealed() == false`，清单属性非空
  （specTitle/specVersion/specVendor/implTitle 稳定，implVersion/implVendor 随构建商变化）。

## 4. 测试用例设计（映射到 PackageTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：非 final、非接口、实现 AnnotatedElement、无公开构造器 | testWhiteBox |
| P0 | getName / Class.getPackage 稳定性 / 静态 getPackage / getPackages 非空 | testPackageLookup |
| P0 | isSealed=false、清单属性全非空、hashCode 稳定、toString | testAttributes |
| P0 | 注解查询全空（无 Deprecated 注解） | testAnnotations |
| P0 | isCompatibleWith 版本比较 + 畸形版本抛 NFE | testCompatibility |

## 关键规范细节（易错，已锁入断言）

- **JDK-8 classpath 下的 java.lang 包不密封**（`isSealed() == false`）；JDK-9+ jimage 布局才把
  java.lang 视为 sealed——该差异是版本差异，不是 bug。
- **JDK-8 rt.jar 清单属性非空**（`getSpecificationTitle()` 等非 null），与 JDK-9+ 模块系统下
  （清单属性为 null）不同。
- **`isCompatibleWith` 版本比较**：specVersion "1.8" 时 `isCompatibleWith("1.8")==true`、
  `("1.0")==true`、`("1.9")==false`；畸形版本字符串抛 `NumberFormatException`。
- **JDK-8 `Package` 非 final**（JDK-9+ 才 final），且无公开构造器。

## 注意事项

- 不断言 `getImplementationVersion()/getImplementationVendor()` 的精确值（随构建商变化），仅断言非 null。
- headless 安全，不加 `@key`；结果确定。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 实例语义：同包类共享 Package 实例（String/Integer/Object/Runnable/新加载 StringBuilder 同一实例）；**实测锁定：Package 为值对象——静态 Package.getPackage 在发生过 getPackages() 枚举后可能返回内容相同的新实例，跨 API 只断内容相等**；hashCode==name.hashCode()（规范）；equals 为 Object 同一性（未覆写）。
- manifest 属性（JDK-8 锁定，temurin 8u372 rt.jar）：specTitle "Java Platform API Specification"、specVer "1.8"、specVendor "Oracle Corporation"、implTitle "Java Runtime Environment"、implVer/implVendor 非空；toString "package java.lang, Java Platform API Specification, version 1.8" 精确；classpath 加载 isSealed()==false。
- isCompatibleWith 版本比较矩阵（1.8 为基）：1.8/1.7/1/1.0/0.9/1.8.0 兼容；1.9/1.10（数值比较非字典序）/2 不兼容；"" → NumberFormatException 'For input string: ""' 精确；null → NPE；非数字 → NFE。
- definePackage 契约闭环（不受 manifest 影响的确定性路径）：全 getter 回读、toString "package <n>, <specTitle>, version <specVer>" 精确、hashCode==name hash、null sealBase → 不密封、URL sealBase → isSealed、null specVersion isCompatibleWith → NFE、重复定义 → IllegalArgumentException、子 loader 包对 system loader 不可见（命名空间隔离）。
- 静态查找：getPackage 缺失 → null、getPackage(null) → NPE、getPackages 包含 java.lang/java.util。

### 2. 使用方式
静态嵌套 PkgLoader fixture（definePackage 全参暴露）。

### 3. 参考来源
Oracle javadoc（Package.toString 拼接规则、isCompatibleWith 数值比较、hashCode=name hash）；JDK-8 源码（Manifest 版本解析、NumberFormatException 消息）；双侧实测。

### 4. 测试用例设计 + 易错规范细节
- **易错 1**：Package 实例身份不跨枚举稳定（值对象）——断言 toString/hashCode/equals 内容而非 ==（同 loader 同时刻的 Class.getPackage 间仍可断 ==）。
- **易错 2**：isCompatibleWith 逐段数值比较："1.10" > "1.8"；尾随段缺失视为更小（1.8.0 >= 1.8）。
- **易错 3**：空串版本 NFE 消息走 Integer.parseInt 路径（'For input string: ""'），与 27 的 "Empty version string" 不同。
- **易错 4**：manifest 属性值与构建厂商相关——只锁 spec 三项（规范固定），implVendor/implVersion 断非空。
- 覆盖：testPackageIdentity/testManifestAttributesExact/testDefinedPackageContract。
