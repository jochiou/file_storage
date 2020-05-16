package presignedUrl.model.request;

public class ApiGatewayProxyRequestInput {
    private String resource;
    private String path;
    private String httpMethod;
    private String headers;
    private String multiValueHeaders;
    private String queryStringParameters;
    private String multiValueQueryStringParameters;
    private String pathParameters;
    private String stageVariables;
    private String requestContext;
    private String body;
    private boolean isBase64Encoded;
}
