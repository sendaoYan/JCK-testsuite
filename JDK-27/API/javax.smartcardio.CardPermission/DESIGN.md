# javax.smartcardio.CardPermission 设计案

## 1. API 功能
- 构造器：`CardPermission(String terminalName, String actions)`；`terminalName` 可为 `*`（所有终端），`actions` 为逗号分隔动作或 `*`。
- 动作集合：`connect/reset/exclusive/transmitControl/getBasicChannel/openLogicalChannel`。
- 查询：`getName()`（继承 Permission）、`getActions()`（规范化：`*` 或按词法排序逗号连接）。
- 语义：`implies(Permission)`（actions 子集 + 名称匹配/`*`）、`equals/hashCode`；继承 `java.security.Permission`。

## 2. Java 用户如何使用
- `SecurityManager`/`AccessController.checkPermission(new CardPermission("*", "connect"))` 控制读卡器访问。
- `getActions()` 获取规范化动作串用于策略文件。
- `implies()` 判断一个权限是否被另一个覆盖。

## 3. 开源代码实际用法调研
来源：JDK 自带测试 `test/jdk/javax/smartcardio/TestCardPermission.java`。
- 规范化用例：`"Reset,coNnect"` → `"connect,reset"`；`"exclusive,*,connect"` → `"*"`；全 6 动作 → `"*"`；`null` → `null`。
- 非法动作：`""`、`"foo"`、`"connect, reset"`、`"connect,,reset"`、`"connect,"`、`",connect"` 均抛 `IllegalArgumentException`。
- `implies`：非 CardPermission → false；非子集 → false；`*` 名称覆盖任意终端；同名子集 → true；不同名 → false。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`CardPermissionTest.testBlackBox`）：
- 名称/动作获取与规范化（含 `null` 动作、`*` 折叠）。
- `implies` 语义：`*` 名称、子集动作、同名、不同名、非 CardPermission。
- `equals/hashCode`：同动作同名相等，不同动作/名称不相等，非 CardPermission 不相等。
- 异常：null 名称 NPE；空串/非法动作/逗号边界/空格动作 IAE。
