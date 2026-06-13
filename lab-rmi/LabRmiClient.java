import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class LabRmiClient {
    public static void main(String[] args) throws Exception {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 23000);
        LabService service = (LabService) registry.lookup("labService");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Inventario de Laboratorios ===");
            System.out.println("1. Ver todos los equipos");
            System.out.println("2. Consultar equipo");
            System.out.println("3. Reservar equipo");
            System.out.println("4. Liberar equipo");
            System.out.println("5. Salir"); 
            System.out.print("Opcion: ");
            String opcion = scanner.nextLine().trim();

            if (opcion.equals("5")) break;

            switch (opcion) {
                case "1":
                    List<String> equipos = service.consultarEquipos();
                    System.out.println("--- Equipos ---");
                    for (String e : equipos) System.out.println(e);
                    break;

                case "2":
                    System.out.print("Codigo del equipo: ");
                    String cod = scanner.nextLine().trim();
                    System.out.println(service.consultarEquipo(cod));
                    break;

                case "3":
                    System.out.print("Codigo del equipo a reservar: ");
                    String codR = scanner.nextLine().trim();
                    boolean r = service.reservarEquipo(codR);
                    System.out.println(r ? "RESERVA_EXITOSA" : "ERROR: no se pudo reservar");
                    break;

                case "4":
                    System.out.print("Codigo del equipo a liberar: ");
                    String codL = scanner.nextLine().trim();
                    boolean l = service.liberarEquipo(codL);
                    System.out.println(l ? "LIBERACION_EXITOSA" : "ERROR: no se pudo liberar");
                    break;

                default:
                    System.out.println("Opcion no valida.");
            }
        }
        scanner.close();
    }
}