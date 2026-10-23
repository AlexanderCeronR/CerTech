package com.certech.quote;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.certech.notification.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
@EnabledIfEnvironmentVariable(named="CERTECH_SYNTHETIC_DB_ONLY",matches="YES")
@SpringBootTest(properties={"app.notifications.enabled=false","app.notifications.max-attempts=3","app.notifications.retry-seconds=1","app.notifications.max-retry-seconds=60","app.contact.rate-limit.max-requests=1000"})
@AutoConfigureMockMvc
class QuoteIntegrationTest {
 @DynamicPropertySource static void database(DynamicPropertyRegistry r){
  String url=System.getenv("MAIL_TEST_DB_URL");if(url==null||!url.matches("jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/certech_mail_ci"))throw new IllegalStateException("Synthetic loopback DB required");
  r.add("spring.datasource.url",()->url);r.add("spring.datasource.username",()->"certech_mail_ci");r.add("spring.datasource.password",()->System.getenv("MAIL_TEST_DB_PASSWORD"));
 }
 @Autowired QuoteService service;@Autowired QuoteNotificationOutbox outbox;@Autowired JdbcTemplate jdbc;@Autowired MockMvc mvc;@Autowired ObjectMapper json;@Autowired PlatformTransactionManager manager;
 QuoteRequest request(){return new QuoteRequest(UUID.randomUUID(),"Desarrollo Web","Synthetic project","Synthetic description only","Receive enquiries","Contact form","flexible","small","Synthetic Client","qa@example.invalid","Synthetic company","+57 300 000 0000","Synthetic country",true);}
 @BeforeEach void clean(){jdbc.execute("TRUNCATE solicitudes_cotizacion CASCADE");}
 int count(String table){return jdbc.queryForObject("SELECT COUNT(*) FROM "+table,Integer.class);}
 String state(){return jdbc.queryForObject("SELECT status FROM quote_notification_outbox",String.class);}
 NotificationSettings settings(boolean enabled){return new NotificationSettings(enabled,"owner@example.invalid","sender@example.invalid","America/Bogota",3,1,60,300,1);}
 void due(){jdbc.update("UPDATE quote_notification_outbox SET available_at=CURRENT_TIMESTAMP-INTERVAL '1 second'");}
 @Test void httpPersistsAllFieldsAndPendingNotification()throws Exception{
  var q=request();var result=mvc.perform(post("/api/cotizaciones").contentType("application/json").content(json.writeValueAsString(q))).andExpect(status().isCreated()).andReturn();
  assertNotNull(UUID.fromString(json.readTree(result.getResponse().getContentAsString()).get("id").asText()));assertEquals(1,count("solicitudes_cotizacion"));assertEquals(1,count("quote_notification_outbox"));assertEquals("PENDING",state());
  assertEquals(q.requisitos(),jdbc.queryForObject("SELECT requisitos FROM solicitudes_cotizacion",String.class));assertEquals(q.telefono(),jdbc.queryForObject("SELECT telefono FROM solicitudes_cotizacion",String.class));
 }
 @Test void invalidMissingMalformedAndLengthInputsNeverPersist()throws Exception{
  for(String body:List.of("{}","{",json.writeValueAsString(request()).replace("Synthetic Client","x"),json.writeValueAsString(request()).replace("Synthetic project","x".repeat(161)),json.writeValueAsString(request()).replace("qa@example.invalid","invalid"),json.writeValueAsString(request()).replace("Desarrollo Web","Invented"),json.writeValueAsString(request()).replace("\"aceptaPrivacidad\":true","\"aceptaPrivacidad\":false")))mvc.perform(post("/api/cotizaciones").contentType("application/json").content(body)).andExpect(status().isBadRequest());
  assertEquals(0,count("solicitudes_cotizacion"));assertEquals(0,count("quote_notification_outbox"));
 }
 @Test void outboxFailureRollsBackQuote(){jdbc.execute("ALTER TABLE quote_notification_outbox ADD CONSTRAINT synthetic_quote_reject CHECK(false) NOT VALID");try{assertThrows(RuntimeException.class,()->service.create(request()));assertEquals(0,count("solicitudes_cotizacion"));}finally{jdbc.execute("ALTER TABLE quote_notification_outbox DROP CONSTRAINT synthetic_quote_reject");}}
 @Test void identicalRetriesAreIdempotentAndDifferentPayloadIsConflict()throws Exception{
  var q=request();var first=service.create(q);assertEquals(first,service.create(q));assertEquals(1,count("quote_notification_outbox"));
  mvc.perform(post("/api/cotizaciones").contentType("application/json").content(json.writeValueAsString(q).replace("Synthetic project","Different project"))).andExpect(status().isConflict());assertEquals(1,count("solicitudes_cotizacion"));
 }
 @Test void concurrentSubmissionsAndClaimsHaveSingleWinner()throws Exception{
  var q=request();ExecutorService pool=Executors.newFixedThreadPool(4);try{List<Callable<QuoteResponse>> jobs=new ArrayList<>();for(int i=0;i<4;i++)jobs.add(()->service.create(q));var results=pool.invokeAll(jobs);var id=results.getFirst().get().id();for(var f:results)assertEquals(id,f.get().id());assertEquals(1,count("solicitudes_cotizacion"));
  var claims=pool.invokeAll(List.<Callable<Optional<QuoteNotification>>>of(()->outbox.claim(),()->outbox.claim()));assertEquals(1,claims.stream().filter(f->{try{return f.get().isPresent();}catch(Exception e){throw new RuntimeException(e);}}).count());}finally{pool.shutdownNow();}
 }
 @Test void smtpFailureRetriesAreBoundedAndNeverLoseQuote(){service.create(request());QuoteNotificationSender fail=item->{throw new IllegalStateException("Synthetic SMTP failure");};var worker=new QuoteNotificationWorker(outbox,fail,settings(true));for(int i=0;i<3;i++){due();worker.poll();}assertEquals("FAILED",state());assertEquals(1,count("solicitudes_cotizacion"));assertEquals(3,jdbc.queryForObject("SELECT attempts FROM quote_notification_outbox",Integer.class));}
 @Test void sentNotificationIsNotSentTwiceAndDisabledWorkerDoesNothing(){service.create(request());AtomicInteger sent=new AtomicInteger();var worker=new QuoteNotificationWorker(outbox,item->sent.incrementAndGet(),settings(true));new QuoteNotificationWorker(outbox,item->{fail("disabled worker sent");},settings(false)).poll();assertEquals("PENDING",state());worker.poll();worker.poll();assertEquals(1,sent.get());assertEquals("SENT",state());}
 @Test void expiredLeaseRecoversAndOldAcknowledgementCannotOverrideNewLease(){service.create(request());var old=outbox.claim().orElseThrow();due();var fresh=outbox.claim().orElseThrow();outbox.accepted(old);assertEquals("PROCESSING",state());outbox.accepted(fresh);assertEquals("SENT",state());}
 @Test void quoteEmailEscapesVisitorContentAndUsesSimulatedMimeTransport()throws Exception{
  var q=request();service.create(q);jdbc.update("UPDATE solicitudes_cotizacion SET titulo=?,descripcion=?","<script>alert(1)</script>","<img src=x> & untrusted");var item=outbox.claim().orElseThrow();var content=new QuoteEmailTemplate().render(item,"America/Bogota");assertFalse(content.html().contains("<script>"));assertTrue(content.html().contains("&lt;script&gt;"));assertTrue(content.plain().contains(item.quoteId().toString()));assertTrue(content.plain().contains("America/Bogota"));
  JavaMailSender mail=mock(JavaMailSender.class);when(mail.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));var transport=new SmtpNotificationSender(mail,settings(true),new ContactEmailTemplate());new QuoteSmtpSender(transport,new QuoteEmailTemplate(),settings(true)).send(item);verify(mail).send(org.mockito.ArgumentMatchers.<MimeMessage>argThat(m->{try{return QuoteEmailTemplate.SUBJECT.equals(m.getSubject())&&m.getReplyTo()[0].toString().equals(q.correo());}catch(Exception e){return false;}}));
 }

 @Test void allInputLimitsAndEnumsAreEnforcedOnServer()throws Exception{
  Map<String,Object> fields=json.convertValue(request(),new com.fasterxml.jackson.core.type.TypeReference<Map<String,Object>>(){});
  Map<String,Object> invalid=new LinkedHashMap<>();invalid.put("descripcion","x".repeat(3001));invalid.put("objetivo","x".repeat(1001));invalid.put("requisitos","x".repeat(1501));invalid.put("nombre","x".repeat(121));invalid.put("correo","x".repeat(181)+"@example.invalid");invalid.put("empresa","x".repeat(141));invalid.put("telefono","<script>");invalid.put("pais","x".repeat(81));invalid.put("plazo","tomorrow");invalid.put("presupuesto","invented");invalid.put("requestId","not-a-uuid");
  for(var entry:invalid.entrySet()){var copy=new LinkedHashMap<>(fields);copy.put(entry.getKey(),entry.getValue());mvc.perform(post("/api/cotizaciones").contentType("application/json").content(json.writeValueAsString(copy))).andExpect(status().isBadRequest());}assertEquals(0,count("solicitudes_cotizacion"));
 }
 @Test void corsRejectsUnapprovedOriginAndAllowsConfiguredOrigin()throws Exception{
  mvc.perform(options("/api/cotizaciones").header("Origin","https://untrusted.invalid").header("Access-Control-Request-Method","POST")).andExpect(status().isForbidden());
  mvc.perform(options("/api/cotizaciones").header("Origin","http://localhost:5173").header("Access-Control-Request-Method","POST")).andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:5173"));
 }
}
