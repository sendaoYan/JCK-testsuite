# java.lang.module.ModuleDescriptor — 设计方案

## 1. API 功能
模块描述符（final、只读、无 public 构造器）：
- 工厂：`newModule(name[, modifiers])`、`newOpenModule(name)`、`newAutomaticModule(name)`、`read(InputStream/ByteBuffer[, packageFinder])`
- 查询：`name/isOpen/isAutomatic/requires/exports/opens/uses/provides/version/rawVersion/toNameAndVersion/mainClass/packages/modifiers/accessFlags`
- 比较：`compareTo`（按名）、`equals/hashCode`（按名）

## 2. Java 用户如何使用
```java
ModuleDescriptor d = ModuleDescriptor.newModule("com.app")
        .requires("java.sql").exports("com.app.api")
        .provides("com.app.Spi", List.of("com.app.Impl")).build();
d.name(); d.requires(); ... // 读回全部
ModuleDescriptor info = Module.getDescriptor(); // 运行时模块
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleDescriptor.java`：Builder 校验（automatic 模块禁 requires/exports/opens）与隐式 java.base MANDATED require。
- JDK `test/jdk/java/lang/module/ModuleDescriptorTest.java`：Builder 全 API + read 回读 + 异常路径。
- jlink/jdeps、OSGi Equinox 的模块适配层都消费 ModuleDescriptor API。

## 4. 测试用例设计 + 自动化实现
白盒：final、Comparable、无 public 构造器、4 个静态工厂存在。
黑盒：
- `read(InputStream)` 读 java.base 真实 module-info（requires 为空、导出 java.lang、packages、无 mainClass）；
- 全功能 Builder 描述符逐 getter 回读（qualified exports/opens targets、STATIC requires、uses、provides 顺序、version/mainClass/toNameAndVersion）；
- 隐式 java.base MANDATED require；open/automatic 变体（automatic 声明 requires 抛 ISE）；
- `version(String)` 重载；equals/hashCode/compareTo 按名；`read(ByteBuffer)` 与 InputStream 等价；null 名 NPE。
对应 `ModuleDescriptorTest.java` 的 `testBlackBox()`。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 真模块解析闭环：`Module.getResourceAsStream("module-info.class")` → `ModuleDescriptor.read` → name/requires 空/exports java.lang（非限定）/packages 197+/version 存在；两次 read 的 descriptor equals/hashCode/compareTo 全一致，且与 `Module.getDescriptor()` 相等。
- Builder 全字段 round-trip：name/requires（显式 java.base 覆盖隐式 MANDATED）/exports/opens/uses/provides（provider 列表保序）/packages/mainClass/version(String↔Version)。
- 值类型契约：Requires/Exports/Opens/Provides 的 equals/hashCode/compareTo/toString（`synthetic ex.p to [client]`、`pr.S with [A, B]`）；copy-in 重载 `exports(Exports)` 等按身份复用对象。
- Version：`parse("11.0.1")` 往返、严格序 1.0<1.0.1<1.1<2.0、`"1.0-ea"<"1.0"`、尾零忽略（1.0==1.0.0）、数值比较（10>9）；`parse("x")` → IAE "x: Version string does not start with a number"、`parse(null)` → IAE "Null version string"。
- 负例矩阵：模块/包/服务/主类名非法 → IAE 精确消息（`bad..name: Invalid module name: '' is not a Java identifier` 等）；open/automatic 模块 `opens()` → ISE、automatic `exports()` → ISE、automatic `requires()` → ISE；`build()` 两次合法（相等孪生）。
- 坏字节解析：坏版本号 → InvalidModuleDescriptorException "Unsupported major.minor version 0.3"；空输入/空 ByteBuffer → "Truncated module-info.class"；非 module-info 类文件 → "access_flags should be ACC_MODULE"；`read(null)` → NPE。
- 不可变矩阵：requires/exports/opens/uses/provides/packages/modifiers 及各值类型内部集合 add → UOE。

### 2. 使用方式
- 白盒加 12 个 testXxx；全部用自造 descriptor 与真 java.base module-info，不修改任何真模块。

### 3. 参考来源
- Oracle javadoc ModuleDescriptor（Version 语法、Checks 的名字校验消息）；OpenJDK ModuleDescriptor 测试思路（自写）。

### 4. 测试用例设计 + 易错规范细节
- 断言数 52→200。
- 易错点：
  - 模块名分段不得是 Java 关键字（`m.trans.static` → IAE，'static' is not a Java identifier）；
  - 多修饰符 Requires.toString 的词序来自 Set 迭代序（不确定）——只断言包含两词 + 以名结尾；
  - `ModuleDescriptor.equals` 是内容敏感（同名不同 exports 不等），但 `compareTo` 先按名（同名不同内容仍非 0——实测内容也参与）；
  - java.base 的 version 来自运行时镜像（`toNameAndVersion()=="java.base@27.0.1.0.2"`），只断言 startsWith("java.base@")；
  - `packages()` 派生自 exports+opens 源包并集，mainClass 贡献自己的包。
