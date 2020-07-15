package com.justfors.docker;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class DockerApplicationTests {

    private int threadPoolSize = 3;

    private ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

    public static void main(String[] args) throws Exception {
        new DockerApplicationTests().contextLoads(3);
    }

    void contextLoads(int cycleCount) throws Exception {
        for (int i = 0; i < cycleCount; i++) {
            doTest();
            System.out.println();
        }
        System.out.println("DONE");
    }

    private void doTest() throws Exception {
        List<Future<List<Long>>> results = executorService.invokeAll(generateLoad(threadPoolSize, 1));
        double avgltc = results.stream().map(f -> {
            try {
                return f.get().stream().mapToDouble(a -> a).average().getAsDouble();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return 0d;
        }).mapToDouble(a -> a).average().getAsDouble();
        System.out.println(avgltc + "   avg msec");
    }

    private List<RequestLatencyMeter> generateLoad(int load, long requestCount) {
        List<RequestLatencyMeter> result = new ArrayList<>();
        for (int i = 0; i < load; i++) {
            result.add(new RequestLatencyMeter(requestCount));
        }
        return result;
    }


    private class RequestLatencyMeter implements Callable<List<Long>> {

        private long requestsCount;

        RequestLatencyMeter(long requestsCount) {
            this.requestsCount = requestsCount;
        }

        @Override
        public List<Long> call() {
            List<Long> latencies = new ArrayList<>();
            for (int j = 0; j < requestsCount; j++) {
                long start = System.currentTimeMillis();
                String response = makeRequest();
                System.out.println(response);
                long end = System.currentTimeMillis();
                latencies.add(end - start);
            }
            return latencies;
        }
    }

    private String makeRequest() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:7000")
                //.url("http://192.168.99.100:8989")
                //.url("http://192.168.99.101:7777")
                .method("GET", null)
                .build();

        Response response = null;
        String body = null;

        try {
            response = client.newCall(request).execute();
            try (ResponseBody rb = response.body()) {
                body = rb.string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return body;
    }

}
