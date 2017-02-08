package tk.leoforney.phoneimager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.kohsuke.github.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Leo on 12/26/2016.
 */
public class Main {
    private static String API_KEY = "8534df699907e4e673e9ff932af2acdfb4c33056";
    private static String username;
    private static String password;

    private static int stage = 0;

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        stage = 0;

        GitHubBuilder builder = new GitHubBuilder();
        builder.withOAuthToken(API_KEY);

        GitHub github = builder.build();

        GHOrganization organization = github.getOrganization("7351");

        GHRepository selectedRepo = null;
        List<GHRepository> repositoryList = null;
        List<GHAsset> assets = null;

        while (Thread.currentThread().isAlive() && stage != -1) {
            if (stage == 0) { // Display repositories
                repositoryList = new ArrayList<GHRepository>(organization.getRepositories().values());

                log("Choose a repository to download by typing the number:");

                for (GHRepository repository: repositoryList) {
                    log("[" + repositoryList.indexOf(repository) + "] " + repository.getFullName());
                }
                stage++;
            } if (stage == 1) {
                log("Selection: ");
                String selectionString = scanner.next();
                if (NumberUtils.isNumber(selectionString)) {
                    Integer selectionNumber = Integer.parseInt(selectionString);

                    if (selectionNumber + 1 <= repositoryList.size()) {
                        selectedRepo = repositoryList.get(selectionNumber);

                        stage++;
                    } else {
                        log("Incorrect input. Please retry.");
                        stage = 0;
                    }

                } else {
                    log("Incorrect input. Please retry.");
                    stage = 0;
                }
            } if (stage == 2) {
                assets = selectedRepo.listReleases().asList().get(0).getAssets();
                log("Assets for the latest release of " + selectedRepo.getGitTransportUrl());
                for (GHAsset asset: assets) {
                    log(asset.getName());
                }
                stage++;
            } if (stage == 3) {
                File homeDirectory = new File(System.getProperty("user.home") + "/PhoneImager/");
                log("Downloading all files to " + homeDirectory.getAbsolutePath());
                for (GHAsset asset: assets) {
                    FileUtils.writeStringToFile(new File(homeDirectory + File.separator + asset.getName()),
                            getFile(selectedRepo, asset.getId()));
                }
                stage++;
            }
            if (stage == 4) {
                System.out.println("Downloaded all files!");
                stage++;
            }
            Thread.sleep(250);
        }

    }

    private static void log(String logString) {
        System.out.println(logString);
    }

    private static String getFile(GHRepository repo, int id) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        URI uri = new URI("https://api.github.com/repos/" + repo.getFullName() + "/releases/assets/" + id + "?access_token=" + API_KEY);

        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("Accept", "application/octet-stream");

        org.apache.http.HttpResponse response = httpClient.execute(httpGet);

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    private static void terminate() {stage = -1;}
}
