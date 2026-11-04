# java.text.Bidi 测试设计案（JDK-8）

> 覆盖目标：完整契约（final、四个方向常量、LTR/RTL/混合 run、requiresBidi、reorderVisually、
> createLineBidi、三种构造器），非冒烟。

## 1. API 提供了哪些功能

- 常量：`DIRECTION_LEFT_TO_RIGHT=0`、`DIRECTION_RIGHT_TO_LEFT=1`、
  `DIRECTION_DEFAULT_LEFT_TO_RIGHT=-2`、`DIRECTION_DEFAULT_RIGHT_TO_LEFT=-1`。
- 构造器：`Bidi(String, int)`、`Bidi(AttributedCharacterIterator)`、`Bidi(char[], int, byte[], int, int, int)`。
- 查询：`isMixed/isLeftToRight/isRightToLeft/baseIsLeftToRight/getBaseLevel/getLength/getLevelAt/
  getRunCount/getRunLevel/getRunStart/getRunLimit`。
- 工具：`requiresBidi(char[], int, int)`、静态 `reorderVisually(...)`、`createLineBidi(int, int)`。

## 2. Java 用户如何使用

- `new Bidi(text, DIRECTION_DEFAULT_LEFT_TO_RIGHT)` 判断文本方向与 run；
  `reorderVisually` 重排视觉顺序。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/Bidi.java`。
- JDK-27 同目录 DESIGN.md（行为一致）。

## 4. 测试用例设计（映射到 BidiTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：final、四个方向常量、构造器、reorderVisually | testWhiteBox |
| P0 | 纯 LTR 文本 | testLeftToRight |
| P0 | 纯 RTL 文本（希伯来） | testRightToLeft |
| P0 | 混合 run（aאb 三 run） | testMixedRuns |
| P0 | requiresBidi | testRequiresBidi |
| P0 | reorderVisually RTL 反转 / LTR 不变 | testReorderVisually |
| P0 | createLineBidi | testCreateLineBidi |
| P0 | char[]/embeddings/ACI 构造器 | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- 希伯来文本（`אב`）→ isRightToLeft、baseLevel=1、单 run；
  `"aאb"` → isMixed、三 run、levels {0,1,0}。

## 注意事项

- 固定显式方向参数，不依赖默认 locale；headless-safe 不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- run 结构表（纯 LTR/纯 RTL/混合 "abc אב def" 三 run [0,4)L0/[4,6)L1/[6,10)L0）
- flag 矩阵（DEFAULT_LTR/LTR 同构、RTL 基级别反转 LTR 段升 2 级、数字段基级别 1）
- getLevelAt 逐位表 00001100000（index 10 为基级别）
- char[] 六参构造等价；createLineBidi(0,4) 单 run；(-1,2)/(0,99)/(5,2)→IAE；Bidi(null)→IAE；flag 99/-2 容忍不抛
- requiresBidi(null→NPE、坏区间→IAE、纯 LTR false/混合 true)；AttributedCharacterIterator 构造
