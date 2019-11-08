package com.taipower.intranet.ws.client;

import com.taipower.intranet.ws.UnTrustworthyTrustManager;
import com.taipower.intranet.ws.bind.test.GetCountryRequest;
import com.taipower.intranet.ws.bind.test.GetCountryResponse;
import com.taipower.intranet.ws.dto.WsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.ws.transport.http.HttpsUrlConnectionMessageSender;

import javax.net.ssl.TrustManager;

@Component
public class CountryClient {

    @Autowired
    private WsInfo wsData;

    @Autowired
    WebServiceTemplate wsTemplate;

    private static final Logger log = LoggerFactory.getLogger(CountryClient.class);

    public GetCountryResponse getCountry(String country) {

        GetCountryRequest request = new GetCountryRequest();
        request.setName(country);

        log.info("Requesting location for " + country);

        GetCountryResponse response = (GetCountryResponse) wsTemplate.marshalSendAndReceive(
                wsData.getTest(), request,
                        new SoapActionCallback(
                                "http://spring.io/guides/gs-producing-web-service/GetCountryRequest"));

        return response;
    }

    public GetCountryResponse getCountryBySSL(String country) {
        HttpsUrlConnectionMessageSender sender = new HttpsUrlConnectionMessageSender();
        sender.setTrustManagers(new TrustManager[] { new UnTrustworthyTrustManager() }); //ssl
        wsTemplate.setMessageSender(sender);

        GetCountryRequest request = new GetCountryRequest();
        request.setName(country);


        return (GetCountryResponse) wsTemplate.marshalSendAndReceive(wsData.getTest(), request);
    }

}