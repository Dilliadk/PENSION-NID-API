package com.pmo.nid.config;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpsUrlConnectionMessageSender;

@Configuration
public class SoapClientConfig {
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("com.pmo.nid.soap.schemas");
        return marshaller;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller) {
        try {
            WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
            webServiceTemplate.setMarshaller(marshaller);
            webServiceTemplate.setUnmarshaller(marshaller);
            // webServiceTemplate.setDefaultUri("https://pmo.eservices.central.nids.gov.np");
            webServiceTemplate.setDefaultUri("https://pmo.eservices.central.nids.gov.np:443/EServices/services/g2g");
            webServiceTemplate.setMessageSender(httpsUrlConnectionMessageSender());
            return webServiceTemplate;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Bean
    public HttpsUrlConnectionMessageSender httpsUrlConnectionMessageSender() throws Exception {
        // HttpsUrlConnectionMessageSender messageSender = new HttpsUrlConnectionMessageSender();

        // Keystore (p12) => client authentication
        KeyStore clientKeyStore = KeyStore.getInstance("PKCS12");
        try (InputStream keyStoreStream = new ClassPathResource("certs/pmo_tls.p12").getInputStream()) {
            clientKeyStore.load(keyStoreStream, "b3xeOOmDlznFVaiHiLqG".toCharArray());
        }
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(clientKeyStore, "b3xeOOmDlznFVaiHiLqG".toCharArray());

        // Truststore (pem)
        // KeyStore trustStorePem = KeyStore.getInstance(KeyStore.getDefaultType());
        // trustStorePem.load(null, null);

        // try (InputStream trustStoreStream = new ClassPathResource("certs/truststore.pem").getInputStream()) {
        //     CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        //     X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(trustStoreStream);
        //     trustStorePem.setCertificateEntry("alias", certificate);
        // }

        // TrustManagerFactory trustManagerFactoryPem = TrustManagerFactory
        //         .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        // trustManagerFactoryPem.init(trustStorePem);

        // --- Truststore (for trusting the server) ---
        KeyStore serverTrustStore = KeyStore.getInstance("JKS");
        try (InputStream trustStoreStream = new ClassPathResource("certs/truststore.jks").getInputStream()) {
            serverTrustStore.load(trustStoreStream, "nid@123".toCharArray());
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(serverTrustStore);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2"); // Using a specific, common protocol
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        HttpsUrlConnectionMessageSender messageSender = new HttpsUrlConnectionMessageSender();
        messageSender.setSslSocketFactory(sslContext.getSocketFactory());
        messageSender.setHostnameVerifier((hostname, session) -> true);

        // messageSender.setSslProvider(sslContext.getProvider().getName());
        // messageSender.setSslProtocol(sslContext.getProtocol());
        // messageSender.setKeyManagers(keyManagerFactory.getKeyManagers());
        // messageSender.setTrustManagers(trustManagerFactory.getTrustManagers());
        // messageSender.setHostnameVerifier((hostname, session) -> true); // Use with caution

        return messageSender;
    }
}
