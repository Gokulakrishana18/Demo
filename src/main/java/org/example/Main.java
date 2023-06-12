package org.example;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        HttpClient client = HttpClients.createDefault();

        // Replace with your GitHub access token
        String accessToken = "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";

        // Replace with the repository owner, repository name, and base branch
        String owner = "Gokulakrishana18";
        String repo = "Demo";
        String baseBranch = "424428026269aaed4966681759ae43a531719c4b";

        // Replace with the name of the new branch you want to create
        String newBranchName = "DCP-666";

        // Prepare the API endpoint URL
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/git/refs";

        // Prepare the request payload
        String payload = "{\"ref\":\"refs/heads/" + newBranchName + "\",\"sha\":\"" + baseBranch + "\"}";

        // Prepare the HTTP request
        HttpPost request = new HttpPost(url);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

        try {
            // Send the HTTP request
            HttpResponse response = client.execute(request);

            // Read the response
            HttpEntity responseEntity = response.getEntity();
            String responseBody = EntityUtils.toString(responseEntity);

            if (response.getStatusLine().getStatusCode() == 201) {
                System.out.println("Branch created successfully.");
            } else {
                System.out.println("Failed to create branch. Error: " + response.getStatusLine().getStatusCode() + " " + responseBody);
            }

            // Consume the response entity to release resources
            EntityUtils.consume(responseEntity);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}








