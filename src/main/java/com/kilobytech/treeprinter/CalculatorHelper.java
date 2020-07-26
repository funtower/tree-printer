package com.kilobytech.treeprinter;

/**
 * @author huangtao
 * @Title: CalculatorHelper
 * @Description:
 * @date 2020/7/22
 */
public class CalculatorHelper {
    /**
     * 判断两个平衡因子是否异号
     * @param bf1
     * @param bf2
     * @return
     */
    public static boolean isOppositeSign(int bf1, int bf2) {
        return (bf1 ^ bf2) >>> 31 == 1;
    }

    /**
     * 判断旋转方向
     * @param bf
     * @return 1 代表左旋， 0 代表右旋
     */
    public static int rotateOrientation(int bf) {
        return bf >>> 31;
    }

    /**
     * 取绝对值
     * @param n
     * @return
     */
    public static int abs(int n) {
        int i = n >> 31;
        return ((n ^ i) - i);
    }

    /**
     * 交换两值
     */
//    private void swapValue(int a, int b) {
//        a = a ^ b;
//        b = a ^ b;
//        a = a ^ b;
//    }

}
