package com.example.apigee.proxygen.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class GitlabService {

	@Value("${config.gitlab.userName}")
	private String gitUserName;

	@Value("${config.gitlab.authorName}")
	private String gitAuthorName;

	@Value("${config.gitlab.privateToken}")
	private String gitPrivateToken;

	@Value("${config.gitlab.enabled}")
	private boolean gitEnabled;

	@Value("${config.gitlab.configRepo}")
	private String configRepo;
	
	@Value("${config.getlab.startPointForCreateBranch}")
	private String startPointForCreateBranch;
	
	@Value("${config.getlab.nameForCreateBranch}")
	private String nameForCreateBranch;

	@Autowired
	private CommonService commonService;

	Logger logger = LoggerFactory.getLogger(GitlabService.class);

	private Path apiProxyDirectory;

	public void addCommitAndPushToGit() {
		// TODO Auto-generated method stub
		// Show the number of files committed and commit hash

		if (gitEnabled) {
			Git git = null;
			try {
				logger.info("Git integration is enabled. Creating a commit and pushing to the remote repo");
				logger.info("Processing git repo at :" + apiProxyDirectory);
				git = Git.open(apiProxyDirectory.toFile());

				logger.info("Staging files");
				git.add().addFilepattern(".").call();

				this.printGitStatus(git);

				logger.info("Committing changes");
				git.commit().setMessage("Commit from proxy-gen tool")
						.setAuthor(this.gitAuthorName, "dillibabu-automation").call();

				logger.info("Pushing files to remote repo");
				git.push().setForce(true).setCredentialsProvider(
						new UsernamePasswordCredentialsProvider(this.gitUserName, this.gitPrivateToken)).call();

			} catch (InvalidRemoteException invalidRemoteException) {
				invalidRemoteException.printStackTrace();
			} catch (TransportException transportException) {
				transportException.printStackTrace();
			} catch (GitAPIException gitAPIException) {
				gitAPIException.printStackTrace();
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			}
		} else {
			logger.info("Git is not enabled.. Skipping git add, commit and push!!! \n");
		}
	}

	public void initializeGitRepo() {
		logger.info("======================================================");
		logger.info("Initializing Git Integration");

		String gitRepoUrl = commonService.getGitRepoUrl();
		Path workDirectory = commonService.getWorkDirectory();
		String proxyName = commonService.getProxyName();

		this.apiProxyDirectory = Paths.get(workDirectory.toAbsolutePath().toString(), proxyName);

		if (gitEnabled) {
			logger.info("Git integration is enabled. Cloning the remote repo");

			Git git = null;
			try {
				// Clone repo
				git = Git.cloneRepository().setURI(gitRepoUrl).setDirectory(apiProxyDirectory.toFile())
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(this.gitUserName, this.gitPrivateToken))
						.call();

				logger.info("Cloned remote repo: " + gitRepoUrl + " at: " + git.getRepository().getDirectory());
				git.branchCreate().setName(this.nameForCreateBranch).setStartPoint(this.startPointForCreateBranch).call();
				git.checkout().setName(this.nameForCreateBranch).call();

			} catch (InvalidRemoteException invalidRemoteException) {
				invalidRemoteException.printStackTrace();
			} catch (TransportException transportException) {
				transportException.printStackTrace();
			} catch (GitAPIException gitAPIException) {
				gitAPIException.printStackTrace();
			}

		} else { // Just create the proxy directory
			logger.info("Git integration is not enabled. Hence creating the apiproxy directory: " + apiProxyDirectory);
			try {
				Files.createDirectories(apiProxyDirectory);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		logger.info("Git Initialization complete");
		logger.info("====================================================== \n");
	}

	public void checkoutPlatformConfigRepo() {
		// this.apiProxyDirectory = Paths.get(workDirectory.toAbsolutePath().toString(),
		// proxyName);
		Path platformConfigDirectory = Paths.get(this.apiProxyDirectory.toString(), "platform-config-apigee");

		if (gitEnabled) {
			logger.info("Git integration is enabled. Cloning the remote repo:" + configRepo);
			Git git = null;
			try {
				git = Git.cloneRepository().setURI(configRepo).setDirectory(platformConfigDirectory.toFile())
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(this.gitUserName, this.gitPrivateToken))
						.call();

				logger.info("Cloned remote repo:" + configRepo + " at:" + git.getRepository().getDirectory());

			} catch (InvalidRemoteException invalidRemoteException) {
				invalidRemoteException.printStackTrace();
			} catch (TransportException transportException) {
				transportException.printStackTrace();
			} catch (GitAPIException gitAPIException) {
				gitAPIException.printStackTrace();
			}

		} else { // Just create the proxy directory
			logger.info("Git integration is not enabled. Skipping platform config repo updates!!!");
		}
	}

	public void processPlatformConfigRepo(String platformConfigFileName) throws IOException {
		if(gitEnabled) {
			Path platformConfigDirectory = Paths.get(this.apiProxyDirectory.toString(), "platform-config-apigee");
			Path platformConfigFileNameDest = Paths.get(this.apiProxyDirectory.toString(), "platform-config-apigee", "apps",
					"orgs", "example", "proxies", commonService.getProxyName() + ".json");
			Files.copy(Paths.get(platformConfigFileName), platformConfigFileNameDest, StandardCopyOption.REPLACE_EXISTING);
			Git git = null;

			try {
				logger.info("Processing git repo at:" + platformConfigDirectory);
				git = Git.open(platformConfigDirectory.toFile());

				logger.info("Staging files");
				git.add().addFilepattern(".").call();

				this.printGitStatus(git);

				logger.info("Committing changes");
				git.commit().setMessage("Commit from proxy-gen tool. Added proxy:" + commonService.getProxyName())
						.setAuthor(this.gitAuthorName, "dillibabu-automation").call();

				logger.info("Pushing files to remote repo");
				git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(this.gitUserName, this.gitPrivateToken))
						.call();

			} catch (InvalidRemoteException invalidRemoteException) {
				invalidRemoteException.printStackTrace();
			} catch (TransportException transportException) {
				transportException.printStackTrace();
			} catch (GitAPIException gitAPIException) {
				gitAPIException.printStackTrace();
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			}
		}
	}

	private void printGitStatus(Git git) throws NoWorkTreeException, GitAPIException {
		Status status = git.status().call();
		logger.info("**************************************");
		logger.info("    Added: " + status.getAdded());
		logger.info("    Changed: " + status.getChanged());
		logger.info("    Conflicting: " + status.getConflicting());
		logger.info("    ConflictingStageState: " + status.getConflictingStageState());
		logger.info("    IgnoredNotInIndex: " + status.getIgnoredNotInIndex());
		logger.info("    Missing: " + status.getMissing());
		logger.info("    Modified: " + status.getModified());
		logger.info("    Removed: " + status.getRemoved());
		logger.info("    Untracked: " + status.getUntracked());
		logger.info("    UntrackedFolders: " + status.getUntrackedFolders());
		logger.info("**************************************");
	}

	public Path getApiProxyDirectory() {
		return apiProxyDirectory;
	}

	public void setApiProxyDirectory(Path apiProxyDirectory) {
		this.apiProxyDirectory = apiProxyDirectory;
	}
}
