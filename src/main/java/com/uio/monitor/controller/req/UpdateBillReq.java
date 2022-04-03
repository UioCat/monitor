package com.uio.monitor.controller.req;

import com.uio.monitor.controller.resp.BillDTO;
import lombok.Data;

/**
 * @author han xun
 * Date 2022/3/12 21:15
 * Description:
 */
@Data
public class UpdateBillReq extends BillDTO {
    private Boolean deleted;
}
