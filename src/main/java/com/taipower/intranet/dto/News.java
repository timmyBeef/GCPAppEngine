package com.taipower.intranet.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {
    private String id;
    private String type;
    private String sourceType;
    private String title;
    private String newDate;
    private String photo;
    private String content;

}
