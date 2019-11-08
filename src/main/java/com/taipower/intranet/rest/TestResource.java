package com.taipower.intranet.rest;


import com.taipower.intranet.dto.APMCUST;
import com.taipower.intranet.persistence.datasource.DataSourceNames;
import com.taipower.intranet.persistence.datasource.DynamicDataSource;
import com.taipower.intranet.service.CPSService;
import com.taipower.intranet.ws.bind.bill.ResultApp;
import com.taipower.intranet.ws.client.QueryCustBillInfoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class TestResource {

    @Autowired
    private CPSService service;

    @Autowired
    private QueryCustBillInfoClient queryCustBillInfoClient;


    @GetMapping("/apmcust/{custNo}/{area}/list")
    public ResponseEntity<APMCUST> getAPMCUSTList(@PathVariable("custNo") String custNo,
                                                  @PathVariable("area") String area) {


        DataSourceNames ds = DataSourceNames.valueOf(area);
        DynamicDataSource.setDataSource(ds);

        APMCUST rs = service.getAPMCUSTList(custNo);

        return ResponseEntity.ok(rs);
    }

    @GetMapping("/customers/{custNo}/bill-info")
    public ResponseEntity<ResultApp> queryCustBillInfo(@PathVariable("custNo") String custNo) {
        log.info("queryCustBillInfo:{}", custNo);

        ResultApp response = queryCustBillInfoClient.getNBS(custNo);
        log.info("response:{}", response);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get")
    public ResponseEntity<List<String>> get() {

        List<String> rs = new ArrayList<String>();
        rs.add("axxxx");
        rs.add("axxxx");
        rs.add("axxxx");
        rs.add("axxxx");
        rs.add("axxxx");

        return ResponseEntity.ok(rs);
    }


    @PostMapping("/get")
    public ResponseEntity<List<String>> test() {

        List<String> rs = new ArrayList<String>();
        rs.add("axxxx");
        rs.add("axxxx");
        rs.add("axxxx");
        rs.add("axxxx");
        rs.add("axxxx");

        return ResponseEntity.ok(rs);
    }

}
