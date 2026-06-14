package edu.eci.arsw.wellness;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Scanner;

public class AppointmentClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50061)
                .usePlaintext()
                .build();

        AppointmentServiceGrpc.AppointmentServiceBlockingStub stub =
                AppointmentServiceGrpc.newBlockingStub(channel);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Bienestar Universitario ===");
            System.out.println("1. Solicitar cita");
            System.out.println("2. Cancelar cita");
            System.out.println("3. Ver mis citas");
            System.out.println("4. Salir");
            System.out.print("Opcion: ");
            String opcion = scanner.nextLine().trim();

            if (opcion.equals("4")) break;

            switch (opcion) {
                case "1": {
                    System.out.print("Tu ID de estudiante: ");
                    String studentId = scanner.nextLine().trim();

                    System.out.print("Tu nombre: ");
                    String studentName = scanner.nextLine().trim();

                    System.out.print("Correo institucional: ");
                    String email = scanner.nextLine().trim();

                    System.out.println("Tipo de servicio (0=MEDICINE, 1=PSYCHOLOGY, 2=DENTISTRY): ");
                    int tipo = Integer.parseInt(scanner.nextLine().trim());

                    System.out.print("Fecha (ej: 2026-06-20): ");
                    String date = scanner.nextLine().trim();

                    AppointmentRequest request = AppointmentRequest.newBuilder()
                            .setStudentId(studentId)
                            .setStudentName(studentName)
                            .setInstitutionalEmail(email)
                            .setServiceType(ServiceType.forNumber(tipo))
                            .setDate(date)
                            .build();

                    AppointmentResponse response = stub.requestAppointment(request);

                    System.out.println("ID de la cita: " + response.getAppointmentId());
                    System.out.println("Estado: " + response.getStatus());
                    System.out.println("Mensaje: " + response.getMessage());
                    break;
                }

                case "2": {
                    System.out.print("ID de la cita a cancelar: ");
                    String appointmentId = scanner.nextLine().trim();

                    CancelRequest request = CancelRequest.newBuilder()
                            .setAppointmentId(appointmentId)
                            .build();

                    CancelResponse response = stub.cancelAppointment(request);
                    System.out.println("Resultado: " + response.getMessage());
                    break;
                }

                case "3": {
                    System.out.print("Tu ID de estudiante: ");
                    String studentId = scanner.nextLine().trim();

                    StudentRequest request = StudentRequest.newBuilder()
                            .setStudentId(studentId)
                            .build();

                    AppointmentList list = stub.getAppointments(request);

                    if (list.getAppointmentsCount() == 0) {
                        System.out.println("No tienes citas activas.");
                    } else {
                        System.out.println("--- Tus citas ---");
                        for (Appointment a : list.getAppointmentsList()) {
                            System.out.println(
                                "ID: " + a.getAppointmentId()
                                + " | Servicio: " + a.getServiceType()
                                + " | Fecha: " + a.getDate()
                                + " | Estado: " + a.getStatus()
                            );
                        }
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
}