package org.hung;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CmdRunner2 implements CommandLineRunner{
	
	@Value("file:C:\\Users\\kwong\\Documents\\certs\\selfsigned\\ca\\my-rootCA.crt")
	private Resource trustCertFile;
	
	@Autowired
	private ReactorResourceFactory factory;
	
	@Override
	public void run(String... args) throws Exception {
	
		SslContextBuilder nettySslContextBuilder = 
		  SslContextBuilder
	        .forClient()
              .trustManager(trustCertFile.getInputStream());
				
		ReactorClientHttpConnector connector = new ReactorClientHttpConnector(factory,httpClient -> {
			httpClient.secure(spec -> spec.sslContext(nettySslContextBuilder));
			return httpClient;
		});
		
		WebClient client = 
		  WebClient
		    .builder()
		      .clientConnector(connector)
		        .baseUrl("https://localhost:8443")
		          .filter(ExchangeFilterFunctions.basicAuthentication("admin", "password"))
		            .build();

		client
		  .get()
		    .uri("/interval")
		    .accept(MediaType.APPLICATION_STREAM_JSON)  
		    .exchange()
		    .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Long.class))
		    .subscribe(l -> {
		    	log.info("I got a {}",l);
		    });

	}

	@Bean
	ReactorResourceFactory reactorResourceFactory() {
		ReactorResourceFactory factory = new ReactorResourceFactory();
		factory.setUseGlobalResources(false);
		return factory;
	}

}
