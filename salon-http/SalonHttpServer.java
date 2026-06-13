import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class SalonHttpServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        SalonRepository repository = new SalonRepository();

        server.createContext("/rooms", new RoomsHandler(repository));
        server.createContext("/rooms/reserve", new ReserveHandler(repository));
        server.createContext("/rooms/release", new ReleaseHandler(repository));

        server.setExecutor(null);
        server.start();
        System.out.println("SalonHttpServer escuchando en http://localhost:8080");
        System.out.println("Rutas disponibles:");
        System.out.println("  GET  /rooms            -> lista todos los salones");
        System.out.println("  GET  /rooms?id=E301    -> consulta un salon");
        System.out.println("  POST /rooms/reserve?id=E301");
        System.out.println("  POST /rooms/release?id=E301");
    }

    // ── GET /rooms y GET /rooms?id=E303 ──────────────────────────────────────
    static class RoomsHandler implements HttpHandler {
        private SalonRepository repository;
        public RoomsHandler(SalonRepository repository) { this.repository = repository; }

        @Override
        public void handle(HttpExchange exchange) throws java.io.IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendResponse(exchange, 405, "Metodo no permitido. Use GET.");
                return;
            }

            String query = exchange.getRequestURI().getQuery();

            if (query != null && query.startsWith("id=")) {
                // Consulta de un salón específico
                String codigo = query.substring(3);
                Salon salon = repository.findByCodigo(codigo);
                if (salon == null) {
                    sendResponse(exchange, 404, "ERROR_SALON_NO_EXISTE");
                } else {
                    sendResponse(exchange, 200, salon.getCodigo() + ": " + salon.getEstado());
                }
            } else {
                // Lista todos los salones
                StringBuilder sb = new StringBuilder("=== Salones ===\n");
                for (String cod : new String[]{"E301","E302","E303","E304"}) {
                    Salon s = repository.findByCodigo(cod);
                    sb.append(s.getCodigo()).append(": ").append(s.getEstado()).append("\n");
                }
                sendResponse(exchange, 200, sb.toString());
            }
        }
    }

    // ── POST /rooms/reserve?id=E303 ───────────────────────────────────────────
    static class ReserveHandler implements HttpHandler {
        private SalonRepository repository;
        public ReserveHandler(SalonRepository repository) { this.repository = repository; }

        @Override
        public void handle(HttpExchange exchange) throws java.io.IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "Metodo no permitido. Use POST.");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.startsWith("id=")) {
                sendResponse(exchange, 400, "Falta parametro id. Ejemplo: /rooms/reserve?id=E301");
                return;
            }

            String codigo = query.substring(3);
            Salon salon = repository.findByCodigo(codigo);

            if (salon == null) {
                sendResponse(exchange, 404, "ERROR_SALON_NO_EXISTE");
            } else if (salon.isReservado()) {
                sendResponse(exchange, 409, "ERROR_OPERACION_INVALIDA: salon ya reservado");
            } else {
                salon.reservar();
                sendResponse(exchange, 200, "RESERVA_EXITOSA: " + codigo);
            }
        }
    }

    // ── POST /rooms/release?id=E303 ───────────────────────────────────────────
    static class ReleaseHandler implements HttpHandler {
        private SalonRepository repository;
        public ReleaseHandler(SalonRepository repository) { this.repository = repository; }

        @Override
        public void handle(HttpExchange exchange) throws java.io.IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "Metodo no permitido. Use POST.");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.startsWith("id=")) {
                sendResponse(exchange, 400, "Falta parametro id. Ejemplo: /rooms/release?id=E301");
                return;
            }

            String codigo = query.substring(3);
            Salon salon = repository.findByCodigo(codigo);

            if (salon == null) {
                sendResponse(exchange, 404, "ERROR_SALON_NO_EXISTE");
            } else if (!salon.isReservado()) {
                sendResponse(exchange, 409, "ERROR_OPERACION_INVALIDA: salon ya disponible");
            } else {
                salon.liberar();
                sendResponse(exchange, 200, "LIBERACION_EXITOSA: " + codigo);
            }
        }
    }

    // ── Utilidad compartida ───────────────────────────────────────────────────
    static void sendResponse(HttpExchange exchange, int code, String body)
            throws java.io.IOException {
        byte[] bytes = body.getBytes();
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}