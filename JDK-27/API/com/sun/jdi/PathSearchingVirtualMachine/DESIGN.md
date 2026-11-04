# com.sun.jdi.PathSearchingVirtualMachine 行为化测试设计

## 1. API 功能
VirtualMachine 是目标 VM 的镜像：查询类/线程/模块、mirrorOf 造值、事件与请求入口、dispose/exit 生命周期。

公开成员（JDK-27 javap）：
  - `public interface com.sun.jdi.PathSearchingVirtualMachine extends com.sun.jdi.VirtualMachine {`
  - `public abstract java.util.List<java.lang.String> classPath();`
  - `public abstract java.util.List<java.lang.String> bootClassPath();`
  - `public abstract java.lang.String baseDirectory();`

## 2. Java 用户如何使用
从 connector.launch() 得到 vm 后，`allClasses()`/`classesByName()`/`allThreads()`/`mirrorOf()` 查询，`eventRequestManager()`/`eventQueue()` 建请求收事件，最后 `dispose()`。

## 3. 开源代码实际用法调研
来源：~/git/jdk25u-dev/test/jdk/com/sun/jdi/ 的 ClassesByName.java、DataDumpTest.java、HomeTest.java、RunToExit.java。

## 4. 测试用例设计 + 自动化实现
testBlackBox 用 launchTarget 启动子 JVM，断言 name/version/mirrorOf/allClasses/allModules/instanceCounts 等。

实现见同目录 `PathSearchingVirtualMachineTest.java` 的 `testWhiteBox()`（结构/继承/成员形态）与 `testBlackBox()`（组合操作行为断言）。
