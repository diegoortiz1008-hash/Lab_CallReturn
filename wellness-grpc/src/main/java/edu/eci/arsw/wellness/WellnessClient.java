package edu.eci.arsw.wellness;

import edu.eci.arsw.wellness.medical.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Scanner;

public class WellnessClient {

    public static void main(String[] args) {
        // Canal hacia AppointmentService (50061)
        ManagedChannel appointmentChannel = ManagedChannelBuilder
                .forAddress("localhost", 50061)
                .usePlaintext()
                .build();
        AppointmentServiceGrpc.AppointmentServiceBlockingStub appointmentStub =
                AppointmentServiceGrpc.newBlockingStub(appointmentChannel);

        // Canal hacia MedicalService (50062)
        ManagedChannel medicalChannel = ManagedChannelBuilder
                .forAddress("localhost", 50062)
                .usePlaintext()
                .build();
        MedicalServiceGrpc.MedicalServiceBlockingStub medicalStub =
                MedicalServiceGrpc.newBlockingStub(medicalChannel);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Wellness Client (multi-servicio) ===");
            System.out.println("1. Ver especialidades medicas (MedicalService)");
            System.out.println("2. Solicitar cita (AppointmentService)");
            System.out.println("3. Salir");
            System.out.print("Opcion: ");
            String opcion = scanner.nextLine().trim();

            if (opcion.equals("3")) break;

            switch (opcion) {
                case "1": {
                    SpecialtyList list = medicalStub.getSpecialties(Empty.newBuilder().build());
                    System.out.println("--- Especialidades ---");
                    for (Specialty s : list.getSpecialtiesList()) {
                        System.out.println(s.getCode() + " | " + s.getName()
                                + " | Disponible: " + s.getAvailable());
                    }
                    break;
                }

                case "2": {
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

                    AppointmentResponse response = appointmentStub.requestAppointment(request);
                    System.out.println("ID de la cita: " + response.getAppointmentId());
                    System.out.println("Mensaje: " + response.getMessage());
                    break;
                }

                default:
                    System.out.println("Opcion no valida.");
            }
        }

        scanner.close();
        appointmentChannel.shutdown();
        medicalChannel.shutdown();
    }
}