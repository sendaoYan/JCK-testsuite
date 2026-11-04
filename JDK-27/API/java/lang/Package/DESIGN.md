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
