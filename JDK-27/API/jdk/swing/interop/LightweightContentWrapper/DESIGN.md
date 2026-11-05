# jdk.swing.interop.LightweightContentWrapper 设计案

## 1. API 功能
轻量内容抽象类。

## 2. Java 用户如何使用
嵌入场景子类化。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.swing.interop/share/classes/jdk/swing/interop/LightweightContentWrapper.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 abstract；黑盒降级。

## 5. 契约深化（JDK-27）

- 抽象面 16 个方法对象 / 15 个去重名：imageBufferReset 两个重载（(int[],int,int,int,int,int) 与 (int[],int,int,int,int,int,double,double)）+ getComponent/paintLock/paintUnlock/imageReshaped/imageUpdated/focusGrabbed/focusUngrabbed/preferredSizeChanged/maximumSizeChanged/minimumSizeChanged/createDragGestureRecognizer(Class<T>,DragSource,Component,int,DragGestureListener)/createDragSourceContext(DragGestureEvent)/addDropTarget/removeDropTarget。
- 单 public 构造器；匿名全实现 headless 可实例化，paintLock/paintUnlock 可调用。
