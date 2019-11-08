package com.taipower.intranet;

import com.taipower.intranet.persistence.datasource.DynamicDataSourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Import({DynamicDataSourceConfig.class})
public class TaipowerIntranetApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaipowerIntranetApplication.class, args);
	}

}
