package be.jacobsvanroy;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2014  Davy Van Roy
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class LotterDraws {

    private static final int LOTTERY_REF = 60;
    private static final String LOTTER_GET_DRAWS_URL = "http://www.thelotter.com/__Ajax/__AsyncControls.asmx/GetDrawsValueNameList";

    public List<Integer> getDraws() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(LOTTER_GET_DRAWS_URL);
            connection = getDrawsConnection(url, LOTTERY_REF);
            String result = getDrawsContent(connection);
            return mapDrawsToList(result);
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private List<Integer> mapDrawsToList(String result) {
        JsonObject root = (JsonObject) new JsonParser().parse(result);
        JsonArray jsonArray = root.get("d").getAsJsonArray();
        List<Integer> draws = new ArrayList<>();
        jsonArray.forEach(jsonEl -> draws.add(jsonEl.getAsJsonObject().get("DrawRef").getAsInt()));
        return draws;
    }

    private String getDrawsContent(HttpURLConnection connection) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    private HttpURLConnection getDrawsConnection(URL url, int lotteryRef) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        addPayloadToConnection(lotteryRef, connection);
        return connection;
    }

    private void addPayloadToConnection(int lotteryRef, HttpURLConnection connection) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        String payload = "{'lotteryRef':'" + lotteryRef + "'}";
        writer.write(payload);
        writer.close();
    }
}
