package com.justfors.docker;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class DockerApplicationTests {

    private int threadPoolSize = 300;

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
        List<Future<RequestLatencyMeter.StatisticData>> results = executorService.invokeAll(generateLoad(threadPoolSize, 1));
        Map<String, Long> serverMap = new HashMap<>();
        double avgltc = results.stream().map(f -> {
            try {
                RequestLatencyMeter.StatisticData statisticData = f.get();
                Long count = serverMap.get(statisticData.response);
                if (count != null) {
                    serverMap.put(statisticData.response, ++count);
                } else {
                    serverMap.put(statisticData.response, 1L);
                }
                return statisticData.totalTime;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return 0L;
        }).mapToDouble(a -> a).average().getAsDouble();
        serverMap.forEach((k,v) -> System.out.println(k + "\n" + "count of call: " + v));
        System.out.println(avgltc + "   avg msec");
    }

    private List<RequestLatencyMeter> generateLoad(int load, long requestCount) {
        List<RequestLatencyMeter> result = new ArrayList<>();
        for (int i = 0; i < load; i++) {
            result.add(new RequestLatencyMeter(requestCount));
        }
        return result;
    }


    private class RequestLatencyMeter implements Callable<RequestLatencyMeter.StatisticData> {

        private long requestsCount;

        RequestLatencyMeter(long requestsCount) {
            this.requestsCount = requestsCount;
        }

        @Override
        public RequestLatencyMeter.StatisticData call() {
            long start = System.currentTimeMillis();
            String response = null;
            for (int j = 0; j < requestsCount; j++) {
                response = makeRequest();
            }
            long end = System.currentTimeMillis();
            return new StatisticData((end - start), response);
        }

        private class StatisticData {
            long totalTime;
            String response;

            StatisticData(long totalTime, String response) {
                this.totalTime = totalTime;
                this.response = response;
            }
        }
    }

    private String makeRequest() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                //.url("http://localhost:7000")
                //.url("http://192.168.99.100:8989")
                .url("http://192.168.99.101:7777")
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
