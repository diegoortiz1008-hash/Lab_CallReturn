package edu.eci.arsw.wellness;

import edu.eci.arsw.wellness.medical.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Scanner;

public class WellnessGateway {

    public static void main(String[] args) {
        // El Gateway conoce los puertos internos; el usuario no los necesita saber.
        ManagedChannel appointmentChannel = ManagedChannelBuilder
                .forAddress("localhost", 50061)
                .usePlaintext()
                .build();
        AppointmentServiceGrpc.AppointmentServiceBlockingStub appointmentStub =
                AppointmentServiceGrpc.newBlockingStub(appointmentChannel);

        ManagedChannel medicalChannel = ManagedChannelBuilder
                .forAddress("localhost", 50062)
                .usePlaintext()
                .build();
        MedicalServiceGrpc.MedicalServiceBlockingStub medicalStub =
                MedicalServiceGrpc.newBlockingStub(medicalChannel);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== WellnessGateway ===");
            System.out.println("1. requestAppointment");
            System.out.println("2. getStudentWellnessSummary");
            System.out.println("3. Salir");
            System.out.print("Opcion: ");
            String opcion = scanner.nextLine().trim();

            if (opcion.equals("3")) break;

            switch (opcion) {
                case "1": {
                    System.out.print("ID de estudiante: ");
                    String studentId = scanner.nextLine().trim();

                    System.out.print("Nombre: ");
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

                    System.out.println("--- Respuesta del Gateway ---");
                    System.out.println("ID de la cita: " + response.getAppointmentId());
                    System.out.println("Estado: " + response.getStatus());
                    System.out.println("Mensaje: " + response.getMessage());
                    break;
                }

                case "2": {
                    System.out.print("ID de estudiante: ");
                    String studentId = scanner.nextLine().trim();

                    // 1. Consultar citas activas en AppointmentService
                    StudentRequest studentRequest = StudentRequest.newBuilder()
                            .setStudentId(studentId)
                            .build();
                    AppointmentList appointments = appointmentStub.getAppointments(studentRequest);

                    // 2. Consultar especialidades disponibles en MedicalService
                    SpecialtyList specialties = medicalStub.getSpecialties(Empty.newBuilder().build());

                    // 3. Unificar la respuesta para el cliente
                    System.out.println("--- Resumen de Bienestar (Gateway) ---");

                    System.out.println("\nCitas activas:");
                    if (appointments.getAppointmentsCount() == 0) {
                        System.out.println("  No tienes citas activas.");
                    } else {
                        for (Appointment a : appointments.getAppointmentsList()) {
                            System.out.println("  ID: " + a.getAppointmentId()
                                    + " | Servicio: " + a.getServiceType()
                                    + " | Fecha: " + a.getDate()
                                    + " | Estado: " + a.getStatus());
                        }
                    }

                    System.out.println("\nEspecialidades disponibles:");
                    for (Specialty s : specialties.getSpecialtiesList()) {
                        if (s.getAvailable()) {
                            System.out.println("  " + s.getCode() + " | " + s.getName());
                        }
                    }
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