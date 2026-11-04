# javax.swing.plaf.multi — DESIGN

## 1. API 功能
多路复用外观。`MultiLookAndFeelContractTest`：元数据、createUIs 多路收集语义、uisToArray 转换、30 个 MultiXxxUI 族形。

## 2. Java 用户如何使用
`MultiLookAndFeel.createUIs(mui, uis, target)` 静态工厂：取默认 LaF 委托放入 uis，再逐个辅助 LaF 收集；仅一个时直接返回默认委托（不复用 Multi 包装）。`MultiXxxUI.getUIs()` 把收集的 Vector 转数组（空→null）。

## 3. 开源用法调研
- `jdk8u-dev/.../multi/MultiLookAndFeel.java:222`：createUIs 实现——ui==null 返回 null；uis.size()==1 时返回默认委托本体。
- `MultiLookAndFeel.java:271`：uisToArray——null 向量→空数组、空向量→null、非空→等长数组。
- `MultiButtonUI.java:50`：`protected Vector uis = new Vector()`（声明处初始化）。
- 实测：把 MultiLookAndFeel 安装为当前 LaF 会因委托初始化失败（"Multiplexing LAF: createUI() failed"）——测试不得走安装路径。

## 4. 测试用例设计 + 自动化实现（142 断言）
- 元数据：name "Multiplexing Look and Feel"、id "Multiplex"、描述精确、supported true、native false、无窗口装饰、getDefaults 非 null、toString 含描述。
- createUIs 收缩语义（Metal 为默认）：新按钮 → uis.size()==1 且返回值==默认委托本体且非 Multi 包装；重复调用独立；JLabel → LabelUI 委托。
- getUIs：未收集时 null（文档化怪癖）；收集 1 个后经自身 uis 向量（子类暴露 protected 字段）返回 1 元素数组且恒等。
- uisToArray（子类暴露 protected static）：null→长度 0；空→null；两元素保序拷贝。
- 族形（30 个 Multi*UI，反射）：具体类、extends 对应基类（ButtonUI…ViewportUI）、静态 createUI(JComponent)、getUIs() 数组返回；MultiButtonUI.createUI(按钮) 收缩到 MetalButtonUI。

## 5. 易错规范细节
- JDK-8/27 multi 包均无 MultiCheckBoxMenuItemUI/MultiRadioButtonMenuItemUI（MenuItem 一个覆盖全部菜单项）。
- uisToArray(空) 返回 null 而非空数组、(null) 返回空数组——两者相反，分别断言。
- 直接 new MultiButtonUI().getUIs() 因 uis 已初始化返回 null（非 NPE）；NPE 出现在外部代码 `.length` 解引用时。
