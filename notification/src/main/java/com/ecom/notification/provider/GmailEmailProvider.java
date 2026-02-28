    package com.ecom.notification.provider;

    import com.ecom.notification.dto.EmailProductLine;
    import jakarta.mail.internet.MimeMessage;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.mail.javamail.JavaMailSender;
    import org.springframework.mail.javamail.MimeMessageHelper;
    import org.springframework.stereotype.Component;
    import org.thymeleaf.TemplateEngine;
    import org.thymeleaf.context.Context;

    import java.math.BigDecimal;
    import java.util.List;

    @Component
    public class GmailEmailProvider implements EmailProvider{

        @Value("${spring.mail.username}")
        private String fromAddress;
        private final JavaMailSender javaMailSender;
        public final TemplateEngine templateEngine;

        public GmailEmailProvider(final JavaMailSender javaMailSender,
                                  final TemplateEngine templateEngine) {
            this.javaMailSender = javaMailSender;
            this.templateEngine = templateEngine;
        }

        @Override
        public void sendOrderConfirmedMail(String userMail, String username, BigDecimal amount, List<EmailProductLine> productLines) {

            try {
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

                Context ctx = new Context();
                ctx.setVariable("customerName",username);
                ctx.setVariable("totalAmount",amount);
                ctx.setVariable("productLines",productLines);

                String htmlTemplate = templateEngine.process("order-success",ctx);
                helper.setFrom(fromAddress);
                helper.setTo(userMail);
                helper.setSubject("Order Confirmed!");
                helper.setText(htmlTemplate,true);

                javaMailSender.send(mimeMessage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }
