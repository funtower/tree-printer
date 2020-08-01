package com.kilobytech.treeprinter;

import lombok.Data;

import java.util.List;

/**
 * @Title: DeleteVo
 * @Description:
 * @author huangtao
 * @date 2020/8/1
 */
@Data
public class DeleteVo {
    private List<Integer> inputData;
    private Integer del;
}
