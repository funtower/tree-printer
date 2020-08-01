package com.kilobytech.treeprinter;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Title: PrintService
 * @Description:
 * @author huangtao
 * @date 2020/7/31
 */
@Service
public class PrintService {

    public <E extends Comparable> TreeVo build(PrintableTree<E> sourceTree) {
        TreeVo treeVo = new TreeVo();
        List<NodeVo> nodes = new ArrayList<>();
        ArrayList<LineVo> lines = new ArrayList<>();
        Queue<PrintableTree<E>.PrintableNode> access = new LinkedList<>();
        access.offer(sourceTree.getRoot());
        NodeVo root = new NodeVo();
        root.setData(sourceTree.getRoot().toString());
        root.setDepth(sourceTree.getRoot().getPrintableNodeDepth());
        root.setHorizontalOffsetPercent(sourceTree.getRoot().getHorizontalOffsetPercent());
        root.setVerticalOffsetPercent(sourceTree.getRoot().getVerticalOffsetPercent());
        while (!access.isEmpty()) {
            PrintableTree.PrintableNode poll = access.poll();
            NodeVo nodeVo = new NodeVo();
            nodeVo.setData(poll.toString());
            nodeVo.setDepth(poll.getPrintableNodeDepth());
            nodeVo.setHorizontalOffsetPercent(poll.getHorizontalOffsetPercent());
            nodeVo.setVerticalOffsetPercent(poll.getVerticalOffsetPercent());
            nodes.add(nodeVo);
            if (Objects.nonNull(poll.getParent())) {
                Optional<NodeVo> any = nodes.parallelStream().filter(e -> poll.getParent().toString().equalsIgnoreCase(e.getData())).findAny();
                lines.add(new LineVo(any.get(), nodeVo));
            }
            if (Objects.nonNull(poll.getLeft())) {
                access.offer((PrintableTree<E>.PrintableNode) poll.getLeft());
            }
            if (Objects.nonNull(poll.getRight())) {
                access.offer((PrintableTree<E>.PrintableNode) poll.getRight());
            }
        }
        treeVo.setRoot(root);
        treeVo.setNodes(nodes);
        treeVo.setLines(lines);
        return treeVo;
    }
}
