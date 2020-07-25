package com.kilobytech.treeprinter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
class TreePrinterApplicationTests {

    @Test
    void contextLoads() {
        int a = 1 << 2 + 1;
        System.out.println(a);
    }

    @Test
    void contextLoads2() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        map.put(2, 3);
        map.put(3, 4);
        List<Integer> collect = map.values().parallelStream().collect(Collectors.toList());
        IntSummaryStatistics statistics = collect.parallelStream().collect(Collectors.summarizingInt(e -> e));
        System.out.println(statistics.getSum());
        System.out.println(statistics.getMax());
    }

}
