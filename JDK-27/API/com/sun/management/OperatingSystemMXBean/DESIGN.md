# com.sun.management.OperatingSystemMXBean 测试设计

## 1. API 功能

- 接口：`com.sun.management.OperatingSystemMXBean extends java.lang.management.OperatingSystemMXBean`。
- 内存：`getCommittedVirtualMemorySize()`、`getTotalMemorySize()`、`getFreeMemorySize()`；default 别名 `getTotalPhysicalMemorySize()`（= getTotalMemorySize）、`getFreePhysicalMemorySize()`（= getFreeMemorySize）。
- Swap：`getTotalSwapSpaceSize()`、`getFreeSwapSpaceSize()`。
- CPU：`getProcessCpuTime()`（纳秒）、`getCpuLoad()`（系统 CPU 负载，[0,1]，不可用返回负值）、`getProcessCpuLoad()`（进程 CPU 负载）、已弃用 default `getSystemCpuLoad()`（= getCpuLoad()）。
- 继承：`getAvailableProcessors()`、`getSystemLoadAverage()`（不可用返回负值）。

## 2. Java 用户如何使用

`ManagementFactory.getOperatingSystemMXBean()` 返回的 bean 在 HotSpot 上即实现该接口，可直接 cast 后读取各类系统指标。组合链路：

```
OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
long total = os.getTotalMemorySize(); long free = os.getFreeMemorySize();
long committed = os.getCommittedVirtualMemorySize();
double cpu = os.getCpuLoad(); double proc = os.getProcessCpuLoad();
int cpus = os.getAvailableProcessors();
```

## 3. 开源代码实际用法调研

来源 `~/git/jdk25u-dev`（源码 + test/jdk，WebSearch 不可用则以此为准）。

- `~/git/jdk25u-dev/test/jdk/com/sun/management/OperatingSystemMXBean/GetSystemCpuLoad.java`：循环读 `getSystemCpuLoad()`，断言落在 `[0.0, 1.0]`（Windows 上首次可返回 -1，测试里做了平台特判）。
- `~/git/jdk25u-dev/test/jdk/com/sun/management/OperatingSystemMXBean/GetProcessCpuLoad.java`：读 `getProcessCpuLoad()`，断言 `[0,1]`（Windows 偶发 -1 特判）。
- `~/git/jdk25u-dev/test/jdk/com/sun/management/OperatingSystemMXBean/GetCommittedVirtualMemorySize.java`、`GetFreePhysicalMemorySize.java`、`GetTotalPhysicalMemorySize.java`、`TestTotalSwap.java`：分别断言对应 getter 的非负/合理范围。
- `~/git/jdk25u-dev/src/jdk.management/share/classes/com/sun/management/OperatingSystemMXBean.java`：`getSystemCpuLoad()` default 委托 `getCpuLoad()`；`getFreePhysicalMemorySize()`/`getTotalPhysicalMemorySize()` 委托内存 getter；CPU load 不可用时返回负值。

## 4. 测试用例设计 + 自动化实现

对应 `OperatingSystemMXBeanTest.java` 黑盒部分：

1. `instanceof com.sun.management.OperatingSystemMXBean`。
2. 内存：`committed>=0`、`total>0`、`free>=0`、`free<=total`；物理内存别名 `totalPhys>0`、`freePhys>=0`、`freePhys<=totalPhys`。
3. Swap：`totalSwap>=0`、`freeSwap>=0`、`freeSwap<=totalSwap`。
4. CPU：`getProcessCpuTime()>=0`；`getSystemCpuLoad()/getCpuLoad()/getProcessCpuLoad()` 用容差辅助断言（NaN 或 `<=1.0`，兼容首次/不可用返回负值）。
5. 继承指标：`getAvailableProcessors()>=1`、`getSystemLoadAverage()>=-1`。
