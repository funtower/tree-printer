package com.kilobytech.treeprinter;

import com.kilobytech.treeprinter.BalanceBinarySearchTree.Node;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author huangtao
 * @Title: 可打印树
 * @Description:
 * @date 2020/7/25
 */
@Slf4j
public class PrintableTreeV1 {

    private PrintableNode root;

    private int depth;

    private int maxHeight;

    private char[][] container;

    private int containerWidth;

    private int unitBlockLength;

    private int countPerLevel;

    private int displayContentMaxLength;

    private static class PrintableNode {
        private PrintableNode parent;
        private PrintableNode left;
        private PrintableNode right;
        private String nilParent;
        private int data;

        private int printableNodeDepth;

        public PrintableNode(PrintableNode parent, PrintableNode left, PrintableNode right, int data, int printableNodeDepth) {
            this.parent = parent;
            this.left = left;
            this.right = right;
            this.data = data;
            this.printableNodeDepth = printableNodeDepth;
        }

        public PrintableNode(PrintableNode parent, PrintableNode left, PrintableNode right, int data, int printableNodeDepth, String nilParent) {
            this(parent, left, right, data, printableNodeDepth);
            this.nilParent = nilParent;
        }

        @Override
        public String toString() {
            String print = data == Integer.MIN_VALUE ? nilParent : String.valueOf(data);
            return "[" + print + "]";
        }
    }

    public PrintableNode search(int data, PrintableNode search) {
        if (Objects.isNull(search)) {
            System.err.println("未找到节点 [" + data + "]");
            return null;
        }
        if (data < search.data) {
            System.err.println("节点[" + data + "] 比当前搜寻节点 [" + search.data + "] 小，继续往左搜寻");
            return search(data, search.left);
        } else if (data > search.data) {
            System.err.println("节点[" + data + "] 比当前搜寻节点 [" + search.data + "] 大，继续往右搜寻");
            return search(data, search.right);
        } else {
            System.err.println("节点[" + data + "] 等于当前搜寻节点 [" + search.data + "] 停止搜寻");
            return search;
        }
    }

    private int calculateDepth(PrintableNode node) {
        if (node.parent == null) {
            return 1;
        }
        return calculateDepth(node.parent) + 1;
    }

    private PrintableNode deepClone(Node bTreeRoot) {
        Queue<Node> access = new LinkedList();
        PrintableNode pRoot = new PrintableNode(null, null, null, bTreeRoot.getData(), 1);
        access.add(bTreeRoot);

        while (!access.isEmpty()) {
            Node node = access.poll();
            PrintableNode pNode = search(node.getData(), pRoot);
            int depth = calculateDepth(pNode);
            if (Objects.nonNull(node.getLeft())) {
                access.offer(node.getLeft());
                pNode.left = new PrintableNode(pNode, null, null, node.getLeft().getData(), depth + 1);
            } else if (depth < bTreeRoot.getHeight()) {
                pNode.left = new PrintableNode(pNode, null, null, Integer.MIN_VALUE, depth + 1, "NIL-" + pNode.data);
            }
            if (Objects.nonNull(node.getRight())) {
                access.offer(node.getRight());
                pNode.right = new PrintableNode(pNode, null, null, node.getRight().getData(), depth + 1);
            } else if (depth < bTreeRoot.getHeight()) {
                pNode.right = new PrintableNode(pNode, null, null, Integer.MIN_VALUE, depth + 1, "NIL-" + pNode.data);
            }
        }
        return pRoot;
    }

    public PrintableTreeV1(Node root) {
        this.maxHeight = root.getHeight();
        PrintableNode pRoot = deepClone(root);
        fillWithNilNode(pRoot);
        this.root = pRoot;
        this.depth = 1;
        this.unitBlockLength = this.displayContentMaxLength + 2;// 加上两个左右挪动误差字符
        // containerWidth 代表打印容器的最大宽度，也就是最底层的元素个数 * 2 ^ (树高度 - 1)
        this.containerWidth = this.unitBlockLength << (this.maxHeight - 1);
        this.container = new char[2 * this.maxHeight - 1][this.containerWidth];

        // 给字符数组初始化值
        for (int i = 0; i < this.container.length; i++) {
            for (int j = 0; j < this.container[i].length; j++) {
                this.container[i][j] = ' ';
            }
        }

        // countPerLevel 代表下一次访问每层第几个节点
        this.countPerLevel = 1;
    }

    private void fillWithNilNode(PrintableNode pRoot) {
        Queue<PrintableNode> access = new LinkedList<>();
        access.offer(pRoot);
        while (!access.isEmpty()) {
            PrintableNode poll = access.poll();
            if (poll.printableNodeDepth < this.maxHeight) {
                if (Objects.isNull(poll.left)) {
                    poll.left = new PrintableNode(poll, null, null, Integer.MIN_VALUE, poll.printableNodeDepth + 1, "NIL-" + poll);
                }
                access.offer(poll.left);
                if (Objects.isNull(poll.right)) {
                    poll.right = new PrintableNode(poll, null, null, Integer.MIN_VALUE, poll.printableNodeDepth + 1, "NIL-" + poll);
                }
                access.offer(poll.right);
            } else {
                this.displayContentMaxLength = Math.max(this.displayContentMaxLength, poll.toString().length());
            }
        }
    }

    public void print() {
        buildPrintContainer();
        for (int i = 0; i < container.length; i++) {
//            for (int j = 0; j < container[i].length; j++) {
//                System.out.print(container[i][j]);
//            }
            System.out.println(new String(container[i]));
//            try {
//                Files.write(Paths.get("C:\\Users\\huangtao\\Desktop\\diirdir\\print.txt"),
//                        new String(container[i]).getBytes(Charset.forName("UTF-8")),
//                        StandardOpenOption.APPEND);
//                Files.write(Paths.get("C:\\Users\\huangtao\\Desktop\\diirdir\\print.txt"),
//                        "\r\n".getBytes(Charset.forName("UTF-8")),
//                        StandardOpenOption.APPEND);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            System.out.println("\r\n\r\n");
        }
    }

    public void buildPrintContainer() {
        // 等待被访问的节点队列
        Queue<PrintableNode> q = new ConcurrentLinkedQueue<>();
        // 先往队列里塞一个起始节点，这个节点作为驱动节点
        q.offer(root);
        // 若队列不为空，则出队一个节点，将它塞入已被访问节点，并将其邻接节点全部入队
        while (!q.isEmpty()) {
            // 出队一个节点
            PrintableNode poll = q.poll();
            // 处理这个节点
            log.info("正在处理节点" + poll);
            char[] unitBlock = buildUnitBlock(poll);

            int currentDepth = poll.printableNodeDepth;
            // 当前访问层深度大于上次访问层深度时就代表，访问层下降了一个层
            if (currentDepth > this.depth) {
                // 每层访问计数器复位到 1
                this.countPerLevel = 1;
                this.depth++;
            }
            int currentOffset = calculateOffset();
            System.arraycopy(unitBlock, 0, this.container[2 * (this.depth - 1)], currentOffset, unitBlockLength);
            this.countPerLevel++;
            // 将其左、右子树都放入队列
            if (Objects.nonNull(poll.left)) {
                q.offer(poll.left);
            }
            if (Objects.nonNull(poll.right)) {
                q.offer(poll.right);
            }
        }
    }

    /**
     * 深度	    左偏移量
     * 1		width * (1 / 2 )
     * 2		width * (1 / 4 )	width * (3 / 4 )
     * 3		width * (1 / 8 )	width * (3 / 8 )	width * (5 / 8 )	width * (7 / 8 )
     * 4		width * (1 / 16)	width * (3 / 16)	width * (5 / 16)	width * (7 / 16)	width * (9 / 16)	width * (11 / 16)	width * (13 / 16)	width * (15 / 16)
     * ……
     * n 		width * (1 / (1 << n)) 	width * (3 / (1 << n)) 	width * (2 * countPerLevel - 1 / (1 << n))    分子按照奇数项递增…… 		width * ((1 << n) - 1 / (1 << n))
     * 解释一下：
     * width 代表打印容器的最大宽度，也就是最底层的元素个数 * 2 ^ (树高度 - 1)
     * countPerLevel 代表下一次访问每层第几个节点，在同一层每访问一个节点，该值就会加一，当该层访问完了之后，深度 depth 就会加一，并且这个时候 countPerLevel 值会复位到 1
     *
     * @return 左偏移量
     */
    private int calculateOffset() {
        // 最大
        int currentLevelMaxNumerator = (1 << depth) - 1;
        int numerator = 2 * countPerLevel - 1;
        if (numerator > currentLevelMaxNumerator) {
            throw new IllegalStateException("分子计算发生异常，预期应该不大于 " + currentLevelMaxNumerator + "，但结果等于 " + numerator);
        }
        int standardLeftOffset = this.containerWidth * numerator / (1 << depth);
        // 在标准左移基础上还要再减去单元块本身长度的二分之一
        return Math.max(standardLeftOffset - unitBlockLength / 2, 0);
    }

    public char[] buildUnitBlock(PrintableNode node) {
        char[] unitBlock = new char[unitBlockLength];
        Arrays.fill(unitBlock, ' ');
        if (Objects.nonNull(node)) {
            String nodeContent = node.toString();
            char[] data = nodeContent.toCharArray();
            int dataLen = nodeContent.length();
            int locationIndex = Math.max(unitBlockLength / 2 - dataLen / 2, 0);
            System.arraycopy(data, 0, unitBlock, locationIndex, data.length);
        }
        return unitBlock;
    }

    private int calculateLineLeftOffset(int unitLineLength) {
        // 最大
        int currentLevelMaxNumerator = (1 << depth) + 1;
        int numerator = 2 * countPerLevel + 1;
        if (numerator > currentLevelMaxNumerator) {
            throw new IllegalStateException("分子计算发生异常，预期应该不大于 " + currentLevelMaxNumerator + "，但结果等于 " + numerator);
        }
        int standardLeftOffset = this.containerWidth * numerator / (1 << depth + 1);
        // 在标准左移基础上还要再减去单元块本身长度的二分之一
        return Math.max(standardLeftOffset - unitLineLength / 2, 0);
    }

    public char[] buildUnitLine(char lineCharacter) {
        return new char[]{' ', lineCharacter, ' '};
    }

    public static void main(String[] args) throws IOException {
//        int w = 1 << 1;
//        log.info(w);

        BalanceBinarySearchTree btree = new BalanceBinarySearchTree();
        for (int i = 0; i < 20; i++) {
            btree.insert(RandomUtils.nextInt(0, 1000));
        }
//        btree.insert(19);
//        btree.insert(100);
//        btree.insert(190);
//        btree.insert(200);

        PrintableTreeV1 print = new PrintableTreeV1(btree.getRoot());
        print.print();
        System.in.read();
    }
}
