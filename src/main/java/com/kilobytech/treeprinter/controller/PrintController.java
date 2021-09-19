package com.kilobytech.treeprinter.controller;

import com.kilobytech.treeprinter.service.PrintService;
import com.kilobytech.treeprinter.tree.BalanceBinarySearchTree;
import com.kilobytech.treeprinter.tree.PrintableTree;
import com.kilobytech.treeprinter.vo.DeleteVo;
import com.kilobytech.treeprinter.vo.InsertVo;
import com.kilobytech.treeprinter.vo.TreeVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author huangtao
 * @Title: PrintController
 * @Description:
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
