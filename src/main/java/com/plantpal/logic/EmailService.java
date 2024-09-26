package com.plantpal.logic;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;

public class EmailService {

    public void sendMail(String apiKey, String privateApiKey, String fromEmail, String toEmail, String subject, String htmlContent) {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;

        client = new MailjetClient(apiKey, privateApiKey); // Verwende nur die API-Schlüssel

        try {
            request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, new JSONArray()
                            .put(new JSONObject()
                                    .put(Emailv31.Message.FROM, new JSONObject()
                                            .put("Email", fromEmail)
                                            .put("Name", "PlantPal"))
                                    .put(Emailv31.Message.TO, new JSONArray()
                                            .put(new JSONObject()
                                                    .put("Email", toEmail)
                                                    .put("Name", "Empfänger")))
                                    .put(Emailv31.Message.SUBJECT, subject)
                                    .put(Emailv31.Message.HTMLPART, htmlContent)));

            response = client.post(request);

            if (response.getStatus() == 200) {
                System.out.println("E-Mail erfolgreich gesendet.");
            } else {
                System.err.println("Fehler beim Senden der E-Mail: " + response.getStatus() + " " + response.getData());
            }

        } catch (MailjetException e) {
            e.printStackTrace();
        }
    }

}
