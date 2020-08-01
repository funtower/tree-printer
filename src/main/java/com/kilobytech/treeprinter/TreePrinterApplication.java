package com.kilobytech.treeprinter;

import com.kilobytech.treeprinter.tree.BalanceBinarySearchTree;
import com.kilobytech.treeprinter.tree.PrintableTree;
import com.kilobytech.treeprinter.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@SpringBootApplication
@Slf4j
public class TreePrinterApplication {

    public static void main(String[] args) {
        SpringApplication.run(TreePrinterApplication.class, args);
        test();
    }

    public static void test() {
        BalanceBinarySearchTree<Integer> sourceTree = new BalanceBinarySearchTree();
        Scanner scanner = new Scanner(System.in);
        log.error("请输入任意数字，然后按回车键确认进行插入，例如：93 回车，或者用逗号分隔批量插入 1,2,3,4 回车");
        while (true) {
            try {
                String line = scanner.nextLine();
                if ("exit".equals(line)) {
                    // 输入 exit 就是结束进程
                    break;
                } else if (StringUtils.isEmpty(line.trim())) {
                    // 输入空则不处理
                    log.error("请输入任意数字，然后按回车键确认进行插入，你也可以输入 show，查看整棵树结构");
                    continue;
                } else if (line.startsWith("show")) {
                    line = line.trim();
                    if (line.length() == "show".length()) {
                        new PrintableTree<Integer>(sourceTree.getRoot()).print();
                        log.error("show 后面还可以跟参数[你要查看的数据]，例如：show 93 回车");
                    } else {
                        int show = Integer.parseInt(line.substring("show ".length()));
                        BalanceBinarySearchTree<Integer>.Node node = sourceTree.search(show, sourceTree.getRoot());
                        if (Objects.nonNull(node)) {
                            new PrintableTree<Integer>(node).print();
                        } else {
                            log.warn("节点[" + show + "]不存在，show 啥啊？");
                            log.info("不妨先插入，例如：add {}", show);
                        }
                        log.info("你可以查看树根节点的信息，例如：root 回车");
                    }
                } else if (line.startsWith("look")) {
                    line = line.trim();
                    BalanceBinarySearchTree<Integer>.Node root = sourceTree.getRoot();
                    if (Objects.isNull(root)) {
                        log.info("根节点为空");
                        continue;
                    }
                    if (line.length() == "look".length()) {
                        // 层序遍历挨个儿 look
                        Queue<BalanceBinarySearchTree<Integer>.Node> access = new LinkedList<>();
                        access.offer(root);
                        while (!access.isEmpty()) {
                            BalanceBinarySearchTree<Integer>.Node node = access.poll();
                            PrintableTree<Integer> pt = new PrintableTree<>(sourceTree.getRoot());
                            PrintableTree<Integer>.PrintableNode lkPNode = pt.search(node.getData(), pt.getRoot());
                            log.info("节点[{}] 高度：{}，深度：{}，平衡因子：{}，水平偏移：{}，垂直偏移：{}",
                                    node.getData(), node.getHeight(), node.calculateDepth(), node.getBalanceFactor(),
                                    lkPNode.getHorizontalOffsetPercent(), lkPNode.getVerticalOffsetPercent());
                            BalanceBinarySearchTree<Integer>.Node left = node.getLeft();
                            BalanceBinarySearchTree<Integer>.Node right = node.getRight();
                            if (Objects.nonNull(left)) {
                                access.offer(left);
                            }
                            if (Objects.nonNull(right)) {
                                access.offer(right);
                            }
                        }
                        log.error("look 后面还可以跟参数[你要查看的数据]，例如：look 93 回车");
                    } else {
                        int look = Integer.parseInt(line.substring("look ".length()));
                        BalanceBinarySearchTree<Integer>.Node node = sourceTree.search(look, sourceTree.getRoot());
                        PrintableTree<Integer> pt = new PrintableTree<>(sourceTree.getRoot());
                        PrintableTree<Integer>.PrintableNode lkPNode = pt.search(look, pt.getRoot());
                        if (Objects.nonNull(node)) {
                            log.info("节点[{}] 高度：{}，深度：{}，平衡因子：{}，水平偏移：{}，垂直偏移：{}",
                                    node.getData(), node.getHeight(), node.calculateDepth(), node.getBalanceFactor(),
                                    lkPNode.getHorizontalOffsetPercent(), lkPNode.getVerticalOffsetPercent());
                        } else {
                            log.info("节点[" + look + "]不存在，look 啥啊？");
                            log.info("不妨先插入，例如：add {}", look);
                        }
                        log.info("你还可以用 show 打印这个节点的树形图，例如：show {}", look);
                    }
                } else if (line.startsWith("asc batch")) {
                    line = line.trim();
                    int seqBatch = Integer.parseInt(line.substring("asc batch ".length()));
                    LocalDateTime start = LocalDateTime.now();
                    for (int i = 0; i < seqBatch; i++) {
                        if (i % 32768 == 0) {
                            log.info("当前size: " + sourceTree.getSize());
                            log.info("耗时：{} 秒", Duration.between(start, LocalDateTime.now()).getSeconds());
                        }
                        sourceTree.insert(i);
                    }
                    log.info("当前size: " + sourceTree.getSize());
                    log.info("耗时：{} 秒", Duration.between(start, LocalDateTime.now()).getSeconds());
                    log.info("批量顺序插入完毕");
                    log.info("你也可以使用倒序批量插入，例如：desc batch 7");
                } else if (line.startsWith("desc batch")) {
                    line = line.trim();
                    int seqBatch = Integer.parseInt(line.substring("desc batch ".length()));
                    for (int i = seqBatch; i > 0; i--) {
                        sourceTree.insert(i);
                    }
                    log.info("批量倒序插入完毕");
                    log.info("你可以随便搜索一个看看，例如：search 3");
                } else if (line.startsWith("batch")) {
                    line = line.trim();
                    int batchAdd = Integer.parseInt(line.substring("batch ".length()));
                    for (int i = 0; i < batchAdd; i++) {
                        while (!sourceTree.insert(RandomUtil.getRandomIntInRange(0, batchAdd * 10))) ;
                    }
                    log.info("批量插入完毕");
                    log.error("这时候你可以删除某个节点，如：delete 93");
                } else if (line.startsWith("search")) {
                    line = line.trim();
                    int search = Integer.parseInt(line.substring("search ".length()));
                    LocalDateTime start = LocalDateTime.now();
                    BalanceBinarySearchTree<Integer>.Node result = sourceTree.search(search, sourceTree.getRoot());
                    if (Objects.nonNull(result)) {
                        log.info("搜索到数据[" + search + "]耗时{}纳秒", Duration.between(start, LocalDateTime.now()).getNano());
                        log.info("你可以查看这个节点的详细信息，例如：look {}", search);
                    } else {
                        log.info("未搜索到数据[" + search + "]耗时{}纳秒", Duration.between(start, LocalDateTime.now()).getNano());
                        log.info("你可以插入节点，例如：add {}", search);
                    }
                } else if (line.startsWith("delete")) {
                    line = line.trim();
                    int delete = Integer.parseInt(line.substring("delete ".length()));
                    LocalDateTime start = LocalDateTime.now();
                    boolean del = sourceTree.delete(delete);
                    if (del) {
                        log.info("已成功删除元素[" + delete + "]耗时{}", Duration.between(start, LocalDateTime.now()).getSeconds());
                    } else {
                        log.error("未找到{}，不妨先插入，例如：add {}", delete, delete);
                    }
                    log.error("你也可以尝试清空 clean，例如：clean 回车");
                } else if (line.startsWith("asc delete")) {
                    line = line.trim();
                    int seqBatch = Integer.parseInt(line.substring("asc delete ".length()));
                    for (int i = 0; i < seqBatch; i++) {
                        sourceTree.delete(i);
                    }
                    log.info("批量顺序删除完毕");
                } else if (line.startsWith("desc delete")) {
                    line = line.trim();
                    int seqBatch = Integer.parseInt(line.substring("desc delete ".length()));
                    for (int i = seqBatch; i > 0; i--) {
                        sourceTree.delete(i);
                    }
                    log.info("批量倒序删除完毕");
                } else if (line.startsWith("clean")) {
                    line = line.trim();
                    if (line.length() == "clean".length()) {
                        while (sourceTree.getSize() > 0) {
                            Integer data = sourceTree.getRoot().getData();
                            boolean delete = sourceTree.delete(data);
                            if (!delete) {
                                log.info("删除根节点[" + data + "]发生异常");
                            }
                        }
                        log.info("删除完毕，当前 size = " + sourceTree.getSize());
                    } else {
                        int clean = Integer.parseInt(line.substring("clean ".length()));
                        for (int i = 0; i < clean; i++) {
                            boolean delete = sourceTree.delete(RandomUtils.nextInt(0, 102400));
                            if (!delete) {
                                i--;
                            }
                        }
                        log.info("已成功删除 " + clean + "个元素");
                    }
                    log.info("你可以现在你可以查看树的节点个数，例如：size 回车");
                } else if ("reset".equalsIgnoreCase(line.trim())) {
                    sourceTree = new BalanceBinarySearchTree();
                } else if ("root".equalsIgnoreCase(line.trim())) {
                    log.info("root = [" + sourceTree.getRoot().getData() + "]节点的平衡因子：" + sourceTree.getRoot().getBalanceFactor());
                    log.info("root = [" + sourceTree.getRoot().getData() + "]节点的高度：" + sourceTree.getRoot().getHeight());
                    if (Objects.nonNull(sourceTree.getRoot())) {
                        log.info("你可以把 root 删了试试，例如：delete {}", sourceTree.getRoot().getData());
                    } else {
                        log.info("你可以搜索这个根节点看看，例如：search {}", sourceTree.getRoot().getData());
                    }
                } else if ("size".equalsIgnoreCase(line.trim())) {
                    int size = sourceTree.getSize();
                    log.info("当前 size: " + size);
                    log.info("你可以用顺序批量插入 asc batch [最大数]，例如：asc batch 5");
                } else if (line.startsWith("test")) {
                    line = line.trim();
                    if (line.length() == "test".length()) {
                        for (int i = 0; i < 100; i++) {
                            boolean insert = sourceTree.insert(RandomUtils.nextInt(0, 100));
                            if (!insert) {
                                i--;
                            }
                        }
                        while (Objects.nonNull(sourceTree.getRoot())) {
                            int d = RandomUtils.nextInt(0, 100);
                            boolean delete = sourceTree.delete(d);
                            if (delete) {
                                log.info("已删除：[" + d + "]");
                            }
                        }
                    } else {
                        int test = Integer.parseInt(line.substring("test ".length()));
                        for (int i = 0; i < test; i++) {
                            boolean insert = sourceTree.insert(RandomUtils.nextInt(0, test));
                            if (!insert) {
                                i--;
                            }
                        }
                        while (Objects.nonNull(sourceTree.getRoot())) {
                            int d = RandomUtils.nextInt(0, test);
                            boolean delete = sourceTree.delete(d);
                            if (delete) {
                                log.info("已删除：[" + d + "]");
                            }
                        }
                    }
                    log.info("删干净了");
                    log.info("根------" + sourceTree.getRoot());
                    log.info("容量----" + sourceTree.getSize());
                } else if (line.startsWith("add")) {
                    line = line.trim();
                    int add = Integer.parseInt(line.substring("add ".length()));
                    boolean insert = sourceTree.insert(add);
                    if (insert) {
                        new PrintableTree<Integer>(sourceTree.getRoot()).print();
                    }
                    log.info("你现在可以搜索刚插入的这个节点，例如：search {}", add);
                } else {
                    final BalanceBinarySearchTree<Integer> ft = sourceTree;
                    Arrays.stream(line.trim().split(",")).mapToInt(Integer::parseInt).forEach(e -> ft.insert(e));
                    log.info("批量插入的方式还有另外一种 batch [插入个数]，例如：batch 3 回车");
                }
            } catch (NumberFormatException e) {
                log.info(e.getMessage());
                log.error("注意逗号\",\"是英文半角逗号");
                continue;
            }
        }
        scanner.close();
        System.exit(0);
    }
}

