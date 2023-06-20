package gitfunction;

import com.google.gson.Gson;
import com.google.gson.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.xml.bind.DatatypeConverter;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class GitApiExample {

    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private static final String GITHUB_BASE_URL = "https://github.com";
    private static final String GITHUB_OWNER = "Gokulakrishana18";
    private static final String GITHUB_REPO = "Demo";
    private static final String GITHUB_ACCESS_TOKEN = "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";
    private static final String BRANCH_NAME = "DCP-14";
    private static final String FILE_PATH= "src/main/java/org/example/dummy.sql";

    private static final String COMMIT_MESSAGE = "Checking the Main.java";

    public static void main(String[] args) {
        commitAndPushChanges();
    }

    private static void commitAndPushChanges() {
       String url= GITHUB_BASE_URL + "/" + GITHUB_OWNER +"/"+ GITHUB_REPO +"/pull/"+ BRANCH_NAME;
        try {
            // Step 1: Create a new branch
            createBranch();

            // Step 2: Update the file
            updateCommitMessageInFile();

            // Step 3: Push the changes
            pushChanges();

            System.out.println("url :"+ url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createBranch() throws IOException {
        String ref = "refs/heads/" + BRANCH_NAME;
        String url = GITHUB_API_BASE_URL + "/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/git/refs";

        String latestCommitSha = getLatestCommitSha("main");
        if (latestCommitSha == null) {
            System.out.println("Failed to retrieve the latest commit's SHA.");
            return;
        }

        String jsonPayload = "{ \"ref\": \"" + ref + "\", \"sha\": \"" + latestCommitSha + "\" }";

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);
        request.setHeader(HttpHeaders.AUTHORIZATION, "token " + GITHUB_ACCESS_TOKEN);
        request.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 201) {
            System.out.println("Branch created successfully.");
        } else {
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("Failed to create branch. Status code: " + statusCode);
            System.out.println("Response body: " + responseBody);
        }
    }

    private static String getLatestCommitSha(String branchName) throws IOException {
        String url = GITHUB_API_BASE_URL + "/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/git/refs/heads/" + branchName;

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.setHeader(HttpHeaders.AUTHORIZATION, "token " + GITHUB_ACCESS_TOKEN);

        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String responseBody = EntityUtils.toString(response.getEntity());
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            String sha = jsonObject.getAsJsonObject("object").getAsJsonPrimitive("sha").getAsString();
            return sha;
        } else {
            System.out.println("Failed to retrieve the latest commit's SHA. Status code: " + statusCode);
        }
        return null;
    }

    private static void updateCommitMessageInFile() throws IOException {
        //String FILE_PATH= "path/to/file.txt";
        String newCommitMessage = "This the new file";
        String url = GITHUB_API_BASE_URL + "/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/contents/" + FILE_PATH;


            // Get the existing file content
//            String fileContent = getFileContent(filePath);
//            if (fileContent == null) {
//                System.out.println("Failed to retrieve the file content.");
//                return;
//            }

            // Retrieve the file SHA
//            String fileSha = getFileSha(filePath);
//            if (fileSha == null) {
//                System.out.println("Failed to retrieve the file SHA.");
//                return;
//        HttpClient client=HttpClientBuilder.create().build();
//        HttpPut request=new HttpPut(url);
//        request.setHeader(HttpHeaders.AUTHORIZATION,"token" +GITHUB_ACCESS_TOKEN);


            // Create the update payload
            JsonObject updatePayload = new JsonObject();
            updatePayload.addProperty("branch", BRANCH_NAME);
            updatePayload.addProperty("message", newCommitMessage);
            String Content = "This is the updated content.";
                updatePayload.addProperty("content", Base64.getEncoder().encodeToString(Content.getBytes()));
            //}

            HttpClient client = HttpClientBuilder.create().build();
            HttpPut request = new HttpPut(url);
            request.setHeader(HttpHeaders.AUTHORIZATION, "token " + GITHUB_ACCESS_TOKEN);

            // Set the JSON payload in the request
            StringEntity jsonEntity = new StringEntity(updatePayload.toString());
            request.setEntity(jsonEntity);
            request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpResponse response = client.execute(request);
       int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println("Response body: " + responseBody);
            if (statusCode == 201) {
                System.out.println("Create the new file and Commit message successfully.");
            } else {

                System.out.println("Failed to update commit message. Status code: " + statusCode);

            }
        }
    private static void pushChanges() throws IOException {
        String refUrl = GITHUB_API_BASE_URL + "/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/git/refs/heads/" + BRANCH_NAME;

        HttpClient client = HttpClientBuilder.create().build();
        HttpPatch request = new HttpPatch(refUrl);
        request.setHeader(HttpHeaders.AUTHORIZATION, "token " + GITHUB_ACCESS_TOKEN);

        JsonObject refObject = new JsonObject();
        refObject.addProperty("sha", getLatestCommitSha(BRANCH_NAME));

        StringEntity jsonEntity = new StringEntity(refObject.toString());
        request.setEntity(jsonEntity);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println("Response body: " + responseBody);
        if (statusCode == 200) {
            System.out.println("Changes pushed successfully.");
        } else {
            System.out.println("Failed to push changes. Status code: " + statusCode);
//            String responseBody = EntityUtils.toString(response.getEntity());
//            System.out.println("Response body: " + responseBody);
        }
    }

}
