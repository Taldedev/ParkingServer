package com.parklight.util;

import com.parklight.server.Request;
import com.parklight.server.Response;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.Scanner;

/**
 * Sends a single JSON request to another server and reads one JSON response.
 * Used by the parking server to talk to the billing server.
 */
public class RemoteClient {

    private final String host;
    private final int port;
    private final Gson gson = new Gson();

    public RemoteClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // Sends the request and parses the reply into the given type.
    // responseType describes Response<SomeType> for gson.
    public <T> Response<T> send(Request<?> request, Type responseType) throws IOException {
        try (Socket socket = new Socket(host, port);
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
             Scanner reader = new Scanner(new InputStreamReader(socket.getInputStream()))) {

            writer.println(gson.toJson(request));
            writer.flush();

            if (reader.hasNextLine()) {
                return gson.fromJson(reader.nextLine(), responseType);
            }
            return null;
        }
    }
}
