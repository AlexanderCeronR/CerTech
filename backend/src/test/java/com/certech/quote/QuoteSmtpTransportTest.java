package com.certech.quote;
import static org.junit.jupiter.api.Assertions.*;
import com.certech.notification.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
class QuoteSmtpTransportTest {
 @Test void actualMimeIsSentOnlyToLoopbackSmtpSimulator()throws Exception{runSmtp(false);}
 @Test void temporarySmtpRejectionIsReportedForRetry()throws Exception{runSmtp(true);}
 void runSmtp(boolean reject)throws Exception{
  try(ServerSocket server=new ServerSocket(0,1,InetAddress.getByName("127.0.0.1"))){server.setSoTimeout(5000);ExecutorService pool=Executors.newSingleThreadExecutor();try{
   Future<String> transcript=pool.submit(()->{try(Socket socket=server.accept()){socket.setSoTimeout(5000);var reader=new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));var writer=new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.UTF_8),true);writer.print("220 localhost synthetic SMTP\r\n");writer.flush();String line;boolean data=false;StringBuilder mime=new StringBuilder();while((line=reader.readLine())!=null){if(data){if(line.equals(".")){data=false;writer.print(reject?"451 synthetic temporary failure\r\n":"250 queued locally\r\n");writer.flush();}else mime.append(line).append("\n");}else if(line.startsWith("EHLO")||line.startsWith("HELO")||line.startsWith("MAIL")||line.startsWith("RCPT")||line.startsWith("RSET")){writer.print("250 localhost\r\n");writer.flush();}else if(line.equals("DATA")){data=true;writer.print("354 end with dot\r\n");writer.flush();}else if(line.equals("QUIT")){writer.print("221 bye\r\n");writer.flush();break;}else{writer.print("250 ok\r\n");writer.flush();}}return mime.toString();}});
   var settings=new NotificationSettings(true,"owner@example.invalid","sender@example.invalid","America/Bogota",3,1,60,300,1);
   var mail=new JavaMailSenderImpl();mail.setHost("127.0.0.1");mail.setPort(server.getLocalPort());for(String key:new String[]{"connectiontimeout","timeout","writetimeout"})mail.getJavaMailProperties().setProperty("mail.smtp."+key,"3000");
   var sender=new QuoteSmtpSender(new SmtpNotificationSender(mail,settings,new ContactEmailTemplate()),new QuoteEmailTemplate(),settings);
   var item=new QuoteNotification(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1,"owner@example.invalid","Synthetic","qa@example.invalid",null,"Desarrollo Web","small","<script>untrusted</script>",OffsetDateTime.now(),"Synthetic project","Receive enquiries","Contact form","flexible",null,null);
   if(reject)assertThrows(org.springframework.mail.MailException.class,()->sender.send(item));else sender.send(item);
   String mime=transcript.get(6,TimeUnit.SECONDS);assertTrue(mime.contains("multipart/alternative"));assertTrue(mime.contains("text/plain"));assertTrue(mime.contains("text/html"));assertTrue(mime.contains("Reply-To: qa@example.invalid"));assertTrue(mime.contains("From: sender@example.invalid"));assertTrue(mime.contains(item.id().toString()));
  }finally{pool.shutdownNow();}}
 }
}
