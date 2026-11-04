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

## 深化批次（2026-11-05）
新增 Part 3 四个 test 方法（全部字面值经探针在 JDK 27.0.1 实测锁定）：
1. `testExactMajorTable`：28 行精确 major 硬表 {45,45,46,…,71}；**不变量 RELEASE_n.name()=="RELEASE_"+n、ordinal()==n、n≥1 时 major()==44+n**（RELEASE_0 是唯一例外，45≠44+0）；toString 与 name 全等。
2. `testRuntimeVersionNulls`：**RELEASE_0..RELEASE_5 的 runtimeVersion()==null**（1.x 版本号 scheme 无对应 Runtime.Version），RELEASE_6 起 feature==n（6..27 全查）；RELEASE_6.runtimeVersion().toString()=="6"。
3. `testFromMajorBoundaries`：44/0/72/-1/Integer.MAX_VALUE 全部 IAE 且消息含 "Out of range major class file version"；下界 45→RELEASE_1、上界 71→RELEASE_27。
4. `testValueOfRuntimeVersionMap`：parse("5")/("6")/("26")/("27.1")/("27+42")/("27-pre")→对应 RELEASE_n（update/build/pre 均忽略）；parse("28")→IAE 消息含 "RELEASE_28"。
产出：`ClassFileFormatVersionTest.java` 361 断言通过（旧 176）。

### 易错规范细节
- JDK-27 无 minor() 方法（javap 实证只有 major/latest/runtimeVersion/fromMajor/valueOf），早期 minor 版本差异不可经此 API 观测。
- Runtime.Version.parse("8u40") 旧格式在 JDK-27 已不支持（直接 IAE），跨版本用例只能用纯数字 feature 串。
- fromMajor(45) 语义是"别名到 RELEASE_1 而非 RELEASE_0"——RELEASE_0 不可从 major 反解，只能 valueOf("RELEASE_0") 到达。
