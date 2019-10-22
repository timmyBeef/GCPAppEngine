package com.taipower.intranet.rest;


import lombok.extern.slf4j.Slf4j;
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
