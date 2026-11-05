# java.lang.management.ManagementFactory 测试设计案（JDK-8）

> 覆盖目标：完整契约。每个单例 MXBean 访问器非 null、MemoryMXBean 堆/非堆使用量与 pending finalization、
> RuntimeMXBean name（pid@hostname）/uptime/startTime/inputArguments/systemProperties、OperatingSystemMXBean
> name/arch/processors、ThreadMXBean 计数/CPU 时间、ClassLoadingMXBean 计数、CompilationMXBean 守卫 null、
> 内存池/GC/管理器 bean 非空且值界、平台 MBeanServer + 接口集、getPlatformMXBean(s)、MemoryType 枚举、
> MemoryUsage 值不变量。全部确定性（仅非 null/非空/非负/一致性断言，不依赖时钟/随机）。

## 1. API 功能

- 单例 getter：getMemoryMXBean/getThreadMXBean/getRuntimeMXBean/getClassLoadingMXBean/getOperatingSystemMXBean。
- 集合：getMemoryPoolMXBeans/getMemoryManagerMXBeans/getGarbageCollectorMXBeans。
- 平台：getPlatformMBeanServer/getPlatformManagementInterfaces/getPlatformMXBean(s)。

## 2. 用户如何使用

- `ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()` 读堆使用量（MemoryUsage）。
- `getPlatformMBeanServer()` 提供 JMX 平台 MBean 的 MBeanServer。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/management/ManagementFactory.html）。
- OpenJDK 8 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/management/ManagementFactory.java`。
- JCK runtime-8b `tests/api/java_lang/management/ManagementFactory/`。

## 4. 测试用例设计（映射到 ManagementFactoryTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 单例访问器非 null | testSingletons |
| P0 | MemoryMXBean 堆/非堆/pending | testMemoryMXBean |
| P0 | RuntimeMXBean name/uptime/startTime/props | testRuntimeMXBean |
| P0 | OperatingSystemMXBean name/arch/processors | testOperatingSystemMXBean |
| P0 | ThreadMXBean 计数/CPU 时间 | testThreadMXBean |
| P0 | ClassLoadingMXBean 计数 | testClassLoadingMXBean |
| P1 | CompilationMXBean 守卫 null | testCompilationMXBean |
| P0 | 池/GC/管理器 bean 非空 + 值界 | testPoolAndGCBeans |
| P0 | getPlatformMXBean(s)/interfaces | testPlatformMXBeans |
| P1 | MemoryType 枚举 | testMemoryType |
| P1 | MemoryUsage 值不变量 | testMemoryUsageInvariants |

## 易错规范细节（已锁入断言）

- `MemoryType.HEAP.toString()` 为 "Heap memory"（非 "HEAP"）；用 name() 断言枚举名。
- `getPlatformManagementInterfaces()` 返回 `Set<Class<? extends PlatformManagedObject>>`。
- heap/non-heap `committed>=used`；`max>=-1`（-1 表示未指定）；`getObjectPendingFinalizationCount()>=0`。
- `getCompilationMXBean()` 可能为 null（无 JIT），需守卫。
- 断言数随 JVM 内存池/GC 数量变化（JDK-8 109 条，JDK-27 123 条）。

## 5. 契约深化（本批）
- 单例身份矩阵：getMemoryMXBean/getThreadMXBean/getRuntimeMXBean/... 重复调用均返回同一实例。
- getPlatformMBeanServer() 单例身份 + 默认域 "DefaultDomain"；平台 bean 均已注册。
- ObjectName 精确表：全部平台 bean 的 canonical ObjectName 逐一锁定。
- 代理闭环：newPlatformMXBeanProxy(server, "java.lang:type=Threading", ThreadMXBean.class) 后 getThreadCount() 等方法与本地 bean 返回值一致（跨 MBeanServer 真值对照）。
- 代理负例矩阵：错误 ObjectName 类型（值不匹配 bean）/非法名 → 相应运行时异常精确。
- 列表交叉引用：getMemoryManagerMXBeans 名称集 ⊇ GarbageCollectorMXBeans 名称集；pool.getMemoryManagerNames() 与 manager.getMemoryPoolNames() 双向一致。
