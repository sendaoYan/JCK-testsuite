# jdk.dynalink.linker.support.CompositeTypeBasedGuardingDynamicLinker 设计案

## 1. API 功能
组合类型链接器：canLinkType + optimize。

## 2. Java 用户如何使用
new CompositeTypeBasedGuardingDynamicLinker(list)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/support/CompositeTypeBasedGuardingDynamicLinker.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 canLinkType + optimize。
## 5. 契约深化（JDK-27）
- canLinkType 是各委托的 OR；canLinkType-false 的委托对相应接收者类型完全跳过（观察 canLinkType 调用序列）。
- optimize 把混合列表折叠成单元素列表（1 个组合器）；单个 type-based linker 保持不包装。
