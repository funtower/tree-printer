package com.kilobytech.treeprinter;

import java.util.Objects;

/**
 * @author huangtao
 * @Title: INode
 * @Description:
 * @date 2020/7/26
 */
public interface INode<E extends Comparable> {

    /**
     * 获取数据
     * @return
     */
    E getData();

    /**
     * 获取父节点
     * @return
     */
    INode<E> getParent();

    /**
     * 获取左节点
     * @return
     */
    INode<E> getLeft();

    /**
     * 获取右节点
     * @return
     */
    INode<E> getRight();

    /**
     * 从本节点开始向上递归计算深度
     * @return depth
     */
    default int calculateDepth() {
        if (Objects.isNull(getParent())) {
            return 1;
        }
        return getParent().calculateDepth() + 1;
    }

}
