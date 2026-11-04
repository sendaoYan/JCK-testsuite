# com.sun.management.VMOption 测试设计

## 1. API 功能

- 类：`com.sun.management.VMOption`，描述一个 VM 选项的当前值、来源与可写性。
- 构造器：`VMOption(String name, String value, boolean writeable, VMOption.Origin origin)`（源码未做 null 检查，name/value 可传 null，不抛异常）。
- getters：`getName()`、`getValue()`、`getOrigin()`、`isWriteable()`。
- `toString()`：固定格式 `"VM option: <name> value: <value>  origin: <origin>  (read-write|read-only)"`。
- 静态 `from(CompositeData)`：`from(null)` 返回 null。
- 嵌套枚举 `VMOption.Origin`：`DEFAULT / VM_CREATION / ENVIRON_VAR / CONFIG_FILE / MANAGEMENT / ERGONOMIC / ATTACH_ON_DEMAND / OTHER`（共 8 个）。

## 2. Java 用户如何使用

`HotSpotDiagnosticMXBean.getDiagnosticOptions()` / `getVMOption(name)` 返回 VMOption，读取 name/value/origin/isWriteable 决定是否/如何 set。也可直接 new 用于测试或本地封装。组合链路：

```
VMOption o = new VMOption("PrintGC", "false", true, VMOption.Origin.DEFAULT);
o.getName(); o.getValue(); o.getOrigin(); o.isWriteable(); o.toString();
for (VMOption vo : diag.getDiagnosticOptions()) { vo.getName(); vo.getOrigin(); vo.isWriteable(); }
```

## 3. 开源代码实际用法调研

来源 `~/git/jdk25u-dev`（源码 + test/jdk，WebSearch 不可用则以此为准）。

- `~/git/jdk25u-dev/src/jdk.management/share/classes/com/sun/management/VMOption.java`：`toString()` 固定格式；构造器直接赋值、无 null 检查；`from(null)` 返回 null，本地 CompositeData 走快路径。
- `~/git/jdk25u-dev/test/jdk/com/sun/management/HotSpotDiagnosticMXBean/SetVMOption.java`：遍历 `getDiagnosticOptions()` 的 VMOption，用 `getName()`/`isWriteable()`/`getOrigin()` 做断言，并验证 set 后 origin 变为 `MANAGEMENT`。
- `~/git/jdk25u-dev/test/jdk/com/sun/management/VMOptionOpenDataTest.java`：校验 VMOption 的开放数据/toString 行为。

## 4. 测试用例设计 + 自动化实现

对应 `VMOptionTest.java` 黑盒部分：

1. 构造 + getters：`new VMOption("PrintGC","false",true,DEFAULT)` 断言 name/value/isWriteable/origin。
2. `toString()`：包含 name、value、`(read-write)`；read-only 实例含 `(read-only)`。
3. Origin 枚举：`values()` 长度 8 且顺序为 `DEFAULT..OTHER`；每个 `valueOf(name)` 往返一致。
4. `from(null) == null`。
5. 真实 VMOption：遍历 `HotSpotDiagnosticMXBean.getDiagnosticOptions()`，断言 name 非空、value/origin 非 null。
