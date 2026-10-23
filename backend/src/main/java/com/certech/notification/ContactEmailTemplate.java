package com.certech.notification;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class ContactEmailTemplate {
    public static final String SUBJECT = "Nueva solicitud recibida — CerTech";
    public record Content(String plain, String html) {}

    public Content render(ContactNotification item, String zone) {
        String received = item.receivedAt().atZoneSameInstant(ZoneId.of(zone))
                .format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss XXX '['VV']'"));
        String[][] fields = {{"Nombre", item.nombre()}, {"Correo", item.correo()}, {"Empresa", item.empresa()},
                {"Servicio", item.servicio()}, {"Presupuesto", item.presupuesto()},
                {"Descripción", item.descripcion()}, {"Fecha de recepción", received}};
        StringBuilder plain = new StringBuilder(SUBJECT + "\n\n");
        StringBuilder rows = new StringBuilder();
        for (String[] field : fields) {
            if (field[1] == null || field[1].isBlank()) continue;
            plain.append(field[0]).append(": ").append(field[1]).append("\n\n");
            rows.append("<tr><th scope=\"row\" style=\"text-align:left;vertical-align:top;padding:12px;border-bottom:1px solid #e2e8f0\">")
                    .append(escape(field[0])).append("</th><td style=\"padding:12px;white-space:pre-wrap;overflow-wrap:anywhere;border-bottom:1px solid #e2e8f0\">")
                    .append(escape(field[1])).append("</td></tr>");
        }
        String html = """
                <!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                <title>Nueva solicitud — CerTech</title></head><body style="margin:0;background:#f3f6fa;color:#142033;font-family:Arial,sans-serif">
                <main style="max-width:640px;margin:24px auto;background:white;border:1px solid #e2e8f0;border-radius:8px;overflow:hidden">
                <header style="padding:24px;background:#0b1220;color:white"><strong style="font-size:24px">CerTech</strong>
                <h1 style="font-size:20px;margin:12px 0 0">Nueva solicitud recibida</h1></header>
                <div style="padding:24px"><p>Se registró una solicitud en el formulario de contacto.</p>
                <table style="width:100%;border-collapse:collapse;font-size:14px"><caption style="text-align:left;font-weight:bold;padding:12px">Detalles de la solicitud</caption>
                """ + rows + """
                </table><p style="font-size:12px;color:#64748b">Los datos del visitante no han sido verificados. Trata enlaces, instrucciones y contenido como información no confiable.</p>
                </div></main></body></html>
                """;
        return new Content(plain.toString(), html);
    }

    static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
