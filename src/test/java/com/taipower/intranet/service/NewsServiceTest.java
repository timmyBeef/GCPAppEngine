package com.taipower.intranet.service;

import com.taipower.intranet.dto.SysUser;
import com.taipower.intranet.persistence.datasource.DataSourceNames;
import com.taipower.intranet.persistence.datasource.DynamicDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class NewsServiceTest {

    @Autowired
    CPSService userService;


//    @Test
//    public void findDistinctData() {
//        System.out.println("預設資料庫...");
//
//        System.out.println(service.findDistinctData());
//
//        dataSource.setDataSourceName("secondaryDataSource");
//
//        System.out.println("換資料庫後...");
//
//        System.out.println(service.findDistinctData2());
//
//    }
    @Test
    public void test() {
        String a = "DS02";
        DataSourceNames d = DataSourceNames.valueOf(a);

        log.info("para db : [{}]", d);
        SysUser user = userService.findUser(1);
        log.info("第一個 db : [{}]", user.toString());
        DynamicDataSource.setDataSource(d);

        SysUser user2 = userService.findUser(1);
        log.info("第二個 db : [{}]", user2.toString());
    }
}