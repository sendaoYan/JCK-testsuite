# javax.imageio.spi.ImageInputStreamSpi — DESIGN（JDK-8）

## 1. API 功能
`ImageInputStreamSpi` 是输入流插件 SPI 抽象基类（继承 `IIOServiceProvider`）。方法分组：
- 构造：`ImageInputStreamSpi(String,String,Class<?> inputClass)`（inputClass null 抛 `IllegalArgumentException`）。
- 工厂：`createInputStreamInstance(Object)`（**实测硬编码 useCache=true 委托三参版**）、`createInputStreamInstance(Object,boolean,File)`（抽象）。
- 输入类型：`getInputClass()`；缓存开关：`canUseCacheFile()`、`needsCacheFile()`（基类默认均 false）。

## 2. Java 用户如何使用
用户实现本类声明可接受的输入类型（如 `File.class`/`InputStream.class`），`ImageIO.createImageInputStream(Object)` 遍历注册表选中第一个 `inputClass.isInstance(input)` 且工厂成功的 SPI；缓存语义由 `canUseCacheFile`/`needsCacheFile` 声明。

## 3. 开源代码实际用法调研（来源：OpenJDK 8u 源码）
- `jdk/src/share/classes/javax/imageio/spi/FileImageInputStreamSpi.java` / `InputStreamImageInputStreamSpi.java` 等 4 个标准实现：inputClass 分别为 File/InputStream/OutputStream/RandomAccessFile，`createInputStreamInstance` 拒绝非匹配类型（`IllegalArgumentException`）。
- `jdk/src/share/classes/javax/imageio/ImageIO.java`：`createImageInputStream` 先 `IIORegistry` 迭代再 `inputClass.cast`/`isInstance` 判断。
- `jdk/src/share/classes/javax/imageio/spi/ImageInputStreamSpi.java`：一参 `createInputStreamInstance(Object)` 固定 `useCache=true, cacheDir=null` 转发。

## 4. 测试用例设计 + 易错规范细节（`ImageInputStreamSpiTest`）
- 注册表内实测：全局注册表 `ImageInputStreamSpi` 类别 ≥3 个提供者；inputClass 集合含 InputStream/File/RandomAccessFile；`getVendorName()=="Oracle Corporation"`、`canUseCacheFile()==false`。
- 负例（逐 SPI 探针实测）：对每个注册流 SPI，`createInputStreamInstance(null)` 与错误类型（如 String）均抛 `IllegalArgumentException`（两个重载一致）。
- 自写最小子类：构造器默认 `canUseCacheFile()==false`、`needsCacheFile()==false`；三参工厂记录 `(input,useCache,cacheDir)` 透传；一参工厂实测以 `useCache==true`、`cacheDir==null` 转发（记录的实际行为，javadoc 未声明）。
- File SPI 往返：临时文件写入字节 → `createInputStreamInstance(file)` → 读回内容一致 → finally 删除临时文件。
- 易错点：`createInputStreamInstance(null)` 的异常在不同实现间均为 `IllegalArgumentException`（非 NPE），必须逐个注册 SPI 断言。
