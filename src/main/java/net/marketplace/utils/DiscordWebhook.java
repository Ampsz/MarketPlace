package net.marketplace.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiscordWebhook {

    private final String webhookUrl;

    public DiscordWebhook(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void sendPurchaseLog(String playerName, String itemName, double price) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonPayload = String.format(
                    "{\"content\": \"**Purchase Log**: Player `%s` bought item `%s` for `$%.2f`.\"}",
                    playerName, itemName, price
            );

            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Webhook sent successfully.");
            } else {
                System.out.println("Failed to send webhook, response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
