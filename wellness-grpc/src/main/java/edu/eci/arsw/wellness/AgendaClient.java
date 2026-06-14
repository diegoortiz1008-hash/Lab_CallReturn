package edu.eci.arsw.wellness;

import edu.eci.arsw.eciencia.agenda.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Scanner;

public class AgendaClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50072)
                .usePlaintext()
                .build();

        AgendaServiceGrpc.AgendaServiceBlockingStub stub =
                AgendaServiceGrpc.newBlockingStub(channel);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Agenda ECICIENCIA ===");
            System.out.println("1. Ver agenda completa");
            System.out.println("2. Consultar por franja horaria");
            System.out.println("3. Consultar actividad por ID");
            System.out.println("4. Salir");
            System.out.print("Opcion: ");
            String opcion = scanner.nextLine().trim();

            if (opcion.equals("4")) break;

            switch (opcion) {
                case "1": {
                    ActivityList list = stub.getAgenda(AgendaEmpty.newBuilder().build());
                    imprimir(list);
                    break;
                }

                case "2": {
                    System.out.print("Franja horaria (ej: 09:00-11:00): ");
                    String slot = scanner.nextLine().trim();

                    TimeSlotRequest request = TimeSlotRequest.newBuilder()
                            .setTimeSlot(slot)
                            .build();

                    ActivityList list = stub.getActivitiesByTimeSlot(request);
                    imprimir(list);
                    break;
                }

                case "3": {
                    System.out.print("ID de actividad (ej: A01): ");
                    String id = scanner.nextLine().trim();

                    ActivityRequest request = ActivityRequest.newBuilder()
                            .setActivityId(id)
                            .build();

                    ActivityResponse response = stub.getActivity(request);
                    if (!response.getFound()) {
                        System.out.println("Actividad no encontrada.");
                    } else {
                        imprimir(response.getActivity());
                    }
                    break;
                }

                default:
                    System.out.println("Opcion no valida.");
            }
        }

        scanner.close();
        channel.shutdown();
    }

    private static void imprimir(ActivityList list) {
        if (list.getActivitiesCount() == 0) {
            System.out.println("No hay actividades para mostrar.");
            return;
        }
        for (Activity a : list.getActivitiesList()) {
            imprimir(a);
        }
    }

    private static void imprimir(Activity a) {
        System.out.println(
            "ID: " + a.getActivityId()
            + " | " + a.getTitle()
            + " | Ponente: " + a.getSpeaker()
            + " | Horario: " + a.getTimeSlot()
            + " | Lugar: " + a.getLocation()
        );
    }
}