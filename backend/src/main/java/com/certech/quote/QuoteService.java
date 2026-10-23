package com.certech.quote;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.security.MessageDigest;
import java.util.HexFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@Service
public class QuoteService {
 private final JdbcTemplate jdbc; private final ObjectMapper json; private final QuoteNotificationOutbox outbox;
 public QuoteService(JdbcTemplate jdbc,ObjectMapper json,QuoteNotificationOutbox outbox){this.jdbc=jdbc;this.json=json;this.outbox=outbox;}
 @Transactional public QuoteResponse create(QuoteRequest q) {
  if(!q.aceptaPrivacidad()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  final String hash;
  try {hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json.writeValueAsBytes(q)));}
  catch(Exception e){throw new IllegalStateException("Unable to prepare request");}
  UUID id=UUID.randomUUID();
  int inserted=jdbc.update("""
   INSERT INTO solicitudes_cotizacion(id,request_key,payload_hash,servicio,titulo,descripcion,objetivo,requisitos,plazo,presupuesto,nombre,correo,empresa,telefono,pais,acepta_privacidad)
   VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,true) ON CONFLICT (request_key) DO NOTHING
   """,id,q.requestId(),hash,q.servicio(),q.titulo().trim(),q.descripcion().trim(),q.objetivo().trim(),q.requisitos().trim(),q.plazo(),q.presupuesto(),q.nombre().trim(),q.correo(),q.empresa(),q.telefono(),q.pais());
  if(inserted==1) outbox.enqueue(id);
  return jdbc.queryForObject("SELECT id,servicio,fecha_creacion,payload_hash FROM solicitudes_cotizacion WHERE request_key=?",(rs,row)->{
   if(!hash.equals(rs.getString("payload_hash")))throw new ResponseStatusException(HttpStatus.CONFLICT);
   return new QuoteResponse(rs.getObject("id",UUID.class),rs.getString("servicio"),rs.getObject("fecha_creacion",OffsetDateTime.class));
  },q.requestId());
 }
}
