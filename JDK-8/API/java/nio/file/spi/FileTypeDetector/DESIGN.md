# java.nio.file.spi.FileTypeDetector 设计文档

## 1. API 功能

抽象类：probeContentType(Path) 单一抽象方法；经 ServiceLoader 注册后由 Files.probeContentType 委派链调用。

（签名以 jdk8u372 javap -p 实测为准。）

## 2. Java 用户如何使用

- 常规用法：实现子类 + META-INF/services 注册，供 Files.probeContentType 咨询；返回 null 表示未知。
- 失败路径：IOException 由实现自定并向上传播。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- MIME 探测扩展（按内容/扩展名的第三方 detector）
- 用法特点：OpenJDK 自带 MimeTableFileTypeDetector（/etc/mime.types）等；Files 侧按扩展名优先。

## 4. 测试用例设计 + 自动化实现

文件：`FileTypeDetectorTest.java`（白盒 + 黑盒由 main 依次调用）。

设计：
- 白盒：抽象类/protected 构造（JDK-8 另有 private Void 安全门构造，实测差异）/probeContentType 抽象 public 返回 String。
- 子类契约：固定答案探测器、调用计数（缺失文件也计入）、null 合法（未知）、IOException 同对象透传、null 路径行为由子类自定（接口无检查，实测）。
- Files 委派（本机 MIME 表实测，双侧一致）：.txt→text/plain、.pdf→application/pdf、.html→text/html、.png→image/png、捏造扩展/无扩展→null、缺失 .txt 文件→仍 text/plain 不抛 IOException、扩展名优先于内容（%PDF 内容的 .txt 仍 text/plain）、未注册自制 detector 永不被咨询（.jckcustom→null）、目录探测 JDK-8 为 inode/directory 或 null（GIO 探测器，宿主相关；JDK-27 恒 null，差异记录）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 临时目录 + finally 清理；headless 安全，不加 @key。
