package come.codesnack;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleHttpServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/user_check", new UserHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Сервер запущено на порту 8081");
    }

    static class UserHandler implements HttpHandler {
        private Map<String, JsonObject> users;

        public UserHandler() {
            try (InputStream input = getClass().getResourceAsStream("/users.json")) {
                if (input == null) throw new FileNotFoundException("users.json not found");
                JsonObject json = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonObject();
                users = json.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().getAsJsonObject()
                        ));
            } catch (IOException e) {
                throw new RuntimeException("Помилка читання users.json", e);
            }
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery(); // login=is-32fiot-23-093
            String login = null;

            if (query != null && query.startsWith("login=")) {
                login = query.substring(6);
            }

            String response;
            if (login != null && users.containsKey(login)) {
                JsonObject user = users.get(login);
                response = String.format("Прізвище: %s\nІм’я: %s\nКурс: %d\nГрупа: %s",
                        user.get("surname").getAsString(),
                        user.get("name").getAsString(),
                        user.get("course").getAsInt(),
                        user.get("group").getAsString());
            } else {
                response = "Користувача з таким логіном не знайдено або логін не вказано.";
            }

            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }

        }
    }
}

