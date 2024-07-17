package com.example.stockservice.util;

import com.example.grpc.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GrpcResponseHelper {

    private final ObjectMapper objectMapper;

    @Autowired
    public GrpcResponseHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> void sendJsonResponse(String key, T data, StreamObserver<Response> responseObserver) {
        try {
            Map<String, T> resultMap = new HashMap<>();
            resultMap.put(key, data);
            String jsonResponse = objectMapper.writeValueAsString(resultMap);
            Response response = Response.newBuilder().setResult(jsonResponse).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (JsonProcessingException e) {
            sendErrorResponse(e, responseObserver);
        }
    }

    public void sendErrorResponse(Throwable throwable, StreamObserver<Response> responseObserver) {
        String errorMessage;
        Status status;

        if (throwable instanceof StatusRuntimeException) {
            StatusRuntimeException statusRuntimeException = (StatusRuntimeException) throwable;
            status = statusRuntimeException.getStatus();
            errorMessage = status.getDescription();
        } else {
            status = Status.INTERNAL;
            errorMessage = throwable.getMessage();
        }
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", status.getCode().name());
        errorResponse.put("message", errorMessage);

        try {
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            Response response = Response.newBuilder().setResult(jsonResponse).build();
            responseObserver.onNext(response);
            responseObserver.onError(status.asRuntimeException());
        } catch (JsonProcessingException e) {
            Response response = Response.newBuilder()
                .setResult("{\"error\": \"Failed to process error response\"}")
                .build();
            responseObserver.onNext(response);
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to process error response").asRuntimeException());
        }
    }
}
