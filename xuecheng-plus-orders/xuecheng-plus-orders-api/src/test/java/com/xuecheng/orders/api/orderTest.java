package com.xuecheng.orders.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.xuecheng.base.utils.QRCodeUtil.createQRCode;

/**
 * @author: wj
 * @create_time: 2023/5/22 22:35
 * @explain:
 */
public class orderTest {

    /**
     * 生成支付二维码
     *
     * @throws IOException
     */
    @Test
    public void createQR() throws IOException {
        System.out.println(createQRCode("http://192.168.31.245:63030/orders/alipaytest", 200, 200));
    }


}
