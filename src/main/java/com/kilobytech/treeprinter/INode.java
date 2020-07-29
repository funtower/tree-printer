package com.kilobytech.treeprinter;

/**
 * @author huangtao
 * @Title: INode
 * @Description:
 * @date 2020/7/26
 */
public interface INode<E extends Comparable> {

    E getData();

    INode<E> getParent();

    INode<E> getLeft();

    INode<E> getRight();

    /**
     * 从本节点开始向上递归计算深度
     * @return depth
     */
    default int calculateDepth() {
        if (getParent() == null) {
            return 1;
        }
        return getParent().calculateDepth() + 1;
    }

}
