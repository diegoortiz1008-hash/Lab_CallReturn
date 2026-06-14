package edu.eci.arsw.wellness;

import edu.eci.arsw.wellness.medical.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Map;

public class MedicalServer {

    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(50062)
                .addService(new MedicalServiceImpl())
                .build();

        server.start();
        System.out.println("MedicalService gRPC iniciado en puerto 50062");
        server.awaitTermination();
    }

    static class MedicalServiceImpl extends MedicalServiceGrpc.MedicalServiceImplBase {

        private Map<String, Specialty> specialties = new HashMap<>();

        public MedicalServiceImpl() {
            specialties.put("MED", Specialty.newBuilder()
                    .setCode("MED").setName("Medicina General").setAvailable(true).build());
            specialties.put("PSY", Specialty.newBuilder()
                    .setCode("PSY").setName("Psicologia").setAvailable(true).build());
            specialties.put("DEN", Specialty.newBuilder()
                    .setCode("DEN").setName("Odontologia").setAvailable(false).build());
        }

        @Override
        public void getSpecialties(Empty request, StreamObserver<SpecialtyList> responseObserver) {
            SpecialtyList list = SpecialtyList.newBuilder()
                    .addAllSpecialties(specialties.values())
                    .build();

            responseObserver.onNext(list);
            responseObserver.onCompleted();
        }

        @Override
        public void getSpecialty(SpecialtyRequest request, StreamObserver<SpecialtyResponse> responseObserver) {
            Specialty specialty = specialties.get(request.getCode());

            SpecialtyResponse response;
            if (specialty == null) {
                response = SpecialtyResponse.newBuilder().setFound(false).build();
            } else {
                response = SpecialtyResponse.newBuilder().setFound(true).setSpecialty(specialty).build();
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}