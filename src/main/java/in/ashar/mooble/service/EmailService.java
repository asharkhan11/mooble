package in.ashar.mooble.service;

import in.ashar.mooble.configuration.AppProperties;
import in.ashar.mooble.entity.ForgotPasswordOtp;
import in.ashar.mooble.entity.RegistrationOtp;
import in.ashar.mooble.repository.ForgotPasswordOtpRepository;
import in.ashar.mooble.repository.RegistrationOtpRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final PasswordSetupTokenService tokenService;
    private final AppProperties appProperties;
    private final JavaMailSender mailSender;
    private final RegistrationOtpRepository otpRepository;
    private final ForgotPasswordOtpRepository forgotPasswordOtpRepository;

    @Async
    public void sendPasswordSetupEmail(String to) {

        String token = tokenService.createToken(to);

        String link = appProperties.getBaseUrl() + "/auth/set-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Setup Mail");
        message.setText("""
                Hi %s,
                
                Below is the link provided to set a password for your account :
                
                """.formatted(to) + link + """
                
                
                (link will be valid for only 24 hours, so set the password within 24 hours).
                """);
        mailSender.send(message);
    }

    @Async
    public void sendOtp(String email) throws MessagingException {

        String emailTemplate = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <title>OTP Verification</title>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0; padding:0; background-color:#f4f6f8; font-family:Arial, Helvetica, sans-serif;">
                
                  <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f8; padding:20px 0;">
                    <tr>
                      <td align="center">
                
                        <!-- Main Container -->
                        <table width="100%" cellpadding="0" cellspacing="0" style="max-width:520px; background-color:#ffffff; border-radius:8px; overflow:hidden;">
                
                          <!-- Header -->
                          <tr>
                            <td style="padding:24px; text-align:center; background-color:#5B6CFF; color:#ffffff;">
                              <h1 style="margin:0; font-size:22px;">OTP Verification</h1>
                            </td>
                          </tr>
                
                          <!-- Body -->
                          <tr>
                            <td style="padding:24px; color:#333333;">
                              <p style="margin:0 0 16px 0; font-size:15px;">
                                Hello,
                              </p>
                
                              <p style="margin:0 0 16px 0; font-size:15px;">
                                Use the following One-Time Password (OTP) to verify your email address.
                                This OTP is valid for <strong>10 minutes</strong>.
                              </p>
                
                              <!-- OTP Box -->
                              <div style="margin:24px 0; text-align:center;">
                                <span style="
                                  display:inline-block;
                                  padding:14px 28px;
                                  font-size:26px;
                                  letter-spacing:6px;
                                  font-weight:bold;
                                  color:#5B6CFF;
                                  background-color:#f1f3ff;
                                  border-radius:6px;
                                ">
                                  {{OTP}}
                                </span>
                              </div>
                
                              <p style="margin:0 0 16px 0; font-size:14px; color:#555555;">
                                If you did not request this verification, please ignore this email.
                              </p>
                
                              <p style="margin:0; font-size:14px; color:#555555;">
                                Regards,<br>
                                <strong>Mooble Team</strong>
                              </p>
                            </td>
                          </tr>
                
                          <!-- Footer -->
                          <tr>
                            <td style="padding:16px; text-align:center; font-size:12px; color:#888888; background-color:#fafafa;">
                              © 2025 Mooble. All rights reserved.
                            </td>
                          </tr>
                
                        </table>
                
                      </td>
                    </tr>
                  </table>
                
                </body>
                </html>
                
                """;

        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(900000) + 100000);


        String html = emailTemplate.replace("{{OTP}}", otp);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("Mooble Account Verification");
        helper.setText(html, true); // true = HTML

        Optional<RegistrationOtp> optAlready = otpRepository.findByEmail(email);

        if(optAlready.isPresent()){
            RegistrationOtp alreadyOtp = optAlready.get();
            if(alreadyOtp.isVerified()){
                return;
            }
            alreadyOtp.setOtp(otp);
            alreadyOtp.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
            mailSender.send(message);
            otpRepository.save(alreadyOtp);
            return;
        }

        mailSender.send(message);
        RegistrationOtp otpStore = new RegistrationOtp();

        otpStore.setEmail(email);
        otpStore.setOtp(otp);
        otpStore.setVerified(false);
        otpStore.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));

        otpRepository.save(otpStore);

    }



    @Async
    public void sendPasswordForgotOtp(String email) throws MessagingException {

        String emailTemplate = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>Password Reset OTP</title>
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body style="margin:0; padding:0; background-color:#f4f6f8; font-family:Arial, Helvetica, sans-serif;">
          <table width="100%" cellpadding="0" cellspacing="0" style="padding:20px 0;">
            <tr>
              <td align="center">
                <table width="100%" cellpadding="0" cellspacing="0"
                       style="max-width:520px; background:#ffffff; border-radius:8px;">
                  <tr>
                    <td style="padding:24px; background:#5B6CFF; color:#fff; text-align:center;">
                      <h2 style="margin:0;">Password Reset</h2>
                    </td>
                  </tr>

                  <tr>
                    <td style="padding:24px; color:#333;">
                      <p>You requested to reset your Mooble account password.</p>
                      <p>This OTP is valid for <strong>10 minutes</strong>.</p>

                      <div style="text-align:center; margin:24px 0;">
                        <span style="
                          font-size:26px;
                          font-weight:bold;
                          letter-spacing:6px;
                          color:#5B6CFF;
                          background:#f1f3ff;
                          padding:14px 28px;
                          border-radius:6px;">
                          {{OTP}}
                        </span>
                      </div>

                      <p style="font-size:14px; color:#555;">
                        If you did not request a password reset, you can safely ignore this email.
                      </p>

                      <p style="font-size:14px;">
                        — Mooble Team
                      </p>
                    </td>
                  </tr>

                  <tr>
                    <td style="padding:16px; text-align:center; font-size:12px; color:#888;">
                      © 2025 Mooble
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>
       """;

        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(900000) + 100000);


        String html = emailTemplate.replace("{{OTP}}", otp);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("Mooble Password Reset OTP");
        helper.setText(html, true); // true = HTML

        Optional<ForgotPasswordOtp> optAlready = forgotPasswordOtpRepository.findByEmail(email);

        if(optAlready.isPresent()){
            ForgotPasswordOtp alreadyOtp = optAlready.get();

            alreadyOtp.setOtp(otp);
            alreadyOtp.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
            mailSender.send(message);
            forgotPasswordOtpRepository.save(alreadyOtp);
            return;
        }

        mailSender.send(message);
        ForgotPasswordOtp otpStore = new ForgotPasswordOtp();

        otpStore.setEmail(email);
        otpStore.setOtp(otp);
        otpStore.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        forgotPasswordOtpRepository.save(otpStore);

    }

}
