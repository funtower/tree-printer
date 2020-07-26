package com.kilobytech.treeprinter;

import java.util.Comparator;

/**
 * @author huangtao
 * @Title: INode
 * @Description:
 * @date 2020/7/26
 */
public interface INode<T> extends Comparable<T> {

    T getData();

    INode<T> getParent();

    INode<T> getLeft();

    INode<T> getRight();

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
