# com.sun.management.UnixOperatingSystemMXBean 测试设计

## 1. API 功能

- 接口：`com.sun.management.UnixOperatingSystemMXBean extends com.sun.management.OperatingSystemMXBean`。
- `getOpenFileDescriptorCount()`：当前进程已打开的文件描述符数。
- `getMaxFileDescriptorCount()`：当前进程可打开的文件描述符上限。
- 继承 `OperatingSystemMXBean` 的 CPU/内存/swap 指标。

## 2. Java 用户如何使用

Linux/Unix 上 `ManagementFactory.getOperatingSystemMXBean()` 返回的 bean 同时实现该接口，cast 后读取 fd 使用情况与资源指标。组合链路：

```
if (ManagementFactory.getOperatingSystemMXBean() instanceof UnixOperatingSystemMXBean u) {
    long open = u.getOpenFileDescriptorCount();
    long max = u.getMaxFileDescriptorCount();
    long mem = u.getTotalMemorySize();
}
```

## 3. 开源代码实际用法调研

来源 `~/git/jdk25u-dev`（源码 + test/jdk，WebSearch 不可用则以此为准）。

- `~/git/jdk25u-dev/test/jdk/com/sun/management/UnixOperatingSystemMXBean/GetOpenFileDescriptorCount.java`：cast 后断言 `getOpenFileDescriptorCount() >= 0`。
- `~/git/jdk25u-dev/test/jdk/com/sun/management/UnixOperatingSystemMXBean/GetMaxFileDescriptorCount.java`：断言 `getMaxFileDescriptorCount() > 0`。
- `~/git/jdk25u-dev/src/jdk.management/share/classes/com/sun/management/UnixOperatingSystemMXBean.java`：仅新增两个 fd getter，其余继承 OperatingSystemMXBean。

## 4. 测试用例设计 + 自动化实现

对应 `UnixOperatingSystemMXBeanTest.java` 黑盒部分：

1. `instanceof` 防御：非 Unix 平台打印 NOTE 并跳过 Unix 专属断言（本机 Linux 走真分支）。
2. fd：`open >= 0`、`max > 0`、`open <= max`。
3. 继承指标通过 Unix bean 可调用：`getCommittedVirtualMemorySize()>=0`、`getTotalMemorySize()>0`、`getFreeMemorySize()>=0`、`getProcessCpuTime()>=0`、`getTotalSwapSpaceSize()>=0`、`getFreeSwapSpaceSize()>=0`。
