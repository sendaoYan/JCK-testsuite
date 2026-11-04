# java.beans.PropertyEditor — 设计案（JDK-8）

## 一、API 功能
属性编辑器接口（12 个方法）：setValue/getValue、getAsText/setAsText（声明 IllegalArgumentException）、getTags、getCustomEditor/supportsCustomEditor、isPaintable/paintValue、getJavaInitializationString、add/removePropertyChangeListener。

## 二、使用方式
属性表/IDE 按属性类型经 PropertyEditorManager.findEditor 取得编辑器，走 文本(getAsText/setAsText)、标签(getTags)、自定义组件(getCustomEditor)、代码生成(getJavaInitializationString) 四条通道之一。

## 三、参考来源
- Oracle Java SE 8 javadoc：java.beans.PropertyEditor（接口契约：setAsText 抛 IllegalArgumentException、"非 null 返回则应可被 setAsText 解析回"）。
- JavaBeans 规范 1.01-A 第 7 章（PropertyEditor）与第 9.3 节（PropertyEditorManager 交互）。
- OpenJDK 回归思路：`test/jdk/java/beans/PropertyEditor/Test*{Class,Type}{Java,Null,Value}.java`（各内建编辑器 getJavaInitializationString 的 null/值/类名三态）、`Test4968709`、`Test6380849`、`TestEnumClass*`。测试代码自写。

## 四、测试用例设计 + 易错规范细节
覆盖（88 断言）：
1. **testInterfaceInventory**（白盒）：恰 12 个方法；逐一断言签名（返回类型 + 形参类型：paintValue(Graphics,Rectangle)、setAsText(String) 声明 IllegalArgumentException、getTags 返回 String[]、getCustomEditor 返回 Component 等）；PropertyEditorManager 不是 PropertyEditor。
2. **testSupportImplementor**：以 PropertyEditorSupport 兑现接口基本契约（setValue/getValue 恒等、getAsText、默认 flags、"???"、paintValue(null,null) 无操作）。
3. **testIntegerEditorChain**：findEditor(Integer.class)（实现为 com.sun.beans.editors.IntegerEditor，断言行为不断言类名）——42/“42” 文本往返；setAsText("7")→7；init string 为字面量 "7"；**decode 语义（"0x1F"→31）**；setAsText(null) → 值 null、getAsText null、init "null"；垃圾文本 → **NumberFormatException（IllegalArgumentException 子类，实测精确类）**；getTags null、无自定义编辑、不可绘制。
4. **testEditorIndependenceAndEvents**：两次 findEditor 返回独立实例互不串值；PCL：setValue 每次都 fire（含同值）、事件 name/old/new 均为 null、source 为编辑器自身、remove 后停止、重复 remove 无害。
5. **testBooleanEditorChain**：getAsText 首字母大写 "True"/"False"；getTags {"True","False"}；setAsText 大小写不敏感；setAsText(null) 清值；非法文本 → IllegalArgumentException；init string "true"/"false"/"null"。
6. **testStringEditorChain**：getAsText 原样；init string 加引号并转义 `\"`、`\\`、`\n`；StringEditor.setAsText 无条件生效（值 null 也可设）。
7. **testEnumEditorChain**：枚举类型免注册获得编辑器；getTags 为常量名；setAsText("GREEN") 往返；setValue(非枚举) → IllegalArgumentException；setValue(null) 合法。

### 易错规范细节（实证记录）
- 默认编辑器矩阵（双侧一致）：Integer/String/Boolean/Long/Double/Float/Byte/Short 与对应 primitive 有；**Character/char/void 无**；枚举兜底 EnumEditor；java.awt.Color/Font 的编辑器（ColorEditor/FontEditor 是 AWT Panel 子类）在 headless 下实例化抛 HeadlessException 被吞 → findEditor 返回 null——**该结果依赖 headless 状态，故不做断言，仅记录**。
- BooleanEditor 文本为 "True"/"False"（非 "true"/"false"），解析 equalsIgnoreCase——大小写不敏感但展示恒定。
- IntegerEditor 走 `Integer.decode`：接受 0x/0#/# 前缀与负号——比 parseInt 宽。
- 基于 PropertyEditorSupport 的内建编辑器 fire 事件不带 old/new（恒 null）；EnumEditor 才带真实 old/new 且同值不 fire（实现差异，未在接口层断言）。
