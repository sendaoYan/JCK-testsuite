# Package — 设计方案

## 1. API 功能
`java.lang.Package`（final，实现 `AnnotatedElement`，继承 `NamedPackage`）表示包的元数据：
- 名称：`getName`；查询：静态 `getPackage(String)`、`getPackages()`
- 密封：`isSealed()/isSealed(URL)`（java.lang 经 jimage 加载，视为 sealed）；兼容：`isCompatibleWith(String)`
- 清单属性：`getSpecification*/getImplementation*`（Title/Version/Vendor）
- 注解：`getAnnotation/getAnnotations/...`

## 2. Java 用户如何使用
- `SomeClass.class.getPackage().getName()` 获取包名；`Package.getPackage(name)` 按名查找。
```java
Package p = String.class.getPackage();
String version = p.getImplementationVersion();
```

## 3. 开源代码实际用法调研
- 框架读取 `Package.getImplementationVersion()` 显示依赖库版本。
- JDK `test/jdk/java/lang/Package` 系列验证 `getName`、密封、版本兼容。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Package")` 验证 final、实现 `AnnotatedElement`。
黑盒：`java.lang` 包名、静态查找、`getPackages()` 非空；未密封；注解为空；清单属性为 null；`toString/hashCode`。
对应 `<Simple>Test.java` 黑盒部分。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 实例语义：同 8 侧（同包共享实例、值对象身份不跨枚举保证、hashCode==name hash、equals 同一性）。
- bootstrap 包属性（27 锁定，java.base 模块映像）：**六项 manifest getter 全部 null**（与 8 全非空相反）；toString "package java.lang" 精确（无属性后缀）；**isSealed()==true**（模块映像视为密封，与 8 classpath false 相反）。
- isCompatibleWith（null specVersion）：任意版本串（含 null）一律 NumberFormatException；"" 的消息为 "Empty version string" 精确锁定（与 8 的 'For input string: ""' 不同）；null 抛 NFE 而非 NPE（与 8 的 NPE 不同）。
- definePackage 契约闭环：与 8 完全一致的行为（getter 回读/toString 拼接/hashCode/密封矩阵/NFE/重复 IAE/命名空间隔离）——definePackage 虽弃用仍功能完整；getDefinedPackage 反映定义。
- 静态查找：getPackage("java.lang") 非 null（named module 下 bootstrap 包仍可查）、缺失 null、getPackage(null) NPE、getPackages 含 java.lang/java.util。

### 2. 使用方式
静态嵌套 PkgLoader；弃用 API @SuppressWarnings。

### 3. 参考来源
Oracle javadoc（JDK-9+ 模块化后 Package 语义、NamedPackage）；双侧实测。

### 4. 测试用例设计 + 易错规范细节
- **易错 1**：27 上 bootstrap 包无 manifest 属性 → isCompatibleWith 一律 NFE（8 是正常比较）——两侧各自锁定，不可共用断言。
- **易错 2**：isSealed 方向相反（8 false / 27 true）；toString 形式不同（8 带属性尾巴 / 27 裸名）。
- **易错 3**：Package.getPackage 在 9+ 弃用但功能正常。
- **易错 4**：Package 未实现 Comparable、无 versionAtLeast（javap 双侧确认，brief 中猜测项证伪）。
- 覆盖：testPackageIdentity/testBootstrapAttributes/testCompatibleNfe/testDefinedPackageContract。
