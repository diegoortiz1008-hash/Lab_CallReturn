package edu.eci.arsw.wellness;

import edu.eci.arsw.eciencia.agenda.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgendaServer {

    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(50072)
                .addService(new AgendaServiceImpl())
                .build();

        server.start();
        System.out.println("AgendaService gRPC iniciado en puerto 50072");
        server.awaitTermination();
    }

    static class AgendaServiceImpl extends AgendaServiceGrpc.AgendaServiceImplBase {

        private Map<String, Activity> activities = new HashMap<>();

        public AgendaServiceImpl() {
            activities.put("A01", Activity.newBuilder()
                    .setActivityId("A01")
                    .setTitle("Charla: Arquitecturas Distribuidas")
                    .setSpeaker("Rodrigo Gualtero")
                    .setTimeSlot("08:00-09:00")
                    .setLocation("Auditorio Principal")
                    .build());

            activities.put("A02", Activity.newBuilder()
                    .setActivityId("A02")
                    .setTitle("Taller: Introduccion a gRPC")
                    .setSpeaker("Equipo ARSW")
                    .setTimeSlot("09:00-11:00")
                    .setLocation("Lab E301")
                    .build());

            activities.put("A03", Activity.newBuilder()
                    .setActivityId("A03")
                    .setTitle("Taller: Microservicios con Spring Boot")
                    .setSpeaker("Equipo ARSW")
                    .setTimeSlot("09:00-11:00")
                    .setLocation("Lab E302")
                    .build());

            activities.put("A04", Activity.newBuilder()
                    .setActivityId("A04")
                    .setTitle("Charla: Tendencias en Computacion en la Nube")
                    .setSpeaker("Invitado externo")
                    .setTimeSlot("14:00-15:00")
                    .setLocation("Auditorio Principal")
                    .build());
        }

        @Override
        public void getAgenda(AgendaEmpty request, StreamObserver<ActivityList> responseObserver) {
            ActivityList list = ActivityList.newBuilder()
                    .addAllActivities(activities.values())
                    .build();

            responseObserver.onNext(list);
            responseObserver.onCompleted();
        }

        @Override
        public void getActivitiesByTimeSlot(TimeSlotRequest request,
                                             StreamObserver<ActivityList> responseObserver) {
            List<Activity> result = new ArrayList<>();
            for (Activity a : activities.values()) {
                if (a.getTimeSlot().equals(request.getTimeSlot())) {
                    result.add(a);
                }
            }

            ActivityList list = ActivityList.newBuilder()
                    .addAllActivities(result)
                    .build();

            responseObserver.onNext(list);
            responseObserver.onCompleted();
        }

        @Override
        public void getActivity(ActivityRequest request,
                                 StreamObserver<ActivityResponse> responseObserver) {
            Activity activity = activities.get(request.getActivityId());

            ActivityResponse response;
            if (activity == null) {
                response = ActivityResponse.newBuilder().setFound(false).build();
            } else {
                response = ActivityResponse.newBuilder().setFound(true).setActivity(activity).build();
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}