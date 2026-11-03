# java.lang.reflect.ClassFileFormatVersion — 测试设计（JDK-27）

## 1. API 功能
`ClassFileFormatVersion` 枚举（JDK-19+）把 class 文件 major version 语义化：
- 常量：`RELEASE_0` … `RELEASE_27`，JDK-27 共 **28 个**；`RELEASE_0` 与 `RELEASE_1` 共用 major **45**（1.0/1.1 同版），`RELEASE_8`=52，`RELEASE_27`=**71**。
- `latest()`：运行时 JDK 对应版本（RELEASE_27）。
- `major()`：JVMS 4.1 major 号。
- `fromMajor(int)`：major → 枚举；45 别名到 RELEASE_1；未知/负数抛 IAE。
- `runtimeVersion()` / `valueOf(Runtime.Version)`：与 `Runtime.Version` 互转；注意 `runtimeVersion()` 只含 feature（"27"），与完整 `Runtime.version()`（27.0.1.0.2+1）`equals` 为 false，须比对 feature。

## 2. Java 用户如何使用
字节码工具与库在做版本协商时读取：例如判断某类是否需要更高运行时（`ClassFileFormatVersion.fromMajor(bytesMajor).compareTo(latest()) > 0` → UnsupportedClassVersionError 路径）、`AccessFlag` 的版本化位置查询（STRICT 只在 RELEASE_8 前的 METHOD 合法）。

## 3. 开源代码实际用法调研
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/ClassFileFormatVersion.java`（含 fromMajor 的 RELEASE_0/1 合并逻辑与 IAE 分支）。
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/ClassFileFormatVersion/`（BasicTest：latest/major/fromMajor 边界）。
- `java.lang.classfile.ClassFile.latest()` / `ClassFileVersion.of(...)` 以本枚举为参数（场景 1 同构）。
- ASM `Opcodes.V_*` 与本枚举 major 的映射表在 Gradle/ByteBuddy 版本协商代码中出现（场景 2）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ClassFileFormatVersionTest.testBlackBox`）：
1. JDK-27 精确常量：28 个、latest==RELEASE_27、major==71、首 RELEASE_0(45)、RELEASE_1(45)。
2. name/valueOf 往返（28 个逐一）、RELEASE_999 → IAE。
3. fromMajor：除 RELEASE_0 外全部恒等映射（RELEASE_0 的 major 45 别名 RELEASE_1）、52/71、-1 与 999 → IAE。
4. runtimeVersion 链路：RELEASE_8→feature 8、RELEASE_27→27、valueOf(Runtime.Version.parse("27.1"))→RELEASE_27、parse("8")→RELEASE_8、latest.runtimeVersion() 仅 feature、compareTo 关系。
5. Comparable 全序：ordinal 递增、RELEASE_0→RELEASE_1 major 不变、其余 stride 1、antisymmetric。
6. 解码真实 class 文件：直接读字节流（CAFEBABE magic + 偏移 4-7）取 java.base String 与自身 class 的 major=71，fromMajor 还原为 RELEASE_27。
7. 枚举通用机制：getDeclaringClass、EnumSet.range 跨度。
白盒保留原有 7 项断言。产出：`ClassFileFormatVersionTest.java`（176 断言通过）。
