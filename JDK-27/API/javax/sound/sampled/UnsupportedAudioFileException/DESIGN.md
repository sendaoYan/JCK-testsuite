# javax.sound.sampled.UnsupportedAudioFileException — DESIGN

## 1. API 功能
`UnsupportedAudioFileException` 是受检异常（继承 `Exception`），表示音频文件/流数据无法被系统识别。方法：无参构造器与 `UnsupportedAudioFileException(String)`，其余继承自 `Throwable`。

## 2. Java 用户如何使用
`try { AudioSystem.getAudioInputStream(stream); } catch (UnsupportedAudioFileException e) { ... }`，用于处理不支持的文件格式。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/UnsupportedAudioFileException.java`：两个构造器 `super()`/`super(message)`。
- `AudioSystem.getAudioFileFormat/getAudioInputStream` 在 reader 无法识别时抛出该异常。

## 4. 测试用例设计 + 自动化实现（`UnsupportedAudioFileExceptionTest.testBlackBox`）
- 白盒：公开类、继承 `Exception`、两个构造器。
- 黑盒：默认构造 `getMessage/getCause` 为 null 且非 RuntimeException；String 构造 `getMessage` 往返、`toString` 含消息。
