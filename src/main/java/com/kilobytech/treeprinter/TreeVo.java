package com.kilobytech.treeprinter;

import lombok.Data;

import java.util.List;

/**
 * @Title: TreeVo
 * @Description:
 * @author huangtao
 * @date 2020/7/31
 */
@Data
public class TreeVo {

    private NodeVo root;

    private List<NodeVo> nodes;

    private List<LineVo> lines;

}
