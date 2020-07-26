package com.kilobytech.treeprinter;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author huangtao
 * @Title: BalanceBinarySearchTree
 * @Description: 平衡二叉搜索树
 * @date 2020/7/22
 */
@Slf4j
public class BalanceBinarySearchTree {

    // 单纯为了打印方便存的集合，跟数据结构本身没关系
    private List<Node> all = new ArrayList<>();

    private enum RotateType {
        L, // 当前节点单次左旋
        R, // 当前节点单次右旋
        CLR, // 右子节点左旋然后当前节点右旋
        CRL, // 左子节点右旋然后当前节点左旋
        ;
    }

    private enum DeleteType {
        NIL, // 无子节点
        LEFT_ONLY, // 只有左节点
        RIGHT_ONLY, // 只有右节点
        FULL, // 既有左节点，又有右节点
        ;
    }

    public class Node implements INode<Integer> {
        // 数据
        private int data;
        // 父节点
        private Node parent;
        // 左子节点
        private Node left;
        // 右子节点
        private Node right;
        // 平衡因子 = 左子树高度 - 右子树高度
        private int balanceFactor;
        // 高度
        private int height;

        public Node(int data) {
            this.data = data;
            this.height = 1;
        }

        @Override
        public Integer getData() {
            return data;
        }

        public void setData(int data) {
            this.data = data;
        }

        @Override
        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        @Override
        public Node getLeft() {
            return this.left;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        @Override
        public Node getRight() {
            return this.right;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public int getBalanceFactor() {
            return balanceFactor;
        }

        public int getHeight() {
            return height;
        }

        @Override
        public String toString() {
            return "[" + this.data + "]";
        }

        @Override
        public int compareTo(Integer o) {
            return this.data - o.intValue();
        }
    }

    // 树的根节点
    private Node root;

    // 数据节点个数
    private int size;

    public List<Node> getAll() {
        return all;
    }

    public Node getRoot() {
        return root;
    }

    public int getSize() {
        return size;
    }

    /**
     * 删除数据
     * @param data
     * @return
     */
    public boolean delete(int data) {
        Node delete = search(data, root);
        if (Objects.isNull(delete)) {
            return false;
        }
        Node reBalanceStart = searchAndUnmount(delete);
        reBalance(reBalanceStart);
        all = all.stream().filter(e -> e.data != data).collect(Collectors.toList());
        size--;
        return true;
    }

    /**
     * 搜索并卸载节点
     *
     * @param delete
     */
    private Node searchAndUnmount(Node delete) {
        if (Objects.isNull(delete)) {
            throw new IllegalArgumentException("删除的节点不能为空");
        }
        /**
         * 1.若 delete 为叶子节点，直接删除，然后调整平衡
         * 2.若 delete 为只有一棵子树的节点，直接删除该节点，并将其子树的父节点设置
         *   为 delete 的父节点，delete 父节点对应的子树节点设置为这棵子树，然后调
         *   整平衡
         * 3.若 delete 有两棵子树，则分情况处理：
         *  3.1.若 delete 的平衡因子为 0 或者 1，则搜寻左子树的最大值，即搜索左子
         *      树的最右边的节点记为 P，然后交换 delete 和 这个 P 的数据，然后将
         *      P 标记为要删除的节点，于是情况就变成了上面所讲的 1 或者 2
         *  3.2.若 delete 的平衡因子为 -1，则搜寻右子树的最小值，即搜索右子树的最
         *      左边的节点记为 Q，然后交换 delete 和 这个 Q 的数据，然后将 Q 标记
         *      为要删除的节点，于是情况就变成了上面所讲的 1 或者 2
         */
        DeleteType deleteType = getDeleteType(delete);
        Node p = delete.parent;
        switch (deleteType) {
            case NIL:
                log.info("被删除节点[" + delete.data + "]为叶子节点");
                if (delete == root) {
                    root = null;
                    return null;
                } else {
                    deleteChildFromParent(delete, p);
                    return p;
                }
            case LEFT_ONLY:
                log.info("被删除节点[" + delete.data + "]仅有左节点");
                Node leftChild = delete.left;
                connectParentWithGrandchild(delete, p, leftChild);
                return leftChild;
            case RIGHT_ONLY:
                log.info("被删除节点[" + delete.data + "]仅有右节点");
                Node rightChild = delete.right;
                connectParentWithGrandchild(delete, p, rightChild);
                return rightChild;
            default:
                log.info("被删除节点[" + delete.data + "]既有左节点，又有右节点");
                int dBF = delete.balanceFactor;
                Node newDelete;
                if (dBF >= 0) {
                    newDelete = search4Maximum(delete.left);
                } else {
                    newDelete = search4Minimum(delete.right);
                }
                swapValue(delete, newDelete);
                return searchAndUnmount(newDelete);
        }
    }

    /**
     * 交换两个节点的数据
     * @param delete
     * @param newDelete
     */
    private void swapValue(Node delete, Node newDelete) {
        delete.data = delete.data ^ newDelete.data;
        newDelete.data = delete.data ^ newDelete.data;
        delete.data = delete.data ^ newDelete.data;

    }

    /**
     * 从父节点上删除指定节点
     * @param delete
     * @param parent
     */
    private void deleteChildFromParent(Node delete, Node parent) {
        if (parent.left == delete) {
            parent.left = null;
        } else {
            parent.right = null;
        }
    }

    /**
     * 中间节点要被删除时，建立父节点和孙节点的关系
     * @param delete
     * @param parent
     * @param grandchild
     */
    private void connectParentWithGrandchild(Node delete, Node parent, Node grandchild) {
        if (parent.left == delete) {
            parent.left = grandchild;
        } else {
            parent.right = grandchild;
        }
        grandchild.parent = parent;
    }

    /**
     * 计算删除节点类型
     * @param node
     * @return
     */
    private DeleteType getDeleteType(Node node) {
        if (Objects.nonNull(node.left) && Objects.nonNull(node.right)) {
            return DeleteType.FULL;
        } else if (Objects.nonNull(node.left)) {
            return DeleteType.LEFT_ONLY;
        } else if (Objects.nonNull(node.right)) {
            return DeleteType.RIGHT_ONLY;
        } else {
            return DeleteType.NIL;
        }
    }

    /**
     * 搜索
     * @param data
     * @param search
     * @return
     */
    public Node search(int data, Node search) {
        if (Objects.isNull(search)) {
            log.warn("未找到节点 [" + data + "]");
            return null;
        }
        if (data < search.data) {
            log.info("节点[" + data + "] 比当前搜寻节点 [" + search.data + "] 小，继续往左搜寻");
            return search(data, search.left);
        } else if (data > search.data) {
            log.info("节点[" + data + "] 比当前搜寻节点 [" + search.data + "] 大，继续往右搜寻");
            return search(data, search.right);
        }
        log.info("节点[" + data + "] 等于当前搜寻节点 [" + search.data + "] 停止搜寻");
        return search;
    }

    /**
     * 从指定节点开始搜索最大值
     * @param start
     * @return
     */
    public Node search4Maximum(Node start) {
        if (Objects.isNull(start.right)) {
            return start;
        }
        return search4Maximum(start.right);
    }

    /**
     * 从指定节点开始搜索最小值
     * @param start
     * @return
     */
    public Node search4Minimum(Node start) {
        if (Objects.isNull(start.left)) {
            return start;
        }
        return search4Minimum(start.left);
    }

    /**
     * 插入新值
     * @param data
     * @return
     */
    public boolean insert(int data) {
        List<Node> exists = all.parallelStream().filter(e -> e.data == data).collect(Collectors.toList());
        if (exists.size() > 0) {
            log.warn("节点 [" + data + "] 已存在，换个数字好吗 (^_^)!");
            return false;
        }
        Node newNode = new Node(data);

        // 埋点
        all.add(newNode);
        if (Objects.isNull(root)) {
            root = newNode;
            return true;
        }
        // 搜索并将新节点挂载
        Node reBalance = searchAndMount(newNode, root);
        reBalance(reBalance);
        size++;
        return true;
    }

    /**
     * 沿着驱动节点一直平衡到 root 节点
     *
     * @param driver
     */
    private void reBalance(Node driver) {
        Node reBalance = driver;
        do {
            // 重新计算平衡因子和高度
            reCalculateBalanceFactorAndHeight(reBalance);
            // 旋转至平衡
            rotate2Balance(reBalance);
            reBalance = reBalance.parent;
        } while (Objects.nonNull(reBalance));
    }

    /**
     * 旋转至平衡
     */
    private void rotate2Balance(Node rotate) {
        log.info("节点 [" + rotate.data + "] recalculate BF: " + rotate.balanceFactor + " recalculate HG: " + rotate.height);
        if (CalculatorHelper.abs(rotate.balanceFactor) > 1) {
            log.info("节点 [" + rotate.data + "] 重新计算平衡因子后依然不平衡，开始旋转");
            RotateType rotateType = rotateType(rotate);
            switch (rotateType) {
                case R:
                    rightRotate(rotate);
                    break;
                case L:
                    leftRotate(rotate);
                    break;
                case CLR:
                    leftRotate(rotate.left);
                    rightRotate(rotate);
                    break;
                case CRL:
                    rightRotate(rotate.right);
                    leftRotate(rotate);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 获取旋转类型（L左旋、R右旋、CLR先将左子节点左旋，自己再右旋、CRL先将右子节点右旋，自己再左旋）
     *
     * @param node
     * @return
     */
    private RotateType rotateType(Node node) {
        boolean r = CalculatorHelper.rotateOrientation(node.balanceFactor) == 0;
        boolean leftChildNeedLeftRotate = false;
        if (Objects.nonNull(node.left)) {
            leftChildNeedLeftRotate = CalculatorHelper.isOppositeSign(node.balanceFactor, node.left.balanceFactor);
        }
        boolean rightChildNeedRightRotate = false;
        if (Objects.nonNull(node.right)) {
            rightChildNeedRightRotate = CalculatorHelper.isOppositeSign(node.balanceFactor, node.right.balanceFactor);
        }
        if (!r && rightChildNeedRightRotate) {
            return RotateType.CRL;
        }
        if (r && leftChildNeedLeftRotate) {
            return RotateType.CLR;
        }
        if (r) {
            return RotateType.R;
        } else {
            return RotateType.L;
        }
    }

    /**
     * 右旋
     *
     * @param node
     * @return
     */
    private Node rightRotate(Node node) {
        Node ndL = node.left;
        Node ndP = node.parent;
        Node ndLR = node.left.right;

        node.left = ndLR;
        if (Objects.nonNull(ndLR)) {
            ndLR.parent = node;
        }

        ndL.right = node;
        node.parent = ndL;

        if (Objects.nonNull(ndP)) {
            if (ndP.left == node) {
                ndP.left = ndL;
            } else {
                ndP.right = ndL;
            }
            ndL.parent = ndP;
        } else {
            root = ndL;
            root.parent = null;
        }

        node.height = calculateHeight(node);
        node.balanceFactor = calculateBalanceFactor(node);

        ndL.height = calculateHeight(ndL);
        ndL.balanceFactor = calculateBalanceFactor(ndL);
        log.info("[" + node.data + "] 节点右旋完毕，新节点[" + ndL.data + "]平衡因子：" + ndL.balanceFactor);
        log.info("[" + node.data + "] 节点右旋完毕，新节点[" + ndL.data + "]高度：" + ndL.height);
        return ndL;
    }

    /**
     * 左旋
     *
     * @param node
     * @return
     */
    private Node leftRotate(Node node) {
        Node ndR = node.right;
        Node ndP = node.parent;
        Node ndRL = node.right.left;

        node.right = ndRL;
        if (Objects.nonNull(ndRL)) {
            ndRL.parent = node;
        }

        ndR.left = node;
        node.parent = ndR;

        if (Objects.nonNull(ndP)) {
            if (ndP.left == node) {
                ndP.left = ndR;
            } else {
                ndP.right = ndR;
            }
            ndR.parent = ndP;
        } else {
            root = ndR;
            root.parent = null;
        }

        node.height = calculateHeight(node);
        node.balanceFactor = calculateBalanceFactor(node);

        ndR.height = calculateHeight(ndR);
        ndR.balanceFactor = calculateBalanceFactor(ndR);
        log.info("[" + node.data + "] 节点左旋完毕，新节点[" + ndR.data + "]平衡因子：" + ndR.balanceFactor);
        log.info("[" + node.data + "] 节点左旋完毕，新节点[" + ndR.data + "]高度：" + ndR.height);
        return ndR;
    }

    /**
     * 计算平衡因子
     * @param node
     * @return
     */
    private int calculateBalanceFactor(Node node) {
        int hL = Objects.isNull(node.left) ? 0 : node.left.height;
        int hR = Objects.isNull(node.right) ? 0 : node.right.height;
        return hL - hR;
    }

    /**
     * 计算高度
     * @param node
     * @return
     */
    private int calculateHeight(Node node) {
        int hL = Objects.isNull(node.left) ? 0 : node.left.height;
        int hR = Objects.isNull(node.right) ? 0 : node.right.height;
        return Integer.max(hL, hR) + 1;
    }

    /**
     * 增加新节点
     * @param newNode
     * @param mount
     */
    private void add(Node newNode, Node mount) {
        if (newNode.data > mount.data) {
            mount.right = newNode;
        } else {
            mount.left = newNode;
        }
        newNode.parent = mount;
    }

    /**
     * 搜索挂载节点，并将新节点挂载上去
     * @param newNode
     * @param mount
     * @return
     */
    public Node searchAndMount(Node newNode, Node mount) {
        // 如果新节点比当前搜寻的节点小，那么将当前搜寻节点的右节点赋给下一搜寻节点，
        // 如果新节点比当前搜寻的节点大（或者相等）,那么将当前搜寻节点的左节点赋给下一搜寻节点
        boolean smaller = newNode.data < mount.data;
        Node next = smaller ? mount.left : mount.right;
        // 如果下一搜寻节点为空，则代表搜寻到边际节点了
        if (Objects.isNull(next)) {
            // 将新节点挂载到指定节点上
            add(newNode, mount);
            return mount;
        } else {
            // 如果下一搜寻节点不为空，则代表还可以继续搜寻
            return searchAndMount(newNode, next);
        }
    }

    /**
     * 重新计算平衡因子和高度
     */
    private void reCalculateBalanceFactorAndHeight(Node node) {
        node.height = calculateHeight(node);
        node.balanceFactor = calculateBalanceFactor(node);
        log.info("节点 [" + node.data + "] recalculate BF: " + node.balanceFactor + " recalculate HG: " + node.height);
    }

}
