package com.uio.monitor;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.fastjson.JSON;
import com.uio.monitor.vo.BillExcelDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;


/**
 * @author han xun
 * Date 2022/8/26 20:56
 * Description:
 */
@SpringBootTest
@Slf4j
public class ExcelUtilsTest {

    /**
     * 最简单的读
     * <p>3. 直接读即可
     */
    @Test
    public void simpleRead() {
        String fileName = "/Users/uio/Documents/8月开销记录.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 这里每次会读取100条数据 然后返回过来 直接调用使用数据就行
        EasyExcel.read(fileName, BillExcelDTO.class, new PageReadListener<BillExcelDTO>(dataList -> {
            for (BillExcelDTO demoData : dataList) {
                log.info("读取到一条数据{}", JSON.toJSONString(demoData));
            }
        })).sheet().doRead();
    }
}
