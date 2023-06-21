package newFile;

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

public class git {

    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private static final String GITHUB_BASE_URL = "https://github.com";
    private static final String GITHUB_OWNER = "Gokulakrishana18";
    private static final String GITHUB_REPO = "Demo";
    private static final String GITHUB_ACCESS_TOKEN = "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";
    private static final String BRANCH_NAME = "DCP-24";
    private static final String FILE_PATH = "src/main/java/org/example/dummy.sql";
    private static final String FILE_PATH1 = "src/main/java/org/example/dummy1.sql";

    private static final String COMMIT_MESSAGE = "Checking the Main.java";

    public static void main(String[] args) {
        commitAndPushChanges();
    }

    private static void commitAndPushChanges() {
        String url = GITHUB_BASE_URL + "/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/pull/" + BRANCH_NAME;
        try {
            // Step 1: Create a new branch
            createBranch();

            // Step 2: Update the file
            updateCommitMessageInFiles();

            // Step 3: Push the changes
            pushChanges();

            // Step 4: Create a pull request
            createPullRequest();

            System.out.println("url :" + url);
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

    private static void updateCommitMessageInFiles() throws IOException {
        String newCommitMessage = "This the new file";

        // File 1
        String url1 = GITHUB_API_BASE_URL + "/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/contents/" + FILE_PATH;
        String content1 = "This is the updated content for file 1.";
        JsonObject updatePayload1 = createUpdatePayload(BRANCH_NAME, newCommitMessage, content1);

        // File 2
        String url2 = GITHUB_API_BASE_URL + "/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/contents/" + FILE_PATH1;
        String content2 = "This is the updated content for file 2.";
        JsonObject updatePayload2 = createUpdatePayload(BRANCH_NAME, newCommitMessage, content2);

        HttpClient client = HttpClientBuilder.create().build();

        // Update file 1
        executeFileUpdate(url1, updatePayload1, client, "file 1");

        // Update file 2
        executeFileUpdate(url2, updatePayload2, client, "file 2");
    }

    private static JsonObject createUpdatePayload(String branchName, String commitMessage, String content) {
        JsonObject updatePayload = new JsonObject();
        updatePayload.addProperty("branch", branchName);
        updatePayload.addProperty("message", commitMessage);
        updatePayload.addProperty("content", Base64.getEncoder().encodeToString(content.getBytes()));
        return updatePayload;
    }

    private static void executeFileUpdate(String url, JsonObject updatePayload, HttpClient client, String fileDescription) throws IOException {
        HttpPut request = new HttpPut(url);
        request.setHeader(HttpHeaders.AUTHORIZATION, "token " + GITHUB_ACCESS_TOKEN);
        StringEntity jsonEntity = new StringEntity(updatePayload.toString());
        request.setEntity(jsonEntity);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(response.getEntity());
       // System.out.println("Response body for " + fileDescription + ": " + responseBody);
        if (statusCode == 201) {
            System.out.println("Created the new " + fileDescription + " and updated commit message successfully.");
        } else {
            System.out.println("Failed to update commit message for " + fileDescription + ". Status code: " + statusCode);
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
       // System.out.println("Response body: " + responseBody);
        if (statusCode == 200) {
            System.out.println("Changes pushed successfully.");
        } else {
            System.out.println("Failed to push changes. Status code: " + statusCode);
        }
    }

    private static void createPullRequest() throws IOException {
        String url = GITHUB_API_BASE_URL + "/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/pulls";

        JsonObject pullRequestPayload = new JsonObject();
        pullRequestPayload.addProperty("title", "Pull Request Title");
        pullRequestPayload.addProperty("head", BRANCH_NAME);
        pullRequestPayload.addProperty("base", "main");

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);
        request.setHeader(HttpHeaders.AUTHORIZATION, "token " + GITHUB_ACCESS_TOKEN);

        StringEntity jsonEntity = new StringEntity(pullRequestPayload.toString());
        request.setEntity(jsonEntity);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(response.getEntity());
        //System.out.println("Response body: " + responseBody);
        if (statusCode == 201) {
            System.out.println("Pull request created successfully.");
        } else {
            System.out.println("Failed to create pull request. Status code: " + statusCode);
        }
    }
}
