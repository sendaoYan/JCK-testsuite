# java.util.zip.ZipError 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

Error 子类（非异常）：zip 系统级不可恢复错误。

## 2. Java 用户如何使用

区别于 IOException 体系；VirtualMachineError 平级。

## 3. 参考来源

javadoc：Unrecoverable error。

## 4. 测试用例设计

ZipErrorTest.testContracts：Error 父类、构造器。
