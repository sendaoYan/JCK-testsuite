# javax.imageio.spi.ImageOutputStreamSpi — DESIGN（JDK-8）

## 1. API 功能
`ImageOutputStreamSpi` 是输出流插件 SPI 抽象基类（继承 `IIOServiceProvider`），与 `ImageInputStreamSpi` 镜像。方法分组：
- 构造：`ImageOutputStreamSpi(String,String,Class<?> outputClass)`（outputClass null 抛 `IllegalArgumentException`）。
- 工厂：`createOutputStreamInstance(Object)`（实测硬编码 useCache=true 委托三参版）、`createOutputStreamInstance(Object,boolean,File)`（抽象）。
- 输出类型：`getOutputClass()`；缓存开关：`canUseCacheFile()`、`needsCacheFile()`（默认 false）。

## 2. Java 用户如何使用
用户实现本类声明输出类型（File/OutputStream/RandomAccessFile），`ImageIO.createImageOutputStream(Object)` 遍历注册表按 `outputClass.isInstance` 选中；与输入侧对称。

## 3. 开源代码实际用法调研（来源：OpenJDK 8u 源码）
- `jdk/src/share/classes/javax/imageio/spi/FileImageOutputStreamSpi.java` / `OutputStreamImageOutputStreamSpi.java` / `RAFImageOutputStreamSpi.java`：outputClass 为 File/OutputStream/RandomAccessFile，非匹配类型抛 `IllegalArgumentException`。
- `jdk/src/share/classes/javax/imageio/ImageIO.java`：`createImageOutputStream` 与输入侧同一选择算法。
- `jdk/src/share/classes/javax/imageio/spi/ImageOutputStreamSpi.java`：一参工厂固定 `useCache=true` 转发。

## 4. 测试用例设计 + 易错规范细节（`ImageOutputStreamSpiTest`）
- 注册表内实测：全局注册表该类别 ≥2 个提供者；outputClass 集合含 File/OutputStream/RandomAccessFile；vendor/version 为 Oracle 常量。
- 负例（逐 SPI 实测精确异常）：`createOutputStreamInstance(null)` 与错误类型均 `IllegalArgumentException`（两重载一致）。
- 自写最小子类：默认缓存开关 false/false；三参工厂参数透传记录；一参工厂实测以 useCache=true、cacheDir=null 转发（记录的实际行为）。
- File SPI 写往返：临时文件 → `createOutputStreamInstance(file)` → 写字节+flush → 文件内容逐字节一致 → finally 删除。
- 易错点：与输入侧同构但类别独立；`getOutputClass()` 在一参工厂拒绝路径上同样抛 IAE 而非 NPE。
