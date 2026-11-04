# java.awt.CompositeContext 黑盒行为测试设计

## 1. API 功能
- 接口（`Composite` 创建的混合上下文）。
- `compose(Raster src, Raster dstIn, WritableRaster dstOut)`：按混合规则合成两个光栅。
- `dispose()`：释放上下文资源。

## 2. Java 用户如何使用
通常不直接使用；由 `Graphics2D.setComposite(Composite)` 在内部调用。直接用例是图像合成管线：
```java
CompositeContext ctx = AlphaComposite.Src.createContext(cm, cm, null);
ctx.compose(src.getRaster(), dst.getRaster(), dst.getRaster());
ctx.dispose();
```
组合链路：`AlphaComposite.createContext` → `compose` → 读回像素校验 → `dispose`。

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 `src/java.desktop/share/classes/java/awt/CompositeContext.java`：接口方法声明（`compose(Raster,Raster,WritableRaster)`、`dispose`）。
- JDK 实现：`sun.java2d.*` 的 `AlphaCompositeContext`/`OpaqueCompositeContext` 按 Porter-Duff 规则合成（`Src` 取源、`Dst` 保目标、`Clear` 清零）。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 经 `AlphaComposite.Src/SrcOver/Dst/Clear.createContext` 得到非空上下文。
- `Src` compose：输出取源（红覆盖绿）。
- `SrcOver`（不透明源）compose：源覆盖目标。
- `Dst` compose：保留目标（绿不变）。
- `Clear` compose：输出透明黑。
- 各上下文 `dispose` 可调用。
断言 11 项，像素为固定整数（TYPE_INT_ARGB 全内存操作，headless-safe）。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：同 Composite 联测：SRC_OVER 透明底出 0xFF804020 精确像素；dispose；接口方法存在。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
