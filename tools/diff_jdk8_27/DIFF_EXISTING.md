# JDK-8 vs JDK-27 现有行为化用例差分清单

> 由 `tools/diff_jdk8_27/diff_existing_suite.py` 生成。
> 共 829 个测试类。
> 三分类：`expected`（规范允许的进化，测试已用公共父类/contains 处理）、
> `candidate`（真实未覆盖分支 → 新测试场景）、`env`（环境噪声，忽略）。

## candidate — 真实未覆盖分支 (29)

### java/text/Format/Field/FieldTest.java
- 类别：`divergent`
- 差异：N jdk8=74 jdk27=78; 1 lines only on JDK-8, 1 lines only on JDK-27
- 说明：JDK-9+ 新增 NumberFormat.Field.PREFIX/SUFFIX 常量，反射枚举未锁定常量集合（N=74→78）
- 仅 JDK-8 输出：
  - `PASS (74 assertions)`
- 仅 JDK-27 输出：
  - `PASS (78 assertions)`

### java/time/Instant/InstantTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：ChronoField.INSTANT_SECONDS.range() 由全 long 域改为有界 Instant 域（行为进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: range INSTANT_SECONDS`
  - `	at InstantTest.check(InstantTest.java:38)`
  - `	at InstantTest.testTemporalAccessors(InstantTest.java:97)`
  - `	at InstantTest.main(InstantTest.java:220)`

### java/time/LocalDate/LocalDateTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：MIN.minusDays 越界异常字段由 Year 改为 EpochDay（异常消息字段进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: MIN.minusDays JDK8: DateTimeException message <Invalid value for EpochDay (valid values -365243219162 - 365241780471): -365243219163> want <Invalid value for Year (valid values -999999999 - 999999999): -1000000000>`
  - `	at LocalDateTest.xact(LocalDateTest.java:309)`
  - `	at LocalDateTest.testDeepClampAndWith(LocalDateTest.java:389)`
  - `	at LocalDateTest.main(LocalDateTest.java:291)`

### java/time/Year/YearTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：Year.parse 前导 '+' 号 JDK-8 拒绝、JDK-27 接受（符号宽松化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: parse plus sign JDK8: expected DateTimeParseException but nothing was thrown`
  - `	at YearTest.xact(YearTest.java:47)`
  - `	at YearTest.testDeepYear(YearTest.java:107)`
  - `	at YearTest.main(YearTest.java:37)`

### java/time/format/DateTimeFormatterBuilder/DateTimeFormatterBuilderTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：分数段无小数点解析异常消息文本变化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: fraction no-decimal parse JDK8 leftover: DateTimeParseException message <Text '30.5' could not be parsed at index 0> want <Text '30.5' could not be parsed, unparsed text found at index 2>`
  - `	at DateTimeFormatterBuilderTest.xact(DateTimeFormatterBuilderTest.java:62)`
  - `	at DateTimeFormatterBuilderTest.testDeepBuilder(DateTimeFormatterBuilderTest.java:102)`
  - `	at DateTimeFormatterBuilderTest.main(DateTimeFormatterBuilderTest.java:44)`

### java/time/temporal/ChronoField/ChronoFieldTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：EPOCH_DAY.range() 由对称域改为真实 LocalDate 边界（行为进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: EpochDay range JDK8: got <-365243219162 - 365241780471> want <-365249999634 - 365249999634>`
  - `	at ChronoFieldTest.xeq(ChronoFieldTest.java:64)`
  - `	at ChronoFieldTest.testDeepChronoField(ChronoFieldTest.java:89)`
  - `	at ChronoFieldTest.main(ChronoFieldTest.java:38)`

### java/time/temporal/ValueRange/ValueRangeTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：ValueRange.of(3,1,2) IAE 消息 'maximum' → 'smallest maximum'（消息进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: of(3,1,2) JDK8: IllegalArgumentException message <Minimum value must be less than smallest maximum value> want <Minimum value must be less than maximum value>`
  - `	at ValueRangeTest.xact(ValueRangeTest.java:55)`
  - `	at ValueRangeTest.testDeepValueRange(ValueRangeTest.java:96)`
  - `	at ValueRangeTest.main(ValueRangeTest.java:37)`

### java/time/zone/ZoneOffsetTransitionRule/ZoneOffsetTransitionRuleTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：null timeDefinition NPE 消息拼写 timeDefnition → timeDefinition（消息修正）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: null timeDefinition: NullPointerException message <timeDefinition> want <timeDefnition>`
  - `	at ZoneOffsetTransitionRuleTest.xact(ZoneOffsetTransitionRuleTest.java:56)`
  - `	at ZoneOffsetTransitionRuleTest.testDeepZOTR(ZoneOffsetTransitionRuleTest.java:130)`
  - `	at ZoneOffsetTransitionRuleTest.main(ZoneOffsetTransitionRuleTest.java:38)`

### java/math/BigDecimal/BigDecimalTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：BigDecimal.valueOf(NaN/±Inf) NFE 消息由 null 改为 'Infinite or NaN'（消息进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: valueOf(NaN) JDK-8 无消息: expected null message but was "Infinite or NaN"`
  - `	at BigDecimalTest.checkNullMsg(BigDecimalTest.java:351)`
  - `	at BigDecimalTest.testDoubleConstructorPrecision(BigDecimalTest.java:403)`
  - `	at BigDecimalTest.main(BigDecimalTest.java:792)`

### java/lang/IllegalThreadStateException/IllegalThreadStateExceptionTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：空 ThreadGroup.destroy JDK-8 抛 ITSE、JDK-27 成功（行为进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: empty group destroy succeeds`
  - `	at IllegalThreadStateExceptionTest.check(IllegalThreadStateExceptionTest.java:11)`
  - `	at IllegalThreadStateExceptionTest.testGroupDestroyTriggers(IllegalThreadStateExceptionTest.java:229)`
  - `	at IllegalThreadStateExceptionTest.main(IllegalThreadStateExceptionTest.java:281)`

### java/lang/System/SystemTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：System.arraycopy 负长度异常消息 JDK-8 null、JDK-27 非 null（消息进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: arraycopy negative length message null [JDK8]`
  - `	at SystemTest.check(SystemTest.java:20)`
  - `	at SystemTest.testArraycopyExceptions(SystemTest.java:306)`
  - `	at SystemTest.main(SystemTest.java:686)`

### java/lang/Thread/ThreadTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：Thread((String)null) 等 NPE 消息变化
- JDK-8 stderr 尾：
  - `java.lang.Exception: Stack trace`
  - `	at java.lang.Thread.dumpStack(Thread.java:1341)`
  - `	at ThreadTest.testIntrospectionAndGroups(ThreadTest.java:1244)`
  - `	at ThreadTest.main(ThreadTest.java:1302)`
- JDK-27 stderr 尾：
  - `	at ThreadTest.testIntrospectionAndGroups(ThreadTest.java:1244)`
  - `	at ThreadTest.main(ThreadTest.java:1302)`
  - `Exception in thread "thread-test-main" java.lang.AssertionError: Thread((String)null) NPE message: NPE message <'name' is null> != expected <name cannot be null>`
  - `	at ThreadTest.expectNPEMsg(ThreadTest.java:67)`
  - `	at ThreadTest.testNamePrecise(ThreadTest.java:360)`
  - `	at ThreadTest.main(ThreadTest.java:1312)`

### java/lang/annotation/Annotation/AnnotationTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：标记注解 toString 格式变化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: marker toString: @AnnotationTest.Marker()`
  - `	at AnnotationTest.check(AnnotationTest.java:19)`
  - `	at AnnotationTest.testToString(AnnotationTest.java:242)`
  - `	at AnnotationTest.main(AnnotationTest.java:393)`

### java/lang/annotation/AnnotationTypeMismatchException/AnnotationTypeMismatchExceptionTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：非 transient Method 序列化异常类型变化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK 8 should throw NotSerializableException for non-transient Method`
  - `	at AnnotationTypeMismatchExceptionTest.check(AnnotationTypeMismatchExceptionTest.java:23)`
  - `	at AnnotationTypeMismatchExceptionTest.testSerialization(AnnotationTypeMismatchExceptionTest.java:102)`
  - `	at AnnotationTypeMismatchExceptionTest.main(AnnotationTypeMismatchExceptionTest.java:167)`

### java/lang/invoke/MethodType/MethodTypeTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：MethodType.fromDescriptor 解析失败消息/行为变化
- JDK-27 stderr 尾：
  - `	at java.base/sun.invoke.util.BytecodeDescriptor.parseMethod(BytecodeDescriptor.java:81)`
  - `	at java.base/java.lang.invoke.MethodType.fromDescriptor(MethodType.java:1197)`
  - `	at java.base/java.lang.invoke.MethodType.fromMethodDescriptorString(MethodType.java:1180)`
  - `	at MethodTypeTest.lambda$testDescriptorString$1(MethodTypeTest.java:489)`
  - `	at MethodTypeTest.checkThrows(MethodTypeTest.java:37)`
  - `	... 2 more`

### java/util/ArrayDeque/ArrayDequeTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：fail-fast 迭代器：JDK-8 尾部 remove 后仍抛 CME，JDK-27 不再抛（ArrayDeque 迭代器行为进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: iterator CME after tail remove: expected ConcurrentModificationException but nothing was thrown`
  - `	at ArrayDequeTest.checkThrows(ArrayDequeTest.java:48)`
  - `	at ArrayDequeTest.testFailFast(ArrayDequeTest.java:172)`
  - `	at ArrayDequeTest.main(ArrayDequeTest.java:455)`

### java/util/Deque/DequeTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：fail-fast 迭代器：descending 头 remove 后 CME 行为变化（与 ArrayDeque 同源）
- JDK-27 stderr 尾：
  - `	at DequeTest.expectThrows(DequeTest.java:32)`
  - `	at DequeTest.testFailFastCme(DequeTest.java:240)`
  - `	at DequeTest.main(DequeTest.java:287)`
  - `Caused by: java.lang.AssertionError: descending CME after head add: expected ConcurrentModificationException`
  - `	at DequeTest.expectThrows(DequeTest.java:28)`
  - `	... 2 more`

### java/util/IdentityHashMap/IdentityHashMapTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：remove(k,等值非同一实例 v)：JDK-8 误用 equals 语义，JDK-19+ 修正为 identity 语义（bug 修复）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: remove(k, equal-but-distinct value) true (equals semantics)`
  - `	at IdentityHashMapTest.check(IdentityHashMapTest.java:23)`
  - `	at IdentityHashMapTest.testRemoveReplaceValueSemantics(IdentityHashMapTest.java:190)`
  - `	at IdentityHashMapTest.main(IdentityHashMapTest.java:355)`

### java/util/PriorityQueue/PriorityQueueTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：不可比较元素 ClassCastException 消息/类型变化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.ClassCastException: class java.lang.Object cannot be cast to class java.lang.Comparable (java.lang.Object and java.lang.Comparable are in module java.base of loader 'bootstrap')`
  - `	at java.base/java.util.PriorityQueue.siftUpComparable(PriorityQueue.java:643)`
  - `	at java.base/java.util.PriorityQueue.siftUp(PriorityQueue.java:639)`
  - `	at java.base/java.util.PriorityQueue.offer(PriorityQueue.java:330)`
  - `	at PriorityQueueTest.testNonComparableJdk8(PriorityQueueTest.java:290)`
  - `	at PriorityQueueTest.main(PriorityQueueTest.java:454)`

### java/util/Properties/PropertiesTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：propertyNames 视图 remove 抛 UnsupportedOperationException 行为变化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.UnsupportedOperationException`
  - `	at java.base/java.util.Collections$UnmodifiableCollection.remove(Collections.java:1098)`
  - `	at PropertiesTest.testPropertyNames(PropertiesTest.java:134)`
  - `	at PropertiesTest.main(PropertiesTest.java:477)`

### java/util/concurrent/CompletableFuture/CompletableFutureTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：CancellationException 消息 JDK-8 为 null、JDK-27 非 null（消息进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: CancellationException message is null on JDK 8`
  - `	at CompletableFutureTest.check(CompletableFutureTest.java:28)`
  - `	at CompletableFutureTest.testCancelContract(CompletableFutureTest.java:294)`
  - `	at CompletableFutureTest.main(CompletableFutureTest.java:580)`

### java/util/concurrent/atomic/AtomicIntegerFieldUpdater/AtomicIntegerFieldUpdaterTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：private 字段 newUpdater 拒绝行为变化（三组 FieldUpdater 同源）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: private field rejected (no exception)`
  - `	at AtomicIntegerFieldUpdaterTest.assertThrows(AtomicIntegerFieldUpdaterTest.java:65)`
  - `	at AtomicIntegerFieldUpdaterTest.testNewUpdaterValidation(AtomicIntegerFieldUpdaterTest.java:91)`
  - `	at AtomicIntegerFieldUpdaterTest.main(AtomicIntegerFieldUpdaterTest.java:125)`

### java/util/concurrent/atomic/AtomicLongFieldUpdater/AtomicLongFieldUpdaterTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：private 字段 newUpdater 拒绝行为变化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: private field rejected (no exception)`
  - `	at AtomicLongFieldUpdaterTest.assertThrows(AtomicLongFieldUpdaterTest.java:65)`
  - `	at AtomicLongFieldUpdaterTest.testNewUpdaterValidation(AtomicLongFieldUpdaterTest.java:91)`
  - `	at AtomicLongFieldUpdaterTest.main(AtomicLongFieldUpdaterTest.java:126)`

### java/util/concurrent/atomic/AtomicReferenceFieldUpdater/AtomicReferenceFieldUpdaterTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：private 字段 newUpdater 拒绝行为变化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: private field rejected (no exception)`
  - `	at AtomicReferenceFieldUpdaterTest.assertThrows(AtomicReferenceFieldUpdaterTest.java:24)`
  - `	at AtomicReferenceFieldUpdaterTest.testNewUpdaterValidation(AtomicReferenceFieldUpdaterTest.java:104)`
  - `	at AtomicReferenceFieldUpdaterTest.main(AtomicReferenceFieldUpdaterTest.java:157)`

### java/util/jar/JarFile/JarFileTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：缺失 jar 异常族：JDK-8 IOException、JDK-27 NoSuchFileException（异常类型细化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: missing jar IOException family: NoSuchFileException`
  - `	at JarFileTest.check(JarFileTest.java:34)`
  - `	at JarFileTest.testContracts(JarFileTest.java:251)`
  - `	at JarFileTest.main(JarFileTest.java:257)`

### java/util/logging/Handler/HandlerTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：Handler.isLoggable(null) JDK-8 抛 NPE、JDK-27 不抛（行为进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: isLoggable(null) NPE: expected NullPointerException but none thrown`
  - `	at HandlerTest.expectThrows(HandlerTest.java:37)`
  - `	at HandlerTest.testBlackBox(HandlerTest.java:116)`
  - `	at HandlerTest.main(HandlerTest.java:187)`

### java/nio/channels/Channels/ChannelsTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：通道适配器已关闭后读写 ClosedChannelException 行为变化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.nio.channels.ClosedChannelException`
  - `	at java.base/java.nio.channels.Channels$ReadableByteChannelImpl.read(Channels.java:299)`
  - `	at ChannelsTest.testChannelAdapters(ChannelsTest.java:313)`
  - `	at ChannelsTest.main(ChannelsTest.java:435)`

### java/nio/channels/SelectionKey/SelectionKeyTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：readyOps 就绪谓词行为变化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: readyOps == OP_WRITE`
  - `	at SelectionKeyTest.check(SelectionKeyTest.java:20)`
  - `	at SelectionKeyTest.testReadinessPredicates(SelectionKeyTest.java:229)`
  - `	at SelectionKeyTest.main(SelectionKeyTest.java:264)`

### java/nio/file/FileSystemLoopException/FileSystemLoopExceptionTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：Files.copy 循环符号链接异常行为变化
- JDK-27 stderr 尾：
  - `	at java.base/sun.nio.fs.UnixFileSystem.copy(UnixFileSystem.java:946)`
  - `	at java.base/sun.nio.fs.UnixFileSystemProvider.copy(UnixFileSystemProvider.java:281)`
  - `	at java.base/java.nio.file.Files.copy(Files.java:1196)`
  - `	at FileSystemLoopExceptionTest.lambda$testContractCopyThroughCycle$0(FileSystemLoopExceptionTest.java:185)`
  - `	at FileSystemLoopExceptionTest.checkThrowsExactIO(FileSystemLoopExceptionTest.java:43)`
  - `	... 2 more`

## expected — 规范允许的进化 (141)

### java/text/DateFormat/DateFormatTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：日语 FULL 日期格式 JDK-9+ 改走 CLDR，无星期字段（JRE 格式数据进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: ja FULL date has no weekday (JRE)`
  - `	at DateFormatTest.check(DateFormatTest.java:25)`
  - `	at DateFormatTest.main(DateFormatTest.java:325)`

### java/text/DateFormatSymbols/DateFormatSymbolsTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：德语 shortMonths 去掉句点（CLDR 数据进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: de shortMonths without periods (JRE)`
  - `	at DateFormatSymbolsTest.check(DateFormatSymbolsTest.java:16)`
  - `	at DateFormatSymbolsTest.testVersionLockedLocaleDataContract(DateFormatSymbolsTest.java:208)`
  - `	at DateFormatSymbolsTest.main(DateFormatSymbolsTest.java:234)`

### java/text/DecimalFormat/DecimalFormatTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：货币实例 toPattern 随 CLDR 数据进化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: currency instance toPattern (JRE data)`
  - `	at DecimalFormatTest.check(DecimalFormatTest.java:33)`
  - `	at DecimalFormatTest.testVersionLockedStringsContract(DecimalFormatTest.java:509)`
  - `	at DecimalFormatTest.main(DecimalFormatTest.java:527)`

### java/text/DecimalFormatSymbols/DecimalFormatSymbolsTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：美元 NaN 符号由 U+FFFD 改为 U+FFFD 之外的 CLDR 值（数据进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: US NaN symbol is U+FFFD (JRE data)`
  - `	at DecimalFormatSymbolsTest.check(DecimalFormatSymbolsTest.java:16)`
  - `	at DecimalFormatSymbolsTest.testVersionLockedLocaleDataContract(DecimalFormatSymbolsTest.java:222)`
  - `	at DecimalFormatSymbolsTest.main(DecimalFormatSymbolsTest.java:241)`

### java/text/NumberFormat/NumberFormatTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：de_DE 货币符号/格式随 CLDR 数据进化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: de_DE currency (JRE)`
  - `	at NumberFormatTest.check(NumberFormatTest.java:31)`
  - `	at NumberFormatTest.testVersionLockedLocaleDataContract(NumberFormatTest.java:359)`
  - `	at NumberFormatTest.main(NumberFormatTest.java:380)`

### java/text/SimpleDateFormat/SimpleDateFormatTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：默认模式 locale 数据进化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: default pattern is M/d/yy h:mm a (JRE data)`
  - `	at SimpleDateFormatTest.check(SimpleDateFormatTest.java:34)`
  - `	at SimpleDateFormatTest.testVersionLockedLocaleDataContract(SimpleDateFormatTest.java:507)`
  - `	at SimpleDateFormatTest.main(SimpleDateFormatTest.java:545)`

### java/time/Month/MonthTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：SHORT_STANDALONE 文本 CLDR 由数字改为缩写（数据进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: SHORT_STANDALONE JDK8 numeric`
  - `	at MonthTest.check(MonthTest.java:20)`
  - `	at MonthTest.testDeepMonth(MonthTest.java:100)`
  - `	at MonthTest.main(MonthTest.java:34)`

### java/time/ZoneId/ZoneIdTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：SHORT_IDS 中 EST 映射由 -05:00 改为 America/Panama（tzdata 进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: SHORT_IDS EST JDK8: got <America/Panama> want <-05:00>`
  - `	at ZoneIdTest.xeq(ZoneIdTest.java:166)`
  - `	at ZoneIdTest.testDeepZoneId(ZoneIdTest.java:196)`
  - `	at ZoneIdTest.main(ZoneIdTest.java:140)`

### java/time/chrono/Era/EraTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：昭和短名 Shōwa vs Showa（CLDR 日文标音进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: showa short name: got <Shōwa> want <Showa>`
  - `	at EraTest.xeq(EraTest.java:74)`
  - `	at EraTest.testDeepEra(EraTest.java:111)`
  - `	at EraTest.main(EraTest.java:48)`

### java/time/chrono/HijrahChronology/HijrahChronologyTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：回历全名 CLDR 文本进化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: hj chronology full name: got <Hijri Calendar (Umm al-Qura)> want <Islamic Umm al-Qura Calendar>`
  - `	at HijrahChronologyTest.xeq(HijrahChronologyTest.java:64)`
  - `	at HijrahChronologyTest.testDeepHijrahChronology(HijrahChronologyTest.java:81)`
  - `	at HijrahChronologyTest.main(HijrahChronologyTest.java:38)`

### java/time/chrono/HijrahEra/HijrahEraTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：回历纪年全名 CLDR 文本进化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: full name: got <Anno Hegirae> want <Anno Domini>`
  - `	at HijrahEraTest.xeq(HijrahEraTest.java:56)`
  - `	at HijrahEraTest.testDeepHijrahEra(HijrahEraTest.java:66)`
  - `	at HijrahEraTest.main(HijrahEraTest.java:30)`

### java/time/chrono/JapaneseEra/JapaneseEraTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：昭和短名 CLDR 文本进化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: showa short: got <Shōwa> want <Showa>`
  - `	at JapaneseEraTest.xeq(JapaneseEraTest.java:58)`
  - `	at JapaneseEraTest.testDeepJapaneseEra(JapaneseEraTest.java:75)`
  - `	at JapaneseEraTest.main(JapaneseEraTest.java:32)`

### java/time/chrono/MinguoDate/MinguoDateTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：民国纪年格式 CLDR 文本进化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: format era pattern: got <Minguo 0113-06-15> want <R.O.C. 0113-06-15>`
  - `	at MinguoDateTest.xeq(MinguoDateTest.java:62)`
  - `	at MinguoDateTest.testDeepMinguoDate(MinguoDateTest.java:83)`
  - `	at MinguoDateTest.main(MinguoDateTest.java:36)`

### java/time/chrono/MinguoEra/MinguoEraTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：民国纪年全名 CLDR 文本进化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: ROC full name: got <Minguo> want <Anno Domini>`
  - `	at MinguoEraTest.xeq(MinguoEraTest.java:57)`
  - `	at MinguoEraTest.testDeepMinguoEra(MinguoEraTest.java:69)`
  - `	at MinguoEraTest.main(MinguoEraTest.java:31)`

### java/time/chrono/ThaiBuddhistDate/ThaiBuddhistDateTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：佛历纪年格式 CLDR 文本进化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: tb format era pattern: got <BE 2567-06-15> want <B.E. 2567-06-15>`
  - `	at ThaiBuddhistDateTest.xeq(ThaiBuddhistDateTest.java:60)`
  - `	at ThaiBuddhistDateTest.testDeepThaiBuddhistDate(ThaiBuddhistDateTest.java:78)`
  - `	at ThaiBuddhistDateTest.main(ThaiBuddhistDateTest.java:34)`

### java/time/chrono/ThaiBuddhistEra/ThaiBuddhistEraTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：佛历纪年全名 CLDR 文本进化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: BE full name: got <BE> want <Anno Domini>`
  - `	at ThaiBuddhistEraTest.xeq(ThaiBuddhistEraTest.java:58)`
  - `	at ThaiBuddhistEraTest.testDeepThaiBuddhistEra(ThaiBuddhistEraTest.java:70)`
  - `	at ThaiBuddhistEraTest.main(ThaiBuddhistEraTest.java:32)`

### java/time/format/DateTimeFormatter/DateTimeFormatterTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：MEDIUM 时间格式窄不换行空格 U+202F vs 普通空格（CLDR 数据进化）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: loc time MEDIUM JDK8 space: got <10:15:30 AM> want <10:15:30 AM>`
  - `	at DateTimeFormatterTest.xeq(DateTimeFormatterTest.java:223)`
  - `	at DateTimeFormatterTest.testDeepFormatter(DateTimeFormatterTest.java:265)`
  - `	at DateTimeFormatterTest.main(DateTimeFormatterTest.java:197)`

### java/time/format/TextStyle/TextStyleTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：SHORT_STANDALONE 文本 CLDR 数据进化
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: SHORT_STANDALONE JDK8 numeric: got <Feb> want <2>`
  - `	at TextStyleTest.xeq(TextStyleTest.java:56)`
  - `	at TextStyleTest.testDeepTextStyle(TextStyleTest.java:86)`
  - `	at TextStyleTest.main(TextStyleTest.java:30)`

### java/lang/ArithmeticException/ArithmeticExceptionTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: unreachable: absExact must be absent on JDK-8`
  - `	at ArithmeticExceptionTest.testAbsExactAbsent(ArithmeticExceptionTest.java:234)`
  - `	at ArithmeticExceptionTest.main(ArithmeticExceptionTest.java:253)`

### java/lang/Byte/ByteTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 Byte has no compareUnsigned (JDK 9+)`
  - `	at ByteTest.check(ByteTest.java:14)`
  - `	at ByteTest.testWhiteBoxStructural(ByteTest.java:353)`
  - `	at ByteTest.main(ByteTest.java:389)`

### java/lang/Class/ClassTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: toGenericString array exact (JDK-8 form): int[]`
  - `	at ClassTest.check(ClassTest.java:44)`
  - `	at ClassTest.testToStringFamily(ClassTest.java:313)`
  - `	at ClassTest.main(ClassTest.java:733)`

### java/lang/ClassLoader/ClassLoaderTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 system loader class name`
  - `	at ClassLoaderTest.check(ClassLoaderTest.java:75)`
  - `	at ClassLoaderTest.testLoaderIdentityChain(ClassLoaderTest.java:194)`
  - `	at ClassLoaderTest.main(ClassLoaderTest.java:478)`

### java/lang/CloneNotSupportedException/CloneNotSupportedExceptionTest.java
- 类别：`spec_added`
- 差异：compiles on JDK-27 only
- 说明：JDK-9+ 新增（JDK-8 源未引用，编译期捕获）

### java/lang/Compiler/CompilerTest.java
- 类别：`spec_removed`
- 差异：compiles on JDK-8 only: 22 errors
- 说明：JDK-8 专属 API 在 JDK-27 移除（规范允许）

### java/lang/Deprecated/DeprecatedTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 Deprecated declares zero members (javap-confirmed)`
  - `	at DeprecatedTest.check(DeprecatedTest.java:18)`
  - `	at DeprecatedTest.testMembersAbsent(DeprecatedTest.java:61)`
  - `	at DeprecatedTest.main(DeprecatedTest.java:158)`

### java/lang/IndexOutOfBoundsException/IndexOutOfBoundsExceptionTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: unreachable: (int) ctor must be absent on JDK-8`
  - `	at IndexOutOfBoundsExceptionTest.testIntCtorAbsent(IndexOutOfBoundsExceptionTest.java:224)`
  - `	at IndexOutOfBoundsExceptionTest.main(IndexOutOfBoundsExceptionTest.java:326)`

### java/lang/NullPointerException/NullPointerExceptionTest.java
- 类别：`divergent`
- 差异：N jdk8=73 jdk27=74; 1 lines only on JDK-8, 1 lines only on JDK-27
- 说明：JDK-27 常量/字段/MBean 增多（N 计数变化）
- 仅 JDK-8 输出：
  - `PASS (73 assertions)`
- 仅 JDK-27 输出：
  - `PASS (74 assertions)`

### java/lang/Object/ObjectTest.java
- 类别：`divergent`
- 差异：N jdk8=167 jdk27=169; 1 lines only on JDK-8, 1 lines only on JDK-27
- 说明：JDK-27 常量/字段/MBean 增多（N 计数变化）
- 仅 JDK-8 输出：
  - `PASS (167 assertions)`
- 仅 JDK-27 输出：
  - `PASS (169 assertions)`

### java/lang/Package/PackageTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: Package is not final in JDK-8`
  - `	at PackageTest.check(PackageTest.java:17)`
  - `	at PackageTest.testWhiteBox(PackageTest.java:25)`
  - `	at PackageTest.main(PackageTest.java:85)`

### java/lang/Process/ProcessTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: pid should be absent in JDK-8 (it is a JDK-9+/17+ API)`
  - `	at ProcessTest.expectNoMethod(ProcessTest.java:141)`
  - `	at ProcessTest.testWhiteBoxApiSurface(ProcessTest.java:344)`
  - `	at ProcessTest.main(ProcessTest.java:379)`

### java/lang/ProcessBuilder/ProcessBuilderTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: startPipeline should be absent in JDK-8`
  - `	at ProcessBuilderTest.testWhiteBox(ProcessBuilderTest.java:61)`
  - `	at ProcessBuilderTest.main(ProcessBuilderTest.java:517)`

### java/lang/Runtime/RuntimeTest.java
- 类别：`spec_removed`
- 差异：compiles on JDK-8 only: 3 warnings
- 说明：JDK-8 专属 API 在 JDK-27 移除（规范允许）

### java/lang/SecurityManager/SecurityManagerTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: not @Deprecated on JDK-8`
  - `	at SecurityManagerTest.check(SecurityManagerTest.java:11)`
  - `	at SecurityManagerTest.testWhiteBox(SecurityManagerTest.java:73)`
  - `	at SecurityManagerTest.main(SecurityManagerTest.java:229)`

### java/lang/Short/ShortTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 Short has no compareUnsigned (JDK 9+)`
  - `	at ShortTest.check(ShortTest.java:14)`
  - `	at ShortTest.testWhiteBoxStructural(ShortTest.java:365)`
  - `	at ShortTest.main(ShortTest.java:402)`

### java/lang/StackTraceElement/StackTraceElementTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: 7-arg constructor should be absent in JDK-8`
  - `	at StackTraceElementTest.testWhiteBox(StackTraceElementTest.java:30)`
  - `	at StackTraceElementTest.main(StackTraceElementTest.java:419)`

### java/lang/SuppressWarnings/SuppressWarningsTest.java
- 类别：`divergent`
- 差异：N jdk8=40 jdk27=33; 1 lines only on JDK-8, 1 lines only on JDK-27
- 说明：JDK-27 常量/字段/MBean 增多（N 计数变化）
- 仅 JDK-8 输出：
  - `PASS (40 assertions)`
- 仅 JDK-27 输出：
  - `PASS (33 assertions)`

### java/lang/ThreadDeath/ThreadDeathTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: no @Deprecated on JDK-8`
  - `	at ThreadDeathTest.check(ThreadDeathTest.java:11)`
  - `	at ThreadDeathTest.testClassFacts(ThreadDeathTest.java:34)`
  - `	at ThreadDeathTest.main(ThreadDeathTest.java:147)`

### java/lang/ThreadGroup/ThreadGroupTest.java
- 类别：`spec_removed`
- 差异：compiles on JDK-8 only: 31 warnings
- 说明：JDK-8 专属 API 在 JDK-27 移除（规范允许）

### java/lang/Throwable/ThrowableTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `	at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:319)`
  - `	at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:267)`
  - `	at java.base/java.lang.reflect.Field.checkCanSetAccessible(Field.java:207)`
  - `	at java.base/java.lang.reflect.Field.setAccessible(Field.java:201)`
  - `	at ThrowableTest.testWhiteBox(ThrowableTest.java:155)`
  - `	at ThrowableTest.main(ThrowableTest.java:755)`

### java/lang/annotation/ElementType/ElementTypeTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: constants and declaration order: [TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE, MODULE, RECORD_COMPONENT]`
  - `	at ElementTypeTest.check(ElementTypeTest.java:16)`
  - `	at ElementTypeTest.testBehavior(ElementTypeTest.java:36)`
  - `	at ElementTypeTest.main(ElementTypeTest.java:144)`

### java/lang/instrument/ClassFileTransformer/ClassFileTransformerTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK 8: exactly one transform overload`
  - `	at ClassFileTransformerTest.check(ClassFileTransformerTest.java:15)`
  - `	at ClassFileTransformerTest.testDeepInterfaceSurface(ClassFileTransformerTest.java:88)`
  - `	at ClassFileTransformerTest.main(ClassFileTransformerTest.java:102)`

### java/lang/invoke/LambdaMetafactory/LambdaMetafactoryTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: class not final on JDK-8`
  - `	at LambdaMetafactoryTest.check(LambdaMetafactoryTest.java:22)`
  - `	at LambdaMetafactoryTest.testStructureDeep(LambdaMetafactoryTest.java:99)`
  - `	at LambdaMetafactoryTest.main(LambdaMetafactoryTest.java:366)`

### java/lang/invoke/MethodHandleProxies/MethodHandleProxiesTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: wrapper class is a Proxy (JDK-8)`
  - `	at MethodHandleProxiesTest.check(MethodHandleProxiesTest.java:26)`
  - `	at MethodHandleProxiesTest.testProxyObjectSemantics(MethodHandleProxiesTest.java:197)`
  - `	at MethodHandleProxiesTest.main(MethodHandleProxiesTest.java:249)`

### java/lang/invoke/MethodHandles/Lookup/LookupTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: findVirtual private IAE (no nests in 8): expected IllegalAccessException but nothing was thrown`
  - `	at LookupTest.checkThrowsMsg(LookupTest.java:66)`
  - `	at LookupTest.testFindVirtualDeep(LookupTest.java:133)`
  - `	at LookupTest.main(LookupTest.java:336)`

### java/lang/invoke/MethodHandles/MethodHandlesTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: publicLookup only PUBLIC mode`
  - `	at MethodHandlesTest.check(MethodHandlesTest.java:38)`
  - `	at MethodHandlesTest.testLookup(MethodHandlesTest.java:300)`
  - `	at MethodHandlesTest.main(MethodHandlesTest.java:556)`

### java/lang/management/BufferPoolMXBean/BufferPoolMXBeanTest.java
- 类别：`divergent`
- 差异：N jdk8=23 jdk27=27; 1 lines only on JDK-8, 1 lines only on JDK-27
- 说明：JDK-27 常量/字段/MBean 增多（N 计数变化）
- 仅 JDK-8 输出：
  - `PASS (23 assertions)`
- 仅 JDK-27 输出：
  - `PASS (27 assertions)`

### java/lang/management/GarbageCollectorMXBean/GarbageCollectorMXBeanTest.java
- 类别：`divergent`
- 差异：N jdk8=33 jdk27=44; 1 lines only on JDK-8, 1 lines only on JDK-27
- 说明：JDK-27 常量/字段/MBean 增多（N 计数变化）
- 仅 JDK-8 输出：
  - `PASS (33 assertions)`
- 仅 JDK-27 输出：
  - `PASS (44 assertions)`

### java/lang/management/ManagementFactory/ManagementFactoryTest.java
- 类别：`divergent`
- 差异：N jdk8=176 jdk27=205; 1 lines only on JDK-8, 1 lines only on JDK-27
- 说明：JDK-27 常量/字段/MBean 增多（N 计数变化）
- 仅 JDK-8 输出：
  - `PASS (176 assertions)`
- 仅 JDK-27 输出：
  - `PASS (205 assertions)`

### java/lang/management/MemoryManagerMXBean/MemoryManagerMXBeanTest.java
- 类别：`divergent`
- 差异：N jdk8=35 jdk27=45; 1 lines only on JDK-8, 1 lines only on JDK-27
- 说明：JDK-27 常量/字段/MBean 增多（N 计数变化）
- 仅 JDK-8 输出：
  - `PASS (35 assertions)`
- 仅 JDK-27 输出：
  - `PASS (45 assertions)`

### java/lang/management/MemoryPoolMXBean/MemoryPoolMXBeanTest.java
- 类别：`divergent`
- 差异：N jdk8=79 jdk27=101; 1 lines only on JDK-8, 1 lines only on JDK-27
- 说明：JDK-27 常量/字段/MBean 增多（N 计数变化）
- 仅 JDK-8 输出：
  - `PASS (79 assertions)`
- 仅 JDK-27 输出：
  - `PASS (101 assertions)`

### java/lang/management/PlatformManagedObject/PlatformManagedObjectTest.java
- 类别：`divergent`
- 差异：N jdk8=21 jdk27=25; 1 lines only on JDK-8, 1 lines only on JDK-27
- 说明：JDK-27 常量/字段/MBean 增多（N 计数变化）
- 仅 JDK-8 输出：
  - `PASS (21 assertions)`
- 仅 JDK-27 输出：
  - `PASS (25 assertions)`

### java/lang/management/RuntimeMXBean/RuntimeMXBeanTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK 8 reports spec version 1.8, got 27`
  - `	at RuntimeMXBeanTest.check(RuntimeMXBeanTest.java:14)`
  - `	at RuntimeMXBeanTest.testDeepVersionDifferences(RuntimeMXBeanTest.java:111)`
  - `	at RuntimeMXBeanTest.main(RuntimeMXBeanTest.java:129)`

### java/lang/management/ThreadInfo/ThreadInfoTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK 8 has no 4-arg getThreadInfo`
  - `	at ThreadInfoTest.check(ThreadInfoTest.java:15)`
  - `	at ThreadInfoTest.testDeepVersionMethodSurface(ThreadInfoTest.java:229)`
  - `	at ThreadInfoTest.main(ThreadInfoTest.java:246)`

### java/lang/management/ThreadMXBean/ThreadMXBeanTest.java
- 类别：`divergent`
- 差异：N jdk8=52 jdk27=54; 1 lines only on JDK-8, 1 lines only on JDK-27
- 说明：JDK-27 常量/字段/MBean 增多（N 计数变化）
- 仅 JDK-8 输出：
  - `PASS (52 assertions)`
- 仅 JDK-27 输出：
  - `PASS (54 assertions)`

### java/lang/ref/Reference/ReferenceTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: isEnqueued is not deprecated on JDK-8`
  - `	at ReferenceTest.check(ReferenceTest.java:22)`
  - `	at ReferenceTest.testIsEnqueuedDeprecationSurface(ReferenceTest.java:227)`
  - `	at ReferenceTest.main(ReferenceTest.java:243)`

### java/lang/reflect/AccessibleObject/AccessibleObjectTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: no canAccess on JDK-8: expected NoSuchMethodException but nothing was thrown`
  - `	at AccessibleObjectTest.checkThrows(AccessibleObjectTest.java:40)`
  - `	at AccessibleObjectTest.testApiSurfaceLocked(AccessibleObjectTest.java:136)`
  - `	at AccessibleObjectTest.main(AccessibleObjectTest.java:232)`

### java/lang/reflect/AnnotatedType/AnnotatedTypeTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: receiver type is declaring class`
  - `	at AnnotatedTypeTest.check(AnnotatedTypeTest.java:30)`
  - `	at AnnotatedTypeTest.testReceiverType(AnnotatedTypeTest.java:215)`
  - `	at AnnotatedTypeTest.main(AnnotatedTypeTest.java:246)`

### java/lang/reflect/AnnotatedTypeVariable/AnnotatedTypeVariableTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: method TV declared by the method`
  - `	at AnnotatedTypeVariableTest.check(AnnotatedTypeVariableTest.java:27)`
  - `	at AnnotatedTypeVariableTest.testMultiBoundUseSite(AnnotatedTypeVariableTest.java:157)`
  - `	at AnnotatedTypeVariableTest.main(AnnotatedTypeVariableTest.java:186)`

### java/lang/reflect/AnnotatedWildcardType/AnnotatedWildcardTypeTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: no explicit annotated upper bound (recorded on this JDK)`
  - `	at AnnotatedWildcardTypeTest.check(AnnotatedWildcardTypeTest.java:29)`
  - `	at AnnotatedWildcardTypeTest.testLowerBound(AnnotatedWildcardTypeTest.java:105)`
  - `	at AnnotatedWildcardTypeTest.main(AnnotatedWildcardTypeTest.java:196)`

### java/lang/reflect/Constructor/ConstructorTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: private newInstance without accessible IAE: expected IllegalAccessException but nothing was thrown`
  - `	at ConstructorTest.checkThrows(ConstructorTest.java:52)`
  - `	at ConstructorTest.testAccessibility(ConstructorTest.java:177)`
  - `	at ConstructorTest.main(ConstructorTest.java:394)`

### java/lang/reflect/Executable/ExecutableTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 static-nested ctor receiver non-null`
  - `	at ExecutableTest.check(ExecutableTest.java:26)`
  - `	at ExecutableTest.testReceiverTypeEvidence(ExecutableTest.java:197)`
  - `	at ExecutableTest.main(ExecutableTest.java:241)`

### java/lang/reflect/Field/FieldTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: private get without accessible IAE: expected IllegalAccessException but nothing was thrown`
  - `	at FieldTest.checkThrows(FieldTest.java:50)`
  - `	at FieldTest.testAccessibility(FieldTest.java:245)`
  - `	at FieldTest.main(FieldTest.java:755)`

### java/lang/reflect/GenericDeclaration/GenericDeclarationTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: method declaration round-trip`
  - `	at GenericDeclarationTest.check(GenericDeclarationTest.java:17)`
  - `	at GenericDeclarationTest.testMethodAndConstructorDeclarations(GenericDeclarationTest.java:108)`
  - `	at GenericDeclarationTest.main(GenericDeclarationTest.java:132)`

### java/lang/reflect/InvocationHandler/InvocationHandlerTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: exactly one declared method on JDK-8 (no invokeDefault yet)`
  - `	at InvocationHandlerTest.check(InvocationHandlerTest.java:24)`
  - `	at InvocationHandlerTest.testInterfaceContract(InvocationHandlerTest.java:94)`
  - `	at InvocationHandlerTest.main(InvocationHandlerTest.java:218)`

### java/lang/reflect/MalformedParameterizedTypeException/MalformedParameterizedTypeExceptionTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: single public constructor on JDK-8 (no message ctor until 21)`
  - `	at MalformedParameterizedTypeExceptionTest.check(MalformedParameterizedTypeExceptionTest.java:22)`
  - `	at MalformedParameterizedTypeExceptionTest.testHierarchyCensus(MalformedParameterizedTypeExceptionTest.java:52)`
  - `	at MalformedParameterizedTypeExceptionTest.main(MalformedParameterizedTypeExceptionTest.java:133)`

### java/lang/reflect/Member/MemberTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: no accessFlags on JDK-8 Member: expected NoSuchMethodException but nothing was thrown`
  - `	at MemberTest.checkThrows(MemberTest.java:36)`
  - `	at MemberTest.testApiSurfaceLocked(MemberTest.java:184)`
  - `	at MemberTest.main(MemberTest.java:195)`

### java/lang/reflect/Method/MethodTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: private invoke without accessible IAE: expected IllegalAccessException but nothing was thrown`
  - `	at MethodTest.checkThrows(MethodTest.java:55)`
  - `	at MethodTest.testAccessibility(MethodTest.java:262)`
  - `	at MethodTest.main(MethodTest.java:624)`

### java/lang/reflect/Modifier/ModifierTest.java
- 类别：`spec_removed`
- 差异：compiles on JDK-8 only: 1 warning
- 说明：JDK-8 专属 API 在 JDK-27 移除（规范允许）

### java/lang/reflect/Parameter/ParameterTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 reports non-implicit without -parameters`
  - `	at ParameterTest.check(ParameterTest.java:22)`
  - `	at ParameterTest.testInnerCtorParameters(ParameterTest.java:160)`
  - `	at ParameterTest.main(ParameterTest.java:176)`

### java/lang/reflect/TypeVariable/TypeVariableTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: declared by the method`
  - `	at TypeVariableTest.check(TypeVariableTest.java:24)`
  - `	at TypeVariableTest.testMethodAndConstructorVariables(TypeVariableTest.java:148)`
  - `	at TypeVariableTest.main(TypeVariableTest.java:200)`

### java/util/Arrays/ArraysTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `	at ArraysTest.main(ArraysTest.java:520)`
  - `Caused by: java.lang.UnsupportedOperationException: remove`
  - `	at java.base/java.util.Iterator.remove(Iterator.java:102)`
  - `	at ArraysTest.lambda$testAsListDeepContract$0(ArraysTest.java:446)`
  - `	at ArraysTest.checkThrows(ArraysTest.java:28)`
  - `	... 2 more`

### java/util/Currency/CurrencyTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: CNY symbol in US is CNY (JDK-8 non-CLDR data)`
  - `	at CurrencyTest.check(CurrencyTest.java:21)`
  - `	at CurrencyTest.testCurrencyContracts(CurrencyTest.java:118)`
  - `	at CurrencyTest.main(CurrencyTest.java:152)`

### java/util/DoubleSummaryStatistics/DoubleSummaryStatisticsTest.java
- 类别：`divergent`
- 差异：N jdk8=14 jdk27=15; 1 lines only on JDK-8, 1 lines only on JDK-27
- 说明：JDK-27 常量/字段/MBean 增多（N 计数变化）
- 仅 JDK-8 输出：
  - `PASS (14 assertions)`
- 仅 JDK-27 输出：
  - `PASS (15 assertions)`

### java/util/EnumSet/EnumSetTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 EnumSet spliterator exact characteristics SIZED|SUBSIZED|DISTINCT (0x4041)`
  - `	at EnumSetTest.check(EnumSetTest.java:31)`
  - `	at EnumSetTest.testSpliteratorExact(EnumSetTest.java:332)`
  - `	at EnumSetTest.main(EnumSetTest.java:400)`

### java/util/Locale/LocaleTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 filter preserves input-list order`
  - `	at LocaleTest.check(LocaleTest.java:19)`
  - `	at LocaleTest.testLocaleContracts(LocaleTest.java:183)`
  - `	at LocaleTest.main(LocaleTest.java:209)`

### java/util/MissingResourceException/MissingResourceExceptionTest.java
- 类别：`divergent`
- 差异：N jdk8=9 jdk27=8; 1 lines only on JDK-8, 1 lines only on JDK-27
- 说明：JDK-27 常量/字段/MBean 增多（N 计数变化）
- 仅 JDK-8 输出：
  - `PASS (9 assertions)`
- 仅 JDK-27 输出：
  - `PASS (8 assertions)`

### java/util/NoSuchElementException/NoSuchElementExceptionTest.java
- 类别：`divergent`
- 差异：N jdk8=19 jdk27=20; 1 lines only on JDK-8, 1 lines only on JDK-27
- 说明：JDK-27 常量/字段/MBean 增多（N 计数变化）
- 仅 JDK-8 输出：
  - `PASS (19 assertions)`
- 仅 JDK-27 输出：
  - `PASS (20 assertions)`

### java/util/Optional/OptionalTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: isEmpty should not exist on Java 8`
  - `	at OptionalTest.testSurfaceLock(OptionalTest.java:309)`
  - `	at OptionalTest.main(OptionalTest.java:348)`

### java/util/OptionalDouble/OptionalDoubleTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: isEmpty should not exist`
  - `	at OptionalDoubleTest.testSurfaceLock(OptionalDoubleTest.java:215)`
  - `	at OptionalDoubleTest.main(OptionalDoubleTest.java:244)`

### java/util/OptionalInt/OptionalIntTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: isEmpty should not exist on Java 8`
  - `	at OptionalIntTest.testSurfaceLock(OptionalIntTest.java:192)`
  - `	at OptionalIntTest.main(OptionalIntTest.java:226)`

### java/util/OptionalLong/OptionalLongTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: isEmpty should not exist`
  - `	at OptionalLongTest.testSurfaceLock(OptionalLongTest.java:181)`
  - `	at OptionalLongTest.main(OptionalLongTest.java:209)`

### java/util/PropertyResourceBundle/PropertyResourceBundleTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 reads raw UTF-8 bytes as ISO-8859-1 (mojibake, escapes required)`
  - `	at PropertyResourceBundleTest.check(PropertyResourceBundleTest.java:23)`
  - `	at PropertyResourceBundleTest.testUtf8EncodingContract(PropertyResourceBundleTest.java:92)`
  - `	at PropertyResourceBundleTest.main(PropertyResourceBundleTest.java:147)`

### java/util/SplittableRandom/SplittableRandomTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: nextDouble(0.0) message exact on JDK-8`
  - `	at SplittableRandomTest.check(SplittableRandomTest.java:20)`
  - `	at SplittableRandomTest.testIaeMessages(SplittableRandomTest.java:220)`
  - `	at SplittableRandomTest.main(SplittableRandomTest.java:305)`

### java/util/concurrent/AbstractExecutorService/AbstractExecutorServiceTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: invokeAny 空集合 IAE（JDK-8 消息 null）: unexpected message: tasks is empty`
  - `	at AbstractExecutorServiceTest.checkExc(AbstractExecutorServiceTest.java:38)`
  - `	at AbstractExecutorServiceTest.testNegativeCases(AbstractExecutorServiceTest.java:233)`
  - `	at AbstractExecutorServiceTest.main(AbstractExecutorServiceTest.java:297)`

### java/util/concurrent/ExecutorCompletionService/ExecutorCompletionServiceTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: ECS(null executor) 消息为 null（JDK-8 实测）`
  - `	at ExecutorCompletionServiceTest.check(ExecutorCompletionServiceTest.java:22)`
  - `	at ExecutorCompletionServiceTest.checkExc(ExecutorCompletionServiceTest.java:29)`
  - `	at ExecutorCompletionServiceTest.testNegativeCases(ExecutorCompletionServiceTest.java:157)`
  - `	at ExecutorCompletionServiceTest.main(ExecutorCompletionServiceTest.java:184)`

### java/util/concurrent/ExecutorService/ExecutorServiceTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 的 ExecutorService 不扩展 AutoCloseable（版本差异）`
  - `	at ExecutorServiceTest.check(ExecutorServiceTest.java:28)`
  - `	at ExecutorServiceTest.testInterfaceSurface(ExecutorServiceTest.java:53)`
  - `	at ExecutorServiceTest.main(ExecutorServiceTest.java:228)`

### java/util/concurrent/Executors/ExecutorsTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: newFixedThreadPool(0): unexpected message: maximumPoolSize must be positive`
  - `	at ExecutorsTest.checkExc(ExecutorsTest.java:38)`
  - `	at ExecutorsTest.testFactoryNegatives(ExecutorsTest.java:370)`
  - `	at ExecutorsTest.main(ExecutorsTest.java:412)`

### java/util/concurrent/ForkJoinPool/ForkJoinPoolTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 ForkJoinPool does NOT implement ScheduledExecutorService`
  - `	at ForkJoinPoolTest.check(ForkJoinPoolTest.java:24)`
  - `	at ForkJoinPoolTest.testWhiteBoxStructure(ForkJoinPoolTest.java:67)`
  - `	at ForkJoinPoolTest.main(ForkJoinPoolTest.java:409)`

### java/util/concurrent/ForkJoinWorkerThread/ForkJoinWorkerThreadTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 ForkJoinWorkerThread has no getQueuedTaskCount (JDK-9+ only)`
  - `	at ForkJoinWorkerThreadTest.check(ForkJoinWorkerThreadTest.java:17)`
  - `	at ForkJoinWorkerThreadTest.testWhiteBoxStructure(ForkJoinWorkerThreadTest.java:49)`
  - `	at ForkJoinWorkerThreadTest.main(ForkJoinWorkerThreadTest.java:142)`

### java/util/concurrent/Future/FutureTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: exactly 5 declared methods (locked method face)`
  - `	at FutureTest.check(FutureTest.java:20)`
  - `	at FutureTest.testWhiteBoxStructure(FutureTest.java:39)`
  - `	at FutureTest.main(FutureTest.java:140)`

### java/util/concurrent/FutureTask/FutureTaskTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 FutureTask must not expose state()`
  - `	at FutureTaskTest.check(FutureTaskTest.java:23)`
  - `	at FutureTaskTest.testStateMethodAbsentJdk8(FutureTaskTest.java:408)`
  - `	at FutureTaskTest.main(FutureTaskTest.java:430)`

### java/util/concurrent/ScheduledThreadPoolExecutor/ScheduledThreadPoolExecutorTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: STPE(-1) IAE（JDK-8 消息 null）: unexpected message: corePoolSize must be non-negative`
  - `	at ScheduledThreadPoolExecutorTest.checkExc(ScheduledThreadPoolExecutorTest.java:36)`
  - `	at ScheduledThreadPoolExecutorTest.testConstructors(ScheduledThreadPoolExecutorTest.java:287)`
  - `	at ScheduledThreadPoolExecutorTest.main(ScheduledThreadPoolExecutorTest.java:332)`

### java/util/concurrent/ThreadLocalRandom/ThreadLocalRandomTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: nextDouble(0) message <bound must be finite and positive> equals <bound must be positive>`
  - `	at ThreadLocalRandomTest.check(ThreadLocalRandomTest.java:18)`
  - `	at ThreadLocalRandomTest.checkMessage(ThreadLocalRandomTest.java:37)`
  - `	at ThreadLocalRandomTest.testBoundValidation(ThreadLocalRandomTest.java:83)`
  - `	at ThreadLocalRandomTest.main(ThreadLocalRandomTest.java:200)`

### java/util/concurrent/ThreadPoolExecutor/ThreadPoolExecutorTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: corePoolSize<0 IAE: unexpected message: corePoolSize must be non-negative`
  - `	at ThreadPoolExecutorTest.checkExc(ThreadPoolExecutorTest.java:36)`
  - `	at ThreadPoolExecutorTest.testConstructorMatrix(ThreadPoolExecutorTest.java:58)`
  - `	at ThreadPoolExecutorTest.main(ThreadPoolExecutorTest.java:432)`

### java/util/concurrent/TimeUnit/TimeUnitTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: convert(Duration) must be absent in JDK-8`
  - `	at TimeUnitTest.check(TimeUnitTest.java:17)`
  - `	at TimeUnitTest.testAbsentJdk9Methods(TimeUnitTest.java:238)`
  - `	at TimeUnitTest.main(TimeUnitTest.java:253)`

### java/util/concurrent/atomic/AtomicBoolean/AtomicBooleanTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: getPlain must be absent in JDK-8`
  - `	at AtomicBooleanTest.check(AtomicBooleanTest.java:18)`
  - `	at AtomicBooleanTest.testAbsentJdk9Methods(AtomicBooleanTest.java:111)`
  - `	at AtomicBooleanTest.main(AtomicBooleanTest.java:124)`

### java/util/concurrent/atomic/AtomicInteger/AtomicIntegerTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: getPlain must be absent in JDK-8`
  - `	at AtomicIntegerTest.check(AtomicIntegerTest.java:21)`
  - `	at AtomicIntegerTest.testAbsentJdk9Methods(AtomicIntegerTest.java:218)`
  - `	at AtomicIntegerTest.main(AtomicIntegerTest.java:231)`

### java/util/concurrent/atomic/AtomicIntegerArray/AtomicIntegerArrayTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: getPlain must be absent in JDK-8`
  - `	at AtomicIntegerArrayTest.check(AtomicIntegerArrayTest.java:18)`
  - `	at AtomicIntegerArrayTest.testAbsentJdk9Methods(AtomicIntegerArrayTest.java:129)`
  - `	at AtomicIntegerArrayTest.main(AtomicIntegerArrayTest.java:139)`

### java/util/concurrent/atomic/AtomicLong/AtomicLongTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: getPlain must be absent in JDK-8`
  - `	at AtomicLongTest.check(AtomicLongTest.java:21)`
  - `	at AtomicLongTest.testAbsentJdk9Methods(AtomicLongTest.java:208)`
  - `	at AtomicLongTest.main(AtomicLongTest.java:221)`

### java/util/concurrent/atomic/AtomicLongArray/AtomicLongArrayTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: getPlain must be absent in JDK-8`
  - `	at AtomicLongArrayTest.check(AtomicLongArrayTest.java:18)`
  - `	at AtomicLongArrayTest.testAbsentJdk9Methods(AtomicLongArrayTest.java:129)`
  - `	at AtomicLongArrayTest.main(AtomicLongArrayTest.java:139)`

### java/util/concurrent/atomic/AtomicReference/AtomicReferenceTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: getPlain must be absent in JDK-8`
  - `	at AtomicReferenceTest.check(AtomicReferenceTest.java:18)`
  - `	at AtomicReferenceTest.testAbsentJdk9Methods(AtomicReferenceTest.java:114)`
  - `	at AtomicReferenceTest.main(AtomicReferenceTest.java:131)`

### java/util/concurrent/atomic/AtomicReferenceArray/AtomicReferenceArrayTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: getPlain must be absent in JDK-8`
  - `	at AtomicReferenceArrayTest.check(AtomicReferenceArrayTest.java:18)`
  - `	at AtomicReferenceArrayTest.testAbsentJdk9Methods(AtomicReferenceArrayTest.java:139)`
  - `	at AtomicReferenceArrayTest.main(AtomicReferenceArrayTest.java:149)`

### java/util/concurrent/locks/AbstractQueuedLongSynchronizer/AbstractQueuedLongSynchronizerTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: constructor protected on JDK 8`
  - `	at AbstractQueuedLongSynchronizerTest.check(AbstractQueuedLongSynchronizerTest.java:19)`
  - `	at AbstractQueuedLongSynchronizerTest.testWhiteBox(AbstractQueuedLongSynchronizerTest.java:98)`
  - `	at AbstractQueuedLongSynchronizerTest.main(AbstractQueuedLongSynchronizerTest.java:411)`

### java/util/function/Predicate/PredicateTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: static not(Predicate) must be absent in JDK-8`
  - `	at PredicateTest.check(PredicateTest.java:13)`
  - `	at PredicateTest.testContracts(PredicateTest.java:89)`
  - `	at PredicateTest.main(PredicateTest.java:97)`

### java/util/jar/Pack200/Pack200Test.java
- 类别：`spec_removed`
- 差异：compiles on JDK-8 only: 1 error
- 说明：JDK-8 专属 API 在 JDK-27 移除（规范允许）

### java/util/jar/Pack200/Packer/PackerTest.java
- 类别：`spec_removed`
- 差异：compiles on JDK-8 only: 1 error
- 说明：JDK-8 专属 API 在 JDK-27 移除（规范允许）

### java/util/jar/Pack200/Unpacker/UnpackerTest.java
- 类别：`spec_removed`
- 差异：compiles on JDK-8 only: 1 error
- 说明：JDK-8 专属 API 在 JDK-27 移除（规范允许）

### java/util/logging/LoggingMXBean/LoggingMXBeanTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: not deprecated in JDK 8`
  - `	at LoggingMXBeanTest.check(LoggingMXBeanTest.java:18)`
  - `	at LoggingMXBeanTest.testWhiteBox(LoggingMXBeanTest.java:40)`
  - `	at LoggingMXBeanTest.main(LoggingMXBeanTest.java:114)`

### java/util/logging/XMLFormatter/XMLFormatterTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 local date form`
  - `	at XMLFormatterTest.check(XMLFormatterTest.java:17)`
  - `	at XMLFormatterTest.testContracts(XMLFormatterTest.java:79)`
  - `	at XMLFormatterTest.main(XMLFormatterTest.java:92)`

### java/util/stream/Collectors/CollectorsTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: toMap dup message (JDK-8 form): Duplicate key 1 (attempted merging values a and b)`
  - `	at CollectorsTest.check(CollectorsTest.java:30)`
  - `	at CollectorsTest.testContracts(CollectorsTest.java:181)`
  - `	at CollectorsTest.main(CollectorsTest.java:259)`

### java/util/zip/Deflater/DeflaterTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `	at java.base/java.util.zip.Deflater.ensureOpen(Deflater.java:922)`
  - `	at java.base/java.util.zip.Deflater.deflate(Deflater.java:549)`
  - `	at java.base/java.util.zip.Deflater.deflate(Deflater.java:466)`
  - `	at DeflaterTest.lambda$testNullAndBounds$7(DeflaterTest.java:127)`
  - `	at DeflaterTest.checkThrows(DeflaterTest.java:25)`
  - `	... 2 more`

### java/util/zip/Inflater/InflaterTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `	at java.base/java.util.zip.Inflater.ensureOpen(Inflater.java:737)`
  - `	at java.base/java.util.zip.Inflater.inflate(Inflater.java:346)`
  - `	at java.base/java.util.zip.Inflater.inflate(Inflater.java:449)`
  - `	at InflaterTest.lambda$testNullAndBounds$6(InflaterTest.java:112)`
  - `	at InflaterTest.checkThrows(InflaterTest.java:26)`
  - `	... 2 more`

### java/util/zip/ZipFile/ZipFileTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `	at java.base/java.util.zip.ZipFile.<init>(ZipFile.java:205)`
  - `	at java.base/java.util.zip.ZipFile.<init>(ZipFile.java:151)`
  - `	at java.base/java.util.zip.ZipFile.<init>(ZipFile.java:165)`
  - `	at ZipFileTest.lambda$testCtor$2(ZipFileTest.java:62)`
  - `	at ZipFileTest.checkThrows(ZipFileTest.java:32)`
  - `	... 2 more`

### java/nio/Buffer/BufferTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: position is final`
  - `	at BufferTest.check(BufferTest.java:20)`
  - `	at BufferTest.testWhiteBox(BufferTest.java:61)`
  - `	at BufferTest.main(BufferTest.java:325)`

### java/nio/ByteBuffer/ByteBufferTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 has no slice(int,int) (JDK-13+)`
  - `	at ByteBufferTest.check(ByteBufferTest.java:23)`
  - `	at ByteBufferTest.testJdk8Surface(ByteBufferTest.java:646)`
  - `	at ByteBufferTest.main(ByteBufferTest.java:673)`

### java/nio/ByteOrder/ByteOrderTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: ByteOrder extends Object`
  - `	at ByteOrderTest.check(ByteOrderTest.java:17)`
  - `	at ByteOrderTest.testWhiteBox(ByteOrderTest.java:39)`
  - `	at ByteOrderTest.main(ByteOrderTest.java:156)`

### java/nio/DoubleBuffer/DoubleBufferTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: unexpected method slice on JDK-8 DoubleBuffer`
  - `	at DoubleBufferTest.checkAbsent(DoubleBufferTest.java:620)`
  - `	at DoubleBufferTest.testJdk13PlusApiAbsent(DoubleBufferTest.java:624)`
  - `	at DoubleBufferTest.main(DoubleBufferTest.java:167)`

### java/nio/FloatBuffer/FloatBufferTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: unexpected method slice on JDK-8 FloatBuffer`
  - `	at FloatBufferTest.checkAbsent(FloatBufferTest.java:620)`
  - `	at FloatBufferTest.testJdk13PlusApiAbsent(FloatBufferTest.java:624)`
  - `	at FloatBufferTest.main(FloatBufferTest.java:167)`

### java/nio/IntBuffer/IntBufferTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: unexpected method slice on JDK-8 IntBuffer`
  - `	at IntBufferTest.checkAbsent(IntBufferTest.java:621)`
  - `	at IntBufferTest.testJdk13PlusApiAbsent(IntBufferTest.java:625)`
  - `	at IntBufferTest.main(IntBufferTest.java:193)`

### java/nio/LongBuffer/LongBufferTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: unexpected method slice on JDK-8 LongBuffer`
  - `	at LongBufferTest.checkAbsent(LongBufferTest.java:590)`
  - `	at LongBufferTest.testJdk13PlusApiAbsent(LongBufferTest.java:594)`
  - `	at LongBufferTest.main(LongBufferTest.java:162)`

### java/nio/MappedByteBuffer/MappedByteBufferTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 MappedByteBuffer declares no force(int,int) (added in JDK-13+)`
  - `	at MappedByteBufferTest.check(MappedByteBufferTest.java:22)`
  - `	at MappedByteBufferTest.testWhiteBox(MappedByteBufferTest.java:62)`
  - `	at MappedByteBufferTest.main(MappedByteBufferTest.java:246)`

### java/nio/ShortBuffer/ShortBufferTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: unexpected method slice on JDK-8 ShortBuffer`
  - `	at ShortBufferTest.checkAbsent(ShortBufferTest.java:592)`
  - `	at ShortBufferTest.testJdk13PlusApiAbsent(ShortBufferTest.java:596)`
  - `	at ShortBufferTest.main(ShortBufferTest.java:164)`

### java/nio/channels/DatagramChannel/DatagramChannelTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: connected send to other address -> IllegalArgumentException (JDK-8 impl): expected java.lang.IllegalArgumentException got java.nio.channels.AlreadyConnectedException`
  - `	at DatagramChannelTest.checkThrows(DatagramChannelTest.java:35)`
  - `	at DatagramChannelTest.testConnectSemantics(DatagramChannelTest.java:195)`
  - `	at DatagramChannelTest.main(DatagramChannelTest.java:291)`

### java/nio/channels/FileChannel/FileChannelTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8: no map(MapMode,long,long,Arena) overload (JDK-19+ only)`
  - `	at FileChannelTest.check(FileChannelTest.java:31)`
  - `	at FileChannelTest.testWhiteBoxOverloads(FileChannelTest.java:233)`
  - `	at FileChannelTest.main(FileChannelTest.java:767)`

### java/nio/channels/FileLock/FileLockTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.NullPointerException: Null channel`
  - `	at java.base/java.util.Objects.requireNonNull(Objects.java:246)`
  - `	at java.base/java.nio.channels.FileLock.<init>(FileLock.java:151)`
  - `	at FileLockTest$StubLock.<init>(FileLockTest.java:52)`
  - `	at FileLockTest.testStubContract(FileLockTest.java:127)`
  - `	at FileLockTest.main(FileLockTest.java:236)`

### java/nio/channels/OverlappingFileLockException/OverlappingFileLockExceptionTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.nio.channels.OverlappingFileLockException`
  - `	at java.base/sun.nio.ch.FileLockTable.checkList(FileLockTable.java:229)`
  - `	at java.base/sun.nio.ch.FileLockTable.add(FileLockTable.java:123)`
  - `	at java.base/sun.nio.ch.FileChannelImpl.tryLock(FileChannelImpl.java:1738)`
  - `	at OverlappingFileLockExceptionTest.testRealTriggerOverlappingLocks(OverlappingFileLockExceptionTest.java:162)`
  - `	at OverlappingFileLockExceptionTest.main(OverlappingFileLockExceptionTest.java:186)`

### java/nio/channels/SocketChannel/SocketChannelTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: write after shutdownOutput -> ClosedChannelException (JDK-8 impl): expected java.nio.channels.ClosedChannelException got java.nio.channels.AsynchronousCloseException`
  - `	at SocketChannelTest.checkThrows(SocketChannelTest.java:41)`
  - `	at SocketChannelTest.testShutdownOutput(SocketChannelTest.java:299)`
  - `	at SocketChannelTest.main(SocketChannelTest.java:454)`

### java/nio/channels/spi/AbstractInterruptibleChannel/AbstractInterruptibleChannelTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8: self-interrupt + end(false) -> ClosedByInterruptException`
  - `	at AbstractInterruptibleChannelTest.check(AbstractInterruptibleChannelTest.java:15)`
  - `	at AbstractInterruptibleChannelTest.testSelfInterruptContract(AbstractInterruptibleChannelTest.java:141)`
  - `	at AbstractInterruptibleChannelTest.main(AbstractInterruptibleChannelTest.java:31)`

### java/nio/channels/spi/AsynchronousChannelProvider/AsynchronousChannelProviderTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 declares the expected constructors, got 1`
  - `	at AsynchronousChannelProviderTest.check(AsynchronousChannelProviderTest.java:17)`
  - `	at AsynchronousChannelProviderTest.testSurfaceContract(AsynchronousChannelProviderTest.java:95)`
  - `	at AsynchronousChannelProviderTest.main(AsynchronousChannelProviderTest.java:35)`

### java/nio/channels/spi/SelectorProvider/SelectorProviderTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 has no openSocketChannel(ProtocolFamily)`
  - `	at SelectorProviderTest.check(SelectorProviderTest.java:19)`
  - `	at SelectorProviderTest.testProtocolFamilyOverloadsContract(SelectorProviderTest.java:129)`
  - `	at SelectorProviderTest.main(SelectorProviderTest.java:54)`

### java/nio/charset/CoderMalfunctionError/CoderMalfunctionErrorTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 does not wrap arbitrary RuntimeException, got java.nio.charset.CoderMalfunctionError: java.lang.ArithmeticException: kaboom`
  - `	at CoderMalfunctionErrorTest.check(CoderMalfunctionErrorTest.java:28)`
  - `	at CoderMalfunctionErrorTest.testWrapScopeJdk8(CoderMalfunctionErrorTest.java:146)`
  - `	at CoderMalfunctionErrorTest.main(CoderMalfunctionErrorTest.java:177)`

### java/nio/charset/CoderResult/CoderResultTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: A3 CoderResult not final on JDK-8`
  - `	at CoderResultTest.check(CoderResultTest.java:23)`
  - `	at CoderResultTest.testWhiteBox(CoderResultTest.java:47)`
  - `	at CoderResultTest.main(CoderResultTest.java:182)`

### java/nio/charset/StandardCharsets/StandardCharsetsTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: A3 JDK-8 has no UTF_32 constants`
  - `	at StandardCharsetsTest.check(StandardCharsetsTest.java:21)`
  - `	at StandardCharsetsTest.testWhiteBoxA3(StandardCharsetsTest.java:208)`
  - `	at StandardCharsetsTest.main(StandardCharsetsTest.java:219)`

### java/nio/charset/spi/CharsetProvider/CharsetProviderTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定/规范进化（反射可访问性、结构面、版本、弃用等，JDK-27 树已处理）
- JDK-27 stderr 尾：
  - `	at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:319)`
  - `	at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:267)`
  - `	at java.base/java.lang.reflect.Constructor.checkCanSetAccessible(Constructor.java:192)`
  - `	at java.base/java.lang.reflect.Constructor.setAccessible(Constructor.java:185)`
  - `	at CharsetProviderTest.testWhiteBox(CharsetProviderTest.java:127)`
  - `	at CharsetProviderTest.main(CharsetProviderTest.java:251)`

### java/nio/file/FileSystem/FileSystemTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 installedProviders has no 'jrt' scheme`
  - `	at FileSystemTest.check(FileSystemTest.java:22)`
  - `	at FileSystemTest.testContractMatrix(FileSystemTest.java:175)`
  - `	at FileSystemTest.main(FileSystemTest.java:190)`

### java/nio/file/FileSystems/FileSystemsTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 has no newFileSystem(Path, Map) (JDK-13+)`
  - `	at FileSystemsTest.check(FileSystemsTest.java:25)`
  - `	at FileSystemsTest.testZipPathFileSystem(FileSystemsTest.java:164)`
  - `	at FileSystemsTest.main(FileSystemsTest.java:175)`

### java/nio/file/Files/FilesTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8: isSameFile distinct nonexistent -> NoSuchFileException: expected java.nio.file.NoSuchFileException but nothing thrown`
  - `	at FilesTest.checkThrows(FilesTest.java:60)`
  - `	at FilesTest.testStoreProbeSameFile(FilesTest.java:979)`
  - `	at FilesTest.main(FilesTest.java:1018)`

### java/nio/file/Path/PathTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 Path has no of(String,String...) (JDK-11+)`
  - `	at PathTest.check(PathTest.java:20)`
  - `	at PathTest.testJdk8NoPathOf(PathTest.java:433)`
  - `	at PathTest.main(PathTest.java:455)`

### java/nio/file/Paths/PathsTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 has no Path.of(String,String...)`
  - `	at PathsTest.check(PathsTest.java:18)`
  - `	at PathsTest.testJdk8NoPathOf(PathsTest.java:142)`
  - `	at PathsTest.main(PathsTest.java:150)`

### java/nio/file/WatchEvent/WatchEventTest.java
- 类别：`divergent`
- 差异：N jdk8=31 jdk27=35; 1 lines only on JDK-8, 1 lines only on JDK-27
- 说明：JDK-27 常量/字段/MBean 增多（N 计数变化）
- 仅 JDK-8 输出：
  - `PASS (31 assertions)`
- 仅 JDK-27 输出：
  - `PASS (35 assertions)`

### java/nio/file/spi/FileSystemProvider/FileSystemProviderTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：表面锁定：JDK-8 断言 JDK-9+ 方法缺失（spec_added 运行时形态）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK-8 has no exists(Path,LinkOption...) (JDK-27 does)`
  - `	at FileSystemProviderTest.check(FileSystemProviderTest.java:41)`
  - `	at FileSystemProviderTest.testShape(FileSystemProviderTest.java:96)`
  - `	at FileSystemProviderTest.main(FileSystemProviderTest.java:331)`

## env — 环境噪声 (9)

### java/lang/instrument/Instrumentation/InstrumentationTest.java
- 类别：`run_error`
- 差异：run rc jdk8=1 jdk27=1
- 说明：需 javaagent 环境，无法直接 java 运行
- JDK-8 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: nested AgentLife.class compiled into test.classes: ./InstrumentationTest$AgentLife.class`
  - `	at InstrumentationTest.check(InstrumentationTest.java:16)`
  - `	at InstrumentationTest.testDeepAgentClosedLoop(InstrumentationTest.java:68)`
  - `	at InstrumentationTest.main(InstrumentationTest.java:221)`
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: JDK 8 Instrumentation has no redefineModule`
  - `	at InstrumentationTest.check(InstrumentationTest.java:16)`
  - `	at InstrumentationTest.testDeepModuleSurfaceAbsent(InstrumentationTest.java:52)`
  - `	at InstrumentationTest.main(InstrumentationTest.java:220)`

### java/util/concurrent/CompletionService/CompletionServiceTest.java
- 类别：`run_error`
- 差异：run rc jdk8=124 jdk27=124
- 说明：双 JDK 30s 超时（非差分，测试自身慢）

### java/util/stream/StreamSupport/StreamSupportTest.java
- 类别：`divergent`
- 差异：run rc jdk8=124 jdk27=0 (one side crashes)
- 说明：JDK-8 侧 30s 超时

### java/nio/channels/Pipe/SinkChannel/SinkChannelTest.java
- 类别：`run_error`
- 差异：run rc jdk8=124 jdk27=124
- 说明：双 JDK 30s 超时

### java/nio/file/attribute/DosFileAttributeView/DosFileAttributeViewTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：依赖文件系统 DOS 属性支持（平台/环境相关）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: FileStore dos-view support (probed)`
  - `	at DosFileAttributeViewTest.check(DosFileAttributeViewTest.java:24)`
  - `	at DosFileAttributeViewTest.testContract(DosFileAttributeViewTest.java:139)`
  - `	at DosFileAttributeViewTest.main(DosFileAttributeViewTest.java:39)`

### java/nio/file/attribute/DosFileAttributes/DosFileAttributesTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：依赖文件系统 DOS 属性支持
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: FileStore dos-view support`
  - `	at DosFileAttributesTest.check(DosFileAttributesTest.java:23)`
  - `	at DosFileAttributesTest.testContract(DosFileAttributesTest.java:155)`
  - `	at DosFileAttributesTest.main(DosFileAttributesTest.java:38)`

### java/nio/file/attribute/GroupPrincipal/GroupPrincipalTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：依赖 OS 用户/组解析（环境相关）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: UPNFE carries requested group name`
  - `	at GroupPrincipalTest.check(GroupPrincipalTest.java:24)`
  - `	at GroupPrincipalTest.testContract(GroupPrincipalTest.java:125)`
  - `	at GroupPrincipalTest.main(GroupPrincipalTest.java:39)`

### java/nio/file/attribute/UserPrincipalLookupService/UserPrincipalLookupServiceTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：依赖 OS 用户解析（环境相关）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: UPNFE carries requested name`
  - `	at UserPrincipalLookupServiceTest.check(UserPrincipalLookupServiceTest.java:23)`
  - `	at UserPrincipalLookupServiceTest.testContract(UserPrincipalLookupServiceTest.java:108)`
  - `	at UserPrincipalLookupServiceTest.main(UserPrincipalLookupServiceTest.java:38)`

### java/nio/file/attribute/UserPrincipalNotFoundException/UserPrincipalNotFoundExceptionTest.java
- 类别：`divergent`
- 差异：run rc jdk8=0 jdk27=1 (one side crashes)
- 说明：依赖 OS 用户解析（环境相关）
- JDK-27 stderr 尾：
  - `Exception in thread "main" java.lang.AssertionError: service UPNFE carries requested name`
  - `	at UserPrincipalNotFoundExceptionTest.check(UserPrincipalNotFoundExceptionTest.java:15)`
  - `	at UserPrincipalNotFoundExceptionTest.testContract(UserPrincipalNotFoundExceptionTest.java:105)`
  - `	at UserPrincipalNotFoundExceptionTest.main(UserPrincipalNotFoundExceptionTest.java:28)`

## 分类统计

- `consistent`: 647
- `divergent`: 168
- `spec_added`: 1
- `spec_removed`: 7
- `run_error`: 3
- `both_compile_fail`: 0
- `skipped`: 3
