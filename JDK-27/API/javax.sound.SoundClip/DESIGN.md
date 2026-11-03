# javax.sound.SoundClip — DESIGN

## 1. API 功能
`SoundClip` 是 JDK-25 起新增的最终类，用于简单播放短声音片段（含 MIDI 数据）。方法分组：
- 工厂：`static SoundClip createSoundClip(File)`（完整读取文件后返回实例；`null` 抛 `NullPointerException`；读取失败抛 `IOException`）。
- 查询：`canPlay()`（是否可播放，不可播放时其它方法均为 no-op）、`isPlaying()`（当前是否正在播放）。
- 控制：`play()`（从头播放）、`loop()`（循环播放）、`stop()`（停止，循环时必须调用才能停止）。
- 构造器私有：只能通过 `createSoundClip` 获取实例。

## 2. Java 用户如何使用
用户 `SoundClip sc = SoundClip.createSoundClip(new File("alert.wav")); if (sc.canPlay()) { sc.play(); }`，需要循环时 `sc.loop()`，停止时 `sc.stop()`。典型组合链：`createSoundClip(file)` → `canPlay()` → `play()`/`loop()` → `isPlaying()` → `stop()`。若文件内容不是可识别的受支持音频/MIDI 数据，`canPlay()` 返回 `false`，`play/loop/stop/isPlaying` 全部退化为 no-op。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/SoundClip.java`：`createSoundClip` 委托 `com.sun.media.sound.JavaSoundAudioClip.create(file)`，构造函数私有。
- `test/jdk/javax/sound/SoundClip/SoundClipTest.java`：`@key sound headful`，用 `SoundClip.createSoundClip(new File("notafile.wav"))` 验证 `IOException`，再对真实 wav 验证 `canPlay()`、`loop()`、`isPlaying()`、`stop()`。
- `test/jdk/javax/sound/SoundClip/LoopExitTest.java`：验证 `loop()` 后进程仍能正常退出（后台守护线程）。
- `src/java.desktop/share/classes/com/sun/media/sound/JavaSoundAudioClip.java`：`create(File)` 用 `FileInputStream` + `init(InputStream)`；`init` 先按音频解析，失败再按 MIDI 解析，均失败则 `success=false`，`canPlay()` 返回 `false`。

## 4. 测试用例设计 + 自动化实现（`SoundClipTest.testBlackBox`）
- 参数异常：`createSoundClip(null)` 抛 `NullPointerException`；`createSoundClip(不存在文件)` 抛 `IOException`。
- 正常/降级路径：写入不可识别的垃圾字节文件，`createSoundClip` 返回非空 `SoundClip`，`canPlay()==false`、`isPlaying()==false`。
- 组合操作：对不可播放实例依次 `play()/loop()/stop()`，断言均为 no-op（`isPlaying()` 仍为 `false`、`canPlay()` 不变），不依赖真实声卡，保证 headless 确定通过。
