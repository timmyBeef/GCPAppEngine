
package com.taipower.intranet.ws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

@Configuration
public class WsConfiguration {
	private String defaultURI = "http://localhost:8081/ws";

	@Bean
	public Jaxb2Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		// this package must match the package in the <generatePackage> specified in
		// pom.xml
		//marshaller.setContextPath("intranet.ws.bind");
		marshaller.setPackagesToScan("com.taipower.intranet.ws.bind");

		return marshaller;
	}

	@Bean
	public WebServiceTemplate webServiceTemplate() {
		WebServiceTemplate wsTemplate = new WebServiceTemplate();
		wsTemplate.setMarshaller(marshaller());
		wsTemplate.setUnmarshaller(marshaller());
		wsTemplate.setDefaultUri(defaultURI);
		wsTemplate.setMessageSender(webServiceMessageSender());

		return wsTemplate;
	}

	@Bean
	public WebServiceMessageSender webServiceMessageSender() {
		HttpComponentsMessageSender httpComponentsMessageSender = new HttpComponentsMessageSender();
		// timeout for creating a connection
		httpComponentsMessageSender.setConnectionTimeout(300 * 1000);
		// when you have a connection, timeout the read blocks for
		httpComponentsMessageSender.setReadTimeout(300 * 1000);

		return httpComponentsMessageSender;
	}

}
