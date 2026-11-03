# java.text.Bidi 测试方案

## 1. API 功能（按方法分组）

- 常量：`DIRECTION_LEFT_TO_RIGHT=0`、`DIRECTION_RIGHT_TO_LEFT=1`、`DIRECTION_DEFAULT_LEFT_TO_RIGHT=-2`、`DIRECTION_DEFAULT_RIGHT_TO_LEFT=-1`。
- 构造器：`Bidi(String,int)`、`Bidi(AttributedCharacterIterator)`、`Bidi(char[],int,byte[],int,int,int)`。
- 方向/运行：`isMixed`、`isLeftToRight`、`isRightToLeft`、`baseIsLeftToRight`、`getBaseLevel`、`getLength`、`getLevelAt`、`getRunCount`、`getRunLevel`、`getRunStart`、`getRunLimit`。
- 行/工具：`createLineBidi`、静态 `requiresBidi`、静态 `reorderVisually`、`toString`。

## 2. Java 用户如何使用

- 构造：`new Bidi(text, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT)` 做双向文本分析。
- 典型调用：`b.isLeftToRight()`/`b.isRightToLeft()` 判断主导方向；`b.getRunCount()`/`b.getLevelAt(i)`/`b.getRunLevel(r)` 获取运行与嵌入层级；`Bidi.reorderVisually(...)` 按层级重排视觉顺序。
- 组合链路：构造 → `isMixed`/`baseIsLeftToRight`/`getBaseLevel`/`getLength` → `getRunCount`/`getRunLevel`/`getRunStart`/`getRunLimit` → `getLevelAt` 逐字符层级 → `createLineBidi` 取行 → `requiresBidi`/`reorderVisually` 工具。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `java.text.Bidi` 是 Unicode Bidi 算法实现，供 `Swing`/`java.awt.font` 等排版使用。
- JDK 测试 `test/jdk/java/text/Bidi/*.java`：断言 LTR/RTL/混合文本的层级、运行与 `reorderVisually`。
- 通用模式：文本框/编辑器处理阿拉伯语/希伯来语混合文本时做视觉重排。

## 4. 测试用例设计 + 自动化实现

实现位置：`BidiTest.java`（`testWhiteBox()` 白盒 + 六个黑盒方法）。

- 正常链路：纯 LTR `"abc"` 的 isLeftToRight/baseIsLeftToRight/getLength/getBaseLevel/getRunCount/getRunLevel/getRunStart/getRunLimit。
- RTL 链路：希伯来语 `"אב"` 的 isRightToLeft、baseLevel=1、runLevel=1。
- 混合链路：`"aאb"` 的 isMixed、runCount=3、逐字符 getLevelAt、run start/limit。
- 工具链路：`requiresBidi` 对 ASCII/希伯来语真假；`reorderVisually` RTL 反转、LTR 不变；`createLineBidi` 取行。
- 健壮性：纯文本固定、无 Locale/墙钟/随机/全局副作用。headless-safe，不加 `@key`。
