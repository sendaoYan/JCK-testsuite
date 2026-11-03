# com.sun.management.HotSpotDiagnosticMXBean 测试设计

## 1. API 功能

- 接口：`com.sun.management.HotSpotDiagnosticMXBean extends java.lang.management.PlatformManagedObject`。
- `dumpHeap(String outputFile, boolean live)`：把堆导出为 hprof；outputFile 必须以 `.hprof` 结尾，否则 `IllegalArgumentException`；文件已存在/不可写抛 IOException。
- `getDiagnosticOptions()`：返回全部诊断（可写）VM 选项的 `List<VMOption>`。
- `getVMOption(String name)`：按名取 VM 选项；不存在抛 `IllegalArgumentException`，name 为 null 抛 NPE。
- `setVMOption(String name, String value)`：动态改可写 VM 选项；不可写/非法值抛 IllegalArgumentException。
- `dumpThreads(String, ThreadDumpFormat)`：JDK-21 新增 default 方法，默认抛 `UnsupportedOperationException`（本组不测，仅做白盒方法存在断言）。

## 2. Java 用户如何使用

通过 `ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class)` 获取，做诊断：读选项、临时改选项、导 heap dump。组合链路：

```
HotSpotDiagnosticMXBean diag = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
List<VMOption> opts = diag.getDiagnosticOptions();
VMOption o = diag.getVMOption("HeapDumpOnOutOfMemoryError");
String old = o.getValue();
diag.setVMOption("HeapDumpOnOutOfMemoryError", "true");
diag.getVMOption("HeapDumpOnOutOfMemoryError").getValue(); // 验证
diag.setVMOption("HeapDumpOnOutOfMemoryError", old);       // 恢复
diag.dumpHeap("/tmp/x.hprof", true);
```

## 3. 开源代码实际用法调研

来源 `~/git/jdk25u-dev`（源码 + test/jdk，WebSearch 不可用则以此为准）。

- `~/git/jdk25u-dev/test/jdk/com/sun/management/HotSpotDiagnosticMXBean/GetVMOption.java`：`getVMOption("HeapDumpOnOutOfMemoryError")` 断言值；对 `getVMOption("BadOption")` 断言抛 `IllegalArgumentException`。
- `~/git/jdk25u-dev/test/jdk/com/sun/management/HotSpotDiagnosticMXBean/SetVMOption.java`：对可写选项 `HeapDumpOnOutOfMemoryError` 用 `setVMOption` 改值并断言 `getVMOption` 后 origin 变为 `MANAGEMENT`、值与 writeable 匹配；并校验 `getDiagnosticOptions()` 的内容。
- `~/git/jdk25u-dev/test/jdk/com/sun/management/HotSpotDiagnosticMXBean/DumpHeap.java`：`dumpHeap(path, true)` 写文件并解析校验（HprofParser）。
- `~/git/jdk25u-dev/src/jdk.management/share/classes/com/sun/management/HotSpotDiagnosticMXBean.java`：dumpHeap 要求 `.hprof` 后缀；dumpThreads 默认抛 UnsupportedOperationException。

## 4. 测试用例设计 + 自动化实现

对应 `HotSpotDiagnosticMXBeanTest.java` 黑盒部分：

1. `getDiagnosticOptions()` 非空，遍历断言每个 VMOption 的 name/value/origin 非 null，并确认包含已知可写选项 `HeapDumpOnOutOfMemoryError`。
2. `getVMOption("PrintGC")` 非 null 且 `getName().equals("PrintGC")`（注意：本机 PrintGC 实测 `isWriteable()==false`，因此不用它测 set）。
3. `getVMOption` 对不存在的选项抛 `IllegalArgumentException`。
4. `setVMOption` 用真正可写的 `HeapDumpOnOutOfMemoryError`：读原值 → 设为目标值（true/false 取反）→ `getVMOption` 验证 → `finally` 恢复原值（无全局副作用）。
5. `dumpHeap`：`Files.createTempDirectory` 独立目录 + `.hprof` 路径 → 断言文件存在且 size>0 → `finally` 递归清理。
