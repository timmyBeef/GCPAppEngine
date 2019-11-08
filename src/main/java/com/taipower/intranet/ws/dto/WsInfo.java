package com.taipower.intranet.ws.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("taipower.ws.url")
public class WsInfo {

    String hostUrl;
    String qryCustBillInfo;
    String test;

}
