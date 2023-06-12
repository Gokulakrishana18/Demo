package org.example;
import okhttp3.*;

public class Main {
    public static void main(String[] args) {
        OkHttpClient client = new OkHttpClient();

        // Replace with your GitHub access token
        String accessToken = "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";

        // Replace with the repository owner, repository name, and base branch
        String owner = "YOUR_REPOSITORY_OWNER";
        String repo = "YOUR_REPOSITORY_NAME";
        String baseBranch = "master";

        // Replace with the name of the new branch you want to create
        String newBranchName = "new-branch";

        // Prepare the API request
        Request request = new Request.Builder()
                .url("https://api.github.com/repos/" + owner + "/" + repo + "/git/refs")
                .header("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create(MediaType.parse("application/json"),
                        "{\"ref\":\"refs/heads/" + newBranchName + "\",\"sha\":\"" + baseBranch + "\"}"))
                .build();

        try {
            // Send the API request
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                System.out.println("Branch created successfully.");
            } else {
                System.out.println("Failed to create branch. Error: " + response.code() + " " + response.message());
            }

            // Close the response
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}





