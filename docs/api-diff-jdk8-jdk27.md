# JDK-8 vs JDK-27 API 表面差异清单（japicmp 0.26.5）

> 用途：差分测试（第 7 节）与补测的**权威输入**——哪些类/方法在 27 里新增、哪些在 JDK-8 有而 27 删除/变更。
> 生成方式：`japicmp 0.26.5`，`--old` = JDK-8 `rt.jar`，`--new` = JDK-27 `jimage extract` 后打包的扁平 class jar，`--only-modified --ignore-missing-classes --no-annotations --reproducible-report`。
> 生成日期：2026-11-04。

---

## 1. 精确统计

### 1.1 全量（含内部包 `sun.*`/`com.sun.*`/`jdk.*`/`org.omg` 等）

| 维度 | 新增 (NEW) | 删除 (REMOVED) | 变更 (MODIFIED) | 其中签名变更* |
|---|---|---|---|---|
| 类 | 7856 | 4201 | 1740 | — |
| 方法（不含构造器） | 41609 | 28318 | 801 | 144 |
| 构造器 | 6329 | 4135 | 209 | — |
| 字段 | 14595 | 10276 | 134 | — |

> \*「签名变更」= japicmp 显式标记为返回类型/参数类型改变、或 `now static/final`/可见性改变的方法子集；`MODIFIED` 口径更宽，还包含泛型/注解/抛异常清单等非破坏性变化。

### 1.2 稳定导出 API（`java.*`/`javax.*`/`org.w3c`/`org.xml`/`org.ietf`）

| 维度 | 新增 | 删除 | 变更 | 签名变更 |
|---|---|---|---|---|
| 类 | 624 | 276 | 620 | — |
| 方法 | 4611 | 1423 | 113 | 4 |
| 构造器 | 288 | 234 | 110 | — |
| 字段 | 1563 | 144 | 11 | — |

### 1.3 重点关注包（`java.lang`/`java.util`/`java.math`/`java.text`/`java.io`/`java.nio`）

| 维度 | 新增 | 删除 | 变更 | 签名变更 |
|---|---|---|---|---|
| 类 | 375（其中 220 个是全新的 `java.lang.classfile` 子包） | 4 | 244 | — |
| 方法 | 3288 | 66 | 74 | 2 |
| 构造器 | 90 | 0 | 11 | — |
| 字段 | 925（绝大多数是 `Character$UnicodeBlock/UnicodeScript` 新常量与方向性隔离符常量） | 25 | 2 | — |

---

## 2. 结论速览：哪些值得差分与补测

- **27 才有的新 API（补测重点）**：`java.lang`（Record/Sealed/Module/StackWalker/ProcessHandle/ScopedValue/虚拟线程 Thread.Builder、String/StringBuilder 大批方法）、`java.util`（List/Map/Set/`of()`/`copyOf()`、SequencedCollection 系、Optional 增强）、`java.math`（BigInteger.sqrt/rootn、BigDecimal.sqrt/rootn）、`java.io`（readAllBytes/readNBytes/transferTo/null*Stream/readString）、`java.nio`（Buffer 覆写返回子类、slice(int,int)、Path.of、Files.readString/writeString/mismatch、Selector select(Consumer)）、`java.lang.classfile`（全新 220 类）、`java.lang.foreign`、`java.net.http` 等。
- **JDK-8 有而 27 删除（差分测试应标为「JDK-8-only」）**：Java EE 全家桶（JAXB/JAX-WS/SAAJ/JAF/CORBA/JTA/JWS/JAF/JAX-RPC 相关）、`java.security.acl`、`java.rmi.activation`、`java.applet`、`javax.swing.JApplet`、`java.lang.Compiler`、`java.util.jar.Pack200*`、`Thread.stop/suspend/resume/destroy`、`SecurityManager` 旧方法、`finalize()`（FileInputStream/FileOutputStream）等。
- **签名/语义变更（差分重点，二进制兼容但源码/行为可能变）**：`java.nio.Buffer` 系方法返回类型由 `Buffer` 收窄为各子类（协变）；`java.util.SplittableRandom` 的 `nextInt/nextLong/nextDouble` 移入 `RandomGenerator` 默认方法；`Class.getDeclaredXxx` 等反射方法的泛型返回类型；`Optional.orElseThrow()` 新增无参重载。

---

## 3. 重点包差异分组

### 3.1 删除的类（JDK-8 有、27 无，276 个稳定类）

按包族归并（这些类在 27 里**整体消失**，JDK-8-only 测试无法在 27 编译/运行）：

| 包族 | 数量 | 说明 |
|---|---|---|
| `javax.xml.bind*` | 90 | JAXB（JDK-11 移出，转 Jakarta EE / 独立 artifact） |
| `javax.xml.ws*` | 59 | JAX-WS（含 handler/spi/wsaddressing） |
| `javax.xml.soap` | 27 | SAAJ |
| `javax.activation` | 17 | JAF |
| `java.rmi.activation` | 16 | RMI 激活框架 |
| `javax.jws*` | 14 | Web Services 元数据注解 |
| `javax.rmi.CORBA` | 9 | RMI-IIOP CORBA |
| `java.security.acl` | 8 | ACL 安全接口 |
| `javax.annotation` | 6 | `@Generated/@PostConstruct/@PreDestroy/@Resource` 等 |
| `java.applet` | 5 | Applet |
| `javax.management.loading` | 4 | `MLet` 系 |
| `javax.management.remote.rmi` | 5 | IIOP/Tie/Stub 遗留 |
| `javax.activity` | 3 | CORBA Activity |
| `javax.transaction` | 3 | JTA 异常 |
| `javax.swing` | 2 | `JApplet` |
| `java.util.jar` | 3 | `Pack200`/`Packer`/`Unpacker` |
| 其它（`java.lang.Compiler`、`java.beans.AppletInitializer`、`javax.rmi.PortableRemoteObject`、`javax.security.auth.Policy`、`javax.management.modelmbean.XMLParseException`） | 5 | 单点移除 |

> 对补测而言：这些类在 JDK-27 不存在，无需在 JDK-27 侧补测；若项目目标是「同一套测试双 JDK 跑」，则这些类只能进 `JDK-8/API/...` 而不能进 `JDK-27/API/...`。

### 3.2 新增的类（27-only，624 个稳定类）

按包族归并（前 20）：

| 包族 | 数量 | 关键新 API |
|---|---|---|
| `java.lang.classfile*` | 220 | 类文件 API（ClassFile/ClassModel/CodeBuilder/constantpool 等） |
| `javax.crypto*` | 63 | KEM、新算法 Spec/ParameterSpec 等 |
| `java.lang.foreign` | 25 | FFM API（MemorySegment/Arena/FunctionDescriptor 等） |
| `java.awt.desktop` | 30 | Desktop 事件/能力封装 |
| `java.lang` | 29 | Record、Module、ModuleLayer、ScopedValue、StackWalker、ProcessHandle、Thread$Builder、System$Logger、MatchException、WrongThreadException、LazyConstant、IO、IllegalCallerException 等 |
| `java.net.http` | 28 | HTTP Client |
| `java.lang.classfile.constantpool` | 26 | 常量池模型 |
| `java.lang.module` | 19 | ModuleFinder/ModuleReference/Resolver 等 |
| `java.util.concurrent` | 14 | StructuredTaskScope、CompletableFuture 增强、Flow 等 |
| `java.lang.constant` | 12 | ConstantDesc/DynamicConstantDesc 等 |
| `javax.imageio.plugins.tiff` | 12 | TIFF 插件 |
| `java.security` | 11 | KeyPairGenerator 新算法等 |
| `java.lang.invoke` | 10 | MethodHandle/ConstantBootstraps 等 |
| `javax.xml.catalog` | 9 | XML Catalog |
| `java.util.stream` | 8 | Gatherers、Stream 增强 |
| `java.security.spec` | 7 | NamedParameterSpec、新 EC 参数 |
| `java.util.random` | 7 | RandomGenerator 接口族 |
| `java.security.interfaces` | 6 | EdEC/XEC 密钥接口 |
| `java.util` | 6 | SequencedCollection/Map/Set、HexFormat、RandomGeneratorFactory |
| `java.io` | 5 | Serial、ObjectInputFilter 等 |

### 3.3 方法级变更（在已有类上，差分/补测重点）

#### java.lang（非 `classfile` 子包）

- **String（27-only 新增）**：`strip/stripLeading/stripTrailing/stripIndent`、`isBlank`、`lines`、`repeat`、`indent`、`formatted`、`translateEscapes`、`indexOf(int,int,int)`/`indexOf(String,int,int)`、`splitWithDelimiters`、`compareToFoldCase/equalsFoldCase`、`encodedLength`、`transform`、`describeConstable/resolveConstantDesc`、`chars/codePoints`。
- **Math/StrictMath（27-only 新增）**：`floorDiv/floorMod/ceilDiv/ceilMod`、`*Exact` 溢出抛出族（`absExact/ceilDivExact/floorDivExact/divideExact/negateExact/incrementExact/decrementExact/powExact/unsignedMultiplyExact`）、`multiplyHigh/unsignedMultiplyHigh/multiplyFull`、`fma`、`clamp`、`acosh/asinh/atanh`。
- **Integer/Long/Short/Byte**：`compress/expand`（位压缩/展开）、`compareUnsigned`、`parseInt(CharSequence,int,int,int)` 等带范围重载、`describeConstable/resolveConstantDesc`。
- **Float**：`float16ToFloat/floatToFloat16`（半精度）。
- **Thread（27-only 新增）**：`ofVirtual/ofPlatform`、`startVirtualThread`、`threadId`、`isVirtual`、`join/sleep(Duration)`、`onSpinWait`。**删除**：`stop()/stop(Throwable)/suspend()/resume()/destroy()/countStackFrames()`。
- **SecurityManager（删除）**：`checkTopLevelWindow`、`checkSystemClipboardAccess`、`checkAwtEventQueueAccess`、`checkMemberAccess`、`classDepth/classLoaderDepth/currentClassLoader/currentLoadedClass/getInCheck/inClass/inClassLoader`；`getClassContext()` 返回类型泛型化。
- **Runtime（删除）**：`runFinalizersOnExit`、`traceInstructions`、`traceMethodCalls`、`getLocalizedInputStream/OutputStream`；新增 `version()`。
- **Process/ProcessBuilder（27-only 新增）**：`pid/toHandle/info/onExit/children/descendants/inputReader/outputWriter/errorReader/supportsNormalTermination/waitFor(Duration)`；`startPipeline`。
- **Class（27-only 新增）**：`getModule`、`getNestHost/getNestMembers/isNestmateOf`、`isRecord/getRecordComponents`、`isSealed/getPermittedSubclasses`、`isHidden`、`getPackageName`、`describeConstable/descriptorString`、`arrayType/componentType`、`forName(Module,String)/forPrimitiveName`、`accessFlags`；`getDeclaredXxx/getXxx` 反射方法返回类型泛型化（`Constructor[] -> Constructor<T>[]` 等）。
- **ClassLoader（27-only 新增）**：`getName`、`getPlatformClassLoader`、`getDefinedPackages/getDefinedPackage`、`getUnnamedModule`、`isRegisteredAsParallelCapable`、`findClass(String,String)`、`findResource(String,String)`、`resources`。
- **Enum/Boolean/Byte/Short/Integer/Long/Float/Double**：`describeConstable()`（Constable 接口落地）。
- **StackTraceElement**：新增 `getClassLoaderName/getModuleName/getModuleVersion` + 六参构造器。
- **Deprecated**：新增 `since()/forRemoval()` 元素。

#### java.util

- **工厂方法与不可变集合（27-only）**：`List.of(...)/copyOf/ofLazy`、`Set.of(...)/copyOf/ofLazy`、`Map.of(...)/ofEntries/copyOf/ofLazy/entry`、`Map.Entry.copyOf`。
- **Sequenced 集合（JDK-21，27-only）**：`SequencedCollection/Map/Set` 接口族；`List/Deque/SortedSet/NavigableSet/LinkedHashSet/LinkedList` 的 `getFirst/getLast/addFirst/addLast/removeFirst/removeLast/reversed`；`LinkedHashMap/SortedMap/NavigableMap/TreeMap` 的 `putFirst/putLast/reversed/sequenced*`。
- **Arrays（27-only 新增）**：`compare/mismatch/equals/compareUnsigned` 全套（含范围重载）。
- **Objects**：`checkIndex/checkFromToIndex/checkFromIndexSize`、`requireNonNullElse/Get`、`toIdentityString`。
- **Optional/OptionalInt/Long/Double**：`isEmpty/or/orElseThrow()/ifPresentOrElse/stream`。
- **Properties**：JDK-9 起覆写 `Hashtable` 的 `get/put/keySet/entrySet/forEach/merge/compute*/replaceAll/putIfAbsent` 等，使其语义一致（japicmp 记为「新增覆写」）；新增 `contains/containsKey/containsValue`、`storeToXML(...,Charset)`、`Properties(int)`。
- **Locale**：`of(...)`、`getISOCountries(IsoCountryCode)`、`availableLocales()/getISOCountries()` 返回 Stream；`caseFoldLanguageTag`；`getDisplayXxx()` 返回类型泛型微调。
- **Random/随机（27-only）**：`Random.from(RandomGenerator)`、`java.util.random.RandomGenerator` 接口族、`Collections.shuffle(List,RandomGenerator)`。
- **SplittableRandom（语义变更）**：`nextInt/nextLong/nextDouble` 等实例方法移入 `RandomGenerator` 默认方法，类内不再声明（japicmp 记为删除，二进制兼容）；新增 `split(s)/nextBytes`。
- **其它**：`Scanner.findAll/tokens` + Charset 构造器、`ServiceLoader.stream/findFirst/load(ModuleLayer,..)`、`UUID.ofEpochMillis`、`Currency.getNumericCodeAsString/availableCurrencies`、`TimeZone.availableIDs` 返回 Stream、`Deque.reversed`、`Collection.toArray(IntFunction)`、`Comparator.min/max`、`Enumeration.asIterator`、`EnumSet.spliterator`。

#### java.math

- **BigInteger（27-only 新增）**：`sqrt()/sqrtAndRemainder()`、`rootn(int)/rootnAndRemainder(int)`、`parallelMultiply(BigInteger)`；新增构造器 `(int,byte[],int,int)`、`(byte[],int,int)`。
- **BigDecimal（27-only 新增）**：`sqrt(MathContext)`、`rootn(int,MathContext)`。

#### java.text

- **NumberFormat（27-only 新增）**：`getCompactNumberInstance()/getCompactNumberInstance(Locale,Style)`、`isStrict()/setStrict(boolean)`；`DecimalFormat`/`ChoiceFormat` 同样新增 `isStrict/setStrict`。
- **DecimalFormatSymbols**：`getLocale()`、`getMonetaryGroupingSeparator()/setMonetaryGroupingSeparator(char)`。
- **NumberFormatProvider**：新增 `getCompactNumberInstance` SPI。
- `ChoiceFormat/DecimalFormat/MessageFormat/SimpleDateFormat` 新增 `toString()`（覆写）。

#### java.io

- **InputStream/OutputStream（27-only 新增）**：`readAllBytes/readNBytes/skipNBytes/transferTo/nullInputStream`、`writeBytes/nullOutputStream`。
- **Reader/Writer**：`nullReader/nullWriter`、`Reader.of(CharSequence)/readAllAsString/readAllLines/transferTo`。
- **文件流**：`FileReader/FileWriter/PrintStream/PrintWriter` 新增 Charset 构造器；`Console.charset/isTerminal/format/printf/readLine(Locale,..)`。
- **对象序列化**：`ObjectInputStream.setObjectInputFilter/getObjectInputFilter`、`ObjectInputFilter` 新接口族；`ObjectStreamException/InvalidClassException/InvalidObjectException` 新增 Throwable 构造器；`ObjectInputStream$GetField.get` 抛受检异常（语义变更）。
- **删除**：`FileInputStream.finalize()`、`FileOutputStream.finalize()`。

#### java.nio

- **Buffer（协变返回类型，签名变更重点）**：`clear/flip/limit/mark/position/reset/rewind` 返回类型由 `Buffer` 收窄为各子类；新增 `slice()/slice(int,int)/duplicate()`。
- **各 ByteBuffer/CharBuffer/IntBuffer 等**：新增 `mismatch`、`slice(int,int)`、绝对批量 `get/put`（带 index 的重载）、`alignedSlice/alignmentOffset`（ByteBuffer）。
- **Path（27-only 新增 + 抽象→默认方法）**：`of(String,String[])/of(URI)`、`resolve(...)` 批量重载；`endsWith/startsWith/iterator/resolve/resolveSibling/toFile/register` 由抽象方法改为 default（japicmp 记为 `METHOD_ABSTRACT_NOW_DEFAULT`）。
- **Files**：`readString/writeString/mismatch`。
- **FileChannel**：`map(...,Arena)`（FFM 重载）。
- **Selector**：`select(Consumer)/select(Consumer,long)/selectNow(Consumer)`；`SelectionKey.interestOpsAnd/interestOpsOr`。
- **Charset**：`forName(String,Charset)`；`Channels.newReader/newWriter`。

---

## 4. japicmp 吃 jrt 镜像：需要 `jimage extract`

**结论**：japicmp 0.26.5 的 `--old`/`--new` **只接受 jar 文件，不接受目录**，更不接受 jrt 镜像路径（`lib/modules`）。直接喂目录报错：

```
E: File '...' could not be opened as a jar file: ... (Is a directory)
```

因此必须两步：
1. `jimage extract --dir <tmp> <lib/modules>`（用 JDK-27 自带 `jimage`，产出 `<tmp>/<module>/<package>/<Class>.class`）。
2. 因 japicmp 需要**扁平包路径**（`java/lang/String.class`，与 rt.jar 一致），把各模块目录内容合并到单个扁平目录，再用 JDK-27 的 `jar` 打包（**不能用系统 java 21 的 jar**，否则报 `Unsupported major.minor version 71.0`，因为它会去读 `module-info.class` 且 21 解析不了 71 号类文件）。

关键 gotcha：
- **扁平合并时会有 split-package 覆盖**：27351 个 class 合并后剩 27285 个（约 66 个因不同模块同名包/类被覆盖），对 API 表面比对无实质影响。
- **打包时必须排除 `module-info.class`**，否则 `jar` 因多个同名模块描述符冲突/版本问题失败。
- 本次实际命令（扁平 + 打包）：
  ```
  jimage extract --dir jdk27-classes <jdk>/lib/modules
  # 合并 <module>/* -> jdk27-flat/
  find jdk27-flat -name '*.class' ! -name 'module-info.class' ! -name 'package-info.class' > classlist.txt
  <jdk27>/bin/jar cf jdk27-flat.jar @classlist.txt
  java -Xmx8g -jar japicmp-0.26.5-jar-with-dependencies.jar \
    --old <jdk8>/jre/lib/rt.jar --new jdk27-flat.jar \
    --only-modified --ignore-missing-classes --no-annotations --reproducible-report \
    --xml-file diff.xml > japicmp.txt
  ```
- **注意内/外部噪声**：全量统计被 `sun.*`/`com.sun.*`/`jdk.internal.*` 大幅抬高（`com.sun` 家族贡献 5.2 万处变化、`jdk.internal` 1.7 万处、`sun.jvmstat` 等），真正对差分测试有意义的是第 1.2/1.3 节过滤后的稳定 API 数字。

---

## 5. jdeps 补充验证（可选，已做快速验证）

JDK-27 自带 `jdeps -verbose:class` 对 `tools/diff_jdk8_27/work/IntegerParseProbe_jdk27/IntegerParseProbe.class` 运行正常，能解析 71 号类文件并给出类级依赖（`java.base` 下的 `java.lang.Integer/String/System`、`java.util.Locale/TimeZone` 等），EXIT=0。结论：jdeps 27.0.1 可作为 `tools/out/*.types.txt` 类级引用清单的补充交叉验证手段；`tools/out/` 本身是文本文件（非 class），无法直接喂 jdeps，需对 jtreg 编译产物（如 `tools/diff_jdk8_27/work/**`、JTwork）跑。

---

## 6. 给补测/差分测试的行动清单

1. **补测「27-only 新方法」**：优先 `String.strip/repeat/formatted/translateEscapes`、`Math/*Exact`、`Integer/Long.parseXxx(CharSequence,...)`、`Objects.check*`、`Arrays.compare/mismatch`、`BigInteger/BigDecimal.sqrt/rootn`、`InputStream.readAllBytes/transferTo`、`Files.readString/writeString`、`Path.of`、`List/Map/Set.of/copyOf`、`Optional.isEmpty/orElseThrow()`。
2. **差分测试标记「JDK-8-only」**：第 3.1 节 276 个删除类（尤其 Java EE 系 + `Thread.stop/suspend/resume` + `finalize()` + `SecurityManager` 旧方法）只能在 JDK-8 侧跑，JDK-27 侧断言「类/方法不存在」。
3. **差分测试标记「签名/语义变更」**：`java.nio.Buffer` 返回类型协变、`SplittableRandom` 方法上移 `RandomGenerator`、`Optional.orElseThrow()` 新增重载、`Path` 抽象→default、`ObjectInputStream$GetField.get` 抛受检异常——这些是「二进制兼容但源码/行为可能漂移」的点，值得做双 JDK 行为对比。

（原始 japicmp 产物：`/tmp/japicmp-work/diff.xml`（225MB）、`japicmp.txt`（17 万行）、`per_class.tsv`、`group_by_family.tsv`、`new_classes.txt`、`removed_classes.txt`、`modified_classes.json`、`focus_report.txt`，均在 `/tmp/japicmp-work/`。）
