import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SalonClient {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Sistema de Salones ===");
            System.out.println("1. Consultar salón");
            System.out.println("2. Reservar salón");
            System.out.println("3. Liberar salón");
            System.out.println("4. Salir");
            System.out.print("Opción: ");
            String opcion = scanner.nextLine().trim();

            if (opcion.equals("4")) break;

            System.out.print("Código del salón (E301-E304): ");
            String codigo = scanner.nextLine().trim();

            String operacion;
            switch (opcion) {
                case "1": operacion = "CONSULTAR_SALON"; break;
                case "2": operacion = "RESERVAR_SALON"; break;
                case "3": operacion = "LIBERAR_SALON"; break;
                default:
                    System.out.println("Opción no válida.");
                    continue;
            }

            String mensaje = operacion + "," + codigo;
            String respuesta = enviar(mensaje);
            System.out.println("Respuesta: " + respuesta);
        }

        System.out.println("Cliente cerrado.");
        scanner.close();
    }

    private static String enviar(String mensaje) throws Exception {
        Socket socket = new Socket("127.0.0.1", 35000);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));

        out.println(mensaje);
        String respuesta = in.readLine();

        in.close();
        out.close();
        socket.close();
        return respuesta;
    }
}