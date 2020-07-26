package com.kilobytech.treeprinter;

/**
 * @author huangtao
 * @Title: INode
 * @Description:
 * @date 2020/7/26
 */
public interface INode<T> {

    T getData();

    INode<T> getParent();

    INode<T> getLeft();

    INode<T> getRight();

    default int calculateDepth() {
        if (getParent() == null) {
            return 1;
        }
        return getParent().calculateDepth() + 1;
    }
}
