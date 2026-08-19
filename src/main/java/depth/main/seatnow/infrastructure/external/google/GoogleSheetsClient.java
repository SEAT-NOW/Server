package depth.main.seatnow.infrastructure.external.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Component
public class GoogleSheetsClient {

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    @Value("${google.sheets.range}")
    private String range;

    @Value("${google.sheets.credentials-path}")
    private String credentialsPath;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * 구글 스프레드시트에서 캘린더 데이터를 읽어옴
     */
    public List<List<Object>> getCalendarData() throws IOException, GeneralSecurityException {
        InputStream in = new ClassPathResource(credentialsPath).getInputStream();
        GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        Sheets service = new Sheets.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName("Seatnow-Calendar-Sync")
                .build();

        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        return values;
    }
}
