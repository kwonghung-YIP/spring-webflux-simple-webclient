package org.hung;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Component
public class CmdRunner implements CommandLineRunner{

	@Value("file:C:\\Users\\kwong\\Documents\\certs\\selfsigned\\ca\\my-rootCA.crt")
	private Resource trustCertFile;
	
	@Override
	public void run(String... args) throws Exception {
		SslContextBuilder nettySslContextBuilder = 
		  SslContextBuilder
	        .forClient()
              .trustManager(trustCertFile.getInputStream());
        
		HttpClient reactorHttpClient = 
		  HttpClient
		    .create()
		      .secure(spec -> spec.sslContext(nettySslContextBuilder));
		    
		WebClient client = 
		  WebClient
		    .builder()
		      .clientConnector(new ReactorClientHttpConnector(reactorHttpClient))
		        .baseUrl("https://localhost:8443")
		          .filter(ExchangeFilterFunctions.basicAuthentication("admin", "password"))
		            .build();
		
		client
		  .get()
		    .uri("/interval")
		      .retrieve()
		        .bodyToMono(Long.class)
		          .block();
	}

}
