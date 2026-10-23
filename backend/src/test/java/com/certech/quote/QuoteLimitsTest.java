package com.certech.quote;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.servlet.MockMvc;
@EnabledIfEnvironmentVariable(named="CERTECH_SYNTHETIC_DB_ONLY",matches="YES")
@SpringBootTest(properties={"app.notifications.enabled=false","app.contact.rate-limit.max-requests=1"})
@AutoConfigureMockMvc
class QuoteLimitsTest {
 @DynamicPropertySource static void db(DynamicPropertyRegistry r){QuoteIntegrationTest.database(r);}
 @Autowired MockMvc mvc;
 @Test void bodyLimitRateLimitAndUntrustedForwardedHeadersApply()throws Exception{
 mvc.perform(post("/api/cotizaciones").contentType("application/json").content("x".repeat(16385))).andExpect(status().isPayloadTooLarge());
 mvc.perform(post("/api/cotizaciones").contentType("application/json").content("{}")).andExpect(status().isBadRequest()).andExpect(header().string("X-Content-Type-Options","nosniff"));
 mvc.perform(post("/api/cotizaciones").header("X-Forwarded-For","198.51.100.12").contentType("application/json").content("{}")).andExpect(status().isTooManyRequests()).andExpect(header().exists("Retry-After"));
 }
}
