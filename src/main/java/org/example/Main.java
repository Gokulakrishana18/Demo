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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class GitApiExample {

    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private static final String GITHUB_OWNER = "Gokulakrishana18";
    private static final String GITHUB_REPO = "Demo";
    private static final String GITHUB_ACCESS_TOKEN = "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";
    private static final String BRANCH_NAME = "DCP-2";
    private static final String filePath = "src/main/java/org/example/Main.java";

    private static final String COMMIT_MESSAGE = "Checking the Main.java";

    public static void main(String[] args) {
        commitAndPushChanges();
    }

    private static void commitAndPushChanges() {
        try {
            // Step 1: Create a new branch
            createBranch();

            // Step 2: Update the file
            updateCommitMessageInFile();

            // Step 3: Commit the changes
             commitChanges();

            // Step 4: Push the changes
            pushChanges();

            System.out.println("Changes committed and pushed successfully.");
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

    private static void updateCommitMessageInFile() {
        //String filePath = "path/to/file.txt";
        String newCommitMessage = "Updated commit message";
        String url = GITHUB_API_BASE_URL + "/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/contents/" + filePath;

        try {
            // Get the existing file content
            String fileContent = getFileContent(filePath);
            if (fileContent == null) {
                System.out.println("Failed to retrieve the file content.");
                return;
            }

            // Retrieve the file SHA
            String fileSha = getFileSha(filePath);
            if (fileSha == null) {
                System.out.println("Failed to retrieve the file SHA.");
                return;
            }

            // Create the update payload
            JsonObject updatePayload = new JsonObject();
            updatePayload.addProperty("branch", BRANCH_NAME);
            updatePayload.addProperty("message", newCommitMessage);
            updatePayload.addProperty("sha", fileSha);

            // Check if you want to keep the old content
            boolean keepOldContent = true;
            if (keepOldContent) {
                String updatedContent = "This is the updated content.";
                updatePayload.addProperty("content", fileContent + Base64.getEncoder().encodeToString(updatedContent.getBytes()));
            } else {
                // Replace the file content with the updated content
                String updatedContent = "This is the updated content.";
                updatePayload.addProperty("content", Base64.getEncoder().encodeToString(updatedContent.getBytes()));
            }

            HttpClient client = HttpClientBuilder.create().build();
            HttpPut request = new HttpPut(url);
            request.setHeader(HttpHeaders.AUTHORIZATION, "token " + GITHUB_ACCESS_TOKEN);

            // Set the JSON payload in the request
            StringEntity jsonEntity = new StringEntity(updatePayload.toString());
            request.setEntity(jsonEntity);
            request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                System.out.println("Commit message updated successfully.");
            } else {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("Failed to update commit message. Status code: " + statusCode);
                System.out.println("Response body: " + responseBody);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String getFileContent(String filePath) throws IOException {
        String url = GITHUB_API_BASE_URL + "/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/contents/" + filePath;
        System.out.println("url: "+url);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.setHeader(HttpHeaders.AUTHORIZATION, "token " + GITHUB_ACCESS_TOKEN);

        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String responseBody = EntityUtils.toString(response.getEntity());
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            String fileContent = jsonResponse.get("content").getAsString();
            //System.out.println("fileContent :"+ fileContent);
            String file=decodeBase64(fileContent);
            // return new String(Base64.getDecoder().decode(fileContent), StandardCharsets.UTF_8);
            return file;
        } else {
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("Failed to retrieve the file content. Status code: " + statusCode);
            System.out.println("Response body: " + responseBody);
            return null;
        }
    }
    public static String decodeBase64(String encodedContent) {
        byte[] decodedBytes = DatatypeConverter.parseBase64Binary(encodedContent);
        return DatatypeConverter.printBase64Binary(decodedBytes);
    }

    private static String getFileSha(String filePath) throws IOException {
        String url = GITHUB_API_BASE_URL + "/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/contents/" + filePath;

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.setHeader(HttpHeaders.AUTHORIZATION, "token " + GITHUB_ACCESS_TOKEN);

        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String responseBody = EntityUtils.toString(response.getEntity());
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            System.out.println("ResponseSha: "+jsonResponse.get("sha").getAsString());
            return jsonResponse.get("sha").getAsString();
        } else {
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("Failed to retrieve the file SHA. Status code: " + statusCode);
            System.out.println("Response body: " + responseBody);
            return null;
        }
    }



    private static void commitChanges() throws IOException {
        String commitUrl = GITHUB_API_BASE_URL + "/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/git/commits";

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(commitUrl);
        request.setHeader(HttpHeaders.AUTHORIZATION, "token " + GITHUB_ACCESS_TOKEN);

        JsonObject commitObject = new JsonObject();
        commitObject.addProperty("message", COMMIT_MESSAGE);
        commitObject.addProperty("tree", getLatestCommitSha(BRANCH_NAME));

        JsonArray parentsArray = new JsonArray();
        parentsArray.add(getLatestCommitSha("main"));
        commitObject.add("parents", parentsArray);

        JsonObject authorObject = new JsonObject();
        authorObject.addProperty("name", "Your Name");
        authorObject.addProperty("email", "your-email@example.com");
        authorObject.addProperty("date", getCurrentDateTime());
        commitObject.add("author", authorObject);

        StringEntity jsonEntity = new StringEntity(commitObject.toString());
        request.setEntity(jsonEntity);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 201) {
            System.out.println("Commit created successfully.");
        } else {
            System.out.println("Failed to create commit. Status code: " + statusCode);
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("Response body: " + responseBody);
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
        if (statusCode == 200) {
            System.out.println("Changes pushed successfully.");
        } else {
            System.out.println("Failed to push changes. Status code: " + statusCode);
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("Response body: " + responseBody);
        }
    }

    private static String getTreeSha(String branchName) throws IOException {
        String commitUrl = GITHUB_API_BASE_URL + "/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/commits/" + branchName;

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet commitRequest = new HttpGet(commitUrl);
        commitRequest.setHeader(HttpHeaders.AUTHORIZATION, "token " + GITHUB_ACCESS_TOKEN);

        HttpResponse commitResponse = client.execute(commitRequest);
        int commitStatusCode = commitResponse.getStatusLine().getStatusCode();
        if (commitStatusCode == 200) {
            String commitResponseBody = EntityUtils.toString(commitResponse.getEntity());
            JsonObject commitObject = new Gson().fromJson(commitResponseBody, JsonObject.class);
            String treeUrl = commitObject.getAsJsonObject("commit").getAsJsonObject("tree").get("url").getAsString();

            HttpGet treeRequest = new HttpGet(treeUrl);
            treeRequest.setHeader(HttpHeaders.AUTHORIZATION, "token " + GITHUB_ACCESS_TOKEN);

            HttpResponse treeResponse = client.execute(treeRequest);
            int treeStatusCode = treeResponse.getStatusLine().getStatusCode();
            if (treeStatusCode == 200) {
                String treeResponseBody = EntityUtils.toString(treeResponse.getEntity());
                JsonObject treeObject = new Gson().fromJson(treeResponseBody, JsonObject.class);
                String treeSha = treeObject.get("sha").getAsString();
                return treeSha;
            } else {
                System.out.println("Failed to retrieve tree SHA. Status code: " + treeStatusCode);
                String responseBody = EntityUtils.toString(treeResponse.getEntity());
                System.out.println("Response body: " + responseBody);
            }
        } else {
            System.out.println("Failed to retrieve commit. Status code: " + commitStatusCode);
            String responseBody = EntityUtils.toString(commitResponse.getEntity());
            System.out.println("Response body: " + responseBody);
        }
        return "";
    }

    private static String getLatestCommitSha1(String branchName) throws IOException {
        String url = GITHUB_API_BASE_URL + "/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/commits/" + branchName;

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.setHeader(HttpHeaders.AUTHORIZATION, "token " + GITHUB_ACCESS_TOKEN);

        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String responseBody = EntityUtils.toString(response.getEntity());
            JsonObject commitObject = new Gson().fromJson(responseBody, JsonObject.class);
            String commitSha = commitObject.get("sha").getAsString();
            return commitSha;
        } else {
            System.out.println("Failed to retrieve latest commit SHA. Status code: " + statusCode);
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("Response body: " + responseBody);
        }
        return "";
    }

    private static String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return now.format(formatter);
    }
}
