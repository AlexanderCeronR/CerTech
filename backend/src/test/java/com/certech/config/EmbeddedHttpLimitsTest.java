package com.certech.config;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.*;
import static org.junit.jupiter.api.Assertions.*;

/** Real local HTTP transport; no application scanning, database or migrations. */
@SpringBootTest(classes = EmbeddedHttpLimitsTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"server.address=127.0.0.1", "app.http.max-body-bytes=32",
                "server.max-http-request-header-size=1KB"})
class EmbeddedHttpLimitsTest {
    @LocalServerPort private int port;
    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext context;
    private final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class, FlywayAutoConfiguration.class})
    @Import({RequestBodyLimitFilter.class, ProbeController.class})
    static class TestApplication {}

    @RestController
    static class ProbeController {
        @PostMapping("/probe")
        String probe(@RequestBody String body, HttpServletRequest request) {
            return request.getRemoteAddr() + "|" + request.getScheme() + "|" + body;
        }
    }

    private HttpRequest.Builder request() {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/probe"))
                .timeout(java.time.Duration.ofSeconds(10)).header("Content-Type", "text/plain");
    }

    @Test
    void forwardedHeadersCannotChangeAddressOrScheme() throws Exception {
        HttpResponse<String> response = client.send(request()
                .header("Forwarded", "for=198.51.100.9;proto=https;host=untrusted.example")
                .header("X-Forwarded-For", "198.51.100.9")
                .header("X-Forwarded-Proto", "https")
                .POST(HttpRequest.BodyPublishers.ofString("accepted")).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("127.0.0.1|http|accepted", response.body());
    }

    @Test
    void acceptsExactLimitAndRejectsLargerFixedAndChunkedBodies() throws Exception {
        assertEquals(200, client.send(request().POST(HttpRequest.BodyPublishers.ofString("x".repeat(32))).build(),
                HttpResponse.BodyHandlers.ofString()).statusCode());
        for (HttpRequest.BodyPublisher publisher : new HttpRequest.BodyPublisher[] {
                HttpRequest.BodyPublishers.ofString("x".repeat(33)),
                HttpRequest.BodyPublishers.ofInputStream(() -> new ByteArrayInputStream(new byte[33]))}) {
            HttpResponse<String> response = client.send(request().POST(publisher).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(413, response.statusCode());
            assertTrue(response.body().contains("message"));
        }
    }

    @Test
    void containerRejectsOversizedHeaders() throws Exception {
        int status = client.send(request().header("X-Oversized", "x".repeat(2048))
                .POST(HttpRequest.BodyPublishers.ofString("ok")).build(), HttpResponse.BodyHandlers.ofString()).statusCode();
        assertTrue(status == 400 || status == 431, "Unexpected header rejection status: " + status);
    }

    @Test
    void containerUsesConfiguredTimeoutsAndSwallowLimit() {
        var server = (org.springframework.boot.web.embedded.tomcat.TomcatWebServer) context.getWebServer();
        var protocol = (org.apache.coyote.http11.AbstractHttp11Protocol<?>) server.getTomcat()
                .getConnector().getProtocolHandler();
        assertEquals(10_000, protocol.getConnectionTimeout());
        assertEquals(15_000, protocol.getKeepAliveTimeout());
        assertEquals(16_384, protocol.getMaxSwallowSize());
    }
}
