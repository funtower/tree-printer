package com.kilobytech.treeprinter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: NodeVo
 * @Description:
 * @author huangtao
 * @date 2020/7/31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeVo {

    private String data;

    private int depth;

    // 水平偏移百分比
    private double horizontalOffsetPercent;

    // 垂直偏移百分比
    private double verticalOffsetPercent;
}
