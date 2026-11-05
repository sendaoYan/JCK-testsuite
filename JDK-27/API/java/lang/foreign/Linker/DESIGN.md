# Linker — 设计方案

## 1. API 功能
`java.lang.foreign.Linker`（接口）提供本地下行/上行调用。工厂 `nativeLinker()`；`downcallHandle`（两个重载）、`upcallStub`、`defaultLookup()`、`canonicalLayouts()`。

## 2. Java 用户如何使用
```java
Linker linker = Linker.nativeLinker();
MethodHandle mh = linker.downcallHandle(symbol, FunctionDescriptor.of(ValueLayout.JAVA_INT));
Map<String,MemoryLayout> canon = linker.canonicalLayouts();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestDowncall`/`TestUpcall` 是 `nativeLinker()` 的权威用法。
- jextract 用 `canonicalLayouts()` 查 C 类型对应布局；Netty ffm 分支用 `downcallHandle` 封装 native。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 各方法返回类型（MethodHandle/MemorySegment/SymbolLookup/Map）。
黑盒：`nativeLinker()` 非空；`canonicalLayouts()` 含 int/long/byte/double 等 primitive 布局且非空；`defaultLookup()` 非空且找不到不存在符号（不真调 native）。
对应 `LinkerTest.java` 白盒 + 黑盒部分。

## 5. 真实 downcall/upcall 行为深化（LinkerNativeCallTest / LinkerUpcallTest / LinkerNativeAccessWarningTest）

### 5.1 实证结论（jdk-27.0.1.0.2+1, Linux x64, glibc, gcc 10.5）
- **jtreg 头形式**：jtreg 8.3 直接接受 `@run main/othervm --enable-native-access=ALL-UNNAMED <Test>`（`--` 前缀 VM 选项直传，jtr 中记录为 `run=USER_SPECIFIED main/othervm --enable-native-access\=ALL-UNNAMED`），已实测。
- **符号查找可行形式**：`Linker.defaultLookup()` 在本平台**直接解析 libc 函数**（strlen/strcmp/strtod/labs/llabs/qsort/sprintf/memset/strchr 全部命中）——这恰是 Linker javadoc 的权威示例路径（"the default lookup is used to search the address of the strlen native function"）。`SymbolLookup.libraryLookup("libc", arena)` 抛 IAE（需精确 soname），`libraryLookup("libc.so.6", arena)` 可用。测试以 defaultLookup 为主、`libc.so.6`/`libc.so`/`libc.dylib` 链为兜底。
- **JDK-27 无 `Linker.Option.isTrivial()`**（JDK 22 废弃后移除），对应物是 `critical(boolean)`；方法名是 `firstVariadicArg(int)`（非 firstVariadicArgument）；`FunctionDescriptor` 无 `withCallConv`（javap 实证）。
- **captureCallState 参数位置**：捕获段"directly following the target address"（javadoc 原文）——bound 形式无地址参数 ⇒ 捕获段成为**第一个**参数，type 为 `(MemorySegment,int)void`；unbound 形式为 `(addr,state,int)void`。Linux x64 合法捕获名仅 `errno`，未知名在**构造 Option 时**即抛 IAE（不是链接时）。
- **firstVariadicArg 校验时机**：`0 <= index <= N` 在 **downcallHandle 链接时**校验，越界（-1、N+1）抛 IAE；index==N 与 0 均可链接。
- **qsort 比较器调用次数**：n=16 随机输入实测 44 次（glibc 2.35）；跨 libc 不稳定 ⇒ 只断言 `>= n-1`（任何正确比较排序的普适下界）与排序结果精确相等；n=0/n=1 实测 0 次调用。
- **upcall 参数段形状**：回调收到的裸指针段 byteSize==0，直接 `get(JAVA_INT,0)` 会 IndexOutOfBounds；必须用 `ValueLayout.ADDRESS.withTargetLayout(JAVA_INT)` 声明描述符，Java 侧才能解引用。
- **NULL 目标地址**：`downcallHandle(fd)` + `MemorySegment.NULL` 地址 ⇒ 干净的 `IllegalArgumentException("Symbol is NULL")`，不会跳 0 崩溃。
- **堆段**：`MemorySegment.ofArray(...)` 作为 ADDRESS 实参 ⇒ `IllegalArgumentException("Heap segment not allowed")`。
- **upcall 中抛 Java 异常**（实测）：异常沿 C 栈回传时走 "Unrecoverable uncaught exception" 路径**直接终止 VM，不可 catch**——这是 FFM 规定（upcall 不得逃逸异常），因此不做 jtreg 断言，仅记录。
- **--enable-native-access 警告（JEP 472）**：无 flag 时 `System.load` 与 `Linker::downcallHandle` 各自向 stderr 打 4 行 WARNING 块（点名受限方法与 `--enable-native-access`）；带 flag 时 stderr 干净。LinkerNativeAccessWarningTest 用 ProcessBuilder 驱动子 JVM 断言两侧（单文件源码启动器 `java Child.java` 运行子进程）。

### 5.2 新增文件
- `LinkerNativeCallTest.java`（57 断言）：strlen/strcmp(符号语义)/abs/labs/llabs/strtod(含 endptr 回写偏移)/strchr(非空+偏移+NULL)/memset(混合 carrier+返回同一段)/sprintf(真变参 firstVariadicArg) 精确返回值；unbound 形式 type 断言与调用；13-carrier 矩阵（自编译 C 库，byte/short/int/long/float/double/boolean/char 全 carrier + 极值组合，double 精确值）；errno 捕获（7777 回读、二次更新、bound/unbound type、未知名 IAE）；firstVariadicArg 链接时校验；critical(true)/critical(false)/plain 三路同值；NULL 地址 IAE、堆段 IAE。
- `LinkerUpcallTest.java`（18 断言）：qsort 升序/降序精确排序结果 + 调用次数下界；比较器经 targetLayout 解引用并记录观察值集合（并集==输入集）；n=0/1 边界零调用；`jck_apply_and_count`（C 循环回调累计 acc=57、calls=5 经 out 参数回读）；同一 stub 复用于两次调用；arena close 后 stub ⇒ IllegalStateException；upcallStub 类型不匹配（含 void handle + 非 void 描述符）⇒ IllegalArgumentException。
- `LinkerNativeAccessWarningTest.java`（15 断言）：子 JVM 四象限（load/ffm × 有/无 flag）stderr 警告存在性 + stdout 行为值。

### 5.3 C 库说明
- 每个测试内嵌自写 C 源（无 jni.h 依赖），运行时写入 jtreg scratch（`user.dir`），`gcc -shared -fPIC` 编译（有界等待 60s），`System.load` + `loaderLookup()` 取符号；`.c/.so` 只落 scratch，绝不入库。
- 函数：`jck_carrier_sum`（13 carrier 求和）、`jck_is_null`、`jck_set_errno`、`jck_read_long`/`jck_write_long`、`jck_sum2`、`jck_apply_and_count`（回调驱动）。

## 5. 契约深化（JDK-27 实测锁定）
- **nativeLinker() 同一实例**（两次 ==）；canonicalLayouts 不可修改（put → UOE）、含 size_t/long long/wchar_t、≥20 项、size_t 指针宽。
- **Option 面（27 实测）**：`critical(boolean)`（isTrivial 已更名）、firstVariadicArg、captureCallState、captureStateLayout() 非空 StructLayout。
- **strlen downcall 真闭环（glibc）**：defaultLookup().find("strlen") present → `downcallHandle(addr, fd(long,ADDRESS))` → invoke(allocateFrom("hello"))==5、""==0；critical(true) 变体同链路 "==4"。
- **未绑定形态**：downcallHandle(fd) 的 MethodHandle 首参是 MemorySegment（调用目标地址在前）。
- **NPE 矩阵**：downcallHandle(null,fd)、downcallHandle(fd,null opts)、upcallStub(null/fd null)。
- **upcall 生命周期**：stub 是零长 native 段、arena 存活期 alive；close 后 scope 死、地址仍可读；死 stub 传入 downcall → ISE（LinkerUpcallTest C 环节覆盖）。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
