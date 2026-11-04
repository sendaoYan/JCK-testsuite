# java.awt.image.LookupTable 设计案

## 1. API 功能分组
- **类型**：abstract class。
- **构造**：`LookupTable(int offset, int numComponents)`。
- **查询**：`getNumComponents()/getOffset()`；抽象 `lookupPixel(int[],int[])`。

## 2. Java 用户如何使用
定义查找表变换；子类实现 `lookupPixel`，`LookupOp` 用它逐像素查表。getOffset/getNumComponents 描述表结构。

## 3. 开源代码实际用法调研
- `java.awt.image.LookupTable` 是查表抽象，`ByteLookupTable/ShortLookupTable` 是其实现（`src/java.desktop/share/classes/java/awt/image/`）。
- `test/jdk/java/awt/image/` LookupOp 用例基于 LookupTable 做像素映射（本用例结构/契约参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`LookupTableTest.testBlackBox()`，匿名子类）：
1. getNumComponents/getOffset 精确。
2. lookupPixel 恒等/偏移映射。
白盒（`testWhiteBox()`）：public abstract、getNumComponents/getOffset/lookupPixel 签名。
