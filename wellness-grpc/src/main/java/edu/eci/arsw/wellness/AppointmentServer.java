package edu.eci.arsw.wellness;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AppointmentServer {

    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(50061)
                .addService(new AppointmentServiceImpl())
                .build();

        server.start();
        System.out.println("AppointmentService gRPC iniciado en puerto 50061");
        server.awaitTermination();
    }

    static class AppointmentServiceImpl extends AppointmentServiceGrpc.AppointmentServiceImplBase {

        // appointmentId -> Appointment
        private Map<String, Appointment> appointments = new HashMap<>();

        @Override
        public void requestAppointment(AppointmentRequest request,
                                        StreamObserver<AppointmentResponse> responseObserver) {

            String id = UUID.randomUUID().toString().substring(0, 8);

            Appointment appointment = Appointment.newBuilder()
                    .setAppointmentId(id)
                    .setStudentId(request.getStudentId())
                    .setServiceType(request.getServiceType())
                    .setDate(request.getDate())
                    .setStatus(Status.REQUESTED)
                    .build();

            appointments.put(id, appointment);

            AppointmentResponse response = AppointmentResponse.newBuilder()
                    .setAppointmentId(id)
                    .setStatus(Status.REQUESTED)
                    .setSuccess(true)
                    .setMessage("Cita creada correctamente")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void cancelAppointment(CancelRequest request,
                                       StreamObserver<CancelResponse> responseObserver) {

            Appointment appointment = appointments.get(request.getAppointmentId());
            CancelResponse response;

            if (appointment == null) {
                response = CancelResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("ERROR: cita no encontrada")
                        .build();
            } else if (appointment.getStatus() == Status.CANCELLED) {
                response = CancelResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("ERROR: la cita ya estaba cancelada")
                        .build();
            } else {
                Appointment updated = appointment.toBuilder()
                        .setStatus(Status.CANCELLED)
                        .build();
                appointments.put(request.getAppointmentId(), updated);

                response = CancelResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Cita cancelada correctamente")
                        .build();
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void getAppointments(StudentRequest request,
                                     StreamObserver<AppointmentList> responseObserver) {

            List<Appointment> result = new ArrayList<>();
            for (Appointment a : appointments.values()) {
                if (a.getStudentId().equals(request.getStudentId())
                        && a.getStatus() != Status.CANCELLED) {
                    result.add(a);
                }
            }

            AppointmentList list = AppointmentList.newBuilder()
                    .addAllAppointments(result)
                    .build();

            responseObserver.onNext(list);
            responseObserver.onCompleted();
        }
    }
}