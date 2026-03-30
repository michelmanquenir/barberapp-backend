package barberiapp.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RestTemplate restTemplate;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.api.url}")
    private String brevoApiUrl;

    @Value("${mail.from.email}")
    private String fromEmail;

    @Value("${mail.from.name:WeServ}")
    private String fromName;

    @Value("${resend.super-admin.email}")
    private String superAdminEmail;

    @PostConstruct
    void logMailConfig() {
        log.info("EmailService inicializado (Brevo) — from={}, superAdmin={}", fromEmail, superAdminEmail);
    }

    public record AppointmentEmailData(
            String shopName,
            String barberName,
            String service,
            String date,
            String time,
            String total
    ) {}

    // ─── Notificaciones al super admin ────────────────────────────────────────

    @Async("emailExecutor")
    public void sendNewUserRegisteredToAdmin(String newUserName, String newUserEmail, String role) {
        String roleLabel = "BUSINESS_OWNER".equals(role) ? "Propietario de Negocio" : "Cliente";
        String body = "<p style='font-size:16px;color:#111827;'>🔔 <strong>Nuevo usuario registrado</strong></p>" +
                      "<table style='width:100%;border-collapse:collapse;margin-top:16px;border-radius:8px;overflow:hidden;'>" +
                      row("Nombre",    newUserName) +
                      row("Email",     newUserEmail) +
                      row("Tipo",      roleLabel) +
                      row("Estado",    "Pendiente de aprobación") +
                      "</table>" +
                      "<p style='color:#6b7280;font-size:13px;margin-top:16px;'>Ingresa al panel de super admin para aprobar o rechazar esta cuenta.</p>";
        send(superAdminEmail, "Nuevo usuario registrado: " + newUserName, "#7c3aed", body);
    }

    // ─── Cuenta de usuario ────────────────────────────────────────────────────

    @Async("emailExecutor")
    public void sendWelcomeEmail(String to, String name, String role) {
        boolean isOwner = "BUSINESS_OWNER".equals(role);
        String title = isOwner ? "¡Bienvenido a WeServ!" : "¡Cuenta creada con éxito!";
        String extra = isOwner
                ? "<p style='color:#6b7280;font-size:14px;'>Tu cuenta de propietario está siendo revisada. " +
                  "Recibirás un correo cuando sea aprobada.</p>"
                : "<p style='color:#6b7280;font-size:14px;'>Ya puedes explorar y reservar citas en los mejores negocios.</p>";

        String body = "<p style='font-size:16px;color:#111827;'>Hola, <strong>" + escHtml(name) + "</strong> 👋</p>" +
                      "<p style='color:#374151;margin-top:8px;'>Tu registro fue exitoso. " +
                      (isOwner ? "Ahora solo espera la aprobación del administrador." : "¡Todo listo para comenzar!") + "</p>" +
                      extra;

        send(to, title, "#111827", body);
    }

    @Async("emailExecutor")
    public void sendAccountApproved(String to, String name) {
        String body = "<p style='font-size:16px;color:#111827;'>Hola, <strong>" + escHtml(name) + "</strong> 🎉</p>" +
                      "<p style='color:#374151;margin-top:8px;'>¡Tu cuenta ha sido <strong>aprobada</strong>! " +
                      "Ya puedes iniciar sesión y usar todas las funciones de la plataforma.</p>" +
                      badge("#16a34a", "✅ Cuenta Activa");
        send(to, "¡Tu cuenta fue aprobada!", "#16a34a", body);
    }

    @Async("emailExecutor")
    public void sendAccountRejected(String to, String name) {
        String body = "<p style='font-size:16px;color:#111827;'>Hola, <strong>" + escHtml(name) + "</strong>,</p>" +
                      "<p style='color:#374151;margin-top:8px;'>Lamentamos informarte que tu solicitud de cuenta " +
                      "no fue aprobada en esta ocasión. Si crees que es un error, por favor contáctanos.</p>" +
                      badge("#dc2626", "❌ Solicitud rechazada");
        send(to, "Tu solicitud de cuenta fue rechazada", "#dc2626", body);
    }

    // ─── Negocio ──────────────────────────────────────────────────────────────

    @Async("emailExecutor")
    public void sendShopPending(String to, String ownerName, String shopName) {
        String body = "<p style='font-size:16px;color:#111827;'>Hola, <strong>" + escHtml(ownerName) + "</strong> 👋</p>" +
                      "<p style='color:#374151;margin-top:8px;'>Tu negocio <strong>\"" + escHtml(shopName) + "\"</strong> " +
                      "fue registrado correctamente y está <strong>en revisión</strong>. " +
                      "Recibirás un correo cuando sea aprobado.</p>" +
                      badge("#d97706", "⏳ En revisión");
        send(to, "Tu negocio está en revisión", "#d97706", body);
    }

    @Async("emailExecutor")
    public void sendShopApproved(String to, String ownerName, String shopName) {
        String body = "<p style='font-size:16px;color:#111827;'>Hola, <strong>" + escHtml(ownerName) + "</strong> 🎉</p>" +
                      "<p style='color:#374151;margin-top:8px;'>¡Tu negocio <strong>\"" + escHtml(shopName) + "\"</strong> " +
                      "ha sido <strong>aprobado</strong> y ya está visible en la plataforma!</p>" +
                      badge("#16a34a", "✅ Negocio Activo");
        send(to, "¡Tu negocio fue aprobado!", "#16a34a", body);
    }

    @Async("emailExecutor")
    public void sendShopRejected(String to, String ownerName, String shopName) {
        String body = "<p style='font-size:16px;color:#111827;'>Hola, <strong>" + escHtml(ownerName) + "</strong>,</p>" +
                      "<p style='color:#374151;margin-top:8px;'>Tu negocio <strong>\"" + escHtml(shopName) + "\"</strong> " +
                      "no fue aprobado. Si necesitas más información, contáctanos.</p>" +
                      badge("#dc2626", "❌ Negocio rechazado");
        send(to, "Tu negocio no fue aprobado", "#dc2626", body);
    }

    // ─── Productos ────────────────────────────────────────────────────────────

    @Async("emailExecutor")
    public void sendProductApproved(String to, String ownerName, String productName) {
        String body = "<p style='font-size:16px;color:#111827;'>Hola, <strong>" + escHtml(ownerName) + "</strong> 🎉</p>" +
                      "<p style='color:#374151;margin-top:8px;'>Tu producto <strong>\"" + escHtml(productName) + "\"</strong> " +
                      "fue <strong>aprobado</strong> y ya está visible para los clientes.</p>" +
                      badge("#16a34a", "✅ Producto Activo");
        send(to, "¡Tu producto fue aprobado!", "#16a34a", body);
    }

    @Async("emailExecutor")
    public void sendProductRejected(String to, String ownerName, String productName) {
        String body = "<p style='font-size:16px;color:#111827;'>Hola, <strong>" + escHtml(ownerName) + "</strong>,</p>" +
                      "<p style='color:#374151;margin-top:8px;'>Tu producto <strong>\"" + escHtml(productName) + "\"</strong> " +
                      "no fue aprobado. Si tienes dudas, contáctanos.</p>" +
                      badge("#dc2626", "❌ Producto rechazado");
        send(to, "Tu producto no fue aprobado", "#dc2626", body);
    }

    // ─── Recuperación de contraseña ───────────────────────────────────────────

    @Async("emailExecutor")
    public void sendPasswordResetCode(String to, String name, String code) {
        log.info("sendPasswordResetCode invocado — to={}, name={}", to, name);
        String body = "<p style='font-size:16px;color:#111827;'>Hola, <strong>" + escHtml(name) + "</strong> 🔐</p>" +
                      "<p style='color:#374151;margin-top:8px;'>Recibimos una solicitud para restablecer tu contraseña. " +
                      "Usa el siguiente código para continuar:</p>" +
                      "<div style='text-align:center;margin:28px 0;'>" +
                      "<span style='display:inline-block;font-size:36px;font-weight:700;letter-spacing:12px;color:#111827;" +
                      "background:#f3f4f6;border-radius:12px;padding:16px 28px;border:2px solid #e5e7eb;'>" +
                      escHtml(code) + "</span></div>" +
                      "<p style='color:#6b7280;font-size:13px;'>Este código expira en <strong>15 minutos</strong>. " +
                      "Si no solicitaste este cambio, puedes ignorar este correo.</p>";
        send(to, "Código para restablecer tu contraseña", "#2563eb", body);
    }

    // ─── Citas: cliente ───────────────────────────────────────────────────────

    @Async("emailExecutor")
    public void sendAppointmentCreatedClient(String to, String name, AppointmentEmailData data) {
        String body = "<p style='font-size:16px;color:#111827;'>Hola, <strong>" + escHtml(name) + "</strong> 📅</p>" +
                      "<p style='color:#374151;margin-top:8px;'>Tu cita fue reservada exitosamente. " +
                      "Aquí tienes el resumen:</p>" +
                      appointmentTable(data) +
                      "<p style='color:#6b7280;font-size:13px;margin-top:12px;'>El negocio confirmará tu cita próximamente.</p>";
        send(to, "Tu cita fue reservada", "#2563eb", body);
    }

    @Async("emailExecutor")
    public void sendAppointmentConfirmed(String to, String name, AppointmentEmailData data) {
        String body = "<p style='font-size:16px;color:#111827;'>Hola, <strong>" + escHtml(name) + "</strong> ✅</p>" +
                      "<p style='color:#374151;margin-top:8px;'>¡Tu cita ha sido <strong>confirmada</strong>! Te esperamos:</p>" +
                      appointmentTable(data);
        send(to, "¡Tu cita fue confirmada!", "#16a34a", body);
    }

    @Async("emailExecutor")
    public void sendAppointmentCancelledClient(String to, String name, AppointmentEmailData data) {
        String body = "<p style='font-size:16px;color:#111827;'>Hola, <strong>" + escHtml(name) + "</strong>,</p>" +
                      "<p style='color:#374151;margin-top:8px;'>Lamentamos informarte que tu cita en " +
                      "<strong>" + escHtml(data.shopName()) + "</strong> fue <strong>cancelada</strong> por el negocio.</p>" +
                      appointmentTable(data) +
                      "<p style='color:#6b7280;font-size:13px;margin-top:12px;'>Puedes reservar una nueva cita cuando quieras.</p>";
        send(to, "Tu cita fue cancelada", "#dc2626", body);
    }

    @Async("emailExecutor")
    public void sendAppointmentCompleted(String to, String name, AppointmentEmailData data) {
        String body = "<p style='font-size:16px;color:#111827;'>Hola, <strong>" + escHtml(name) + "</strong> 🎊</p>" +
                      "<p style='color:#374151;margin-top:8px;'>¡Tu cita en <strong>" + escHtml(data.shopName()) + "</strong> " +
                      "fue completada! Esperamos que hayas tenido una excelente experiencia.</p>" +
                      appointmentTable(data) +
                      "<p style='color:#6b7280;font-size:13px;margin-top:12px;'>¿Qué tal estuvo? Puedes dejar tu opinión en la app.</p>";
        send(to, "¡Cita completada!", "#16a34a", body);
    }

    // ─── Citas: propietario ───────────────────────────────────────────────────

    @Async("emailExecutor")
    public void sendAppointmentCreatedOwner(String to, String ownerName, String clientName, AppointmentEmailData data) {
        String body = "<p style='font-size:16px;color:#111827;'>Hola, <strong>" + escHtml(ownerName) + "</strong> 📬</p>" +
                      "<p style='color:#374151;margin-top:8px;'><strong>" + escHtml(clientName) + "</strong> " +
                      "ha reservado una nueva cita en tu negocio <strong>" + escHtml(data.shopName()) + "</strong>:</p>" +
                      appointmentTable(data);
        send(to, "Nueva cita reservada en tu negocio", "#2563eb", body);
    }

    @Async("emailExecutor")
    public void sendAppointmentCancelledOwner(String to, String ownerName, String clientName, AppointmentEmailData data) {
        String body = "<p style='font-size:16px;color:#111827;'>Hola, <strong>" + escHtml(ownerName) + "</strong>,</p>" +
                      "<p style='color:#374151;margin-top:8px;'>El cliente <strong>" + escHtml(clientName) + "</strong> " +
                      "ha <strong>cancelado</strong> su cita en <strong>" + escHtml(data.shopName()) + "</strong>:</p>" +
                      appointmentTable(data);
        send(to, "Una cita fue cancelada por el cliente", "#dc2626", body);
    }

    // ─── Helpers privados ─────────────────────────────────────────────────────

    private void send(String to, String subject, String headerColor, String bodyContent) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> payload = Map.of(
                "sender",  Map.of("name", fromName, "email", fromEmail),
                "to",      List.of(Map.of("email", to)),
                "subject", subject,
                "htmlContent", buildHtml(subject, headerColor, bodyContent)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(brevoApiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email '{}' enviado a {} via Brevo", subject, to);
            } else {
                log.error("Brevo respondió {} al enviar '{}' a {}: {}", response.getStatusCode(), subject, to, response.getBody());
            }
        } catch (Exception e) {
            log.error("Error enviando email '{}' a {}: {}", subject, to, e.getMessage(), e);
        }
    }

    private String buildHtml(String title, String headerColor, String bodyContent) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>%s</title>
            </head>
            <body style="margin:0;padding:0;background:#f3f4f6;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="padding:32px 16px;">
                <tr>
                  <td align="center">
                    <table width="560" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 1px 4px rgba(0,0,0,0.08);">

                      <!-- Header -->
                      <tr>
                        <td style="background:%s;padding:28px 32px;">
                          <p style="margin:0;font-size:13px;color:rgba(255,255,255,0.7);letter-spacing:1px;text-transform:uppercase;">WeServ</p>
                          <p style="margin:6px 0 0;font-size:22px;font-weight:700;color:#ffffff;">%s</p>
                        </td>
                      </tr>

                      <!-- Body -->
                      <tr>
                        <td style="padding:32px;">
                          %s
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="background:#f9fafb;padding:20px 32px;border-top:1px solid #e5e7eb;">
                          <p style="margin:0;font-size:12px;color:#9ca3af;text-align:center;">
                            © 2025 WeServ · Este correo fue enviado automáticamente, no respondas a este mensaje.
                          </p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(escHtml(title), headerColor, escHtml(title), bodyContent);
    }

    private String appointmentTable(AppointmentEmailData d) {
        return "<table style='width:100%;border-collapse:collapse;margin-top:20px;border-radius:8px;overflow:hidden;'>" +
               row("Negocio", d.shopName()) +
               row("Barbero / Atendedor", d.barberName()) +
               row("Servicio", d.service()) +
               row("Fecha", d.date()) +
               row("Hora", d.time()) +
               row("Total", d.total()) +
               "</table>";
    }

    private String row(String label, String value) {
        return "<tr>" +
               "<td style='padding:10px 14px;background:#f9fafb;font-size:13px;color:#6b7280;font-weight:600;width:40%;border-bottom:1px solid #e5e7eb;'>" + escHtml(label) + "</td>" +
               "<td style='padding:10px 14px;background:#ffffff;font-size:14px;color:#111827;border-bottom:1px solid #e5e7eb;'>" + escHtml(value != null ? value : "—") + "</td>" +
               "</tr>";
    }

    private String badge(String color, String text) {
        return "<div style='display:inline-block;margin-top:16px;padding:8px 16px;background:" + color + ";border-radius:99px;'>" +
               "<span style='color:#fff;font-size:13px;font-weight:600;'>" + text + "</span></div>";
    }

    private String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
