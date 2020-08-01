package com.kilobytech.treeprinter;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Title: ILine
 * @Description:
 * @author huangtao
 * @date 2020/7/31
 */
@Data
@AllArgsConstructor
public class LineVo {
    private NodeVo from;
    private NodeVo to;
}
