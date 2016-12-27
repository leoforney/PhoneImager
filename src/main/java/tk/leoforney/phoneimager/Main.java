package tk.leoforney.phoneimager;

import com.github.axet.vget.VGet;
import org.apache.commons.lang.math.NumberUtils;
import org.kohsuke.github.*;

import java.io.File;
import java.util.*;

/**
 * Created by Leo on 12/26/2016.
 */
public class Main {
    private static String API_KEY = "8534df699907e4e673e9ff932af2acdfb4c33056";

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
                log("Assets for the latest release of " +selectedRepo.getGitTransportUrl());
                for (GHAsset asset: assets) {
                    log(asset.getName());
                }
                stage++;
            } if (stage == 3) {
                File homeDirectory = new File(System.getProperty("user.home") + "/PhoneImagerTemp/");
                log("Downloading all files to " + homeDirectory.getAbsolutePath());
                for (GHAsset asset: assets) {
                    new VGet(asset.getUrl(), new File(homeDirectory + asset.getName()));
                }
                terminate();
            }
            Thread.sleep(250);
        }

    }

    private static void log(String logString) {
        System.out.println(logString);
    }

    private static void terminate() {stage = -1;}
}
