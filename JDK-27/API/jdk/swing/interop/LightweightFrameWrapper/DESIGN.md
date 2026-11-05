# jdk.swing.interop.LightweightFrameWrapper 设计案

## 1. API 功能
轻量帧包装（concrete）。

## 2. Java 用户如何使用
嵌入场景 new + dispose。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.swing.interop/share/classes/jdk/swing/interop/LightweightFrameWrapper.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 concrete；黑盒降级（嵌入上下文）。

## 5. 契约深化（JDK-27）

- 委托面精确：15 个 public 声明方法（14 个去重名，notifyDisplayChanged 两个重载）——dispose/setVisible/setBounds/notifyDisplayChanged(int)/notifyDisplayChanged(double,double)/setHostBounds/addWindowFocusListener/setContent/emulateActivation/findComponentAt/createMouseEvent/createMouseWheelEvent/createKeyEvent/createUngrabEvent/isCompEqual，全为实例方法（无静态）。
- 形态：具体类（非 abstract）、public 无参构造器、直接超类 Object（自身不是 Component，是给外部框架用的纯委托）。
