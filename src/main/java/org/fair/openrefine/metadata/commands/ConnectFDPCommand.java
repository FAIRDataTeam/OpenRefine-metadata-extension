package org.fair.openrefine.metadata.commands;

import com.google.refine.commands.Command;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ConnectFDPCommand extends Command {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uri = request.getParameter("uri");

        logger.info("Contacting FAIR Data Point on URI: " + uri);
        try {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "text/turtle");

            if (conn.getResponseCode() != 200) {
                respond(response, "error", "HTTP error code: " + conn.getResponseCode());
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder x = new StringBuilder();
                String currentLine;

                while ((currentLine = in.readLine()) != null)
                    x.append(currentLine).append('\n');

                in.close();
                respond(response, "ok", x.toString());
            }
        } catch (IOException e) {
            respond(response, "error", "Failed to contact FAIR Data Point: " + uri);
        }
    }
}
