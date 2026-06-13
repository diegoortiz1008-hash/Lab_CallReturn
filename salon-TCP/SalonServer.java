import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SalonServer {
    public static void main(String[] args) throws Exception {
        SalonRepository repository = new SalonRepository();
        ServerSocket serverSocket = new ServerSocket(35000);
        System.out.println("SalonServer TCP escuchando en puerto 35000...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String request = in.readLine();
            String response = processRequest(request, repository);
            System.out.println("Request: " + request + " -> " + response);
            out.println(response);

            in.close();
            out.close();
            clientSocket.close();
        }
    }

    private static String processRequest(String request, SalonRepository repository) {
        if (request == null || !request.contains(",")) {
            return "ERROR_FORMATO_INVALIDO";
        }

        String[] parts = request.split(",", 2);
        String operacion = parts[0].trim();
        String codigo = parts[1].trim();

        Salon salon = repository.findByCodigo(codigo);

        if (salon == null) {
            return "ERROR_SALON_NO_EXISTE";
        }

        switch (operacion) {
            case "CONSULTAR_SALON":
                return salon.getEstado();

            case "RESERVAR_SALON":
                if (salon.isReservado()) return "ERROR_OPERACION_INVALIDA";
                salon.reservar();
                return "RESERVA_EXITOSA";

            case "LIBERAR_SALON":
                if (!salon.isReservado()) return "ERROR_OPERACION_INVALIDA";
                salon.liberar();
                return "LIBERACION_EXITOSA";

            default:
                return "ERROR_OPERACION_INVALIDA";
        }
    }
}