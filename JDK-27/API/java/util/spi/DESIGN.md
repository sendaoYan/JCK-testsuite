# java.util.spi 包（JDK-27 侧） 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

同 JDK-8 面 + ToolProvider（findFirst("javac") 交互）、ResourceBundleProvider（函数式）、AbstractResourceBundleProvider（format 校验）、LocaleNameProvider Unicode 扩展键/类型方法（getDisplayUnicodeExtensionKey/Type）。

## 2. Java 用户如何使用

tp.run(..., "--version") rc==0 输出含 "javac"；ARBP 非法 format/null IAE。

## 3. 参考来源

JDK-9+ spi 增量 javadoc。

## 4. 测试用例设计

JDK-8 拷贝 + ToolProvider/ResourceBundleProvider/AbstractResourceBundleProvider/LocaleNameProvider 增量块。
