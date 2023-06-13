package org.example;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        HttpClient client = HttpClients.createDefault();
        String accessToken = "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";
        String owner = "Gokulakrishana18";
        String repo = "Demo";
       String baseBranch = "424428026269aaed4966681759ae43a531719c4b";
        //String baseBranch = "main";
        String newBranchName = "DCP-19";
        String createBranchUrl = String.format("https://api.github.com/repos/%s/%s/git/refs", owner, repo);
        String branchPayload = "{\"ref\":\"refs/heads/%s\",\"sha\":\"%s\"}";
        HttpPost createBranchRequest = new HttpPost(createBranchUrl);
        createBranchRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        createBranchRequest.setEntity(new StringEntity(String.format(branchPayload, newBranchName, baseBranch), ContentType.APPLICATION_JSON));
        try {
            HttpResponse createBranchResponse = client.execute(createBranchRequest);
            HttpEntity createBranchResponseEntity = createBranchResponse.getEntity();
            String createBranchResponseBody = EntityUtils.toString(createBranchResponseEntity);
            if (createBranchResponse.getStatusLine().getStatusCode() == 201) {
                System.out.println("Branch created successfully.");
                String commitSha = createBranchResponseBody.substring(createBranchResponseBody.lastIndexOf(":") + 2, createBranchResponseBody.lastIndexOf("\""));
              String com="/api.github.com/repos/Gokulakrishana18/Demo/git/commits/main";
               System.out.println("commitsha:"+commitSha);
           String getTreeUrl="https://api.github.com/repos/"+owner+"/"+repo+"/git/trees/"+baseBranch;
           System.out.println("Url Values :"+getTreeUrl);
           // "url": "https://api.github.com/repos/octocat/Hello-World/trees/9fb037999f264ba9a7fc6274d15fa3ae2ab98312",
               //String getTreeUrl = String.format("https://api.github.com/repos/%s/%s/git/trees/%s?recursive=1", owner, repo, com);
                System.out.println("getTreeUrl:"+getTreeUrl);
                HttpGet getTreeRequest = new HttpGet(getTreeUrl);
                getTreeRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                HttpResponse getTreeResponse = client.execute(getTreeRequest);
                HttpEntity getTreeResponseEntity = getTreeResponse.getEntity();
                String getTreeResponseBody = EntityUtils.toString(getTreeResponseEntity);
                if (getTreeResponse.getStatusLine().getStatusCode() == 200){
                    JSONObject treeObject = new JSONObject(getTreeResponseBody);
                    String treeSha = treeObject.getString("sha");
                    //"url": "https://api.github.com/repos/octocat/Hello-World/git/commits/7638417db6d59f3c431d3e1f261cc637155684cd",
                    String createCommitUrl = String.format("https://api.github.com/repos/%s/%s/git/commits", owner, repo);
                    System.out.println("CreateCommit url :"+createCommitUrl);
                    String commitPayload = "{\"message\":\"Initial commit\",\"tree\":\"%s\",\"parents\":[\"%s\"]}";
                    System.out.println("Commit PayLoad: "+commitPayload);
                    HttpPost createCommitRequest = new HttpPost(createCommitUrl);
                    createCommitRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                    createCommitRequest.setEntity(new StringEntity(String.format(commitPayload, treeSha, baseBranch), ContentType.APPLICATION_JSON));
                    HttpResponse createCommitResponse = client.execute(createCommitRequest);
                    HttpEntity createCommitResponseEntity = createCommitResponse.getEntity();
                    String createCommitResponseBody = EntityUtils.toString(createCommitResponseEntity);
                    System.out.println("createCommitResponseBody:"+createCommitResponseBody);
                    if (createCommitResponse.getStatusLine().getStatusCode() == 201) {
                        System.out.println("Commit created successfully.");
                    } else {
                        System.out.println("Failed to create commit. Error: " + createCommitResponse.getStatusLine().getStatusCode() + " " + createCommitResponseBody);
                    }

                    EntityUtils.consume(createCommitResponseEntity);
                } else {
                    System.out.println("Failed to retrieve tree. Error: " + getTreeResponse.getStatusLine().getStatusCode() + "  :  " + getTreeResponseBody);
                }

                EntityUtils.consume(getTreeResponseEntity);
            } else {
                System.out.println("Failed to create branch. Error: " + createBranchResponse.getStatusLine().getStatusCode() + " " + createBranchResponseBody);
            }

            EntityUtils.consume(createBranchResponseEntity);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
