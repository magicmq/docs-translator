/*
 *    Copyright 2025 magicmq
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.magicmq.docstranslator;


import dev.magicmq.docstranslator.config.Repository;
import org.apache.commons.io.FileUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public class MavenResolver {

    private static final Logger logger = LoggerFactory.getLogger(MavenResolver.class);

    private final RepositorySystem system;
    private final RepositorySystemSession session;
    private final List<RemoteRepository> remoteRepositories;
    private final List<String> excludeArtifacts;
    private final String dependencyScope;

    private final BlockingQueue<FetchRequest> requestQueue = new LinkedBlockingQueue<>();

    public MavenResolver(Path workingDir) throws IOException {
        logger.info("Initializing Maven repository directory...");

        Path mavenDir = workingDir.resolve(SettingsProvider.get().getSettings().getMaven().getPath()).toAbsolutePath();
        if (SettingsProvider.get().getSettings().getMaven().isDeleteOnStart() && Files.exists(mavenDir)) {
            logger.info("Deleting local repository folder...");
            FileUtils.deleteDirectory(mavenDir.toFile());
        }
        Files.createDirectories(mavenDir);

        logger.info("Initializing Maven repository session and locator...");

        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(org.eclipse.aether.spi.connector.RepositoryConnectorFactory.class,
                org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory.class);
        locator.addService(org.eclipse.aether.spi.connector.transport.TransporterFactory.class,
                org.eclipse.aether.transport.http.HttpTransporterFactory.class);
        locator.addService(org.eclipse.aether.spi.connector.transport.TransporterFactory.class,
                org.eclipse.aether.transport.file.FileTransporterFactory.class);
        this.system = locator.getService(RepositorySystem.class);

        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepo = new LocalRepository(mavenDir.toFile());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        this.session = session;

        logger.info("Adding remote repositories...");

        this.remoteRepositories = new ArrayList<>();
        if (SettingsProvider.get().getSettings().getMaven().isUseCentral()) {
            logger.debug("Adding Maven central repository...");
            this.remoteRepositories.add(new RemoteRepository.Builder(
                    "central",
                    "default",
                    "https://repo.maven.apache.org/maven2/"
            ).build());
        }
        for (Repository repository : SettingsProvider.get().getSettings().getMaven().getRepositories()) {
            logger.debug("Adding Repository {}", repository);
            this.remoteRepositories.add(new RemoteRepository.Builder(
                    repository.getId(),
                    "default",
                    repository.getUrl()
            ).build());
        }

        this.excludeArtifacts = SettingsProvider.get().getSettings().getMaven().getExcludeArtifacts();
        this.dependencyScope = SettingsProvider.get().getSettings().getMaven().getDependencyScope();

        logger.info("Initializing background worker...");

        Thread worker = new Thread(this::processRequests, "maven-resolver-worker");
        worker.setDaemon(true);
        worker.start();
    }

    public CompletableFuture<List<Artifact>> fetch(String job, String artifact) {
        CompletableFuture<List<Artifact>> future = new CompletableFuture<>();
        requestQueue.offer(new FetchRequest(job, artifact, future));
        return future;
    }

    private List<Artifact> fetchInternal(String artifact) {
        logger.info("Fetching artifact '{}' and its dependencies...", artifact);

        List<Artifact> toReturn = new ArrayList<>();

        try {
            Artifact rootArtifact = new DefaultArtifact(artifact);
            Dependency dependency = new Dependency(rootArtifact, dependencyScope);

            CollectRequest collectRequest = new CollectRequest(dependency, remoteRepositories);

            DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);
            DependencyResult dependencyResult = system.resolveDependencies(session, dependencyRequest);

            List<Artifact> allArtifacts = new ArrayList<>(dependencyResult.getArtifactResults().stream().map(ArtifactResult::getArtifact).toList());

            if (excludeArtifacts != null)
                allArtifacts.removeIf(toCheck ->
                        excludeArtifacts.contains(toCheck.getGroupId() + ":" + toCheck.getArtifactId()) || excludeArtifacts.contains(toCheck.getGroupId()));

            List<Artifact> sources = fetchSources(allArtifacts);

            toReturn.addAll(sources);
        } catch (DependencyResolutionException e) {
            logger.error("Error when resolving dependencies for artifact '{}'", artifact, e);
        }

        return toReturn;
    }

    private List<Artifact> fetchSources(List<Artifact> artifacts) {
        List<Artifact> sourceResults = new ArrayList<>();
        for (Artifact artifact : artifacts) {
            logger.info("Fetching sources for artifact '{}'...", artifact);

            Artifact sourcesArtifact = new DefaultArtifact(
                    artifact.getGroupId(),
                    artifact.getArtifactId(),
                    "sources",
                    "jar",
                    artifact.getVersion()
            );
            try {
                ArtifactRequest sourcesRequest = new ArtifactRequest();
                sourcesRequest.setArtifact(sourcesArtifact);
                sourcesRequest.setRepositories(remoteRepositories);
                sourceResults.add(system.resolveArtifact(session, sourcesRequest).getArtifact());
            } catch (ArtifactResolutionException e) {
                logger.error("Error when resolving dependency artifact '{}'", artifact, e);
            }
        }
        return sourceResults;
    }

    private void processRequests() {
        while (true) {
            FetchRequest request = null;
            try {
                request = requestQueue.take();

                MDC.put("job", request.job);

                List<Artifact> result = fetchInternal(request.artifact);
                request.future.complete(result);
            } catch (Exception e) {
                if (request != null)
                    request.future.completeExceptionally(e);
                else
                    logger.error("Failed to take request from queue", e);
            } finally {
                MDC.clear();
            }
        }
    }

    private record FetchRequest(String job, String artifact, CompletableFuture<List<Artifact>> future) {}
}
