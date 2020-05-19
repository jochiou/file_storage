package presignedUrl;

import java.io.BufferedReader;
import java.io.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.google.gson.stream.JsonReader;
import jdk.nashorn.internal.parser.JSONParser;
import com.google.gson.*;
/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyResponseEvent, Object> {

    public Object handleRequest(final APIGatewayProxyResponseEvent input, final Context context) {
        //context = context.getLogger();
        System.out.println("orig input: " + input);
        System.out.println("request body: " + input.getBody());

        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");


        try {
            //final String pageContents = this.getPageContents("https://checkip.amazonaws.com");
            /*
            ObjectMapper mapper = new ObjectMapper();
            //mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            JsonNode jsonObj = mapper.readTree(input.toString().trim());
            String fileSignature = jsonObj.get("fileSignature").toString();
            */

            Gson gson = new Gson();
            JsonReader jr = new JsonReader(new StringReader(input.getBody().trim()));
            jr.setLenient(true);
            Map keyValueMap = (Map) gson.fromJson(jr, Object.class);
            //gson.fromJson(gson.toJson(input.toString().trim()), String.class);
            String fileSignature = keyValueMap.get("fileSignature").toString();

            if(fileSignature.trim().length() < 1){
                return new GatewayResponse("Invalid file name", headers, 500);
            }

            String output = createPresignedUrl(fileSignature);
            //String.format("{ \"message\": \"hello world\", \"location\": \"%s\" }", pageContents);
            //APIGatewayProxyResponseEvent output = new APIGatewayProxyResponseEvent();
            //output.setBody(url);
            return new GatewayResponse(output, headers, 200);
        } catch (JsonParseException e){
            e.printStackTrace();
            return new GatewayResponse("Invalid json syntax: " + ", Input: " + input + ", Error Message: " + e.getMessage(), headers, 500);
        } catch (Exception e) {
            e.printStackTrace();
            return new GatewayResponse("Unexpected error happened: " + ", Input: " + input + ", Error Message: " + e.getMessage(), headers, 500);
        }
    }

    private String getPageContents(String address) throws IOException {
        URL url = new URL(address);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private String createPresignedUrl(String fileSignature) throws AmazonServiceException, SdkClientException, IOException {
        Regions clientRegion = Regions.US_WEST_2;
        //String bucketName = "public-file-storage-oregon";
        String bucketName = "file-storage-oregon";
        String objectKey = fileSignature;


        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
                /*
                AmazonS3ClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider())
                .withRegion(clientRegion)
                .build();
                */
        // Set the pre-signed URL to expire after one hour.
        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);

        // Generate the pre-signed URL.
        System.out.println("Generating pre-signed URL.");
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey)
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration);

        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        System.out.println("URL = " + url.toString());

        return url.toString();
    }
}
