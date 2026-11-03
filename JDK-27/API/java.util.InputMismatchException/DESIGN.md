# java.util.InputMismatchException — 测试设计案

## 1. API 功能
Scanner 读取的 token 与请求类型不匹配或超出范围时抛出的异常。继承 NoSuchElementException。两个构造器：()、String。

## 2. Java 用户如何使用
`new Scanner("xyz").nextInt()` 时抛出；用户捕获后提示重新输入。
链路：nextInt/nextDouble -> token 转换失败 -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `Scanner.nextInt`（本地 `~/git/jdk25u-dev`）匹配失败时 `throw new InputMismatchException()`；
  越界时 `throw new InputMismatchException(...)`——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/Scanner/`。

## 4. 测试用例设计 + 自动化实现（InputMismatchExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 继承 | extends NoSuchElementException |
| 构造器 | ()/String 两个构造、消息回传 |
| 触发 | Scanner("xyz").nextInt() 抛 InputMismatchException |

共 3 组场景、约 5 个 check（`testBlackBox()`）。
