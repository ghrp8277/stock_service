package com.example.stockservice.util;

import com.example.grpc.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            sendErrorResponse(e.getMessage(), responseObserver);
        }
    }

    public void sendErrorResponse(String errorMessage, StreamObserver<Response> responseObserver) {
        Response response = Response.newBuilder()
            .setResult("{\"error\": \"" + errorMessage + "\"}")
            .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
