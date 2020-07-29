# tree-printer

#### 介绍
Java 语言实现的树节点打印工具

#### 安装教程

    mvn clean install -Dmaven.test.skip=true

#### 使用说明

1.  运行 TreePrinterApplication
2.  在控制台输入命令与程序交互

#### 命令集
    插入节点：
        输入一个数字或多个数字用","分隔，例如：1,2,3,4,5
    删除节点：
        delete 2
    树状图显示：
        show或者show 3
    清空树上所有节点：
        clean
    批量插入：
        batch 个数，例如：batch 6，批量随机插入 6 个元素
    顺序批量插入：
        asc batch 起始数字，例如：asc batch 6，从0开始一直插入到6
    倒序批量插入：
        desc batch 起始数字，例如：desc batch 6，从6开始一直插入到0
    查看：   
        look 节点数据，例如：look 3，则打印数据为三的节点的高度、深度、平衡因子、坐标信息
    搜索：
        search 节点数据，例如 search 2，则搜索 2
    查看size:
        size
        
#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 码云特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  码云官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解码云上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是码云最有价值开源项目，是码云综合评定出的优秀开源项目
5.  码云官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  码云封面人物是一档用来展示码云会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
