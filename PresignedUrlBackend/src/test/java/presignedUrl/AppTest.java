package presignedUrl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.Test;

public class AppTest {
  @Test
  public void successfulResponse() {
    String stringInput = "{\n" +
            "  \"fileSignature\": \"test file from local\",\n" +
            "  \"key2\": \"value2\",\n" +
            "  \"key3\": \"value3\"\n" +
            "}";
    APIGatewayProxyResponseEvent event = new APIGatewayProxyResponseEvent();
    event.setBody("{\n" +
            "  \"fileSignature\": \"testfilefromlambda\"\n" +
            "}");
    App app = new App();
    GatewayResponse result = (GatewayResponse) app.handleRequest(event, null);
    assertEquals(result.getStatusCode(), 200);
    assertEquals(result.getHeaders().get("Content-Type"), "application/json");
    String content = result.getBody();
    assertNotNull(content);
    //assertTrue(content.contains("\"message\""));
    //assertTrue(content.contains("\"hello world\""));
    //assertTrue(content.contains("\"location\""));
  }
}
