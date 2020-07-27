package com.kilobytech.treeprinter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

/**
 * @author huangtao
 * @Title: Console
 * @Description:
 * @date 2020/7/26
 */
@Slf4j
public class Console {

    public static void main(String[] args) {
        BalanceBinarySearchTree<Integer> bbst = new BalanceBinarySearchTree();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {

                String line = scanner.nextLine();
                if ("-1".equals(line)) {
                    // 输入 -1 就是结束进程
                    break;
                } else if (StringUtils.isEmpty(line.trim())) {
                    // 输入空则不处理
                    continue;
                } else if (line.startsWith("show")) {
                    line = line.trim();
                    if (line.length() == "show".length()) {
                        new PrintableTreeV2(bbst.getRoot()).print();
                    } else {
                        int show = Integer.parseInt(line.substring("show ".length()));
                        BalanceBinarySearchTree.Node node = bbst.search(show, bbst.getRoot());
                        if (Objects.nonNull(node)) {
                            new PrintableTreeV2(node).print();
                        } else {
                            log.info("节点[" + show + "]不存在，show 啥啊？");
                        }
                    }
                } else if (line.startsWith("batch")) {
                    line = line.trim();
                    int batchAdd = Integer.parseInt(line.substring("batch ".length()));
                    for (int i = 0; i < batchAdd; i++) {
                        while (!bbst.insert(RandomUtil.getRandomIntInRange(0, 10240))) ;
                    }
                } else if (line.startsWith("search")) {
                    line = line.trim();
                    int search = Integer.parseInt(line.substring("search ".length()));
                    bbst.search(search, bbst.getRoot());
                } else if (line.startsWith("delete")) {
                    line = line.trim();
                    int delete = Integer.parseInt(line.substring("delete ".length()));
                    bbst.delete(delete);
                } else if (line.startsWith("clean")) {
                    line = line.trim();
                    int clean = Integer.parseInt(line.substring("clean ".length()));
                    for (int i = 0; i < clean; i++) {
                        boolean delete = bbst.delete(RandomUtils.nextInt(0, 102400));
                        if (!delete) {
                            i--;
                        }
                    }
                    System.out.println("已成功删除 " + clean + "个元素");
                } else if ("reset".equalsIgnoreCase(line.trim())) {
                    bbst = new BalanceBinarySearchTree();
                } else if ("size".equalsIgnoreCase(line.trim())) {
                    int size = bbst.getSize();
                    System.out.println("当前 size: " + size);
                } else if (line.startsWith("test")) {
                    line = line.trim();
                    if (line.length() == "test".length()) {
                        for (int i = 0; i < 100; i++) {
                            boolean insert = bbst.insert(RandomUtils.nextInt(0, 100));
                            if (!insert) {
                                i--;
                            }
                        }
                        while (Objects.nonNull(bbst.getRoot())) {
                            int d = RandomUtils.nextInt(0, 100);
                            boolean delete = bbst.delete(d);
                            if (delete) {
                                log.error("已删除：[" + d + "]");
                            }
                        }
                        log.info("删干净了");
                    } else {
                        int test = Integer.parseInt(line.substring("test ".length()));
                        for (int i = 0; i < test; i++) {
                            boolean insert = bbst.insert(RandomUtils.nextInt(0, test));
                            if (!insert) {
                                i--;
                            }
                        }
                        while (Objects.nonNull(bbst.getRoot())) {
                            int d = RandomUtils.nextInt(0, test);
                            boolean delete = bbst.delete(d);
                            if (delete) {
                                log.info("已删除：[" + d + "]");
                            }
                        }
                        log.info("删干净了");
                        log.error("根------" + bbst.getRoot());
                        log.error("容量----" + bbst.getSize());
                    }
                } else {
                    final BalanceBinarySearchTree ft = bbst;
                    Arrays.stream(line.trim().split(",")).mapToInt(Integer::parseInt).forEach(e -> ft.insert(e));
                    log.error("root = [" + bbst.getRoot().getData() + "]节点的平衡因子：" + bbst.getRoot().getBalanceFactor());
                    log.error("root = [" + bbst.getRoot().getData() + "]节点的高度：" + bbst.getRoot().getHeight());
                }
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
                continue;
            }
        }
        scanner.close();
    }
}
