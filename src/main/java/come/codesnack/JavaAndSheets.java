package come.codesnack;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

public class JavaAndSheets {
private static Sheets sheetsService;
private static final String APPLICATION_NAME = "Study System";
private static String SPREADSHEET_ID = "18GW00ocA-6MhRBLjkVszJXtfcNuYVjAb8afW3brPnBI";

private static Credential authorize() throws IOException, GeneralSecurityException {
    InputStream in = JavaAndSheets.class.getResourceAsStream("/credentials.json");
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
            GsonFactory.getDefaultInstance(), new InputStreamReader(in)
    );
    List<String> scopes = Arrays.asList(
            SheetsScopes.SPREADSHEETS,
            SheetsScopes.DRIVE
    );


    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(),
            clientSecrets, scopes)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
            .setAccessType("offline")
            .build();
    Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

    return credential;
}
public static Sheets getSheetsService() throws IOException, GeneralSecurityException{
    Credential credential = authorize();
    return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
}
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        sheetsService = getSheetsService();

        String range = "Оцінки!A2:G";

        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
            return;
        }

        Map<String, List<Integer>> scoresMap = new HashMap<>();

        for (List<Object> row : values) {
            if (row.size() < 6) continue;

            String group = row.get(3).toString();
            String subject = row.get(4).toString();
            int score;

            try {
                score = Integer.parseInt(row.get(5).toString());
            } catch (NumberFormatException e) {
                System.out.println("Помилка парсингу: " + row);
                continue;
            }

            String key = group + " - " + subject;

            scoresMap.putIfAbsent(key, new ArrayList<>());
            scoresMap.get(key).add(score);
        }

        List<List<Object>> statsData = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : scoresMap.entrySet()) {
            String[] parts = entry.getKey().split(" - ");
            String group = parts[0];
            String subject = parts[1];

            List<Integer> scores = entry.getValue();
            double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0);

            statsData.add(Arrays.asList(group, subject, avgScore));
        }

        String statsRange = "Статистика!A2:C";

        try {
            System.out.println("Сроба запису в діапазон: " + statsRange);
            System.out.println("Дані для запису: " + statsData);

            ValueRange body = new ValueRange().setValues(statsData);

            sheetsService.spreadsheets().values()
                    .update(SPREADSHEET_ID, statsRange, body)
                    .setValueInputOption("RAW")
                    .execute();

            System.out.println("Статистику оновлено успішно!");
        } catch (Exception e) {
            System.err.println("Помилка при записі даних:");
            e.printStackTrace();
        }

    }

}
