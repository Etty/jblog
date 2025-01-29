package com.practice.jblog.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.TransportUtils;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;

@Configuration
@PropertySource("classpath:env.properties")
@EnableTransactionManagement
public class AppConfig {
    @Value("${elastic.host}")
    private String elastic_host;

    @Value("${elastic.port}")
    private int elastic_port;

    @Value("${elastic.security_enabled}")
    private boolean elastic_security_enabled;

    @Value("${elastic.cert_fingerprint}")
    private String elastic_cert_fingerprint;

    @Value("${elastic.api_key}")
    private String elastic_api_key;

    @Value("${elastic.user}")
    private String elastic_user;

    @Value("${elastic.password}")
    private String elastic_password;



    @Bean
    public ElasticsearchClient elasticsearchClient() {
        if (elastic_security_enabled) {
            return getEsClientSecure();
        } else {
            return getEsClient();
        }
    }

    private ElasticsearchClient getEsClient() {
        String serverUrl = "http://" + elastic_host + ":" + elastic_port;
        String apiKey = elastic_api_key;

        RestClient restClient = RestClient
                .builder(HttpHost.create(serverUrl))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization", "ApiKey " + apiKey)
                })
                .build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());
        ElasticsearchClient esClient = new ElasticsearchClient(transport);

        return esClient;
    }

    private ElasticsearchClient getEsClientSecure() {
        SSLContext sslContext = TransportUtils
                .sslContextFromCaFingerprint(elastic_cert_fingerprint);

        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(
                AuthScope.ANY, new UsernamePasswordCredentials(elastic_user, elastic_password)
        );

        RestClient restClient = RestClient
                .builder(new HttpHost(elastic_host, elastic_port, "https"))
                .setHttpClientConfigCallback(hc -> hc
                        .setSSLContext(sslContext)
                        .setDefaultCredentialsProvider(credsProv)
                )
                .build();

        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient esClient = new ElasticsearchClient(transport);
        return esClient;
    }
}
