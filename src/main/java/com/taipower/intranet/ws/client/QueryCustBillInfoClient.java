package com.taipower.intranet.ws.client;

import com.taipower.intranet.ws.UnTrustworthyTrustManager;
import com.taipower.intranet.ws.bind.bill.GetBillInfoByAPP;
import com.taipower.intranet.ws.bind.bill.GetBillInfoByAPPResponse;
import com.taipower.intranet.ws.bind.bill.Result;
import com.taipower.intranet.ws.bind.bill.ResultApp;
import com.taipower.intranet.ws.dto.WsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpsUrlConnectionMessageSender;

import javax.net.ssl.TrustManager;

@Component
public class QueryCustBillInfoClient {

    @Autowired
    private WsInfo wsInfo;

    @Autowired
    WebServiceTemplate wsTemplate;

    private static final Logger log = LoggerFactory.getLogger(QueryCustBillInfoClient.class);

    public ResultApp getNBS(String custNo) {

        GetBillInfoByAPP request = new GetBillInfoByAPP();

        request.setCustNo(custNo);

        log.info("Requesting custNo: {}", custNo);

        String url = wsInfo.getHostUrl() + wsInfo.getQryCustBillInfo();

        log.info("url: {}", url);


        GetBillInfoByAPPResponse response = (GetBillInfoByAPPResponse) wsTemplate.marshalSendAndReceive(url, request);
        // resultcode: E, 代表錯誤
        return response.getReturn();
    }

    public Result getNBSBySSL(String custNo) {
        HttpsUrlConnectionMessageSender sender = new HttpsUrlConnectionMessageSender();
        sender.setTrustManagers(new TrustManager[] { new UnTrustworthyTrustManager() }); //ssl
        wsTemplate.setMessageSender(sender);

        GetBillInfoByAPP request = new GetBillInfoByAPP();
        request.setCustNo(custNo);

        String url = wsInfo.getHostUrl() + wsInfo.getQryCustBillInfo();

        return (Result) wsTemplate.marshalSendAndReceive(url, request);
    }



}