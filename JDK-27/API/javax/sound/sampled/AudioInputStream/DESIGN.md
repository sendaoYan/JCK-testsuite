# javax.sound.sampled.AudioInputStream — DESIGN

## 1. API 功能
`AudioInputStream` 是带指定音频格式和帧长度的 `InputStream`。方法分组：
- 构造器：`AudioInputStream(InputStream, AudioFormat, long frameLength)`（按字节流 + 帧长）、`AudioInputStream(TargetDataLine)`。
- 格式/长度：`getFormat()`、`getFrameLength()`。
- 读取：`read()`（仅 frameSize==1）、`read(byte[])`、`read(byte[], int, int)`（总是读整数个帧、受 frameLength 限制）、`skip(long)`（按整数帧跳过）。
- 流能力：`available()`、`close()`、`mark(int)`、`reset()`、`markSupported()`。

## 2. Java 用户如何使用
典型组合链：`new AudioInputStream(new ByteArrayInputStream(data), format, frameCount)` → `getFrameLength()` → `available()` → `read(buf)`（读到整数帧）→ `skip(n)` → `mark/reset` 重放 → `read() == -1` 判定到帧尾。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/sampled/AudioInputStream.java:248`：`read(byte[],int,int)` 先 `len -= len % frameSize` 保证整数帧。
- `src/java.desktop/share/classes/javax/sound/sampled/AudioInputStream.java:258`：`frameLength != NOT_SPECIFIED` 时 `framePos >= frameLength` 返回 -1。
- `test/jdk/javax/sound/sampled/AudioInputStream/AISReadFraction.java`：多组 frameSize 下验证读取总是整数帧（bug 4948663）。

## 4. 测试用例设计 + 自动化实现（`AudioInputStreamTest.testBlackBox`）
- 16-bit 单声道（frameSize=2）、4 帧数据：`getFormat/getFrameLength/available/read/skip` 组合，含 `read(...,3)` 截断为 2 字节、`skip(3)` 截断为 2 字节、读到帧尾返回 -1。
- `mark/reset` 往返重放相同字节。
- frameSize=1 时 `read()` 单字节（含 0x81 无符号展开）与 EOF。
- frameSize>1 时 `read()` 抛 `IOException`；`close()` 无异常完成。
