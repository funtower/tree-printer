package com.kilobytech.treeprinter;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @Title: PrintController
 * @Description:
 * @author huangtao
 * @date 2020/7/31
 */
@RestController
@RequestMapping("/print")
public class PrintController {

    @Resource
    private PrintService printService;

    private final ThreadLocal<BalanceBinarySearchTree<Integer>> tree = ThreadLocal.withInitial(() -> new BalanceBinarySearchTree<>());

    @GetMapping("/{count}")
    public TreeVo print(@PathVariable int count) {
        BalanceBinarySearchTree<Integer> sourceTree = tree.get();
        for (int i = 0; i < count; i++) {
            sourceTree.insert(i);
        }
        PrintableTree<Integer> pTree = new PrintableTree<>(sourceTree.getRoot());
        tree.remove();
        return printService.build(pTree);
    }

    @PostMapping("/insert")
    public TreeVo insert(@RequestBody InsertVo insertVo) {
        BalanceBinarySearchTree<Integer> sourceTree = getIntegerBalanceBinarySearchTree(insertVo.getInputData());
        PrintableTree<Integer> pTree = new PrintableTree<>(sourceTree.getRoot());
        tree.remove();
        return printService.build(pTree);
    }

    @PostMapping("/delete")
    public TreeVo delete(@RequestBody DeleteVo deleteVo) {
        BalanceBinarySearchTree<Integer> sourceTree = getIntegerBalanceBinarySearchTree(deleteVo.getInputData());
        sourceTree.delete(deleteVo.getDel());
        PrintableTree<Integer> pTree = new PrintableTree<>(sourceTree.getRoot());
        tree.remove();
        if (Objects.isNull(pTree.getRoot())) {
            return null;
        }
        return printService.build(pTree);
    }

    private BalanceBinarySearchTree<Integer> getIntegerBalanceBinarySearchTree(List<Integer> inputData) {
        BalanceBinarySearchTree<Integer> sourceTree = tree.get();
        inputData.forEach(e -> sourceTree.insert(e));
        return sourceTree;
    }
}
