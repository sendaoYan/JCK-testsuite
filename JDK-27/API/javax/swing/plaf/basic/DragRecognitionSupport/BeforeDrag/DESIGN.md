# javax.swing.plaf.basic.DragRecognitionSupport.BeforeDrag — DESIGN

## 1. API 功能
`DragRecognitionSupport.BeforeDrag` 接口：拖拽识别前回调 `dragStarting(MouseEvent)`（宿主类 `DragRecognitionSupport` 包私有，接口无法直接具名引用）。

## 2. Java 用户如何使用
由拖拽识别逻辑回调；宿主类包私有，故测试用 `Proxy` 动态实现该接口并反射调用 `dragStarting(MouseEvent)`。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/DragRecognitionSupport.java`（jdk25u-dev）。

## 4. 测试用例设计 + 自动化实现（`BeforeDragTest.testBlackBox`）
Proxy 动态实现 → 反射取得 `dragStarting` → 构造 `MouseEvent` 调用 → 断言回调被触发。
