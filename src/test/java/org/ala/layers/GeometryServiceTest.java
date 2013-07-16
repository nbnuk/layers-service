package org.ala.layers;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ala.layers.dao.LayerIntersectDAO;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class is to help with debugging of the geometry service. The tests are
 * all ignored so that they do not get run as part of the build process.
 * 
 * @author 1551F
 * 
 */
public class GeometryServiceTest {

    @Test
    public void testgeometryWkt() throws Exception {
        String json = "{\"wkt\": \"POLYGON((125.5185546875 -21.446934836644,128.2431640625 -21.446934836644,128.2431640625 -19.138942324356,125.5185546875 -19.138942324356,125.5185546875 -21.446934836644))\", \"name\": \"test\", \"description\": \"test\", \"user_id\": \"1551\"}";

        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("http://localhost:8080/layers-service/shape/upload/wkt");

        post.setRequestHeader("Content-Type", "application/json");
        post.setRequestBody(json);

        int returnCode = httpClient.executeMethod(post);
        Assert.assertEquals(200, returnCode);

        String responseText = post.getResponseBodyAsString();
        System.out.println(responseText);
        Assert.assertTrue(responseText.matches("\\{\"id\":\\d+\\}"));
    }

    @Test
    public void testgeometryWkt2() throws Exception {
        String json = "{\"geojson\":{\"centre\":[149.8095703125,-26.247925472749493],\"areaKmSq\":287407.35535557417,\"state\":\"Queensland\",\"locality\":\"LOT 2 Jackson-Wandoan Road, Woleebee QLD 4426, Australia\",\"userDrawn\":\"Polygon\",\"type\":\"Polygon\",\"lga\":\"Western Downs (R)\",\"coordinates\":[[[145.1953125,-18.47960905583197],[148.1396484375,-20.49906428341304],[148.7109375,-22.32975230437647],[150.49072265625,-22.806567100271508],[151.2158203125,-25.443274612305746],[151.875,-28.516969440401045],[150.99609375,-34.016241889667015],[154.423828125,-26.352497858154],[149.853515625,-19.973348786110602],[145.1953125,-18.47960905583197]]]},\"name\":\"Polygon Site\",\"description\":\"my description\",\"user_id\":\"34\",\"api_key\":\"dummy_key\"}";
        // String json =
        // "{\"geojson\":\"{\"centre\":[149.8095703125,-26.247925472749493],\"areaKmSq\":287407.35535557417,\"state\":\"Queensland\",\"locality\":\"LOT 2 Jackson-Wandoan Road, Woleebee QLD 4426, Australia\",\"userDrawn\":\"Polygon\",\"type\":\"Polygon\",\"lga\":\"Western Downs (R)\",\"coordinates\":[[[145.1953125,-18.47960905583197],[148.1396484375,-20.49906428341304],[148.7109375,-22.32975230437647],[150.49072265625,-22.806567100271508],[151.2158203125,-25.443274612305746],[151.875,-28.516969440401045],[150.99609375,-34.016241889667015],[154.423828125,-26.352497858154],[149.853515625,-19.973348786110602],[145.1953125,-18.47960905583197]]]}\",\"name\":\"Polygon Site\",\"description\":\"my description\",\"user_id\":\"34\",\"api_key\":\"dummy_key\"}";
        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("http://localhost:8080/layers-service/shape/upload/geojson");

        post.setRequestHeader("Content-Type", "application/json");
        post.setRequestBody(json);

        int returnCode = httpClient.executeMethod(post);
        Assert.assertEquals(200, returnCode);

        String responseText = post.getResponseBodyAsString();
        System.out.println(responseText);
        Assert.assertTrue(responseText.matches("\\{\"id\":\\d+\\}"));
    }

    @Test
    public void testgeometryGeojson() throws Exception {
        String json = "{\"geojson\": {\"type\":\"Polygon\",\"coordinates\":[[[125.5185546875,-21.446934836644001],[128.2431640625,-21.446934836644001],[128.2431640625,-19.138942324356002],[125.5185546875,-19.138942324356002],[125.5185546875,-21.446934836644001]]]}, \"name\": \"test\", \"description\": \"test\", \"user_id\": \"1551\"}";

        System.out.println(json);
        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("http://spatial-dev.ala.org.au/layers-service/shape/upload/geojson");

        post.setRequestHeader("Content-Type", "application/json");
        post.setRequestBody(json);

        int returnCode = httpClient.executeMethod(post);
        Assert.assertEquals(200, returnCode);

        String responseText = post.getResponseBodyAsString();
        System.out.println(responseText);
        Assert.assertTrue(responseText.matches("\\{\"id\":\\d+\\}"));
    }

    @Test
    public void testPointRadiusUpload() throws Exception {
        String json = "{\"name\": \"test\", \"description\": \"test\", \"user_id\": \"1551\", \"api_key\": \"b3f3c932-ba88-4ad5-b429-f947475024af\"}";

        System.out.println(json);
        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("http://spatial-dev.ala.org.au/layers-service/shape/upload/pointradius/-35/149/1");

        post.setRequestHeader("Content-Type", "application/json");
        post.setRequestBody(json);

        int returnCode = httpClient.executeMethod(post);
        Assert.assertEquals(200, returnCode);

        String responseText = post.getResponseBodyAsString();
        System.out.println(responseText);
        Assert.assertTrue(responseText.matches("\\{\"id\":\\d+\\}"));
    }

    @Test
    public void testUpdateGeojson() throws Exception {
        String json = "{\"geojson\": {\"type\":\"Polygon\",\"coordinates\":[[[125.5185546875,-21.446934836644001],[128.2431640625,-21.446934836644001],[128.2431640625,-19.138942324356002],[125.5185546875,-19.138942324356002],[125.5185546875,-21.446934836644001]]]}, \"name\": \"test\", \"description\": \"test\", \"user_id\": \"1551\"}";

        System.out.println(json);
        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("http://localhost:8080/layers-service/shape/upload/geojson");

        post.setRequestHeader("Content-Type", "application/json");
        post.setRequestBody(json);

        int returnCode = httpClient.executeMethod(post);
        Assert.assertEquals(200, returnCode);

        String responseText = post.getResponseBodyAsString();
        System.out.println(responseText);

        Pattern p = Pattern.compile("\\{\"id\":(\\d+)\\}");
        Matcher m = p.matcher(responseText);
        m.find();
        String pid = m.group(1);

        System.out.println(pid);

        String json2 = "{\"geojson\": {\"type\":\"Polygon\",\"coordinates\":[[[125.5185546875,-26.446934836644001],[128.2431640625,-26.446934836644001],[128.2431640625,-19.138942324356002],[125.5185546875,-19.138942324356002],[125.5185546875,-26.446934836644001]]]}, \"name\": \"test\", \"description\": \"test\", \"user_id\": \"1551\"}";
        PostMethod post2 = new PostMethod("http://localhost:8080/layers-service/shape/upload/geojson/" + pid);

        post2.setRequestHeader("Content-Type", "application/json");
        post2.setRequestBody(json2);

        returnCode = httpClient.executeMethod(post2);
        Assert.assertEquals(200, returnCode);

        String responseText2 = post2.getResponseBodyAsString();
        System.out.println(responseText2);
    }

    @Test
    @Ignore
    public void testMalformedJSON() throws Exception {
        String json = "#%#%#";
        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("http://localhost:8080/layers-service/shape/upload/wkt");

        post.setRequestHeader("Content-Type", "application/json");
        post.setRequestBody(json);

        int returnCode = httpClient.executeMethod(post);
        Assert.assertEquals(200, returnCode);

        String responseText = post.getResponseBodyAsString();
        System.out.println(responseText);
        Assert.assertTrue(responseText.equals("{\"error\":\"Malformed request. Expecting a JSON object.\"}"));
    }

    @Test
    @Ignore
    public void testInvalidJSON() throws Exception {
        // Using a list here when we need a map
        String json = "[1, 2, 3]";
        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("http://localhost:8080/layers-service/shape/upload/wkt");

        post.setRequestHeader("Content-Type", "application/json");
        post.setRequestBody(json);

        int returnCode = httpClient.executeMethod(post);
        Assert.assertEquals(200, returnCode);

        String responseText = post.getResponseBodyAsString();
        System.out.println(responseText);
        Assert.assertTrue(responseText.equals("{\"error\":\"Malformed request. Expecting a JSON object.\"}"));
    }

    @Test
    @Ignore
    public void testMalformedWkt() throws Exception {
        String json = "{\"wkt\": \"lalala\", \"name\": \"test\", \"description\": \"test\", \"userid\": \"1551\"}";

        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("http://localhost:8080/layers-service/shape/upload/wkt");

        post.setRequestHeader("Content-Type", "application/json");
        post.setRequestBody(json);

        int returnCode = httpClient.executeMethod(post);
        Assert.assertEquals(200, returnCode);

        String responseText = post.getResponseBodyAsString();
        System.out.println(responseText);
        Assert.assertTrue(responseText.equals("{\"error\":\"Malformed WKT.\"}"));
    }

    @Test
    @Ignore
    public void testMalformedGeoJSON() throws Exception {
        String json = "{\"geojson\": \"lalala\", \"name\": \"test\", \"description\": \"test\", \"userid\": \"1551\"}";

        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("http://localhost:8080/layers-service/shape/upload/wkt");

        post.setRequestHeader("Content-Type", "application/json");
        post.setRequestBody(json);

        int returnCode = httpClient.executeMethod(post);
        Assert.assertEquals(200, returnCode);

        String responseText = post.getResponseBodyAsString();
        System.out.println(responseText);
        Assert.assertTrue(responseText.equals("{\"error\":\"Malformed GeoJSON geometry. Note that only GeoJSON geometries can be supplied here. Features and FeatureCollections cannot.\"}"));
    }

    // Invalid pid

    @Test
    @Ignore
    public void testGetGeoJson() throws Exception {
        HttpClient httpClient = new HttpClient();
        GetMethod get = new GetMethod("http://localhost:8080/layers-service/shape/upload/geojson/1");

        int returnCode = httpClient.executeMethod(get);
        Assert.assertEquals(200, returnCode);

        String responseText = get.getResponseBodyAsString();
        System.out.println(responseText);
        Assert.assertTrue(responseText
                .equals("{\"geojson\":{\"type\":\"Polygon\",\"coordinates\":[[[125.5185546875,-21.446934836644],[128.2431640625,-21.446934836644],[128.2431640625,-19.138942324356],[125.5185546875,-19.138942324356],[125.5185546875,-21.446934836644]]]},\"id\":1,\"name\":\"test\",\"description\":\"test\",\"user_id\":\"1551\",\"time_added\":\"1970-01-01T09:41+1000\"}"));
    }

    @Test
    @Ignore
    public void testGetWkt() throws Exception {
        HttpClient httpClient = new HttpClient();
        GetMethod get = new GetMethod("http://localhost:8080/layers-service/shape/upload/wkt/1");

        int returnCode = httpClient.executeMethod(get);
        Assert.assertEquals(200, returnCode);

        String responseText = get.getResponseBodyAsString();
        System.out.println(responseText);
        Assert.assertTrue(responseText
                .equals("{\"wkt\":\"POLYGON((125.5185546875 -21.446934836644,128.2431640625 -21.446934836644,128.2431640625 -19.138942324356,125.5185546875 -19.138942324356,125.5185546875 -21.446934836644))\",\"id\":1,\"name\":\"test\",\"description\":\"test\",\"user_id\":\"1551\",\"time_added\":\"1970-01-01T09:41+1000\"}"));
    }

    @Test
    @Ignore
    public void testDelete() throws Exception {
        // Create layer
        String json = "{\"wkt\": \"POLYGON((125.5185546875 -21.446934836644,128.2431640625 -21.446934836644,128.2431640625 -19.138942324356,125.5185546875 -19.138942324356,125.5185546875 -21.446934836644))\", \"name\": \"test\", \"description\": \"test\", \"userid\": \"1551\"}";

        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("http://localhost:8080/layers-service/shape/upload/wkt");

        post.setRequestHeader("Content-Type", "application/json");
        post.setRequestBody(json);
        int returnCode = httpClient.executeMethod(post);
        Assert.assertEquals(200, returnCode);
        String responseText = post.getResponseBodyAsString();
        String pid = responseText.replace("{", "").replace("}", "").split(":")[1];

        // Delete layer
        DeleteMethod delete = new DeleteMethod("http://localhost:8080/layers-service/geometry/delete/" + pid);
        int returnCode2 = httpClient.executeMethod(delete);
        Assert.assertEquals(200, returnCode2);
        String responseText2 = delete.getResponseBodyAsString();
        Assert.assertTrue(responseText2.equals("{\"deleted\":true}"));

        // Check layer pid now invalid
        GetMethod get = new GetMethod("http://localhost:8080/layers-service/shape/upload/wkt/" + pid);

        int returnCode3 = httpClient.executeMethod(get);
        Assert.assertEquals(200, returnCode3);

        String responseText3 = get.getResponseBodyAsString();
        Assert.assertTrue(responseText3.matches("{\"error\":\"Invalid geometry ID: \\d+\"}"));

    }

    @Test
    @Ignore
    public void testPointRadiusIntersection() throws Exception {
        HttpClient httpClient = new HttpClient();
        GetMethod get = new GetMethod("http://localhost:8080/layers-service/geometry/intersect_point_radius");
        get.setQueryString("latitude=-20&longitude=127&radius=10");

        int returnCode = httpClient.executeMethod(get);
        Assert.assertEquals(200, returnCode);

        String responseText = get.getResponseBodyAsString();
        System.out.println(responseText);
        Assert.assertTrue(responseText.matches("{\"intersecting_ids\":[(\\d+,)*\\d+]}"));
    }

    @Test
    @Ignore
    public void testAreaIntersectionWkt() throws Exception {
        String json = "{\"wkt\": \"POLYGON((126.44140625 -20.297236602241,127.056640625 -20.297236602241,127.056640625 -19.636380658043,126.44140625 -19.636380658043,126.44140625 -20.297236602241))\"}";

        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("http://localhost:8080/layers-service/geometry/intersect_area/wkt");

        post.setRequestHeader("Content-Type", "application/json");
        post.setRequestBody(json);

        int returnCode = httpClient.executeMethod(post);
        Assert.assertEquals(200, returnCode);

        String responseText = post.getResponseBodyAsString();
        System.out.println(responseText);
        Assert.assertTrue(responseText.matches("{\"intersecting_ids\":[(\\d+,)*\\d+]}"));
    }

    @Test
    @Ignore
    public void testGeometryCollection() throws Exception {
        String json = "{\"wkt\": \"GEOMETRYCOLLECTION(POINT(4 6),LINESTRING(4 6,7 10))\", \"name\": \"test\", \"description\": \"test\", \"userid\": \"1551\"}";

        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("http://localhost:8080/layers-service/shape/upload/wkt");

        post.setRequestHeader("Content-Type", "application/json");
        post.setRequestBody(json);

        int returnCode = httpClient.executeMethod(post);
        Assert.assertEquals(200, returnCode);

        String responseText = post.getResponseBodyAsString();
        System.out.println(responseText);
        Assert.assertTrue(responseText.equals("{\"error\":\"Invalid geometry. See web service documentation for a discussion of valid geometries.\"}"));
    }

    @Test
    @Ignore
    public void testInvalidGeometry() throws Exception {
        String json = "{\"wkt\": \"POLYGON((0 0,10 10,0 10,10 0,0 0))\", \"name\": \"test\", \"description\": \"test\", \"userid\": \"1551\"}";

        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("http://localhost:8080/layers-service/shape/upload/wkt");

        post.setRequestHeader("Content-Type", "application/json");
        post.setRequestBody(json);

        int returnCode = httpClient.executeMethod(post);
        Assert.assertEquals(200, returnCode);

        String responseText = post.getResponseBodyAsString();
        System.out.println(responseText);
        Assert.assertTrue(responseText.equals("{\"error\":\"Invalid geometry. See web service documentation for a discussion of valid geometries.\"}"));
    }

    @Test
    @Ignore
    public void testFileUploadMultipart() throws Exception {
        // String json =
        // "{\"user_id\": \"1551\", \"api_key\": \"b3f3c932-ba88-4ad5-b429-f947475024af\"}";
        HttpClient httpClient = new HttpClient();
        MultipartPostMethod post = new MultipartPostMethod("http://localhost:6666/layers-service/shape/upload/shp?user_id=1551&api_key=b3f3c932-ba88-4ad5-b429-f947475024af");
        // PostMethod post = new
        // PostMethod("http://localhost:6666/layers-service/shape/upload/shp");
        // post.setParameter(HttpMethodParams.MULTIPART_BOUNDARY,
        // "simple-boundary");
        // StringPart userIdPart = new StringPart("user_id", "1551", "UTF-8");
        // StringPart apiKeyPart = new StringPart("api_key",
        // "b3f3c932-ba88-4ad5-b429-f947475024af", "UTF-8");
        FilePart filePart = new FilePart("shp_file", new File("C:\\Users\\ChrisF\\Desktop\\temp\\15.shp.zip"), "application/zip", null);
        // jsonPart.setContentType("application/json");
        // Part[] parts = { jsonPart, filePart };
        // post.addParameter("json_body", json);
        // post.addParameter("shp_file", new
        // File("C:\\Users\\ChrisF\\Desktop\\temp\\3.shp.zip"));
        // post.addPart(userIdPart);
        // post.addPart(apiKeyPart);
        post.addPart(filePart);
        // MultipartRequestEntity multipartEntity = new
        // MultipartRequestEntity(parts, post.getParams());
        post.setRequestHeader("Content-Type", "multipart/form-data");
        // post.setRequestEntity(multipartEntity);
        int status = httpClient.executeMethod(post);

        String responseText = post.getResponseBodyAsString();
        System.out.println(responseText);
    }

    @Test
    @Ignore
    public void testFileUploadFileUrl() throws Exception {
        String json = "{\"user_id\": \"1551\", \"api_key\": \"b3f3c932-ba88-4ad5-b429-f947475024af\", \"shp_file_url\": \"file:///C:/Users/ChrisF/Desktop/temp/3.shp.zip\"}";

        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("http://localhost:6666/layers-service/shape/upload/shp");

        post.setRequestHeader("Content-Type", "application/json");
        post.setRequestBody(json);

        int returnCode = httpClient.executeMethod(post);
        Assert.assertEquals(200, returnCode);

        String responseText = post.getResponseBodyAsString();
        System.out.println(responseText);
    }

    @Test
    @Ignore
    public void testShapeFileFeatureSave() throws Exception {
        String json = "{\"user_id\": \"1551\", \"api_key\": \"b3f3c932-ba88-4ad5-b429-f947475024af\", \"name\": \"test\", \"description\": \"test\"}";

        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod("http://localhost:6666/layers-service/shape/upload/shp/1373937972954-0/0");

        post.setRequestHeader("Content-Type", "application/json");
        post.setRequestBody(json);

        int returnCode = httpClient.executeMethod(post);
        Assert.assertEquals(200, returnCode);

        String responseText = post.getResponseBodyAsString();
        System.out.println(responseText);
    }

    // @Test
    // @Ignore
    // public void testgeometryKml() throws Exception {
    // String json =
    // "{\"kml\": \"<Polygon><outerBoundaryIs><LinearRing><coordinates>125.5185546875,-21.446934836644001 128.2431640625,-21.446934836644001 128.2431640625,-19.138942324356002 125.5185546875,-19.138942324356002 125.5185546875,-21.446934836644001</coordinates></LinearRing></outerBoundaryIs></Polygon>\", \"name\": \"test\", \"description\": \"test\", \"userid\": \"1551\"}";
    //
    // HttpClient httpClient = new HttpClient();
    // PostMethod post = new
    // PostMethod("http://localhost:8080/layers-service/geometry/kml");
    //
    // post.setRequestHeader("Content-Type", "application/json");
    // post.setRequestBody(json);
    //
    // int returnCode = httpClient.executeMethod(post);
    // Assert.assertEquals(200, returnCode);
    //
    // String responseText = post.getResponseBodyAsString();
    // System.out.println(responseText);
    // Assert.assertTrue(responseText.matches("{\"pid\":\\d+}"));
    // }

    // @Test
    // @Ignore
    // public void testgeometryShp() throws Exception {
    // String json =
    // "{\"zippedShpFileUrl\": \"http://localhost:8080/layers-service/includes/Australia's%20River%20Basins%201997.zip\", \"name\": \"test\", \"description\": \"test\", \"userid\": \"1551\"}";
    //
    // HttpClient httpClient = new HttpClient();
    // PostMethod post = new
    // PostMethod("http://localhost:8080/layers-service/geometry/shp");
    //
    // post.setRequestHeader("Content-Type", "application/json");
    // post.setRequestBody(json);
    //
    // int returnCode = httpClient.executeMethod(post);
    // Assert.assertEquals(200, returnCode);
    //
    // String responseText = post.getResponseBodyAsString();
    // System.out.println(responseText);
    // Assert.assertTrue(responseText.matches("{\"pid\":\\d+}"));
    // }

    // @Test
    // @Ignore
    // public void testGetKml() throws Exception {
    // HttpClient httpClient = new HttpClient();
    // GetMethod get = new
    // GetMethod("http://localhost:8080/layers-service/geometryed/1/kml");
    //
    //
    // int returnCode = httpClient.executeMethod(get);
    // Assert.assertEquals(200, returnCode);
    //
    // String responseText = get.getResponseBodyAsString();
    // System.out.println(responseText);
    // Assert.assertTrue(responseText.equals("{\"kml\":\"<Polygon><outerBoundaryIs><LinearRing><coordinates>125.5185546875,-21.446934836644001 128.2431640625,-21.446934836644001 128.2431640625,-19.138942324356002 125.5185546875,-19.138942324356002 125.5185546875,-21.446934836644001</coordinates></LinearRing></outerBoundaryIs></Polygon>\"}"));
    //
    // }
    //
    // @Test
    // @Ignore
    // public void testGetShp() throws Exception {
    //
    // }

    // Invalid kml

    // Invalid shapefile - not a zip

    // Invalid shapefile - no .shp file

    // Invalid shapefile - corrupt .shp file

}
