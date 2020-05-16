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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;
import com.google.gson.stream.JsonReader;
import jdk.nashorn.internal.parser.JSONParser;
import com.google.gson.*;
/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<Object, Object> {

    public Object handleRequest(final Object input, final Context context) {
        //context = context.getLogger();
        System.out.println("orig input: " + input);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        try {
            //final String pageContents = this.getPageContents("https://checkip.amazonaws.com");
            Gson gson = new Gson();
            JsonReader jr = new JsonReader(new StringReader(input.toString().trim()));
            jr.setLenient(true);
            Map keyValueMap = (Map) gson.fromJson(jr, Object.class);

            //gson.fromJson(gson.toJson(input.toString().trim()), String.class);

            String fileSignature = keyValueMap.get("fileSignature").toString();

            String output = createPresignedUrl(fileSignature);
                    //String.format("{ \"message\": \"hello world\", \"location\": \"%s\" }", pageContents);
            //APIGatewayProxyResponseEvent output = new APIGatewayProxyResponseEvent();
            //output.setBody(url);
            return new GatewayResponse(output, headers, 200);
        } catch (JsonSyntaxException e){
            e.printStackTrace();
            return new GatewayResponse("{invalid json syntax}", headers, 500);
        } catch (Exception e) {
            e.printStackTrace();
            return new GatewayResponse("{}", headers, 500);
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
        String bucketName = "public-file-storage-oregon";
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
