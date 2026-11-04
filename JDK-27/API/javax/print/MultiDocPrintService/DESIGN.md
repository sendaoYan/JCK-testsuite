# javax.print.MultiDocPrintService 黑盒行为测试设计

## 1. API 功能
接口，extends PrintService。方法：createMultiDocPrintJob

## 2. Java 用户如何使用
由 JDK 提供实现或打印服务实现方实现。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/；test/jdk 打印 API 测试通过实现类覆盖。

## 4. 测试用例设计 + 自动化实现
白盒：public interface、父接口、声明方法；黑盒：通过具体实现验证契约（如适用）。

