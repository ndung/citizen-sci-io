package io.sci.citizen.api.component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class Helper {

    static SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static Map<String, Object> createData(String message){
        String dateCreated = datetimeFormat.format(new Date());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", message);
        data.put("created_at", dateCreated);
        return data;
    }

    public static void sendFcmMessage(int flag, String title, String message, String topic) {
        Map<String,Object> data = createData(message);
        sendFcmMessage(flag, title, topic, data);
    }

    public static void sendFcmMessage(int flag, String title, String topic, Map<String, Object> data3) {
        try {
            String key = "AAAAHZAmtHA:APA91bG_0HyDBTYS3Pqd4PW19fMpZPnjipJy3Fewnt7O5nrj9KdKZEqSmyu6sw0hUfNbZrgT4daaDQrZ1XhGjxotmiiLMqppLp6TqSJRu1CjggTTD92z7ZfjBn-GH1jvQY02GK6McDtl";
            String url = "https://fcm.googleapis.com/fcm/send";

            Map<String, Object> data2 = new LinkedHashMap<>();
            data2.put("title", title);
            data2.put("is_background", false);
            data2.put("flag", flag);
            data2.put("data", data3);

            Map<String, Object> data1 = new LinkedHashMap<>();
            data1.put("to", "/topics/" + topic);
            data1.put("data", data2);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(data1);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key=" + key);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            conn.getOutputStream().write(json.getBytes("UTF-8"));
            conn.getInputStream();

            //conn.disconnect();

            String resp = conn.getResponseMessage();

            System.out.println("resp:" + resp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String BIG_LETTER = null;
    private static String SMALL_LETTER = null;
    private static String NUMBERS = null;

    public static String getBigLetter() {
        if (BIG_LETTER == null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 65; i <= 90; i++) {
                sb.append((char) i);
            }
            BIG_LETTER = sb.toString();
        }
        return BIG_LETTER;
    }

    public static String getSmallLetter() {
        if (SMALL_LETTER == null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 97; i <= 122; i++) {
                sb.append((char) i);
            }
            SMALL_LETTER = sb.toString();
        }
        return SMALL_LETTER;
    }

    public static String getNumbers() {
        if (NUMBERS == null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 48; i <= 57; i++) {
                sb.append((char) i);
            }
            NUMBERS = sb.toString();
        }
        return NUMBERS;
    }

    public static void main(String[] args) {
        Helper.sendFcmMessage(0, "Lindung Manik ingin menemui Anda, setuju?", "5MDiJCD9psQCkFgV5jokHPOSAvC3", "1");
    }
}
