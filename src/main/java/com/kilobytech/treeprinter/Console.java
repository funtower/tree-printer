package com.kilobytech.treeprinter;

import lombok.extern.slf4j.Slf4j;
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
        BalanceBinarySearchTree bbst = new BalanceBinarySearchTree();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            if ("-1".equals(line)) {
                // 输入 -1 就是结束进程
                break;
            } else if (StringUtils.isEmpty(line.trim())) {
                // 输入空则不处理
                continue;
            } else if (line.startsWith("look")) {
                if (line.length() == "look".length()) {
                    bbst.getAll().forEach(e -> log.info("节点数据：[" + e.getData() + "] BF: " + e.getBalanceFactor() + " HG: " + e.getHeight()));
                } else {
                    line = line.trim();
                    int look = Integer.parseInt(line.substring("look ".length()));
                    BalanceBinarySearchTree.Node lk = bbst.search(look, bbst.getRoot());
                    bbst.getAll().parallelStream().filter(e -> e == lk).forEach(e -> log.info("节点数据：[" + e.getData() + "] BF: " + e.getBalanceFactor() + " HG: " + e.getHeight()));
                }
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
                    while (!bbst.insert(RandomUtil.getRandomIntInRange(0, 100))) ;
                }
            } else if (line.startsWith("search")) {
                line = line.trim();
                int search = Integer.parseInt(line.substring("search ".length()));
                bbst.search(search, bbst.getRoot());
            } else if (line.startsWith("delete")) {
                line = line.trim();
                int delete = Integer.parseInt(line.substring("delete ".length()));
                bbst.delete(delete);
            } else if ("reset".equalsIgnoreCase(line.trim())) {
                bbst = new BalanceBinarySearchTree();
            } else {
                final BalanceBinarySearchTree ft = bbst;
                Arrays.stream(line.trim().split(",")).mapToInt(Integer::parseInt).forEach(e -> ft.insert(e));
                System.out.println("root = [" + bbst.getRoot().getData() + "]节点的平衡因子：" + bbst.getRoot().getBalanceFactor());
                System.out.println("root = [" + bbst.getRoot().getData() + "]节点的高度：" + bbst.getRoot().getHeight());
            }
        }
        scanner.close();
    }
}
