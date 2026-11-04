# javax.imageio.plugins.jpeg.JPEGImageReadParam — DESIGN

## 1. API 功能
`JPEGImageReadParam` 是 `ImageReadParam` 的子类，为内置 JPEG 读码器增加解码表设置能力。方法分组：
- 构造器：`JPEGImageReadParam()`。
- 表设置：`setDecodeTables(JPEGQTable[], JPEGHuffmanTable[], JPEGHuffmanTable[])`、`unsetDecodeTables()`、`areTablesSet()`。
- 查询：`getQTables()`、`getDCHuffmanTables()`、`getACHuffmanTables()`（均返回副本）。
- 继承的采样：`setSourceSubsampling(...)`、`getSourceXSubsampling()` 等。

## 2. Java 用户如何使用
`new JPEGImageReadParam()` 后（或经 `ImageReader.getDefaultReadParam()`），用 `setDecodeTables(量化表, DC表, AC表)` 为解码缩写流提供表，再用 `areTablesSet()` 确认，最后 `ImageReader.read(0, param)` 读图。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/jpeg/JPEGImageReadParam.java`：`areTablesSet()` 仅以 `qTables != null` 判定；`setDecodeTables` 校验各数组非 null、长度 <=4、DC/AC 等长并 clone。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/jpeg/JPEGImageReader.java`：`getDefaultReadParam()` 返回 `JPEGImageReadParam`，解码缩写流时读取这些表。
- JDK 自带测试 `test/jdk/javax/imageio/plugins/jpeg/` 有缩写 JPEG 解码测试。

## 4. 测试用例设计 + 自动化实现（`JPEGImageReadParamTest.testBlackBox`）
- 默认 `areTablesSet()==false`、各 getter 返回 null。
- `setDecodeTables` 后 `areTablesSet()` 为 true，三类表往返一致；`unsetDecodeTables` 后复位为 null。
- 非法入参：null qTables、DC/AC 长度不一致均抛 `IllegalArgumentException`。
- 继承采样：`setSourceSubsampling(2,3,1,1)` 与 getter 往返。
- 真实 JPEG 写读往返：`ImageReader.getDefaultReadParam()` 是 `JPEGImageReadParam`，内存读回尺寸一致。

## 5. 契约深化补充（evtplug，2026-11）
- 反射断言声明方法集恰 6 个、无新公共字段。
- 校验矩阵（全 IAE，消息 "Invalid JPEG table arrays"）：任一 null；q>4；dc>4/ac>4；dc/ac 不等长（2vs1、2vs0）；拒绝后状态保持 false。
- 防御性拷贝双向：构造后改入参不影响内部；getter 每次新数组。零长度数组合法（areTablesSet==true、长度 0，规范未写实测）。
- 状态不变量：合法 set → 非法 set 抛 IAE 后旧表完整保留；unset 后可再 set。
- 端到端：jpeg writer 写 16x12 → `getDefaultReadParam() instanceof JPEGImageReadParam` → 带表解码尺寸往返；param 复用多次读。
