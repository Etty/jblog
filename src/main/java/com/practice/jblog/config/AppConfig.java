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
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.net.ssl.SSLContext;

@Configuration
@PropertySource("classpath:env.properties")
@EnableTransactionManagement
public class AppConfig {
    @Value("${elastic.host}")
    private String elasticHost;

    @Value("${elastic.port}")
    private int elasticPort;

    @Value("${elastic.security_enabled}")
    private boolean elasticSecurityEnabled;

    @Value("${elastic.cert_fingerprint}")
    private String elasticCertFingerprint;

    @Value("${elastic.api_key}")
    private String elasticApiKey;

    @Value("${elastic.user}")
    private String elasticUser;

    @Value("${elastic.password}")
    private String elasticPassword;



    @Bean
    public ElasticsearchClient elasticsearchClient() {
        if (elasticSecurityEnabled) {
            return getEsClientSecure();
        } else {
            return getEsClient();
        }
    }

    private ElasticsearchClient getEsClient() {
        String serverUrl = "http://" + elasticHost + ":" + elasticPort;
        String apiKey = elasticApiKey;

        RestClient restClient = RestClient
                .builder(HttpHost.create(serverUrl))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization", "ApiKey " + apiKey)
                })
                .build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    private ElasticsearchClient getEsClientSecure() {
        SSLContext sslContext = TransportUtils
                .sslContextFromCaFingerprint(elasticCertFingerprint);

        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(
                AuthScope.ANY, new UsernamePasswordCredentials(elasticUser, elasticPassword)
        );

        RestClient restClient = RestClient
                .builder(new HttpHost(elasticHost, elasticPort, "https"))
                .setHttpClientConfigCallback(hc -> hc
                        .setSSLContext(sslContext)
                        .setDefaultCredentialsProvider(credsProv)
                )
                .build();

        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
}
