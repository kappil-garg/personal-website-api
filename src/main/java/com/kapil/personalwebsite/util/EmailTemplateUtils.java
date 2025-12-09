package com.kapil.personalwebsite.util;

import com.kapil.personalwebsite.dto.ContactRequest;

/**
 * Utility class for building email templates.
 *
 * @author Kapil Garg
 */
public final class EmailTemplateUtils {

    private EmailTemplateUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Builds a professional HTML email body for contact form submissions.
     *
     * @param contactRequest the contact request data
     * @param websiteDomain  the website domain name
     * @return the HTML email body
     */
    public static String buildContactFormEmailBody(ContactRequest contactRequest, String websiteDomain) {
        String name = StringUtils.sanitizeForEmailBody(contactRequest.getName());
        String email = StringUtils.sanitizeForEmailBody(contactRequest.getEmail());
        String subject = StringUtils.sanitizeForEmailBody(contactRequest.getSubject());
        String message = StringUtils.sanitizeForEmailBody(contactRequest.getMessage())
                .replace("\n", "<br>");
        String domain = StringUtils.sanitizeForEmailBody(websiteDomain);
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                            line-height: 1.6;
                            color: #13343b;
                            background-color: #fcfcf9;
                            margin: 0;
                            padding: 20px;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background-color: #fffffd;
                            border-radius: 8px;
                            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                            overflow: hidden;
                        }
                        .header {
                            background: linear-gradient(135deg, #21808d 0%%, #1d7480 100%%);
                            color: #ffffff;
                            padding: 30px 20px;
                            text-align: center;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 24px;
                            font-weight: 600;
                            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                        }
                        .content {
                            padding: 30px 20px;
                        }
                        .field-group {
                            margin-bottom: 20px;
                        }
                        .field-label {
                            font-weight: 600;
                            color: #21808d;
                            font-size: 12px;
                            text-transform: uppercase;
                            letter-spacing: 0.5px;
                            margin-bottom: 5px;
                        }
                        .field-value {
                            color: #13343b;
                            font-size: 16px;
                            padding: 10px;
                            background-color: #fcfcf9;
                            border-left: 3px solid #21808d;
                            border-radius: 4px;
                        }
                        .message-box {
                            background-color: #fcfcf9;
                            border-left: 3px solid #21808d;
                            padding: 15px;
                            border-radius: 4px;
                            margin-top: 10px;
                        }
                        .footer {
                            background-color: #fcfcf9;
                            padding: 20px;
                            text-align: center;
                            font-size: 12px;
                            color: #626c71;
                            border-top: 1px solid rgba(94, 82, 64, 0.2);
                        }
                        .footer a {
                            color: #21808d;
                            text-decoration: none;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>New Contact Form Submission</h1>
                        </div>
                        <div class="content">
                            <div class="field-group">
                                <div class="field-label">Name</div>
                                <div class="field-value">%s</div>
                            </div>
                            <div class="field-group">
                                <div class="field-label">Email</div>
                                <div class="field-value">%s</div>
                            </div>
                            %s
                            <div class="field-group">
                                <div class="field-label">Message</div>
                                <div class="message-box">%s</div>
                            </div>
                        </div>
                        <div class="footer">
                            This message was sent from the contact form on <a href="https://%s">%s</a>
                        </div>
                    </div>
                </body>
                </html>
                """, name, email, buildSubjectField(subject), message, domain, domain);
    }

    /**
     * Builds the subject field HTML if subject is provided.
     *
     * @param subject the email subject
     * @return the subject field HTML or empty string
     */
    private static String buildSubjectField(String subject) {
        if (StringUtils.isNotBlank(subject)) {
            return String.format("""
                    <div class="field-group">
                        <div class="field-label">Subject</div>
                        <div class="field-value">%s</div>
                    </div>
                    """, subject);
        }
        return "";
    }

}
