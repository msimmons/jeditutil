package net.contrapt.jeditutil;

import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test json utilities
 */
public class JSONUtilTest {
   String jsonString="{\n" +
         "  \"_id\": {\n" +
         "    \"_time\": 1366151950,\n" +
         "    \"_machine\": -1319750898,\n" +
         "    \"_inc\": 1767306696,\n" +
         "    \"_new\": false\n" +
         "  },\n" +
         "  \"value\": \"    \",\n" +
         "  \"srNumber\": \"SR70090423170\",\n" +
         "  \"baseCost\": 515,\n" +
         "  \"costComponent\": {\n" +
         "    \"value\": \"   \",\n" +
         "    \"name\": \"PRICE_EXPERIMENT\",\n" +
         "    \"amount\": 0\n" +
         "  },\n" +
         "  \"paymentInfo\": {\n" +
         "    \"value\": \"             \",\n" +
         "    \"type\": \"charity\",\n" +
         "    \"source\": \"CHARITY\",\n" +
         "    \"sourceId\": 16,\n" +
         "    \"name\": \"Michelle Williams\",\n" +
         "    \"address1\": \"999 Fake St.\",\n" +
         "    \"address2\": \"\",\n" +
         "    \"city\": \"Stamford\",\n" +
         "    \"state\": \"CT\",\n" +
         "    \"zip\": \"06902\",\n" +
         "    \"country\": \"United States\",\n" +
         "    \"isCharity\": true,\n" +
         "    \"payableTo\": \"Sarah Morris\"\n" +
         "  },\n" +
         "  \"dbCollection\": \"itemAccepted\",\n" +
         "  \"headers\": {\n" +
         "    \"destination\": \"queue://erp.item\",\n" +
         "    \"routeId\": \"mongoLogger\",\n" +
         "    \"exchangeId\": \"ID-ip-10-195-210-235-53987-1365798169660-4-1705\",\n" +
         "    \"create_dt\": \"2013-04-16 22:39:10 GMT\"\n" +
         "  }\n" +
         "}";

   @Test
   public void convertJson() throws IOException {
      JSONUtil util = new JSONUtil();
      System.out.println(util.json2xml(jsonString));
   }
}
