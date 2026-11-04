# javax.swing.plaf.basic 常量与小型数据契约 — DESIGN

## 1. API 功能
`BasicConstantsContractTest` 聚合 basic 包的确定性数据：BasicSliderUI 方向常量、BasicScrollBarUI 高亮常量（protected，经子类暴露）、BasicComboBoxRenderer/Editor、BasicIconFactory、BasicArrowButton、BasicInternalFrameTitlePane 构造。

## 2. Java 用户如何使用
滑块/滚动条 UI 内部用方向常量区分滚动语义；组合框渲染器/编辑器可直接换装：`comboBox.setRenderer(new BasicComboBoxRenderer())`；BasicArrowButton 是四个方向的独立按钮组件。

## 3. 开源用法调研
- `jdk8u-dev/.../basic/BasicSliderUI.java:50-53`：POSITIVE_SCROLL=+1、NEGATIVE_SCROLL=-1、MIN_SCROLL=-2、MAX_SCROLL=+2。
- `jdk8u-dev/.../basic/BasicScrollBarUI.java`：NO_HIGHLIGHT=0、DECREASE_HIGHLIGHT=1、INCREASE_HIGHLIGHT=2（protected static final）。
- `jdk8u-dev/.../basic/BasicComboBoxEditor.java`：getEditorComponent 返回可编辑 JTextField；Enter 触发 ActionEvent。
- `jdk8u-dev/.../basic/BasicIconFactory.java`：menuArrow 4x8、menuItemCheck 9x9、menuItemArrow 4x8，共享单例。

## 4. 测试用例设计 + 自动化实现（42 断言）
- SliderUI 四常量精确值 + 全序（MIN<NEG<POS<MAX）。
- ScrollBarUI 三常量经 `ProbeScrollBarUI extends BasicScrollBarUI` 静态暴露（0/1/2）。
- 渲染器：本身非 UIResource、opaque、有 no-focus 边框；getListCellRendererComponent 返回 this 且文本=值；null 值渲染空串；UIResource 变体标记；Serializable（JLabel 基）。
- 编辑器：组件为可编辑 JTextField；setItem/getItem 与字段文本互通；null 清空；UIResource 变体；字段 postActionEvent 恰触发一次监听。
- 图标工厂：三个图标非空 + 精确尺寸（4x8/9x9/4x8）+ 两次调用同实例。
- 箭头按钮：N/S/E/W 方向往返、setDirection、pref 非空；16x16 全 256 px 不透明绘制。
- 标题面板：BasicInternalFrameTitlePane(new JInternalFrame("title")) 构造出子组件、pref 非空且高>0。

## 5. 易错规范细节
- protected static final 常量在 JDK-27 强封装下不能 setAccessible 读取——测试子类静态方法暴露是双侧兼容写法。
- BasicComboBoxRenderer 默认水平对齐是 JLabel 默认（LEADING），不是 CENTER。
