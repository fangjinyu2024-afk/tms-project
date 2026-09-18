package com.zxinfotek.tms.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.zxinfotek.tms")
@MapperScan("com.zxinfotek.tms.**.mapper")
@EnableScheduling
public class TmsAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmsAdminApplication.class, args);
    }
}
