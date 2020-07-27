package com.kilobytech.treeprinter;

import com.kilobytech.treeprinter.BalanceBinarySearchTree.Node;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * @author huangtao
 * @Title: 可打印树
 * @Description:
 * @date 2020/7/25
 */
@Slf4j
public class PrintableTreeV2<T extends Comparable> {

    // 树的根节点
    private PrintableNode root;

    // 上次访问的节点的深度
    private int lastAccessDepth;

    // 树的高度
    private int maxHeight;

    // 打印的字符都装在这个容器中，这个容器用这个矩阵实现，以利用的矩阵有下标索引模拟每个打印字符位置的坐标信息
    private char[][] container;

    // 打印容器最大宽度
    private int containerWidth;

    // 每个节点元素打印的字符串长度
    private int displayContentMaxLength;

    /**
     * 每个节点打印的字符串长度 + 左右误差
     * 这里说一下我在测试过程中发现误差的概念，因为在计算节点元素偏移值的时候会涉及到除法，
     * 而除法会将除不尽的小数位舍去，那么这样就会造成个别几个节点元素的偏移值比预期值少一，
     * 这样就可能会造成本次偏移后把上次偏移的的节点元素字符给覆盖了，举个例子，比如预期显
     * 示的是[90][101],那么因为[90[的字符长度是4，[101]的字符长度是5，那么如果发生误差的
     * 情况下就会导致[101]这一串字符整体偏移少了一位，那么接下来若有个[1010]偏移未发生误
     * 差，由于[101]少偏移了一位，就会导致[1010]把他本该偏移的位置给占了，结果打印出来的效
     * 果就像这样[90] [101[1010]，看见没，那个[101]右边的括号没了，被[1010]的左边的括号
     * 覆盖了，所以，为了解决这个问题，我们在打印字符左右再加一个空格字符，就好比打印内容外
     * 面套一层壳子，这样就会变成 [90]   [101] [1010]，被覆盖的是空格字符，标签本身就没
     * 受到影响
     */
    private int unitBlockLength;

    // 每层访问的元素计数器
    private int countPerLevel;

    /**
     * 节点深度和打印容器行数的对应关系
     * 举个例子：
     *  1                [74]
     *  2                 ∧
     *  3                / \
     *  4               /   \
     *  5              /     \
     *  6             /       \
     *  7            /         \
     *  8           /           \
     *  9          /             \
     *  10       [63]          [94]
     *  11        ∧            ∧
     *  12       / \           / \
     *  13      /   \         /   \
     *  14     /     \       /     \
     *  15  [61]   [71]   [75]   [95]
     * 这样一棵二叉树，深度(depth)是1，打印的行数是1，所以他们的对应关系就是1 -> 1，
     * 深度是2，打印行数是10，所以他们的对应关系就是2 -> 10，同理可知3 -> 15，这个
     * 这个就是以深度(depth)为key，打印行为值的集合
     */
    private Map<Integer, Integer> depthNodePrintRowMapping;

    /**
     * 打印节点
     */
    private class PrintableNode implements INode<T> {
        // 父节点
        private PrintableNode parent;
        // 左子节点
        private PrintableNode left;
        // 右子节点
        private PrintableNode right;
        /**
         * 虚拟空节点显示的内容，比如当前节点是[30]节点的虚拟左子节点，那么显示的内容就是[L-NIL-30]，若是右子节点则显示[R-NIL-30]，
         * 若为[30]节点的虚拟左子节点右子节点，即[30]节点的孙子节点，那么显示的内容就是[L-R-NIL-30]，其他情况同理
         */
        private String nilParent;
        // 节点数据
        private final T data;
        // 节点的偏移量
        private int offset;
        // 打印节点的深度
        private final int printableNodeDepth;

        public PrintableNode(PrintableNode parent, PrintableNode left, PrintableNode right, T data, int printableNodeDepth) {
            this.parent = parent;
            this.left = left;
            this.right = right;
            this.data = data;
            this.printableNodeDepth = printableNodeDepth;
        }

        public PrintableNode(PrintableNode parent, PrintableNode left, PrintableNode right, T data, int printableNodeDepth, String nilParent) {
            this(parent, left, right, data, printableNodeDepth);
            this.nilParent = nilParent;
        }

        /**
         * 若当前节点不为空节点，就打印该节点的数据，否则就打印该节点的的非空父节点并加上 NIL 标识
         * @return
         */
        @Override
        public String toString() {
            String print = Objects.isNull(data) ? nilParent : String.valueOf(data);
            return "[" + print + "]";
        }

        @Override
        public T getData() {
            return this.data;
        }

        @Override
        public INode<T> getParent() {
            return this.parent;
        }

        @Override
        public INode<T> getLeft() {
            return this.left;
        }

        @Override
        public INode<T> getRight() {
            return this.right;
        }

    }

    /**
     * 从根节点开始搜索指定节点
     * @param data
     * @param search
     * @return
     */
    public <T extends Comparable> PrintableNode search(T data, PrintableNode search) {
        if (Objects.isNull(search)) {
            log.info("未找到节点 [" + data + "]");
            return null;
        }
        if (data.compareTo(search.getData()) < 0) {
            log.info("节点[" + data + "] 比当前搜寻节点 [" + search.getData() + "] 小，继续往左搜寻");
            return search(data, search.left);
        } else if (data.compareTo(search.getData()) > 0) {
            log.info("节点[" + data + "] 比当前搜寻节点 [" + search.getData() + "] 大，继续往右搜寻");
            return search(data, search.right);
        } else {
            log.info("节点[" + data + "] 等于当前搜寻节点 [" + search.getData() + "] 停止搜寻");
            return search;
        }
    }

    /**
     * 以平衡二叉树的根节点为起始复制一棵可打印的树
     * 平衡二叉树可能会有空节点，不方便打印，所以复
     * 制一棵一模一样的树，只不过在这个拷贝树上从上
     * 到下直到最底层，用虚拟子节点填满所有的空节点，
     * 举个例子：
     * 源平衡二叉树长这个样子
     *          3
     *          ^
     *        1   5
     *       /   /
     *      0   4
     * --------------------------------------------------------------------
     * 拷贝树长这个样子
     *         3
     *         ^
     *       /   \
     *     1      5
     *    / \    / \
     *   0  NIL 4  NIL
     * ---------------------------------------------------------------------
     * 问：为啥要这么处理一手？
     * 答：其实是因为源二叉树就是一棵普通的二叉树，打印空节点会错位，而拷贝后的可打印
     * 二叉树则包含了节点打印时的坐标偏移量，打印字符存储的矩阵容器及其宽高，节点显
     * 示宽度等信息，所以需要这么拷贝一棵树出来，原则上也是为了不影响原树的结构和属，
     * 毕竟打印只是打印而已，就好比类的 toString 方法因该只读取变量数据不写入变量数
     * 据是一个道理
     * 其实方法也很简单，也就是使用了广度优先搜索每层拷贝，将数据，高度这些信息把扒过来
     * 即可，但是平衡因子是暂时没用到，所以这个属性就不用拷贝过来了
     * @param bTreeRoot
     * @return
     */
    private PrintableNode deepClone(Node bTreeRoot) {
        // 队列先进先出
        Queue<Node> access = new LinkedList();
        // 根据给定的源树根节点复制一个可打印树的根节点
        PrintableNode pRoot = new PrintableNode(null, null, null, (T) bTreeRoot.getData(), 1);
        // 将根节点入队
        access.add(bTreeRoot);
        while (!access.isEmpty()) {
            // 若队列不为空，则出队该元素进行处理，并将其子节点入队
            Node node = access.poll();
            // 根据源树节点数据到拷贝树里进行搜索对应位置的节点
            PrintableNode pNode = search(node.getData(), pRoot);
            // 因为平衡二叉搜索树没有保存深度属性，所以我们可以通过向上递归搜索的方式计算出来该节点的深度信息
            int depth = pNode.calculateDepth();
            // 若节点的子节点为空则创建一个虚拟子节点，并设置该子节点的深度为当前节点深度+1，然后将其挂载到当前节点上，
            // 若不为空则复制节点数据并同样设置子节点深度为当前深度+1，然后将其挂载到当前节点上
            if (Objects.nonNull(node.getLeft())) {
                // 左子节点入队
                access.offer(node.getLeft());
                pNode.left = new PrintableNode(pNode, null, null, (T) node.getLeft().getData(), depth + 1);
            } else if (depth < bTreeRoot.getHeight()) {
                pNode.left = new PrintableNode(pNode, null, null, null, depth + 1, "L-NIL-" + pNode.getData());
            }
            if (Objects.nonNull(node.getRight())) {
                // 右子节点入队
                access.offer(node.getRight());
                pNode.right = new PrintableNode(pNode, null, null, (T) node.getRight().getData(), depth + 1);
            } else if (depth < bTreeRoot.getHeight()) {
                pNode.right = new PrintableNode(pNode, null, null, null, depth + 1, "R-NIL-" + pNode.getData());
            }
        }
        return pRoot;
    }

    /**
     * 用虚拟子节点填满为空的节点，同样使用的是广度优先搜索
     * @param pRoot
     */
    private void fillWithNilNode(PrintableNode pRoot) {
        // 访问队列
        Queue<PrintableNode> access = new LinkedList<>();
        // 可打印数的根节点入队
        access.offer(pRoot);
        while (!access.isEmpty()) {
            // 若队列不为空则出队一个节点
            PrintableNode poll = access.poll();
            if (poll.printableNodeDepth < this.maxHeight) {
                // 若该节点的深度小于最大的深度也就是整棵树的高度，并且子节点为空，就创建虚拟子节点并挂载到这个节点上
                if (Objects.isNull(poll.left)) {
                    poll.left = new PrintableNode(poll, null, null, null, poll.printableNodeDepth + 1, poll.nilParent.replaceAll("NIL", "L-NIL"));
                }
                // 左子节点入队
                access.offer(poll.left);
                if (Objects.isNull(poll.right)) {
                    poll.right = new PrintableNode(poll, null, null, null, poll.printableNodeDepth + 1, poll.nilParent.replaceAll("NIL", "R-NIL"));
                }
                // 右子节点入队
                access.offer(poll.right);
            } else {
                // 若该节点已经是最后一层了，那么这一层的数据量肯定是最大的，那么就需要
                // 将这一层的每个节点打印字符串长度进行统计并且设置全局最大的打印长度，
                // 这个值会在计算容器宽度的时候用到
                this.displayContentMaxLength = Math.max(this.displayContentMaxLength, poll.toString().length());
            }
        }
    }

    /**
     * 获取深度和打印行数的对应关系中打印行数的最大值，也就是总共要打印的行数
     * @return
     */
    private int getMaxLineNumber4Print() {
        return this.depthNodePrintRowMapping
                .values()
                .parallelStream()
                .collect(Collectors.toList())
                .stream()
                .collect(Collectors.summarizingInt(e -> e)).getMax();
    }

    public PrintableTreeV2(Node root) {
        if (Objects.isNull(root)) {
            return;
        }
        // 源树和可打印树的高度保持一致
        this.maxHeight = root.getHeight();
        // 将源树复制一份变成可打印树
        PrintableNode pRoot = deepClone(root);
        // 将子节点用虚拟子节点填满
        fillWithNilNode(pRoot);
        // 将处理好的可打印树的根节点设置为全局变量
        this.root = pRoot;
        // 上次访问的节点深度，默认从根节点开始访问
        this.lastAccessDepth = 1;
        // 深度和节点打印起始行的对应关系
        this.depthNodePrintRowMapping = new HashMap<>();
        // 节点元素打印的字符串长度
        this.unitBlockLength = this.displayContentMaxLength + 2;// 加上两个左右挪动误差字符
        // containerWidth 代表打印容器的最大宽度，也就是最底层的元素个数 * 2 ^ (树高度 - 1)
        this.containerWidth = this.unitBlockLength << (this.maxHeight - 1);
        // 第一层的打印行就是第一行
        this.depthNodePrintRowMapping.put(1, 1);
        // 将每一层的深度都映射到打印起始行并存入映射关系
        for (int i = 2; i <= this.maxHeight; i++) {
            // 根据深度计算当前层与上一层的行数差距
            int lineRowGap = getItemOfGap(i) + 1;
            // 对应关系：当前层数打印行 = 当前层与上层的行数差距 + 上一层的打印行数 + 1
            this.depthNodePrintRowMapping.put(i, this.depthNodePrintRowMapping.get(i - 1) + lineRowGap + 1);
        }
        // 取映射关系中的打印行的最大值，就是最后一行的行数了，起始行是1，不是0
        int maxRow = getMaxLineNumber4Print();
        /**
         * 到这就可以构建一个打印矩阵容器了，如下所示，行数可以从上面对应关系中取出来，列数就是容器最大的宽度
         *  ____________________
         * |____|____|_3__|____|
         * |____|__/_|__\_|____|
         * |____|_1__|___4|____|
         * |__/_|__\_|_/__|_\__|
         * |_0__|_NIL|NIL_|__5_|
         */
        this.container = new char[maxRow][this.containerWidth];
        // 给字符数组初始化值
        for (int i = 0; i < this.container.length; i++) {
            for (int j = 0; j < this.container[i].length; j++) {
                // 初始化容器每个元素
                this.container[i][j] = ' ';
            }
        }

        // countPerLevel 代表下一次访问每层第几个节点
        this.countPerLevel = 1;
    }

    /**
     * 根据深度值计算当前深度于上层深度之间的行数差距
     * gap(depth) = width * 1 / 2^(depth)
     * @param depth
     * @return
     */
    private int getItemOfGap(int depth) {
        return this.containerWidth / (1 << depth);
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
        // 分子的上限值
        int currentLevelMaxNumerator = (1 << lastAccessDepth) - 1;
        int numerator = 2 * countPerLevel - 1;
        if (numerator > currentLevelMaxNumerator) {
            throw new IllegalStateException("分子计算发生异常，预期应该不大于 " + currentLevelMaxNumerator + "，但结果等于 " + numerator);
        }
        int standardLeftOffset = this.containerWidth * numerator / (1 << lastAccessDepth);
        // 在标准左移基础上还要再减去单元块本身长度的二分之一
        return Math.max(standardLeftOffset - unitBlockLength / 2, 0);
    }

    /**
     * 构建打印单元
     * @param node
     * @return
     */
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

    /**
     * 构建打印容器
     */
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
            if (currentDepth > this.lastAccessDepth) {
                // 每层访问计数器复位到 1
                this.countPerLevel = 1;
                this.lastAccessDepth++;
            }
            int currentOffset = calculateOffset();
            System.arraycopy(unitBlock, 0, this.container[this.depthNodePrintRowMapping.get(this.lastAccessDepth) - 1], currentOffset, unitBlockLength);
            // 保存当前节点的偏移量
            poll.offset = currentOffset;
            // 略过第一层上面的连线
            if (this.lastAccessDepth != 1 && (this.depthNodePrintRowMapping.get(this.lastAccessDepth) - this.depthNodePrintRowMapping.get(this.lastAccessDepth - 1)) > 0) {
                /**
                 * 连线部分步骤：
                 * 一、计算出当前层与上一层之间的行数差距，例如以下这棵树，根节点处在容器的中间也就是整个容器宽度的1/2处，
                 * 而第二层分别处于1/4和3/4处，第三层分别处于1/8、3/8、5/8和7/8处，由此可以看出其每个节点到其父节点之
                 * 间的横向距离差距是容器宽度的[1/2^(深度) - 1/2^(深度+1)]，化简后得：差距 = 容器宽度 /2^(深度)，即：
                 * gap = containerWidth / (1 << depth)
                 * 1                        [20]
                 * 2                          ^
                 * 3                         / \
                 * 4                        /   \
                 * 5                       /     \
                 * 6                      /       \
                 * 7                     /         \
                 * 8                    /           \
                 * 9                   /             \
                 * 10                 /               \
                 * 11                /                 \
                 * 12               /                   \
                 * 13              /                     \
                 * 14             /                       \
                 * 15            /                         \
                 * 16           /                           \
                 * 17         [11]                        [30]
                 * 18           ^                           ^
                 * 19          / \                         / \
                 * 20         /   \                       /   \
                 * 21        /     \                     /     \
                 * 22       /       \                   /       \
                 * 23      /         \                 /         \
                 * 24     /           \               /           \
                 * 25    /             \             /             \
                 * 26  [7]          [19]       [L-NIL-30]    [R-NIL-30]
                 * 二、从上一行的横坐标位置开始打印"/"或"\"代表左、右子树连线，
                 * 每层计数器为奇数项时打印"/"，并且往左偏移，每下降一行偏离值减一，一直从上面那一层的位置开始往左打印到当前层的位置结束
                 * 每层计算器为偶数项时打印"\"，并且往右偏移，每下降一行偏离值加一，一直从上面那一层的位置开始往右打印到当前层的位置结束
                 * 这里需要注意一个细节，第一层那个位置本来奇数项打印完"/"后，又会被偶数项的"\"覆盖，就像下面这样，原因就是偶数项和奇数
                 * 项都会从这个位置开始打印，所以最好的解决办法就是奇数项跳过不打，偶数项的时候打"^"或者"∧"，这样的连线才是有灵魂的连线
                 *         [11]
                 *           \
                 *          / \
                 *         /   \
                 *        /     \
                 *       /       \
                 *      /         \
                 *     /           \
                 *    /             \
                 *  [7]          [19]
                 * 连线部分就搞定了！
                 */
                int item = getItemOfGap(this.lastAccessDepth);
                int startRow = this.depthNodePrintRowMapping.get(this.lastAccessDepth - 1);
                for (int i = 0; i < item + 1; i++) {
                    if (this.countPerLevel % 2 == 1) {
                        if (i != 0) {
                            this.container[startRow + i][poll.parent.offset + this.unitBlockLength / 2 - i] = '/';
                        }
                    } else {
                        // 这两个区别在于尖顶字符的大小，第一个大点写文件显示的话会好看点，但是在控制台会莫名其妙的偏移，第二个不会在控制台偏移，但是小点，看起来位置会偏上
//                        this.container[startRow + i][poll.parent.offset + this.unitBlockLength / 2 + i] = i == 0 ? '∧' : '\\';
                        this.container[startRow + i][poll.parent.offset + this.unitBlockLength / 2 + i] = i == 0 ? '^' : '\\';
                    }
                }

            }
            // 最后别忘了把每层的节点访问计数器加一
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

    public void print() {
        if (Objects.isNull(this.root)) {
            log.error("节点为空[NULL]");
            return;
        }
        buildPrintContainer();
        for (int i = 0; i < container.length; i++) {
//            挨个儿打印容器元素即可
//            for (int j = 0; j < container[i].length; j++) {
//                System.out.print(container[i][j]);
//            }
            // 当然也可以直接将整行字符数组创建成一个字符串来打印
            log.error(new String(container[i]));
        }
    }

}
