# java.beans.PropertyEditorManager — 设计案（JDK-8）

## 一、API 功能
编辑器注册表（全静态）：`registerEditor(targetType, editorClass)`（editorClass=null 清除注册）、`findEditor(targetType)`（null 表示无）、`get/setEditorSearchPath`。
查找顺序（实现语义）：① 显式注册表（预置 byte/short/int/long/boolean/float/double 七个 primitive）→ ② 目标类型全限定名+Editor 同包类 → ③ 搜索路径包 + 简单名 + Editor（默认路径 `sun.beans.editors`，实际映射到 com.sun.beans.editors）→ ④ 枚举兜底 EnumEditor。注册表与搜索路径为 JVM 全局（按 ThreadGroupContext 隔离）。

## 二、使用方式
`PropertyEditorManager.registerEditor(MyType.class, MyEditor.class)` 后 `findEditor` 每次返回 MyEditor **新实例**；改搜索路径可注入按命名约定的编辑器包。

## 三、参考来源
- Oracle Java SE 8 javadoc：java.beans.PropertyEditorManager（三级查找、"editorClass 为 null 取消注册"、Sun 实现默认搜索路径 {"sun.beans.editors"}）。
- JavaBeans 规范 1.01-A 第 9.3 节。
- OpenJDK 回归思路：`test/jdk/java/beans/PropertyEditor/Test7087876.java`（注册/查找）、`Test6397609`、`6380849/`；JDK 源 com/sun/beans/finder/{PropertyEditorFinder,InstanceFinder}.java（8u372 src.zip 逐行核实）。测试代码自写。

## 四、测试用例设计 + 易错规范细节
覆盖（68 断言）。**全局状态纪律**：只用本文件独有的嵌套类型注册，测后一律 `registerEditor(type, null)` 清除并恢复默认搜索路径（try/finally）。
1. **testWhiteBox**：非接口、恰 4 个 public 方法且全部 static。
2. **testDefaultEditorMatrix**：默认搜索路径恰为 {"sun.beans.editors"}（双侧实证一致）；8 个数值/布尔包装类默认编辑器非空；Character/void/Object/自定义类型 null；primitive int/boolean 有（注册表预置）且可用（"42"/"True" 文本）；wrapper 与 primitive 两次查找得到不同实例。
3. **testLegacyRegistrationAndSearchPath**：保留旧版检查（String→StringEditor 注册查找、搜索路径非空、Custom 注册/清除、{"pkg1","pkg2"} set/get），随后**恢复全局状态**并断言清除注册后默认 String 编辑器回归（且不再是 StringEditor）。
4. **testFreshInstancePerFind**：两次 findEditor 返回不同对象、同类。
5. **testRegisterLifecycle**：未注册 null → 注册 Ed1 可查且每次新实例 → 覆盖注册 Ed2 → null 清除 → 再注册 → 再清除。
6. **testPrimitiveRegistration**：char.class 默认无 → 注册 Ed1 有 → 清除后无。
7. **testSearchPathSemantics**：get 返回防御性克隆（改动无效）；set 克隆输入；**setEditorSearchPath(null) → 空数组（长度 0），不是恢复默认**；恢复默认后 equals {"sun.beans.editors"}。
8. **testEmptyPathHidesSearchPathDefaults**（易错，证明两段式查找）：空路径下 String/Short 默认编辑器消失，而 int.class（注册表预置）仍在、显式注册仍可用；恢复路径后默认编辑器回归。
9. **testEnumFallback**：枚举免注册得编辑器，tags 为常量名，setAsText 往返。
10. **testNullHandling**：`findEditor(null)` → **NullPointerException**（双侧一致，getEnumConstants 触发）；`registerEditor(null, Ed1)` **不抛**且 findEditor(null) 返回该编辑器（实现行为，finally 中 registerEditor(null,null) 清除）；清除后 findEditor(null) 再次 NPE。

### 易错规范细节（实证记录）
- 注册表是 JVM 全局且 WeakCache 允许 null key 注册——测试必须 finally 清理；JDK 内建类型只做"注册 String 后清除"且测后断言默认编辑器回归。
- `registerEditor(type, null)` 是"清除显式注册"，**不会**让搜索路径默认编辑器消失（String 清除后仍能从路径找到 StringEditor）。
- setEditorSearchPath(null) → EMPTY 而非默认路径：恢复需显式 set {"sun.beans.editors"}。
- findEditor(null) NPE（双侧同、消息不同）；registerEditor(null, ed) 合法（记录性行为）。
- headless 下 findEditor(Color/Font) 因编辑器是 AWT 组件而实例化失败 → null（依赖 headless，不断言，仅记录）。
