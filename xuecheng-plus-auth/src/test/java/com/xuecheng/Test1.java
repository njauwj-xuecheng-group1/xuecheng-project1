package com.xuecheng;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author: wj
 * @create_time: 2023/5/18 23:44
 * @explain:
 */

public class Test1 {

    @Test
    public void test() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        System.err.println(bCryptPasswordEncoder.matches("111111", "$2a$10$nxPKkYSez7uz2YQYUnwhR.z57km3yqKn3Hr/p1FR6ZKgc18u.Tvqm"));

    }
}




